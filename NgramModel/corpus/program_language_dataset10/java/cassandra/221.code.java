package org.apache.cassandra.dht;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import org.apache.cassandra.io.ICompactSerializer2;
import org.apache.cassandra.utils.Pair;
public abstract class AbstractBounds implements Serializable
{
    private static final long serialVersionUID = 1L;
    private static AbstractBoundsSerializer serializer = new AbstractBoundsSerializer();
    public static ICompactSerializer2<AbstractBounds> serializer()
    {
        return serializer;
    }
    private enum Type
    {
        RANGE,
        BOUNDS
    }
    public final Token left;
    public final Token right;
    protected transient final IPartitioner partitioner;
    public AbstractBounds(Token left, Token right, IPartitioner partitioner)
    {
        this.left = left;
        this.right = right;
        this.partitioner = partitioner;
    }
    public Pair<AbstractBounds,AbstractBounds> split(Token token)
    {
        assert left.equals(token) || contains(token);
        AbstractBounds lb = createFrom(token);
        AbstractBounds rb = lb != null && token.equals(right) ? null : new Range(token, right);
        return new Pair<AbstractBounds,AbstractBounds>(lb, rb);
    }
    @Override
    public int hashCode()
    {
        return 31 * left.hashCode() + right.hashCode();
    }
    @Override
    public abstract boolean equals(Object obj);
    public abstract boolean contains(Token start);
    public abstract AbstractBounds createFrom(Token right);
    public abstract List<AbstractBounds> unwrap();
    public static List<AbstractBounds> normalize(Collection<? extends AbstractBounds> bounds)
    {
        List<AbstractBounds> output = new ArrayList<AbstractBounds>();
        for (AbstractBounds bound : bounds)
            output.addAll(bound.unwrap());
        Collections.sort(output, new Comparator<AbstractBounds>()
        {
            public int compare(AbstractBounds b1, AbstractBounds b2)
            {
                return b1.left.compareTo(b2.left);
            }
        });
        return output;
    }
    private static class AbstractBoundsSerializer implements ICompactSerializer2<AbstractBounds>
    {
        public void serialize(AbstractBounds range, DataOutput out) throws IOException
        {
            out.writeInt(range instanceof Range ? Type.RANGE.ordinal() : Type.BOUNDS.ordinal());
            Token.serializer().serialize(range.left, out);
            Token.serializer().serialize(range.right, out);
        }
        public AbstractBounds deserialize(DataInput in) throws IOException
        {
            if (in.readInt() == Type.RANGE.ordinal())
                return new Range(Token.serializer().deserialize(in), Token.serializer().deserialize(in));
            return new Bounds(Token.serializer().deserialize(in), Token.serializer().deserialize(in));
        }
    }
}
