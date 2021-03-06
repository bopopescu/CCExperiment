package org.apache.cassandra.gms;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.service.StorageService;
public class Gossiper implements IFailureDetectionEventListener
{
    static final ApplicationState[] STATES = ApplicationState.values();
    private ScheduledFuture<?> scheduledGossipTask;
    private class GossipTask implements Runnable
    {
        public void run()
        {
            try
            {
                MessagingService.instance().waitUntilListening();
                endpointStateMap_.get(localEndpoint_).getHeartBeatState().updateHeartBeat();
                List<GossipDigest> gDigests = new ArrayList<GossipDigest>();
                Gossiper.instance.makeRandomGossipDigest(gDigests);
                if ( gDigests.size() > 0 )
                {
                    Message message = makeGossipDigestSynMessage(gDigests);
                    boolean gossipedToSeed = doGossipToLiveMember(message);
                    doGossipToUnreachableMember(message);
                    if (!gossipedToSeed || liveEndpoints_.size() < seeds_.size())
                        doGossipToSeed(message);
                    if (logger_.isTraceEnabled())
                        logger_.trace("Performing status check ...");
                    doStatusCheck();
                }
            }
            catch (Exception e)
            {
                logger_.error("Gossip error", e);
            }
        }
    }
    public final static int intervalInMillis_ = 1000;
    public final static int QUARANTINE_DELAY = StorageService.RING_DELAY * 2;
    private static Logger logger_ = LoggerFactory.getLogger(Gossiper.class);
    public static final Gossiper instance = new Gossiper();
    private InetAddress localEndpoint_;
    private long aVeryLongTime_;
    private long FatClientTimeout_;
    private Random random_ = new Random();
    private Comparator<InetAddress> inetcomparator = new Comparator<InetAddress>()
    {
        public int compare(InetAddress addr1,  InetAddress addr2)
        {
            return addr1.getHostAddress().compareTo(addr2.getHostAddress());
        }
    };
    private List<IEndpointStateChangeSubscriber> subscribers_ = new CopyOnWriteArrayList<IEndpointStateChangeSubscriber>();
    private Set<InetAddress> liveEndpoints_ = new ConcurrentSkipListSet<InetAddress>(inetcomparator);
    private Map<InetAddress, Long> unreachableEndpoints_ = new ConcurrentHashMap<InetAddress, Long>();
    private Set<InetAddress> seeds_ = new ConcurrentSkipListSet<InetAddress>(inetcomparator);
    Map<InetAddress, EndpointState> endpointStateMap_ = new ConcurrentHashMap<InetAddress, EndpointState>();
    Map<InetAddress, Long> justRemovedEndpoints_ = new ConcurrentHashMap<InetAddress, Long>();
    private Gossiper()
    {
        aVeryLongTime_ = 259200 * 1000;
        FatClientTimeout_ = (long)(QUARANTINE_DELAY / 2);
        FailureDetector.instance.registerFailureDetectionEventListener(this);
    }
    public void register(IEndpointStateChangeSubscriber subscriber)
    {
        subscribers_.add(subscriber);
    }
    public void unregister(IEndpointStateChangeSubscriber subscriber)
    {
        subscribers_.remove(subscriber);
    }
    public Set<InetAddress> getLiveMembers()
    {
        Set<InetAddress> liveMbrs = new HashSet<InetAddress>(liveEndpoints_);
        liveMbrs.add(localEndpoint_);
        return liveMbrs;
    }
    public Set<InetAddress> getUnreachableMembers()
    {
        return unreachableEndpoints_.keySet();
    }
    public long getEndpointDowntime(InetAddress ep)
    {
        Long downtime = unreachableEndpoints_.get(ep);
        if (downtime != null)
            return System.currentTimeMillis() - downtime;
        else
            return 0L;
    }
    public void convict(InetAddress endpoint)
    {
        EndpointState epState = endpointStateMap_.get(endpoint);
        if (epState.isAlive())
        {
            logger_.info("InetAddress {} is now dead.", endpoint);
            isAlive(endpoint, epState, false);
        }
    }
    int getMaxEndpointStateVersion(EndpointState epState)
    {
        int maxVersion = epState.getHeartBeatState().getHeartBeatVersion();
        for (VersionedValue value : epState.getApplicationStateMap().values())
            maxVersion = Math.max(maxVersion,  value.version);
        return maxVersion;
    }
    void evictFromMembership(InetAddress endpoint)
    {
        unreachableEndpoints_.remove(endpoint);
    }
    public void removeEndpoint(InetAddress endpoint)
    {
        for (IEndpointStateChangeSubscriber subscriber : subscribers_)
            subscriber.onRemove(endpoint);
        liveEndpoints_.remove(endpoint);
        unreachableEndpoints_.remove(endpoint);
        endpointStateMap_.remove(endpoint);
        FailureDetector.instance.remove(endpoint);
        justRemovedEndpoints_.put(endpoint, System.currentTimeMillis());
    }
    void makeRandomGossipDigest(List<GossipDigest> gDigests)
    {
        EndpointState epState = endpointStateMap_.get(localEndpoint_);
        int generation = epState.getHeartBeatState().getGeneration();
        int maxVersion = getMaxEndpointStateVersion(epState);
        gDigests.add( new GossipDigest(localEndpoint_, generation, maxVersion) );
        List<InetAddress> endpoints = new ArrayList<InetAddress>(endpointStateMap_.keySet());
        Collections.shuffle(endpoints, random_);
        for (InetAddress endpoint : endpoints)
        {
            epState = endpointStateMap_.get(endpoint);
            if (epState != null)
            {
                generation = epState.getHeartBeatState().getGeneration();
                maxVersion = getMaxEndpointStateVersion(epState);
                gDigests.add(new GossipDigest(endpoint, generation, maxVersion));
            }
            else
            {
            	gDigests.add(new GossipDigest(endpoint, 0, 0));
            }
        }
        StringBuilder sb = new StringBuilder();
        for ( GossipDigest gDigest : gDigests )
        {
            sb.append(gDigest);
            sb.append(" ");
        }
        if (logger_.isTraceEnabled())
            logger_.trace("Gossip Digests are : " + sb.toString());
    }
    public boolean isKnownEndpoint(InetAddress endpoint)
    {
        return endpointStateMap_.containsKey(endpoint);
    }
    public int getCurrentGenerationNumber(InetAddress endpoint)
    {
    	return endpointStateMap_.get(endpoint).getHeartBeatState().getGeneration();
    }
    Message makeGossipDigestSynMessage(List<GossipDigest> gDigests) throws IOException
    {
        GossipDigestSynMessage gDigestMessage = new GossipDigestSynMessage(DatabaseDescriptor.getClusterName(), gDigests);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream( bos );
        GossipDigestSynMessage.serializer().serialize(gDigestMessage, dos);
        return new Message(localEndpoint_, StorageService.Verb.GOSSIP_DIGEST_SYN, bos.toByteArray());
    }
    Message makeGossipDigestAckMessage(GossipDigestAckMessage gDigestAckMessage) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        GossipDigestAckMessage.serializer().serialize(gDigestAckMessage, dos);
        if (logger_.isTraceEnabled())
            logger_.trace("@@@@ Size of GossipDigestAckMessage is " + bos.toByteArray().length);
        return new Message(localEndpoint_, StorageService.Verb.GOSSIP_DIGEST_ACK, bos.toByteArray());
    }
    Message makeGossipDigestAck2Message(GossipDigestAck2Message gDigestAck2Message) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        GossipDigestAck2Message.serializer().serialize(gDigestAck2Message, dos);
        return new Message(localEndpoint_, StorageService.Verb.GOSSIP_DIGEST_ACK2, bos.toByteArray());
    }
    boolean sendGossip(Message message, Set<InetAddress> epSet)
    {
        int size = epSet.size();
        List<InetAddress> liveEndpoints = new ArrayList<InetAddress>(epSet);
        int index = (size == 1) ? 0 : random_.nextInt(size);
        InetAddress to = liveEndpoints.get(index);
        if (logger_.isTraceEnabled())
            logger_.trace("Sending a GossipDigestSynMessage to {} ...", to);
        MessagingService.instance().sendOneWay(message, to);
        return seeds_.contains(to);
    }
    boolean doGossipToLiveMember(Message message)
    {
        int size = liveEndpoints_.size();
        if ( size == 0 )
            return false;
        return sendGossip(message, liveEndpoints_);
    }
    void doGossipToUnreachableMember(Message message)
    {
        double liveEndpoints = liveEndpoints_.size();
        double unreachableEndpoints = unreachableEndpoints_.size();
        if ( unreachableEndpoints > 0 )
        {
            double prob = unreachableEndpoints / (liveEndpoints + 1);
            double randDbl = random_.nextDouble();
            if ( randDbl < prob )
                sendGossip(message, unreachableEndpoints_.keySet());
        }
    }
    void doGossipToSeed(Message message)
    {
        int size = seeds_.size();
        if ( size > 0 )
        {
            if ( size == 1 && seeds_.contains(localEndpoint_) )
            {
                return;
            }
            if ( liveEndpoints_.size() == 0 )
            {
                sendGossip(message, seeds_);
            }
            else
            {
                double probability = seeds_.size() / (double)( liveEndpoints_.size() + unreachableEndpoints_.size() );
                double randDbl = random_.nextDouble();
                if ( randDbl <= probability )
                    sendGossip(message, seeds_);
            }
        }
    }
    void doStatusCheck()
    {
        long now = System.currentTimeMillis();
        Set<InetAddress> eps = endpointStateMap_.keySet();
        for ( InetAddress endpoint : eps )
        {
            if ( endpoint.equals(localEndpoint_) )
                continue;
            FailureDetector.instance.interpret(endpoint);
            EndpointState epState = endpointStateMap_.get(endpoint);
            if ( epState != null )
            {
                long duration = now - epState.getUpdateTimestamp();
                if (!epState.getHasToken() && !epState.isAlive() && (duration > FatClientTimeout_))
                {
                    if (StorageService.instance.getTokenMetadata().isMember(endpoint))
                        epState.setHasToken(true);
                    else
                    {
                        logger_.info("FatClient " + endpoint + " has been silent for " + FatClientTimeout_ + "ms, removing from gossip");
                        removeEndpoint(endpoint);
                    }
                }
                if ( !epState.isAlive() && (duration > aVeryLongTime_) )
                {
                    evictFromMembership(endpoint);
                }
            }
        }
        if (!justRemovedEndpoints_.isEmpty())
        {
            Map<InetAddress, Long> copy = new HashMap<InetAddress, Long>(justRemovedEndpoints_);
            for (Map.Entry<InetAddress, Long> entry : copy.entrySet())
            {
                if ((now - entry.getValue()) > QUARANTINE_DELAY)
                {
                    if (logger_.isDebugEnabled())
                        logger_.debug(QUARANTINE_DELAY + " elapsed, " + entry.getKey() + " gossip quarantine over");
                    justRemovedEndpoints_.remove(entry.getKey());
                }
            }
        }
    }
    public EndpointState getEndpointStateForEndpoint(InetAddress ep)
    {
        return endpointStateMap_.get(ep);
    }
    EndpointState getStateForVersionBiggerThan(InetAddress forEndpoint, int version)
    {
        if (logger_.isTraceEnabled())
            logger_.trace("Scanning for state greater than " + version + " for " + forEndpoint);
        EndpointState epState = endpointStateMap_.get(forEndpoint);
        EndpointState reqdEndpointState = null;
        if ( epState != null )
        {
            int localHbVersion = epState.getHeartBeatState().getHeartBeatVersion();
            if ( localHbVersion > version )
            {
                reqdEndpointState = new EndpointState(epState.getHeartBeatState());
            }
            for (Entry<ApplicationState, VersionedValue> entry : epState.getApplicationStateMap().entrySet())
            {
                VersionedValue value = entry.getValue();
                if ( value.version > version )
                {
                    if ( reqdEndpointState == null )
                    {
                        reqdEndpointState = new EndpointState(epState.getHeartBeatState());
                    }
                    final ApplicationState key = entry.getKey();
                    if (logger_.isTraceEnabled())
                        logger_.trace("Adding state " + key + ": " + value.value);
                    reqdEndpointState.addApplicationState(key, value);
                }
            }
        }
        return reqdEndpointState;
    }
    public int compareEndpointStartup(InetAddress addr1, InetAddress addr2)
    {
        EndpointState ep1 = getEndpointStateForEndpoint(addr1);
        EndpointState ep2 = getEndpointStateForEndpoint(addr2);
        assert ep1 != null && ep2 != null;
        return ep1.getHeartBeatState().getGeneration() - ep2.getHeartBeatState().getGeneration();
    }    
    void notifyFailureDetector(List<GossipDigest> gDigests)
    {
        IFailureDetector fd = FailureDetector.instance;
        for ( GossipDigest gDigest : gDigests )
        {
            EndpointState localEndpointState = endpointStateMap_.get(gDigest.endpoint_);
            if ( localEndpointState != null )
            {
                int localGeneration = endpointStateMap_.get(gDigest.endpoint_).getHeartBeatState().generation_;
                int remoteGeneration = gDigest.generation_;
                if ( remoteGeneration > localGeneration )
                {
                    fd.report(gDigest.endpoint_);
                    continue;
                }
                if ( remoteGeneration == localGeneration )
                {
                    int localVersion = getMaxEndpointStateVersion(localEndpointState);
                    int remoteVersion = gDigest.maxVersion_;
                    if ( remoteVersion > localVersion )
                    {
                        fd.report(gDigest.endpoint_);
                    }
                }
            }
        }
    }
    void notifyFailureDetector(Map<InetAddress, EndpointState> remoteEpStateMap)
    {
        IFailureDetector fd = FailureDetector.instance;
        for (Entry<InetAddress, EndpointState> entry : remoteEpStateMap.entrySet())
        {
            InetAddress endpoint = entry.getKey();
            EndpointState remoteEndpointState = entry.getValue();
            EndpointState localEndpointState = endpointStateMap_.get(endpoint);
            if ( localEndpointState != null )
            {
                int localGeneration = localEndpointState.getHeartBeatState().generation_;
                int remoteGeneration = remoteEndpointState.getHeartBeatState().generation_;
                if ( remoteGeneration > localGeneration )
                {
                    fd.report(endpoint);
                    continue;
                }
                if ( remoteGeneration == localGeneration )
                {
                    int localVersion = getMaxEndpointStateVersion(localEndpointState);
                    int remoteVersion = remoteEndpointState.getHeartBeatState().getHeartBeatVersion();
                    if ( remoteVersion > localVersion )
                    {
                        fd.report(endpoint);
                    }
                }
            }
        }
    }
    void markAlive(InetAddress addr, EndpointState localState)
    {
        if (logger_.isTraceEnabled())
            logger_.trace("marking as alive {}", addr);
        if ( !localState.isAlive() )
        {
            isAlive(addr, localState, true);
            logger_.info("InetAddress {} is now UP", addr);
        }
    }
    private void handleNewJoin(InetAddress ep, EndpointState epState)
    {
        if (justRemovedEndpoints_.containsKey(ep))
            return;
    	logger_.info("Node {} is now part of the cluster", ep);
        handleMajorStateChange(ep, epState, false);
    }
    private void handleGenerationChange(InetAddress ep, EndpointState epState)
    {
        logger_.info("Node {} has restarted, now UP again", ep);
        handleMajorStateChange(ep, epState, true);
    }
    private void handleMajorStateChange(InetAddress ep, EndpointState epState, boolean isKnownNode)
    {
        endpointStateMap_.put(ep, epState);
        isAlive(ep, epState, isKnownNode);
        for (IEndpointStateChangeSubscriber subscriber : subscribers_)
            subscriber.onJoin(ep, epState);
    }
    void applyStateLocally(Map<InetAddress, EndpointState> epStateMap)
    {
        for (Entry<InetAddress, EndpointState> entry : epStateMap.entrySet())
        {
            InetAddress ep = entry.getKey();
            if ( ep.equals( localEndpoint_ ) )
                continue;
            EndpointState localEpStatePtr = endpointStateMap_.get(ep);
            EndpointState remoteState = entry.getValue();
            if ( localEpStatePtr != null )
            {
            	int localGeneration = localEpStatePtr.getHeartBeatState().getGeneration();
            	int remoteGeneration = remoteState.getHeartBeatState().getGeneration();
            	if (remoteGeneration > localGeneration)
            	{
                    handleGenerationChange(ep, remoteState);
            	}
            	else if ( remoteGeneration == localGeneration )
            	{
	                int localMaxVersion = getMaxEndpointStateVersion(localEpStatePtr);
	                int remoteMaxVersion = getMaxEndpointStateVersion(remoteState);
	                if ( remoteMaxVersion > localMaxVersion )
	                {
	                    markAlive(ep, localEpStatePtr);
	                    applyHeartBeatStateLocally(ep, localEpStatePtr, remoteState);
	                    applyApplicationStateLocally(ep, localEpStatePtr, remoteState);
	                }
            	}
            }
            else
            {
            	handleNewJoin(ep, remoteState);
            }
        }
    }
    void applyHeartBeatStateLocally(InetAddress addr, EndpointState localState, EndpointState remoteState)
    {
        HeartBeatState localHbState = localState.getHeartBeatState();
        HeartBeatState remoteHbState = remoteState.getHeartBeatState();
        if ( remoteHbState.getGeneration() > localHbState.getGeneration() )
        {
            localState.setHeartBeatState(remoteHbState);
        }
        if ( localHbState.getGeneration() == remoteHbState.getGeneration() )
        {
            if ( remoteHbState.getHeartBeatVersion() > localHbState.getHeartBeatVersion() )
            {
                int oldVersion = localHbState.getHeartBeatVersion();
                localState.setHeartBeatState(remoteHbState);
                if (logger_.isTraceEnabled())
                    logger_.trace("Updating heartbeat state version to " + localState.getHeartBeatState().getHeartBeatVersion() + " from " + oldVersion + " for " + addr + " ...");
            }
        }
    }
    void applyApplicationStateLocally(InetAddress addr, EndpointState localStatePtr, EndpointState remoteStatePtr)
    {
        Map<ApplicationState, VersionedValue> localAppStateMap = localStatePtr.getApplicationStateMap();
        for (Entry<ApplicationState, VersionedValue> remoteEntry : remoteStatePtr.getApplicationStateMap().entrySet())
        {
            ApplicationState remoteKey = remoteEntry.getKey();
            VersionedValue remoteValue = remoteEntry.getValue();
            VersionedValue localValue = localAppStateMap.get(remoteKey);
            if ( localValue == null )
            {
                localStatePtr.addApplicationState(remoteKey, remoteValue);
                doNotifications(addr, remoteKey, remoteValue);
                continue;
            }
            int remoteGeneration = remoteStatePtr.getHeartBeatState().getGeneration();
            int localGeneration = localStatePtr.getHeartBeatState().getGeneration();
            assert remoteGeneration >= localGeneration; 
            if ( remoteGeneration > localGeneration )
            {
                localStatePtr.addApplicationState(remoteKey, remoteValue);
                doNotifications(addr, remoteKey, remoteValue);
                continue;
            }
            if ( remoteGeneration == localGeneration )
            {
                int remoteVersion = remoteValue.version;
                int localVersion = localValue.version;
                if ( remoteVersion > localVersion )
                {
                    localStatePtr.addApplicationState(remoteKey, remoteValue);
                    doNotifications(addr, remoteKey, remoteValue);
                }
            }
        }
    }
    void doNotifications(InetAddress addr, ApplicationState state, VersionedValue value)
    {
        for (IEndpointStateChangeSubscriber subscriber : subscribers_)
        {
            subscriber.onChange(addr, state, value);
        }
    }
    void isAlive(InetAddress addr, EndpointState epState, boolean value)
    {
        epState.isAlive(value);
        if (value)
        {
            liveEndpoints_.add(addr);
            unreachableEndpoints_.remove(addr);
            for (IEndpointStateChangeSubscriber subscriber : subscribers_)
                subscriber.onAlive(addr, epState);
        }
        else
        {
            liveEndpoints_.remove(addr);
            unreachableEndpoints_.put(addr, System.currentTimeMillis());
            for (IEndpointStateChangeSubscriber subscriber : subscribers_)
                subscriber.onDead(addr, epState);
        }
        if (epState.isAGossiper())
            return;
        epState.isAGossiper(true);
    }
    void requestAll(GossipDigest gDigest, List<GossipDigest> deltaGossipDigestList, int remoteGeneration)
    {
        deltaGossipDigestList.add( new GossipDigest(gDigest.getEndpoint(), remoteGeneration, 0) );
    }
    void sendAll(GossipDigest gDigest, Map<InetAddress, EndpointState> deltaEpStateMap, int maxRemoteVersion)
    {
        EndpointState localEpStatePtr = getStateForVersionBiggerThan(gDigest.getEndpoint(), maxRemoteVersion) ;
        if ( localEpStatePtr != null )
            deltaEpStateMap.put(gDigest.getEndpoint(), localEpStatePtr);
    }
    void examineGossiper(List<GossipDigest> gDigestList, List<GossipDigest> deltaGossipDigestList, Map<InetAddress, EndpointState> deltaEpStateMap)
    {
        for ( GossipDigest gDigest : gDigestList )
        {
            int remoteGeneration = gDigest.getGeneration();
            int maxRemoteVersion = gDigest.getMaxVersion();
            EndpointState epStatePtr = endpointStateMap_.get(gDigest.getEndpoint());
            if ( epStatePtr != null )
            {
                int localGeneration = epStatePtr.getHeartBeatState().getGeneration();
                int maxLocalVersion = getMaxEndpointStateVersion(epStatePtr);
                if ( remoteGeneration == localGeneration && maxRemoteVersion == maxLocalVersion )
                    continue;
                if ( remoteGeneration > localGeneration )
                {
                    requestAll(gDigest, deltaGossipDigestList, remoteGeneration);
                }
                if ( remoteGeneration < localGeneration )
                {
                    sendAll(gDigest, deltaEpStateMap, 0);
                }
                if ( remoteGeneration == localGeneration )
                {
                    if ( maxRemoteVersion > maxLocalVersion )
                    {
                        deltaGossipDigestList.add( new GossipDigest(gDigest.getEndpoint(), remoteGeneration, maxLocalVersion) );
                    }
                    if ( maxRemoteVersion < maxLocalVersion )
                    {
                        sendAll(gDigest, deltaEpStateMap, maxRemoteVersion);
                    }
                }
            }
            else
            {
                requestAll(gDigest, deltaGossipDigestList, remoteGeneration);
            }
        }
    }
    public void start(InetAddress localEndpoint, int generationNbr)
    {
        localEndpoint_ = localEndpoint;
        Set<InetAddress> seedHosts = DatabaseDescriptor.getSeeds();
        for (InetAddress seed : seedHosts)
        {
            if (seed.equals(localEndpoint))
                continue;
            seeds_.add(seed);
        }
        EndpointState localState = endpointStateMap_.get(localEndpoint_);
        if ( localState == null )
        {
            HeartBeatState hbState = new HeartBeatState(generationNbr);
            localState = new EndpointState(hbState);
            localState.isAlive(true);
            localState.isAGossiper(true);
            endpointStateMap_.put(localEndpoint_, localState);
        }
        DatabaseDescriptor.getEndpointSnitch().gossiperStarting();
        scheduledGossipTask = StorageService.scheduledTasks.scheduleWithFixedDelay(new GossipTask(),
                                                                                   Gossiper.intervalInMillis_,
                                                                                   Gossiper.intervalInMillis_,
                                                                                   TimeUnit.MILLISECONDS);
    }
    public void addSavedEndpoint(InetAddress ep)
    {
        EndpointState epState = endpointStateMap_.get(ep);
        if (epState == null)
        {
            epState = new EndpointState(new HeartBeatState(0));
            epState.isAlive(false);
            epState.isAGossiper(true);
            epState.setHasToken(true);
            endpointStateMap_.put(ep, epState);
            unreachableEndpoints_.put(ep, System.currentTimeMillis());
        }
    }
    public void addLocalApplicationState(ApplicationState state, VersionedValue value)
    {
        assert !StorageService.instance.isClientMode();
        EndpointState epState = endpointStateMap_.get(localEndpoint_);
        assert epState != null;
        epState.addApplicationState(state, value);
    }
    public void stop()
    {
        scheduledGossipTask.cancel(false);
    }
    public void initializeNodeUnsafe(InetAddress addr, int generationNbr) {
        EndpointState localState = endpointStateMap_.get(addr);
        if ( localState == null )
        {
            HeartBeatState hbState = new HeartBeatState(generationNbr);
            localState = new EndpointState(hbState);
            localState.isAlive(true);
            localState.isAGossiper(true);
            endpointStateMap_.put(addr, localState);
        }
    }
}
