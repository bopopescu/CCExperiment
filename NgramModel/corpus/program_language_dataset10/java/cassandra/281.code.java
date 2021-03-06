package org.apache.cassandra.io.sstable;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOError;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.io.util.BufferedRandomAccessFile;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.cassandra.utils.EstimatedHistogram;
import org.apache.cassandra.utils.Pair;
public abstract class SSTable
{
    static final Logger logger = LoggerFactory.getLogger(SSTable.class);
    public static final String COMPONENT_DATA = Component.Type.DATA.repr;
    public static final String COMPONENT_INDEX = Component.Type.PRIMARY_INDEX.repr;
    public static final String COMPONENT_FILTER = Component.Type.FILTER.repr;
    public static final String COMPONENT_STATS = Component.Type.STATS.repr;
    public static final String TEMPFILE_MARKER = "tmp";
    public final Descriptor descriptor;
    protected final Set<Component> components;
    public final CFMetaData metadata;
    public final IPartitioner partitioner;
    protected final EstimatedHistogram estimatedRowSize;
    protected final EstimatedHistogram estimatedColumnCount;
    protected SSTable(Descriptor descriptor, CFMetaData metadata, IPartitioner partitioner)
    {
        this(descriptor, new HashSet<Component>(), metadata, partitioner);
    }
    protected SSTable(Descriptor descriptor, Set<Component> components, CFMetaData metadata, IPartitioner partitioner)
    {
        this(descriptor, components, metadata, partitioner, defaultRowHistogram(), defaultColumnHistogram());
    }
    static EstimatedHistogram defaultColumnHistogram()
    {
        return new EstimatedHistogram(114);
    }
    static EstimatedHistogram defaultRowHistogram()
    {
        return new EstimatedHistogram(150);
    }
    protected SSTable(Descriptor descriptor, Set<Component> components, CFMetaData metadata, IPartitioner partitioner, EstimatedHistogram rowSizes, EstimatedHistogram columnCounts)
    {
        this.descriptor = descriptor;
        Set<Component> dataComponents = new HashSet<Component>(components);
        for (Component component : components)
            assert component.type != Component.Type.COMPACTED_MARKER;
        this.components = Collections.unmodifiableSet(dataComponents);
        this.metadata = metadata;
        this.partitioner = partitioner;
        estimatedRowSize = rowSizes;
        estimatedColumnCount = columnCounts;
    }
    public EstimatedHistogram getEstimatedRowSize()
    {
        return estimatedRowSize;
    }
    public EstimatedHistogram getEstimatedColumnCount()
    {
        return estimatedColumnCount;
    }
    public static boolean delete(Descriptor desc, Set<Component> components)
    {
        try
        {
            if (components.contains(Component.DATA))
                FileUtils.deleteWithConfirm(desc.filenameFor(Component.DATA));
            for (Component component : components)
            {
                if (component.equals(Component.DATA) || component.equals(Component.COMPACTED_MARKER))
                    continue;
                FileUtils.deleteWithConfirm(desc.filenameFor(component));
            }
            FileUtils.delete(desc.filenameFor(Component.COMPACTED_MARKER));
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
        logger.info("Deleted " + desc);
        return true;
    }
    public String getFilename()
    {
        return descriptor.filenameFor(COMPONENT_DATA);
    }
    public String getColumnFamilyName()
    {
        return descriptor.cfname;
    }
    public String getTableName()
    {
        return descriptor.ksname;
    }
    public static Pair<Descriptor,Component> tryComponentFromFilename(File dir, String name)
    {
        try
        {
            return Component.fromFilename(dir, name);
        }
        catch (Exception e)
        {
            if (!"snapshots".equals(name))
                logger.warn("Invalid file '{}' in data directory {}.", name, dir);
            return null;
        }
    }
    static Set<Component> componentsFor(final Descriptor desc) throws IOException
    {
        final Set<Component> components = new HashSet<Component>();
        desc.directory.list(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                Pair<Descriptor,Component> component = tryComponentFromFilename(dir, name);
                if (component != null && component.left.equals(desc))
                    components.add(component.right);
                return false;
            }
        });
        return components;
    }
    static long estimateRowsFromData(Descriptor desc, BufferedRandomAccessFile dfile) throws IOException
    {
        final int SAMPLES_CAP = 1000, BYTES_CAP = (int)Math.min(100000000, dfile.length());
        int keys = 0;
        long dataPosition = 0;
        while (dataPosition < BYTES_CAP && keys < SAMPLES_CAP)
        {
            dfile.seek(dataPosition);
            ByteBufferUtil.skipShortLength(dfile);
            long dataSize = SSTableReader.readRowSize(dfile, desc);
            dataPosition = dfile.getFilePointer() + dataSize;
            keys++;
        }
        dfile.seek(0);
        return dfile.length() / (dataPosition / keys);
    }
    static long estimateRowsFromIndex(BufferedRandomAccessFile ifile) throws IOException
    {
        final int SAMPLES_CAP = 10000, BYTES_CAP = (int)Math.min(10000000, ifile.length());
        int keys = 0;
        while (ifile.getFilePointer() < BYTES_CAP && keys < SAMPLES_CAP)
        {
            ByteBufferUtil.skipShortLength(ifile);
            ifile.skipBytes(8);
            keys++;
        }
        assert keys > 0 && ifile.getFilePointer() > 0 && ifile.length() > 0;
        long estimatedRows = ifile.length() / (ifile.getFilePointer() / keys);
        ifile.seek(0);
        return estimatedRows;
    }
    public static long getTotalBytes(Iterable<SSTableReader> sstables)
    {
        long sum = 0;
        for (SSTableReader sstable : sstables)
        {
            sum += sstable.length();
        }
        return sum;
    }
    public long bytesOnDisk()
    {
        long bytes = 0;
        for (Component component : components)
        {
            bytes += new File(descriptor.filenameFor(component)).length();
        }
        return bytes;
    }
    @Override
    public String toString()
    {
        return getClass().getName() + "(" +
               "path='" + getFilename() + '\'' +
               ')';
    }
}
