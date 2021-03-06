package org.apache.cassandra.streaming;
import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.cassandra.dht.AbstractBounds;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.net.CompactEndpointSerializationHelper;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
class StreamRequestMessage
{
    private static ICompactSerializer<StreamRequestMessage> serializer_;
    static
    {
        serializer_ = new StreamRequestMessageSerializer();
    }
    protected static ICompactSerializer<StreamRequestMessage> serializer()
    {
        return serializer_;
    }
    protected final long sessionId;
    protected final InetAddress target;
    protected final PendingFile file;
    protected final Collection<Range> ranges;
    protected final String table;
    protected final OperationType type;
    StreamRequestMessage(InetAddress target, Collection<Range> ranges, String table, long sessionId, OperationType type)
    {
        this.target = target;
        this.ranges = ranges;
        this.table = table;
        this.sessionId = sessionId;
        this.type = type;
        file = null;
    }
    StreamRequestMessage(InetAddress target, PendingFile file, long sessionId)
    {
        this.target = target;
        this.file = file;
        this.sessionId = sessionId;
        this.type = file.type;
        ranges = null;
        table = null;
    }
    Message makeMessage()
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try
        {
            StreamRequestMessage.serializer().serialize(this, dos);
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
        return new Message(FBUtilities.getLocalAddress(), StorageService.Verb.STREAM_REQUEST, bos.toByteArray() );
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder("");
        if (file == null)
        {
            sb.append(table);
            sb.append("@");
            sb.append(target);
            sb.append("------->");
            for ( Range range : ranges )
            {
                sb.append(range);
                sb.append(" ");
            }
            sb.append(type);
        }
        else
        {
            sb.append(file.toString());
        }
        return sb.toString();
    }
    private static class StreamRequestMessageSerializer implements ICompactSerializer<StreamRequestMessage>
    {
        public void serialize(StreamRequestMessage srm, DataOutputStream dos) throws IOException
        {
            dos.writeLong(srm.sessionId);
            CompactEndpointSerializationHelper.serialize(srm.target, dos);
            if (srm.file != null)
            {
                dos.writeBoolean(true);
                PendingFile.serializer().serialize(srm.file, dos);
            }
            else
            {
                dos.writeBoolean(false);
                dos.writeUTF(srm.table);
                dos.writeInt(srm.ranges.size());
                for (Range range : srm.ranges)
                {
                    AbstractBounds.serializer().serialize(range, dos);
                }
                dos.writeUTF(srm.type.name());
            }
        }
        public StreamRequestMessage deserialize(DataInputStream dis) throws IOException
        {
            long sessionId = dis.readLong();
            InetAddress target = CompactEndpointSerializationHelper.deserialize(dis);
            boolean singleFile = dis.readBoolean();
            if (singleFile)
            {
                PendingFile file = PendingFile.serializer().deserialize(dis);
                return new StreamRequestMessage(target, file, sessionId);
            }
            else
            {
                String table = dis.readUTF();
                int size = dis.readInt();
                List<Range> ranges = (size == 0) ? null : new ArrayList<Range>();
                for( int i = 0; i < size; ++i )
                {
                    ranges.add((Range) AbstractBounds.serializer().deserialize(dis));
                }
                OperationType type = OperationType.valueOf(dis.readUTF());
                return new StreamRequestMessage(target, ranges, table, sessionId, type);
            }
        }
    }
}
