package org.apache.cassandra.db.columniterator;
import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.ColumnFamily;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.io.sstable.SSTableReader;
import org.apache.cassandra.io.util.FileDataInput;
import org.apache.cassandra.utils.ByteBufferUtil;
public class SSTableSliceIterator implements IColumnIterator
{
    private final FileDataInput fileToClose;
    private IColumnIterator reader;
    private DecoratedKey key;
    public SSTableSliceIterator(SSTableReader sstable, DecoratedKey key, ByteBuffer startColumn, ByteBuffer finishColumn, boolean reversed)
    {
        this.key = key;
        fileToClose = sstable.getFileDataInput(this.key, DatabaseDescriptor.getSlicedReadBufferSizeInKB() * 1024);
        if (fileToClose == null)
            return;
        try
        {
            DecoratedKey keyInDisk = SSTableReader.decodeKey(sstable.partitioner,
                                                             sstable.descriptor,
                                                             ByteBufferUtil.readWithShortLength(fileToClose));
            assert keyInDisk.equals(key)
                   : String.format("%s != %s in %s", keyInDisk, key, fileToClose.getPath());
            SSTableReader.readRowSize(fileToClose, sstable.descriptor);
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
        reader = createReader(sstable, fileToClose, startColumn, finishColumn, reversed);
    }
    public SSTableSliceIterator(SSTableReader sstable, FileDataInput file, DecoratedKey key, ByteBuffer startColumn, ByteBuffer finishColumn, boolean reversed)
    {
        this.key = key;
        fileToClose = null;
        reader = createReader(sstable, file, startColumn, finishColumn, reversed);
    }
    private static IColumnIterator createReader(SSTableReader sstable, FileDataInput file, ByteBuffer startColumn, ByteBuffer finishColumn, boolean reversed)
    {
        return startColumn.remaining() == 0 && !reversed
                 ? new SimpleSliceReader(sstable, file, finishColumn)
                 : new IndexedSliceReader(sstable, file, startColumn, finishColumn, reversed);
    }
    public DecoratedKey getKey()
    {
        return key;
    }
    public ColumnFamily getColumnFamily() throws IOException
    {
        return reader == null ? null : reader.getColumnFamily();
    }
    public boolean hasNext()
    {
        return reader.hasNext();
    }
    public IColumn next()
    {
        return reader.next();
    }
    public void remove()
    {
        throw new UnsupportedOperationException();
    }
    public void close() throws IOException
    {
        if (fileToClose != null)
            fileToClose.close();
    }
}
