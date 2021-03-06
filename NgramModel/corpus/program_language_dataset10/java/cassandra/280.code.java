package org.apache.cassandra.io.sstable;
import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.collections.iterators.CollatingIterator;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.ReducingIterator;
public class ReducingKeyIterator implements Iterator<DecoratedKey>, Closeable
{
    private final CollatingIterator ci;
    private final ReducingIterator<DecoratedKey, DecoratedKey> iter;
    public ReducingKeyIterator(Collection<SSTableReader> sstables)
    {
        ci = FBUtilities.getCollatingIterator();
        for (SSTableReader sstable : sstables)
        {
            ci.addIterator(new KeyIterator(sstable.descriptor));
        }
        iter = new ReducingIterator<DecoratedKey, DecoratedKey>(ci)
        {
            DecoratedKey reduced = null;
            public void reduce(DecoratedKey current)
            {
                reduced = current;
            }
            protected DecoratedKey getReduced()
            {
                return reduced;
            }
        };
    }
    public void close() throws IOException
    {
        for (Object o : ci.getIterators())
        {
            ((KeyIterator) o).close();
        }
    }
    public long getTotalBytes()
    {
        long m = 0;
        for (Object o : ci.getIterators())
        {
            m += ((KeyIterator) o).getTotalBytes();
        }
        return m;
    }
    public long getBytesRead()
    {
        long m = 0;
        for (Object o : ci.getIterators())
        {
            m += ((KeyIterator) o).getBytesRead();
        }
        return m;
    }
    public String getTaskType()
    {
        return "Secondary index build";
    }
    public boolean hasNext()
    {
        return iter.hasNext();
    }
    public DecoratedKey next()
    {
        return iter.next();
    }
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
}
