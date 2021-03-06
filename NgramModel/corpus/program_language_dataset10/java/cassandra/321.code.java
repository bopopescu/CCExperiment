package org.apache.cassandra.locator;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.google.common.collect.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.service.StorageService;
public class TokenMetadata
{
    private static Logger logger = LoggerFactory.getLogger(TokenMetadata.class);
    private BiMap<Token, InetAddress> tokenToEndpointMap;
    private BiMap<Token, InetAddress> bootstrapTokens;
    private Set<InetAddress> leavingEndpoints;
    private ConcurrentMap<String, Multimap<Range, InetAddress>> pendingRanges;
    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);
    private ArrayList<Token> sortedTokens;
    private final CopyOnWriteArrayList<AbstractReplicationStrategy> subscribers;
    public TokenMetadata()
    {
        this(null);
    }
    public TokenMetadata(BiMap<Token, InetAddress> tokenToEndpointMap)
    {
        if (tokenToEndpointMap == null)
            tokenToEndpointMap = HashBiMap.create();
        this.tokenToEndpointMap = tokenToEndpointMap;
        bootstrapTokens = HashBiMap.create();
        leavingEndpoints = new HashSet<InetAddress>();
        pendingRanges = new ConcurrentHashMap<String, Multimap<Range, InetAddress>>();
        sortedTokens = sortTokens();
        subscribers = new CopyOnWriteArrayList<AbstractReplicationStrategy>();
    }
    private ArrayList<Token> sortTokens()
    {
        ArrayList<Token> tokens = new ArrayList<Token>(tokenToEndpointMap.keySet());
        Collections.sort(tokens);
        return tokens;
    }
    public int pendingRangeChanges(InetAddress source)
    {
        int n = 0;
        Range sourceRange = getPrimaryRangeFor(getToken(source));
        for (Token token : bootstrapTokens.keySet())
            if (sourceRange.contains(token))
                n++;
        return n;
    }
    public void updateNormalToken(Token token, InetAddress endpoint)
    {
        assert token != null;
        assert endpoint != null;
        lock.writeLock().lock();
        try
        {
            bootstrapTokens.inverse().remove(endpoint);
            tokenToEndpointMap.inverse().remove(endpoint);
            InetAddress prev = tokenToEndpointMap.put(token, endpoint);
            if (!endpoint.equals(prev))
            {
                if (prev != null)
                    logger.warn("Token " + token + " changing ownership from " + prev + " to " + endpoint);
                sortedTokens = sortTokens();
            }
            leavingEndpoints.remove(endpoint);
            invalidateCaches();
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }
    public void addBootstrapToken(Token token, InetAddress endpoint)
    {
        assert token != null;
        assert endpoint != null;
        lock.writeLock().lock();
        try
        {
            InetAddress oldEndpoint;
            oldEndpoint = bootstrapTokens.get(token);
            if (oldEndpoint != null && !oldEndpoint.equals(endpoint))
                throw new RuntimeException("Bootstrap Token collision between " + oldEndpoint + " and " + endpoint + " (token " + token);
            oldEndpoint = tokenToEndpointMap.get(token);
            if (oldEndpoint != null && !oldEndpoint.equals(endpoint))
                throw new RuntimeException("Bootstrap Token collision between " + oldEndpoint + " and " + endpoint + " (token " + token);
            bootstrapTokens.inverse().remove(endpoint);
            bootstrapTokens.put(token, endpoint);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }
    public void removeBootstrapToken(Token token)
    {
        assert token != null;
        lock.writeLock().lock();
        try
        {
            bootstrapTokens.remove(token);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }
    public void addLeavingEndpoint(InetAddress endpoint)
    {
        assert endpoint != null;
        lock.writeLock().lock();
        try
        {
            leavingEndpoints.add(endpoint);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }
    public void removeEndpoint(InetAddress endpoint)
    {
        assert endpoint != null;
        lock.writeLock().lock();
        try
        {
            bootstrapTokens.inverse().remove(endpoint);
            tokenToEndpointMap.inverse().remove(endpoint);
            leavingEndpoints.remove(endpoint);
            sortedTokens = sortTokens();
            invalidateCaches();
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }
    public Token getToken(InetAddress endpoint)
    {
        assert endpoint != null;
        assert isMember(endpoint); 
        lock.readLock().lock();
        try
        {
            return tokenToEndpointMap.inverse().get(endpoint);
        }
        finally
        {
            lock.readLock().unlock();
        }
    }
    public boolean isMember(InetAddress endpoint)
    {
        assert endpoint != null;
        lock.readLock().lock();
        try
        {
            return tokenToEndpointMap.inverse().containsKey(endpoint);
        }
        finally
        {
            lock.readLock().unlock();
        }
    }
    public boolean isLeaving(InetAddress endpoint)
    {
        assert endpoint != null;
        lock.readLock().lock();
        try
        {
            return leavingEndpoints.contains(endpoint);
        }
        finally
        {
            lock.readLock().unlock();
        }
    }
    public TokenMetadata cloneOnlyTokenMap()
    {
        lock.readLock().lock();
        try
        {
            return new TokenMetadata(HashBiMap.create(tokenToEndpointMap));
        }
        finally
        {
            lock.readLock().unlock();
        }
    }
    public TokenMetadata cloneAfterAllLeft()
    {
        lock.readLock().lock();
        try
        {
            TokenMetadata allLeftMetadata = cloneOnlyTokenMap();
            for (InetAddress endpoint : leavingEndpoints)
                allLeftMetadata.removeEndpoint(endpoint);
            return allLeftMetadata;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }
    public Set<Map.Entry<Token,InetAddress>> entrySet()
    {
        return tokenToEndpointMap.entrySet();
    }
    public InetAddress getEndpoint(Token token)
    {
        lock.readLock().lock();
        try
        {
            return tokenToEndpointMap.get(token);
        }
        finally
        {
            lock.readLock().unlock();
        }
    }
    public Range getPrimaryRangeFor(Token right)
    {
        return new Range(getPredecessor(right), right);
    }
    public ArrayList<Token> sortedTokens()
    {
        lock.readLock().lock();
        try
        {
            return sortedTokens;
        }
        finally
        {
            lock.readLock().unlock();
        }
    }
    private Multimap<Range, InetAddress> getPendingRangesMM(String table)
    {
        Multimap<Range, InetAddress> map = pendingRanges.get(table);
        if (map == null)
        {
            map = HashMultimap.create();
            Multimap<Range, InetAddress> priorMap = pendingRanges.putIfAbsent(table, map);
            if (priorMap != null)
                map = priorMap;
        }
        return map;
    }
    public Map<Range, Collection<InetAddress>> getPendingRanges(String table)
    {
        return getPendingRangesMM(table).asMap();
    }
    public List<Range> getPendingRanges(String table, InetAddress endpoint)
    {
        List<Range> ranges = new ArrayList<Range>();
        for (Map.Entry<Range, InetAddress> entry : getPendingRangesMM(table).entries())
        {
            if (entry.getValue().equals(endpoint))
            {
                ranges.add(entry.getKey());
            }
        }
        return ranges;
    }
    public void setPendingRanges(String table, Multimap<Range, InetAddress> rangeMap)
    {
        pendingRanges.put(table, rangeMap);
    }
    public Token getPredecessor(Token token)
    {
        List tokens = sortedTokens();
        int index = Collections.binarySearch(tokens, token);
        assert index >= 0 : token + " not found in " + StringUtils.join(tokenToEndpointMap.keySet(), ", ");
        return (Token) (index == 0 ? tokens.get(tokens.size() - 1) : tokens.get(index - 1));
    }
    public Token getSuccessor(Token token)
    {
        List tokens = sortedTokens();
        int index = Collections.binarySearch(tokens, token);
        assert index >= 0 : token + " not found in " + StringUtils.join(tokenToEndpointMap.keySet(), ", ");
        return (Token) ((index == (tokens.size() - 1)) ? tokens.get(0) : tokens.get(index + 1));
    }
    public Map<Token, InetAddress> getBootstrapTokens()
    {
        return bootstrapTokens;
    }
    public Set<InetAddress> getLeavingEndpoints()
    {
        return leavingEndpoints;
    }
    public static int firstTokenIndex(final ArrayList ring, Token start, boolean insertMin)
    {
        assert ring.size() > 0;
        int i = Collections.binarySearch(ring, start);
        if (i < 0)
        {
            i = (i + 1) * (-1);
            if (i >= ring.size())
                i = insertMin ? -1 : 0;
        }
        return i;
    }
    public static Token firstToken(final ArrayList<Token> ring, Token start)
    {
        return ring.get(firstTokenIndex(ring, start, false));
    }
    public static Iterator<Token> ringIterator(final ArrayList<Token> ring, Token start, boolean includeMin)
    {
        final boolean insertMin = (includeMin && !ring.get(0).equals(StorageService.getPartitioner().getMinimumToken())) ? true : false;
        final int startIndex = firstTokenIndex(ring, start, insertMin);
        return new AbstractIterator<Token>()
        {
            int j = startIndex;
            protected Token computeNext()
            {
                if (j < -1)
                    return endOfData();
                try
                {
                    if (j == -1)
                        return StorageService.getPartitioner().getMinimumToken();
                    return ring.get(j);
                }
                finally
                {
                    j++;
                    if (j == ring.size())
                        j = insertMin ? -1 : 0;
                    if (j == startIndex)
                        j = -2;
                }
            }
        };
    }
    public void clearUnsafe()
    {
        bootstrapTokens.clear();
        tokenToEndpointMap.clear();
        leavingEndpoints.clear();
        pendingRanges.clear();
        invalidateCaches();
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        lock.readLock().lock();
        try
        {
            Set<InetAddress> eps = tokenToEndpointMap.inverse().keySet();
            if (!eps.isEmpty())
            {
                sb.append("Normal Tokens:");
                sb.append(System.getProperty("line.separator"));
                for (InetAddress ep : eps)
                {
                    sb.append(ep);
                    sb.append(":");
                    sb.append(tokenToEndpointMap.inverse().get(ep));
                    sb.append(System.getProperty("line.separator"));
                }
            }
            if (!bootstrapTokens.isEmpty())
            {
                sb.append("Bootstrapping Tokens:" );
                sb.append(System.getProperty("line.separator"));
                for (Map.Entry<Token, InetAddress> entry : bootstrapTokens.entrySet())
                {
                    sb.append(entry.getValue() + ":" + entry.getKey());
                    sb.append(System.getProperty("line.separator"));
                }
            }
            if (!leavingEndpoints.isEmpty())
            {
                sb.append("Leaving Endpoints:");
                sb.append(System.getProperty("line.separator"));
                for (InetAddress ep : leavingEndpoints)
                {
                    sb.append(ep);
                    sb.append(System.getProperty("line.separator"));
                }
            }
            if (!pendingRanges.isEmpty())
            {
                sb.append("Pending Ranges:");
                sb.append(System.getProperty("line.separator"));
                sb.append(printPendingRanges());
            }
        }
        finally
        {
            lock.readLock().unlock();
        }
        return sb.toString();
    }
    public String printPendingRanges()
    {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Multimap<Range, InetAddress>> entry : pendingRanges.entrySet())
        {
            for (Map.Entry<Range, InetAddress> rmap : entry.getValue().entries())
            {
                sb.append(rmap.getValue() + ":" + rmap.getKey());
                sb.append(System.getProperty("line.separator"));
            }
        }
        return sb.toString();
    }
    public void invalidateCaches()
    {
        for (AbstractReplicationStrategy subscriber : subscribers)
        {
            subscriber.invalidateCachedTokenEndpointValues();
        }
    }
    public void register(AbstractReplicationStrategy subscriber)
    {
        subscribers.add(subscriber);
    }
    public void unregister(AbstractReplicationStrategy subscriber)
    {
        subscribers.remove(subscriber);
    }
    public Collection<InetAddress> getWriteEndpoints(Token token, String table, Collection<InetAddress> naturalEndpoints)
    {
        Map<Range, Collection<InetAddress>> ranges = getPendingRanges(table);
        if (ranges.isEmpty())
            return naturalEndpoints;
        List<InetAddress> endpoints = new ArrayList<InetAddress>(naturalEndpoints);
        for (Map.Entry<Range, Collection<InetAddress>> entry : ranges.entrySet())
        {
            if (entry.getKey().contains(token))
            {
                endpoints.addAll(entry.getValue());
            }
        }
        return endpoints;
    }
    public Map<Token, InetAddress> getTokenToEndpointMap()
    {
        Map<Token, InetAddress> map = new HashMap<Token, InetAddress>(tokenToEndpointMap.size() + bootstrapTokens.size());
        map.putAll(tokenToEndpointMap);
        map.putAll(bootstrapTokens);
        return map;
    }
}
