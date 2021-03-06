package org.apache.cassandra.dht;
import java.nio.ByteBuffer;
import org.junit.Test;
import org.apache.cassandra.utils.FBUtilities;
public class CollatingOrderPreservingPartitionerTest extends PartitionerTestCase<BytesToken>
{
    @Override
    public void initPartitioner()
    {
        partitioner = new CollatingOrderPreservingPartitioner();
    }
    @Test
    public void testTokenFactoryStringsNonUTF()
    {
        Token.TokenFactory factory = this.partitioner.getTokenFactory();
        BytesToken tok = new BytesToken(new byte[]{(byte)0xFF, (byte)0xFF});
        assert tok.compareTo(factory.fromString(factory.toString(tok))) == 0;
    }
    @Test
    public void testCompare()
    {
        assert tok("").compareTo(tok("asdf")) < 0;
        assert tok("asdf").compareTo(tok("")) > 0;
        assert tok("").compareTo(tok("")) == 0;
        assert tok("z").compareTo(tok("a")) > 0;
        assert tok("a").compareTo(tok("z")) < 0;
        assert tok("asdf").compareTo(tok("asdf")) == 0;
        assert tok("asdz").compareTo(tok("asdf")) > 0;
    }
}
