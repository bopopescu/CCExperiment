package org.apache.cassandra.db;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.utils.ByteBufferUtil;
public class ReadResponse
{
private static ICompactSerializer<ReadResponse> serializer_;
    static
    {
        serializer_ = new ReadResponseSerializer();
    }
    public static ICompactSerializer<ReadResponse> serializer()
    {
        return serializer_;
    }
	private final Row row_;
	private final ByteBuffer digest_;
	public ReadResponse(ByteBuffer digest )
    {
        assert digest != null;
		digest_= digest;
        row_ = null;
	}
	public ReadResponse(Row row)
    {
        assert row != null;
		row_ = row;
        digest_ = null;
	}
	public Row row() 
    {
		return row_;
    }
	public ByteBuffer digest() 
    {
		return digest_;
	}
	public boolean isDigestQuery()
    {
    	return digest_ != null;
    }
}
class ReadResponseSerializer implements ICompactSerializer<ReadResponse>
{
	public void serialize(ReadResponse rm, DataOutputStream dos) throws IOException
	{
        dos.writeInt(rm.isDigestQuery() ? rm.digest().remaining() : 0);
        ByteBuffer buffer = rm.isDigestQuery() ? rm.digest() : ByteBufferUtil.EMPTY_BYTE_BUFFER;
        ByteBufferUtil.write(buffer, dos);
        dos.writeBoolean(rm.isDigestQuery());
        if (!rm.isDigestQuery())
        {
            Row.serializer().serialize(rm.row(), dos);
        }
    }
    public ReadResponse deserialize(DataInputStream dis) throws IOException
    {
        byte[] digest = null;
        int digestSize = dis.readInt();
        if (digestSize > 0)
        {
            digest = new byte[digestSize];
            dis.readFully(digest, 0, digestSize);
        }
        boolean isDigest = dis.readBoolean();
        assert isDigest == digestSize > 0;
        Row row = null;
        if (!isDigest)
        {
            row = Row.serializer().deserialize(dis);
        }
        return isDigest ? new ReadResponse(ByteBuffer.wrap(digest)) : new ReadResponse(row);
    } 
}
