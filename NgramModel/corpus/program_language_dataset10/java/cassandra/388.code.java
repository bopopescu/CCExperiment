package org.apache.cassandra.streaming;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.FBUtilities;
class StreamReply
{
    static enum Status
    {
        FILE_FINISHED,
        FILE_RETRY,
        SESSION_FINISHED,
    }
    public static final ICompactSerializer<StreamReply> serializer = new FileStatusSerializer();
    public final long sessionId;
    public final String file;
    public final Status action;
    public StreamReply(String file, long sessionId, Status action)
    {
        this.file = file;
        this.action = action;
        this.sessionId = sessionId;
    }
    public Message createMessage() throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream( bos );
        serializer.serialize(this, dos);
        return new Message(FBUtilities.getLocalAddress(), StorageService.Verb.STREAM_REPLY, bos.toByteArray());
    }
    @Override
    public String toString()
    {
        return "StreamReply(" +
               "sessionId=" + sessionId +
               ", file='" + file + '\'' +
               ", action=" + action +
               ')';
    }
    private static class FileStatusSerializer implements ICompactSerializer<StreamReply>
    {
        public void serialize(StreamReply reply, DataOutputStream dos) throws IOException
        {
            dos.writeLong(reply.sessionId);
            dos.writeUTF(reply.file);
            dos.writeInt(reply.action.ordinal());
        }
        public StreamReply deserialize(DataInputStream dis) throws IOException
        {
            long sessionId = dis.readLong();
            String targetFile = dis.readUTF();
            Status action = Status.values()[dis.readInt()];
            return new StreamReply(targetFile, sessionId, action);
        }
    }
}
