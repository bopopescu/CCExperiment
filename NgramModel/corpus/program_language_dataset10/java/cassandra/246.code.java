package org.apache.cassandra.gms;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.net.CompactEndpointSerializationHelper;
class GossipDigestSynMessage
{
    private static ICompactSerializer<GossipDigestSynMessage> serializer_;
    static
    {
        serializer_ = new GossipDigestSynMessageSerializer();
    }
    String clusterId_;
    List<GossipDigest> gDigests_ = new ArrayList<GossipDigest>();
    public static ICompactSerializer<GossipDigestSynMessage> serializer()
    {
        return serializer_;
    }
    public GossipDigestSynMessage(String clusterId, List<GossipDigest> gDigests)
    {      
        clusterId_ = clusterId;
        gDigests_ = gDigests;
    }
    List<GossipDigest> getGossipDigests()
    {
        return gDigests_;
    }
}
class GossipDigestSerializationHelper
{
    private static Logger logger_ = LoggerFactory.getLogger(GossipDigestSerializationHelper.class);
    static void serialize(List<GossipDigest> gDigestList, DataOutputStream dos) throws IOException
    {
        dos.writeInt(gDigestList.size());
        for ( GossipDigest gDigest : gDigestList )
        {
            GossipDigest.serializer().serialize( gDigest, dos );
        }
    }
    static List<GossipDigest> deserialize(DataInputStream dis) throws IOException
    {
        int size = dis.readInt();            
        List<GossipDigest> gDigests = new ArrayList<GossipDigest>(size);
        for ( int i = 0; i < size; ++i )
        {
            assert dis.available() > 0;
            gDigests.add(GossipDigest.serializer().deserialize(dis));                
        }        
        return gDigests;
    }
}
class EndpointStatesSerializationHelper
{
    private static final Logger logger_ = LoggerFactory.getLogger(EndpointStatesSerializationHelper.class);
    static void serialize(Map<InetAddress, EndpointState> epStateMap, DataOutputStream dos) throws IOException
    {
        dos.writeInt(epStateMap.size());
        for (Entry<InetAddress, EndpointState> entry : epStateMap.entrySet())
        {
            InetAddress ep = entry.getKey();
            CompactEndpointSerializationHelper.serialize(ep, dos);
            EndpointState.serializer().serialize(entry.getValue(), dos);
        }
    }
    static Map<InetAddress, EndpointState> deserialize(DataInputStream dis) throws IOException
    {
        int size = dis.readInt();            
        Map<InetAddress, EndpointState> epStateMap = new HashMap<InetAddress, EndpointState>(size);
        for ( int i = 0; i < size; ++i )
        {
            assert dis.available() > 0;
            InetAddress ep = CompactEndpointSerializationHelper.deserialize(dis);
            EndpointState epState = EndpointState.serializer().deserialize(dis);
            epStateMap.put(ep, epState);
        }
        return epStateMap;
    }
}
class GossipDigestSynMessageSerializer implements ICompactSerializer<GossipDigestSynMessage>
{   
    public void serialize(GossipDigestSynMessage gDigestSynMessage, DataOutputStream dos) throws IOException
    {    
        dos.writeUTF(gDigestSynMessage.clusterId_);
        GossipDigestSerializationHelper.serialize(gDigestSynMessage.gDigests_, dos);
    }
    public GossipDigestSynMessage deserialize(DataInputStream dis) throws IOException
    {
        String clusterId = dis.readUTF();
        List<GossipDigest> gDigests = GossipDigestSerializationHelper.deserialize(dis);
        return new GossipDigestSynMessage(clusterId, gDigests);
    }
}
