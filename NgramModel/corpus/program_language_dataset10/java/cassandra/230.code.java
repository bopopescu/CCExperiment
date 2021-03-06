package org.apache.cassandra.dht;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.utils.ByteBufferUtil;
public class LocalPartitioner implements IPartitioner<LocalToken>
{
    private final AbstractType comparator;
    public LocalPartitioner(AbstractType comparator)
    {
        this.comparator = comparator;
    }
    public DecoratedKey<LocalToken> convertFromDiskFormat(ByteBuffer key)
    {
        return decorateKey(key);
    }
    public DecoratedKey<LocalToken> decorateKey(ByteBuffer key)
    {
        return new DecoratedKey<LocalToken>(getToken(key), key);
    }
    public Token midpoint(Token left, Token right)
    {
        throw new UnsupportedOperationException();
    }
    public LocalToken getMinimumToken()
    {
        return new LocalToken(comparator, ByteBufferUtil.EMPTY_BYTE_BUFFER);
    }
    public LocalToken getToken(ByteBuffer key)
    {
        return new LocalToken(comparator, key);
    }
    public LocalToken getRandomToken()
    {
        throw new UnsupportedOperationException();
    }
    public Token.TokenFactory getTokenFactory()
    {
        throw new UnsupportedOperationException();
    }
    public boolean preservesOrder()
    {
        return true;
    }
    public Map<Token, Float> describeOwnership(List<Token> sortedTokens)
    {
        return Collections.singletonMap((Token)getMinimumToken(), new Float(1.0));
    }
}
