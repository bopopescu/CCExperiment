package org.apache.cassandra.db.marshal;
import java.nio.ByteBuffer;
import org.apache.commons.lang.NotImplementedException;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.utils.ByteBufferUtil;
public class LocalByPartionerType<T extends Token> extends AbstractType
{
    private final IPartitioner<T> partitioner;
    public LocalByPartionerType(IPartitioner<T> partitioner)
    {
        this.partitioner = partitioner;
    }
    public String getString(ByteBuffer bytes)
    {
        return ByteBufferUtil.bytesToHex(bytes);
    }
    public ByteBuffer fromString(String source)
    {
        throw new NotImplementedException();
    }
    public int compare(ByteBuffer o1, ByteBuffer o2)
    {
        return partitioner.decorateKey(o1).compareTo(partitioner.decorateKey(o2));
    }
    public void validate(ByteBuffer bytes) throws MarshalException
    {
        throw new IllegalStateException("You shouldn't be validating this.");
    }
}
