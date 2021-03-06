package org.apache.cassandra.io;
import java.io.DataOutput;
import java.io.IOError;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.*;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import org.apache.commons.collections.iterators.CollatingIterator;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.ColumnIndexer;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.io.sstable.SSTable;
import org.apache.cassandra.io.sstable.SSTableIdentityIterator;
import org.apache.cassandra.io.util.DataOutputBuffer;
import org.apache.cassandra.io.util.IIterableColumns;
import org.apache.cassandra.utils.ReducingIterator;
public class LazilyCompactedRow extends AbstractCompactedRow implements IIterableColumns
{
    private final List<SSTableIdentityIterator> rows;
    private final boolean shouldPurge;
    private final int gcBefore;
    private final DataOutputBuffer headerBuffer;
    private ColumnFamily emptyColumnFamily;
    private LazyColumnIterator iter;
    private int columnCount;
    private long columnSerializedSize;
    public LazilyCompactedRow(ColumnFamilyStore cfStore, List<SSTableIdentityIterator> rows, boolean major, int gcBefore)
    {
        super(rows.get(0).getKey());
        this.gcBefore = gcBefore;
        this.rows = new ArrayList<SSTableIdentityIterator>(rows);
        Set<SSTable> sstables = new HashSet<SSTable>();
        for (SSTableIdentityIterator row : rows)
        {
            sstables.add(row.sstable);
            ColumnFamily cf = row.getColumnFamily();
            if (emptyColumnFamily == null)
                emptyColumnFamily = cf;
            else
                emptyColumnFamily.delete(cf);
        }
        this.shouldPurge = major || !cfStore.isKeyInRemainingSSTables(key, sstables);
        headerBuffer = new DataOutputBuffer();
        ColumnIndexer.serialize(this, headerBuffer);
        columnCount = iter.size;
        columnSerializedSize = iter.serializedSize;
        iter = null;
    }
    public void write(DataOutput out) throws IOException
    {
        if (rows.size() == 1 && !shouldPurge)
        {
            SSTableIdentityIterator row = rows.get(0);
            out.writeLong(row.dataSize);
            row.echoData(out);
            return;
        }
        DataOutputBuffer clockOut = new DataOutputBuffer();
        ColumnFamily.serializer().serializeCFInfo(emptyColumnFamily, clockOut);
        out.writeLong(headerBuffer.getLength() + clockOut.getLength() + columnSerializedSize);
        out.write(headerBuffer.getData(), 0, headerBuffer.getLength());
        out.write(clockOut.getData(), 0, clockOut.getLength());
        out.writeInt(columnCount);
        Iterator<IColumn> iter = iterator();
        while (iter.hasNext())
        {
            IColumn column = iter.next();
            emptyColumnFamily.getColumnSerializer().serialize(column, out);
        }
    }
    public void update(MessageDigest digest)
    {
        digest.update(headerBuffer.getData(), 0, headerBuffer.getLength());
        DataOutputBuffer out = new DataOutputBuffer();
        Iterator<IColumn> iter = iterator();
        while (iter.hasNext())
        {
            IColumn column = iter.next();
            out.reset();
            try
            {
                emptyColumnFamily.getColumnSerializer().serialize(column, out);
            }
            catch (IOException e)
            {
                throw new IOError(e);
            }
            digest.update(out.getData(), 0, out.getLength());
        }
    }
    public boolean isEmpty()
    {
        boolean cfIrrelevant = ColumnFamilyStore.removeDeletedCF(emptyColumnFamily, gcBefore) == null;
        return cfIrrelevant && columnCount == 0;
    }
    public int getEstimatedColumnCount()
    {
        int n = 0;
        for (SSTableIdentityIterator row : rows)
            n += row.columnCount;
        return n;
    }
    public AbstractType getComparator()
    {
        return emptyColumnFamily.getComparator();
    }
    public Iterator<IColumn> iterator()
    {
        for (SSTableIdentityIterator row : rows)
        {
            row.reset();
        }
        Comparator<IColumn> nameComparator = new Comparator<IColumn>()
        {
            public int compare(IColumn o1, IColumn o2)
            {
                return getComparator().compare(o1.name(), o2.name());
            }
        };
        iter = new LazyColumnIterator(new CollatingIterator(nameComparator, rows));
        return Iterators.filter(iter, Predicates.notNull());
    }
    public int columnCount()
    {
        return columnCount;
    }
    private class LazyColumnIterator extends ReducingIterator<IColumn, IColumn>
    {
        ColumnFamily container = emptyColumnFamily.cloneMeShallow();
        long serializedSize = 4; 
        int size = 0;
        public LazyColumnIterator(Iterator<IColumn> source)
        {
            super(source);
        }
        @Override
        protected boolean isEqual(IColumn o1, IColumn o2)
        {
            return o1.name().equals(o2.name());
        }
        public void reduce(IColumn current)
        {
            container.addColumn(current);
        }
        protected IColumn getReduced()
        {
            assert container != null;
            IColumn reduced = container.iterator().next();
            ColumnFamily purged = shouldPurge ? ColumnFamilyStore.removeDeleted(container, gcBefore) : container;
            if (purged == null || !purged.iterator().hasNext())
            {
                container.clear();
                return null;
            }
            container.clear();
            serializedSize += reduced.serializedSize();
            size++;
            return reduced;
        }
    }
}
