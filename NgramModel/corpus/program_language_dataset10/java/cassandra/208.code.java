package org.apache.cassandra.db.marshal;
import java.nio.ByteBuffer;
import org.apache.cassandra.utils.ByteBufferUtil;
public class LongType extends AbstractType
{
    public static final LongType instance = new LongType();
    LongType() {} 
    public int compare(ByteBuffer o1, ByteBuffer o2)
    {
        if (o1.remaining() == 0)
        {
            return o2.remaining() == 0 ? 0 : -1;
        }
        if (o2.remaining() == 0)
        {
            return 1;
        }
        int diff = o1.get(o1.position()) - o2.get(o2.position());
        if (diff != 0)
            return diff;
        return ByteBufferUtil.compareUnsigned(o1, o2);
    }
    public String getString(ByteBuffer bytes)
    {
        if (bytes.remaining() == 0)
        {
            return "";
        }
        if (bytes.remaining() != 8)
        {
            throw new MarshalException("A long is exactly 8 bytes: "+bytes.remaining());
        }
        return String.valueOf(bytes.getLong(bytes.position()));
    }
    public ByteBuffer fromString(String source)
    {
        long longType;
        try
        {
            longType = Long.parseLong(source);
        }
        catch (Exception e)
        {
            throw new RuntimeException("'" + source + "' could not be translated into a LongType.");
        }
        return ByteBufferUtil.bytes(longType);
    }
    public void validate(ByteBuffer bytes) throws MarshalException
    {
        if (bytes.remaining() != 8 && bytes.remaining() != 0)
            throw new MarshalException(String.format("Expected 8 or 0 byte long (%d)", bytes.remaining()));
    }
}
