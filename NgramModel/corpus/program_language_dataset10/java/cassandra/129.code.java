package org.apache.cassandra.db;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import com.google.common.collect.Iterables;
import org.apache.commons.collections.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.concurrent.JMXEnabledThreadPoolExecutor;
import org.apache.cassandra.concurrent.NamedThreadFactory;
import org.apache.cassandra.concurrent.RetryingScheduledThreadPoolExecutor;
import org.apache.cassandra.concurrent.StageManager;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ColumnDefinition;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.columniterator.IColumnIterator;
import org.apache.cassandra.db.commitlog.CommitLog;
import org.apache.cassandra.db.commitlog.CommitLogSegment;
import org.apache.cassandra.db.filter.*;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.db.marshal.BytesType;
import org.apache.cassandra.db.marshal.LocalByPartionerType;
import org.apache.cassandra.dht.*;
import org.apache.cassandra.io.sstable.*;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.thrift.IndexClause;
import org.apache.cassandra.thrift.IndexExpression;
import org.apache.cassandra.thrift.IndexOperator;
import org.apache.cassandra.utils.*;
public class ColumnFamilyStore implements ColumnFamilyStoreMBean
{
    private static Logger logger = LoggerFactory.getLogger(ColumnFamilyStore.class);
    private static final ScheduledThreadPoolExecutor cacheSavingExecutor =
            new RetryingScheduledThreadPoolExecutor("CACHE-SAVER", Thread.MIN_PRIORITY);
    private static final ExecutorService flushSorter
            = new JMXEnabledThreadPoolExecutor(1,
                                               Runtime.getRuntime().availableProcessors(),
                                               StageManager.KEEPALIVE,
                                               TimeUnit.SECONDS,
                                               new LinkedBlockingQueue<Runnable>(Runtime.getRuntime().availableProcessors()),
                                               new NamedThreadFactory("FlushSorter"),
                                               "internal");
    private static final ExecutorService flushWriter
            = new JMXEnabledThreadPoolExecutor(1,
                                               DatabaseDescriptor.getFlushWriters(),
                                               StageManager.KEEPALIVE,
                                               TimeUnit.SECONDS,
                                               new LinkedBlockingQueue<Runnable>(DatabaseDescriptor.getFlushWriters()),
                                               new NamedThreadFactory("FlushWriter"),
                                               "internal");
    public static final ExecutorService postFlushExecutor = new JMXEnabledThreadPoolExecutor("MemtablePostFlusher");
    private Set<Memtable> memtablesPendingFlush = new ConcurrentSkipListSet<Memtable>();
    public final Table table;
    public final String columnFamily;
    public final IPartitioner partitioner;
    private final String mbeanName;
    private boolean invalid = false;
    private volatile int memtableSwitchCount = 0;
    private AtomicInteger fileIndexGenerator = new AtomicInteger(0);
    private Memtable memtable;
    private final ConcurrentSkipListMap<ByteBuffer, ColumnFamilyStore> indexedColumns;
    private AtomicReference<BinaryMemtable> binaryMemtable;
    private SSTableTracker ssTables;
    private LatencyTracker readStats = new LatencyTracker();
    private LatencyTracker writeStats = new LatencyTracker();
    private final EstimatedHistogram recentSSTablesPerRead = new EstimatedHistogram(35);
    private final EstimatedHistogram sstablesPerRead = new EstimatedHistogram(35);
    public final CFMetaData metadata;
    private volatile DefaultInteger minCompactionThreshold;
    private volatile DefaultInteger maxCompactionThreshold;
    private volatile DefaultInteger memtime;
    private volatile DefaultInteger memsize;
    private volatile DefaultDouble memops;
    private final Runnable rowCacheSaverTask = new WrappedRunnable()
    {
        protected void runMayThrow() throws IOException
        {
            ssTables.saveRowCache();
        }
    };
    private final Runnable keyCacheSaverTask = new WrappedRunnable()
    {
        protected void runMayThrow() throws Exception
        {
            ssTables.saveKeyCache();
        }
    };
    public void reload()
    {
        if (!minCompactionThreshold.isModified())
            minCompactionThreshold = new DefaultInteger(metadata.getMinCompactionThreshold());
        if (!maxCompactionThreshold.isModified())
            maxCompactionThreshold = new DefaultInteger(metadata.getMaxCompactionThreshold());
        if (!memtime.isModified())
            memtime = new DefaultInteger(metadata.getMemtableFlushAfterMins());
        if (!memsize.isModified())
            memsize = new DefaultInteger(metadata.getMemtableThroughputInMb());
        if (!memops.isModified())
            memops = new DefaultDouble(metadata.getMemtableOperationsInMillions());
        ssTables.updateCacheSizes();
        for (ByteBuffer indexName : indexedColumns.keySet())
        {
            if (!metadata.getColumn_metadata().containsKey(indexName))
            {
                ColumnFamilyStore indexCfs = indexedColumns.remove(indexName);
                if (indexCfs == null)
                {
                    logger.debug("index {} already removed; ignoring", ByteBufferUtil.bytesToHex(indexName));
                    continue;
                }
                indexCfs.unregisterMBean();
                SystemTable.setIndexRemoved(metadata.tableName, metadata.cfName);
                indexCfs.removeAllSSTables();
            }
        }
        for (ColumnDefinition cdef : metadata.getColumn_metadata().values())
            if (cdef.getIndexType() != null && !indexedColumns.containsKey(cdef.name))
                addIndex(cdef);
    }
    private ColumnFamilyStore(Table table, String columnFamilyName, IPartitioner partitioner, int generation, CFMetaData metadata)
    {
        assert metadata != null : "null metadata for " + table + ":" + columnFamilyName;
        this.table = table;
        columnFamily = columnFamilyName; 
        this.metadata = metadata;
        this.minCompactionThreshold = new DefaultInteger(metadata.getMinCompactionThreshold());
        this.maxCompactionThreshold = new DefaultInteger(metadata.getMaxCompactionThreshold());
        this.memtime = new DefaultInteger(metadata.getMemtableFlushAfterMins());
        this.memsize = new DefaultInteger(metadata.getMemtableThroughputInMb());
        this.memops = new DefaultDouble(metadata.getMemtableOperationsInMillions());
        this.partitioner = partitioner;
        fileIndexGenerator.set(generation);
        memtable = new Memtable(this);
        binaryMemtable = new AtomicReference<BinaryMemtable>(new BinaryMemtable(this));
        if (logger.isDebugEnabled())
            logger.debug("Starting CFS {}", columnFamily);
        ssTables = new SSTableTracker(table.name, columnFamilyName);
        Set<DecoratedKey> savedKeys = readSavedCache(DatabaseDescriptor.getSerializedKeyCachePath(table.name, columnFamilyName));
        List<SSTableReader> sstables = new ArrayList<SSTableReader>();
        for (Map.Entry<Descriptor,Set<Component>> sstableFiles : files(table.name, columnFamilyName, false).entrySet())
        {
            SSTableReader sstable;
            try
            {
                sstable = SSTableReader.open(sstableFiles.getKey(), sstableFiles.getValue(), savedKeys, ssTables, metadata, this.partitioner);
            }
            catch (FileNotFoundException ex)
            {
                logger.error("Missing sstable component in " + sstableFiles + "; skipped because of " + ex.getMessage());
                continue;
            }
            catch (IOException ex)
            {
                logger.error("Corrupt sstable " + sstableFiles + "; skipped", ex);
                continue;
            }
            sstables.add(sstable);
        }
        ssTables.add(sstables);
        indexedColumns = new ConcurrentSkipListMap<ByteBuffer, ColumnFamilyStore>(getComparator());
        for (ColumnDefinition info : metadata.getColumn_metadata().values())
        {
            if (info.getIndexType() != null)
                addIndex(info);
        }
        String type = this.partitioner instanceof LocalPartitioner ? "IndexColumnFamilies" : "ColumnFamilies";
        mbeanName = "org.apache.cassandra.db:type=" + type + ",keyspace=" + this.table.name + ",columnfamily=" + columnFamily;
        try
        {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName nameObj = new ObjectName(mbeanName);
            mbs.registerMBean(this, nameObj);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    protected Set<DecoratedKey> readSavedCache(File path)
    {
        Set<DecoratedKey> keys = new TreeSet<DecoratedKey>();
        if (path.exists())
        {
            ObjectInputStream in = null;
            try
            {
                long start = System.currentTimeMillis();
                logger.info(String.format("reading saved cache %s", path));
                in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(path)));
                while (in.available() > 0)
                {
                    int size = in.readInt();
                    byte[] bytes = new byte[size];
                    in.readFully(bytes);
                    keys.add(StorageService.getPartitioner().decorateKey(ByteBuffer.wrap(bytes)));
                }
                if (logger.isDebugEnabled())
                    logger.debug(String.format("completed reading (%d ms; %d keys) saved cache %s",
                                               System.currentTimeMillis() - start, keys.size(), path));
            }
            catch (IOException ioe)
            {
                logger.warn(String.format("error reading saved cache %s", path.getAbsolutePath()), ioe);
            }
            finally
            {
                FileUtils.closeQuietly(in);
            }
        }
        return keys;
    }
    public boolean reverseReadWriteOrder()
    {
        return metadata.getDefaultValidator().isCommutative();
    }
    public void addIndex(final ColumnDefinition info)
    {
        assert info.getIndexType() != null;
        IPartitioner rowPartitioner = StorageService.getPartitioner();
        AbstractType columnComparator = (rowPartitioner instanceof OrderPreservingPartitioner || rowPartitioner instanceof ByteOrderedPartitioner)
                                        ? BytesType.instance
                                        : new LocalByPartionerType(StorageService.getPartitioner());
        final CFMetaData indexedCfMetadata = CFMetaData.newIndexMetadata(table.name, columnFamily, info, columnComparator);
        ColumnFamilyStore indexedCfs = ColumnFamilyStore.createColumnFamilyStore(table,
                                                                                 indexedCfMetadata.cfName,
                                                                                 new LocalPartitioner(metadata.getColumn_metadata().get(info.name).validator),
                                                                                 indexedCfMetadata);
        if (indexedColumns.putIfAbsent(info.name, indexedCfs) != null)
            return;
        if (indexedCfs.isIndexBuilt())
            return;
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                logger.info("Creating index {}.{}", table, indexedCfMetadata.cfName);
                try
                {
                    forceBlockingFlush();
                }
                catch (ExecutionException e)
                {
                    throw new RuntimeException(e);
                }
                catch (InterruptedException e)
                {
                    throw new AssertionError(e);
                }
                buildSecondaryIndexes(getSSTables(), FBUtilities.singleton(info.name));
                logger.info("Index {} complete", indexedCfMetadata.cfName);
                SystemTable.setIndexBuilt(table.name, indexedCfMetadata.cfName);
            }
        };
        new Thread(runnable, "Create index " + indexedCfMetadata.cfName).start();
    }
    public void buildSecondaryIndexes(Collection<SSTableReader> sstables, SortedSet<ByteBuffer> columns)
    {
        logger.debug("Submitting index build to compactionmanager");
        Table.IndexBuilder builder = table.createIndexBuilder(this, columns, new ReducingKeyIterator(sstables));
        Future future = CompactionManager.instance.submitIndexBuild(this, builder);
        try
        {
            future.get();
            for (ByteBuffer column : columns)
                getIndexedColumnFamilyStore(column).forceBlockingFlush();
        }
        catch (InterruptedException e)
        {
            throw new AssertionError(e);
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException(e);
        }
    }
    void unregisterMBean()
    {
        try
        {
            invalid = true;   
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName nameObj = new ObjectName(mbeanName);
            if (mbs.isRegistered(nameObj))
                mbs.unregisterMBean(nameObj);
            for (ColumnFamilyStore index : indexedColumns.values())
                index.unregisterMBean();
        }
        catch (Exception e)
        {
            logger.warn(e.getMessage(), e);
        }
    }
    public long getMinRowSize()
    {
        long min = 0;
        for (SSTableReader sstable : ssTables)
        {
            if (min == 0 || sstable.getEstimatedRowSize().min() < min)
                min = sstable.getEstimatedRowSize().min();
        }
        return min;
    }
    public long getMaxRowSize()
    {
        long max = 0;
        for (SSTableReader sstable : ssTables)
        {
            if (sstable.getEstimatedRowSize().max() > max)
                max = sstable.getEstimatedRowSize().max();
        }
        return max;
    }
    public long getMeanRowSize()
    {
        long sum = 0;
        long count = 0;
        for (SSTableReader sstable : ssTables)
        {
            sum += sstable.getEstimatedRowSize().median();
            count++;
        }
        return count > 0 ? sum / count : 0;
    }
    public int getMeanColumns()
    {
        long sum = 0;
        int count = 0;
        for (SSTableReader sstable : ssTables)
        {
            sum += sstable.getEstimatedColumnCount().median();
            count++;
        }
        return count > 0 ? (int) (sum / count) : 0;
    }
    public static ColumnFamilyStore createColumnFamilyStore(Table table, String columnFamily)
    {
        return createColumnFamilyStore(table, columnFamily, StorageService.getPartitioner(), DatabaseDescriptor.getCFMetaData(table.name, columnFamily));
    }
    public static synchronized ColumnFamilyStore createColumnFamilyStore(Table table, String columnFamily, IPartitioner partitioner, CFMetaData metadata)
    {
        List<Integer> generations = new ArrayList<Integer>();
        for (Descriptor desc : files(table.name, columnFamily, true).keySet())
        {
            generations.add(desc.generation);
            if (desc.isFromTheFuture())
            {
                throw new RuntimeException("you can't open sstables from the future!");
            }
        }
        Collections.sort(generations);
        int value = (generations.size() > 0) ? (generations.get(generations.size() - 1)) : 0;
        return new ColumnFamilyStore(table, columnFamily, partitioner, value, metadata);
    }
    public static void scrubDataDirectories(String table, String columnFamily)
    {
        for (Map.Entry<Descriptor,Set<Component>> sstableFiles : files(table, columnFamily, true).entrySet())
        {
            Descriptor desc = sstableFiles.getKey();
            Set<Component> components = sstableFiles.getValue();
            if (components.contains(Component.COMPACTED_MARKER) || desc.temporary)
            {
                SSTable.delete(desc, components);
                continue;
            }
            File dataFile = new File(desc.filenameFor(Component.DATA));
            if (components.contains(Component.DATA) && dataFile.length() > 0)
                continue;
            logger.warn("Removing orphans for {}: {}", desc, components);
            for (Component component : components)
            {
                try
                {
                    FileUtils.deleteWithConfirm(desc.filenameFor(component));
                }
                catch (IOException e)
                {
                    throw new IOError(e);
                }
            }
        }
        Pattern tmpCacheFilePattern = Pattern.compile(table + "-" + columnFamily + "-(Key|Row)Cache.*\\.tmp$");
        File dir = new File(DatabaseDescriptor.getSavedCachesLocation());
        if (dir.exists())
        {
            assert dir.isDirectory();
            for (File file : dir.listFiles())
                if (tmpCacheFilePattern.matcher(file.getName()).matches())
                    if (!file.delete())
                        logger.warn("could not delete " + file.getAbsolutePath());
        }
        CFMetaData cfm = DatabaseDescriptor.getCFMetaData(table, columnFamily);
        if (cfm != null) 
        {
            for (ColumnDefinition def : cfm.getColumn_metadata().values())
                scrubDataDirectories(table, CFMetaData.indexName(cfm.cfName, def));
        }
    }
    public void initRowCache()
    {
        int rowCacheSavePeriodInSeconds = DatabaseDescriptor.getTableMetaData(table.name).get(columnFamily).getRowCacheSavePeriodInSeconds();
        int keyCacheSavePeriodInSeconds = DatabaseDescriptor.getTableMetaData(table.name).get(columnFamily).getKeyCacheSavePeriodInSeconds();
        long start = System.currentTimeMillis();
        Set<DecoratedKey> savedKeys = readSavedCache(DatabaseDescriptor.getSerializedRowCachePath(table.name, columnFamily));
        for (DecoratedKey key : savedKeys)
            cacheRow(key);
        if (ssTables.getRowCache().getSize() > 0)
            logger.info(String.format("completed loading (%d ms; %d keys) row cache for %s.%s",
                                      System.currentTimeMillis()-start,
                                      ssTables.getRowCache().getSize(),
                                      table.name,
                                      columnFamily));
        if (rowCacheSavePeriodInSeconds > 0)
        {
            cacheSavingExecutor.scheduleWithFixedDelay(rowCacheSaverTask,
                                                       rowCacheSavePeriodInSeconds,
                                                       rowCacheSavePeriodInSeconds,
                                                       TimeUnit.SECONDS);
        }
        if (keyCacheSavePeriodInSeconds > 0)
        {
            cacheSavingExecutor.scheduleWithFixedDelay(keyCacheSaverTask,
                                                       keyCacheSavePeriodInSeconds,
                                                       keyCacheSavePeriodInSeconds,
                                                       TimeUnit.SECONDS);
        }
    }
    public Future<?> submitRowCacheWrite()
    {
        return cacheSavingExecutor.submit(rowCacheSaverTask);
    }
    public Future<?> submitKeyCacheWrite()
    {
        return cacheSavingExecutor.submit(keyCacheSaverTask);
    }
    private static Map<Descriptor,Set<Component>> files(String keyspace, final String columnFamily, final boolean includeCompacted)
    {
        final Map<Descriptor,Set<Component>> sstables = new HashMap<Descriptor,Set<Component>>();
        for (String directory : DatabaseDescriptor.getAllDataFileLocationsForTable(keyspace))
        {
            new File(directory).list(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    Pair<Descriptor,Component> component = SSTable.tryComponentFromFilename(dir, name);
                    if (component != null && component.left.cfname.equals(columnFamily))
                    {
                        if (includeCompacted || !new File(component.left.filenameFor(Component.COMPACTED_MARKER)).exists())
                        {
                            Set<Component> components = sstables.get(component.left);
                            if (components == null)
                            {
                                components = new HashSet<Component>();
                                sstables.put(component.left, components);
                            }
                            components.add(component.right);
                        }
                        else
                            logger.debug("not including compacted sstable " + component.left.cfname + "-" + component.left.generation);
                    }
                    return false;
                }
            });
        }
        return sstables;
    }
    public String getColumnFamilyName()
    {
        return columnFamily;
    }
    public String getFlushPath()
    {
        long guessedSize = 2L * memsize.value() * 1024*1024; 
        String location = DatabaseDescriptor.getDataFileLocationForTable(table.name, guessedSize);
        if (location == null)
            throw new RuntimeException("Insufficient disk space to flush");
        return getTempSSTablePath(location);
    }
    public String getTempSSTablePath(String directory)
    {
        Descriptor desc = new Descriptor(new File(directory),
                                         table.name,
                                         columnFamily,
                                         fileIndexGenerator.incrementAndGet(),
                                         true);
        return desc.filenameFor(Component.DATA);
    }
    Future<?> maybeSwitchMemtable(Memtable oldMemtable, final boolean writeCommitLog)
    {
        Table.flusherLock.writeLock().lock();
        try
        {
            if (oldMemtable.isFrozen())
                return null;
            if (DatabaseDescriptor.getCFMetaData(metadata.cfId) == null)
                return null; 
            assert memtable == oldMemtable;
            memtable.freeze();
            final CommitLogSegment.CommitLogContext ctx = writeCommitLog ? CommitLog.instance.getContext() : null;
            logger.info("switching in a fresh Memtable for " + columnFamily + " at " + ctx);
            List<ColumnFamilyStore> icc = new ArrayList<ColumnFamilyStore>(indexedColumns.size());
            icc.add(this);
            for (ColumnFamilyStore indexCfs : indexedColumns.values())
            {
                if (!indexCfs.memtable.isClean())
                    icc.add(indexCfs);
            }
            final CountDownLatch latch = new CountDownLatch(icc.size());
            for (ColumnFamilyStore cfs : icc)
            {
                if (!reverseReadWriteOrder())
                {
                    submitFlush(cfs.memtable, latch);
                    cfs.memtable = new Memtable(cfs);
                }
                else
                {
                    Memtable pendingFlush = cfs.memtable;
                    cfs.memtable = new Memtable(cfs);
                    submitFlush(pendingFlush, latch);
                }
            }
            return postFlushExecutor.submit(new WrappedRunnable()
            {
                public void runMayThrow() throws InterruptedException, IOException
                {
                    latch.await();
                    if (writeCommitLog)
                    {
                        CommitLog.instance.discardCompletedSegments(metadata.cfId, ctx);
                    }
                }
            });
        }
        finally
        {
            Table.flusherLock.writeLock().unlock();
            if (memtableSwitchCount == Integer.MAX_VALUE)
            {
                memtableSwitchCount = 0;
            }
            memtableSwitchCount++;
        }
    }
    void switchBinaryMemtable(DecoratedKey key, ByteBuffer buffer)
    {
        binaryMemtable.set(new BinaryMemtable(this));
        binaryMemtable.get().put(key, buffer);
    }
    public void forceFlushIfExpired()
    {
        if (memtable.isExpired())
            forceFlush();
    }
    public Future<?> forceFlush()
    {
        if (memtable.isClean())
            return null;
        return maybeSwitchMemtable(memtable, true);
    }
    public void forceBlockingFlush() throws ExecutionException, InterruptedException
    {
        Future<?> future = forceFlush();
        if (future != null)
            future.get();
    }
    public void forceFlushBinary()
    {
        if (binaryMemtable.get().isClean())
            return;
        submitFlush(binaryMemtable.get(), new CountDownLatch(1));
    }
    Memtable apply(DecoratedKey key, ColumnFamily columnFamily)
    {
        long start = System.nanoTime();
        boolean flushRequested = memtable.isThresholdViolated();
        memtable.put(key, columnFamily);
        ColumnFamily cachedRow = getRawCachedRow(key);
        if (cachedRow != null)
            cachedRow.addAll(columnFamily);
        writeStats.addNano(System.nanoTime() - start);
        return flushRequested ? memtable : null;
    }
    void applyBinary(DecoratedKey key, ByteBuffer buffer)
    {
        long start = System.nanoTime();
        binaryMemtable.get().put(key, buffer);
        writeStats.addNano(System.nanoTime() - start);
    }
    public static ColumnFamily removeDeletedCF(ColumnFamily cf, int gcBefore)
    {
        if (cf.getColumnCount() == 0 && cf.getLocalDeletionTime() <= gcBefore)
            return null;
        return cf;
    }
    public static ColumnFamily removeDeleted(ColumnFamily cf, int gcBefore)
    {
        if (cf == null)
        {
            return null;
        }
        removeDeletedColumnsOnly(cf, gcBefore);
        return removeDeletedCF(cf, gcBefore);
    }
    private static void removeDeletedColumnsOnly(ColumnFamily cf, int gcBefore)
    {
        if (cf.isSuper())
            removeDeletedSuper(cf, gcBefore);
        else
            removeDeletedStandard(cf, gcBefore);
    }
    private static void removeDeletedStandard(ColumnFamily cf, int gcBefore)
    {
        for (Map.Entry<ByteBuffer, IColumn> entry : cf.getColumnsMap().entrySet())
        {
            ByteBuffer cname = entry.getKey();
            IColumn c = entry.getValue();
            if ((c.isMarkedForDelete() && c.getLocalDeletionTime() <= gcBefore)
                || c.timestamp() <= cf.getMarkedForDeleteAt())
            {
                cf.remove(cname);
            }
        }
    }
    private static void removeDeletedSuper(ColumnFamily cf, int gcBefore)
    {
        for (Map.Entry<ByteBuffer, IColumn> entry : cf.getColumnsMap().entrySet())
        {
            SuperColumn c = (SuperColumn) entry.getValue();
            long minTimestamp = Math.max(c.getMarkedForDeleteAt(), cf.getMarkedForDeleteAt());
            for (IColumn subColumn : c.getSubColumns())
            {
                if (subColumn.timestamp() <= minTimestamp
                    || (subColumn.isMarkedForDelete() && subColumn.getLocalDeletionTime() <= gcBefore))
                {
                    c.remove(subColumn.name());
                }
            }
            if (c.getSubColumns().isEmpty() && c.getLocalDeletionTime() <= gcBefore)
            {
                cf.remove(c.name());
            }
        }
    }
    public boolean isKeyInRemainingSSTables(DecoratedKey key, Set<SSTable> sstablesToIgnore)
    {
        for (SSTableReader sstable : ssTables)
        {
            if (!sstablesToIgnore.contains(sstable) && sstable.getBloomFilter().isPresent(key.key))
                return true;
        }
        return false;
    }
    public void addSSTable(SSTableReader sstable)
    {
        assert sstable.getColumnFamilyName().equals(columnFamily);
        ssTables.add(Arrays.asList(sstable));
        CompactionManager.instance.submitMinorIfNeeded(this);
    }
    long getExpectedCompactedFileSize(Iterable<SSTableReader> sstables)
    {
        long expectedFileSize = 0;
        for (SSTableReader sstable : sstables)
        {
            long size = sstable.length();
            expectedFileSize = expectedFileSize + size;
        }
        return expectedFileSize;
    }
    SSTableReader getMaxSizeFile(Iterable<SSTableReader> sstables)
    {
        long maxSize = 0L;
        SSTableReader maxFile = null;
        for (SSTableReader sstable : sstables)
        {
            if (sstable.length() > maxSize)
            {
                maxSize = sstable.length();
                maxFile = sstable;
            }
        }
        return maxFile;
    }
    public void forceCleanup() throws ExecutionException, InterruptedException
    {
        CompactionManager.instance.performCleanup(ColumnFamilyStore.this);
    }
    void markCompacted(Collection<SSTableReader> sstables)
    {
        ssTables.markCompacted(sstables);
    }
    boolean isCompleteSSTables(Collection<SSTableReader> sstables)
    {
        return ssTables.getSSTables().equals(new HashSet<SSTableReader>(sstables));
    }
    void replaceCompactedSSTables(Collection<SSTableReader> sstables, Iterable<SSTableReader> replacements)
    {
        ssTables.replace(sstables, replacements);
    }
    public boolean isInvalid()
    {
        return invalid;
    }
    public void removeAllSSTables()
    {
        ssTables.replace(ssTables.getSSTables(), Collections.<SSTableReader>emptyList());
        for (ColumnFamilyStore indexedCfs : indexedColumns.values())
        {
            indexedCfs.removeAllSSTables();
        }
    }
    void submitFlush(IFlushable flushable, CountDownLatch latch)
    {
        logger.info("Enqueuing flush of {}", flushable);
        flushable.flushAndSignal(latch, flushSorter, flushWriter);
    }
    public int getMemtableColumnsCount()
    {
        return getMemtableThreadSafe().getCurrentOperations();
    }
    public int getMemtableDataSize()
    {
        return getMemtableThreadSafe().getCurrentThroughput();
    }
    public int getMemtableSwitchCount()
    {
        return memtableSwitchCount;
    }
    private Memtable getMemtableThreadSafe()
    {
        Table.flusherLock.readLock().lock();
        try
        {
            return memtable;
        }
        finally
        {
            Table.flusherLock.readLock().unlock();
        }
    }
    public Collection<SSTableReader> getSSTables()
    {
        return ssTables.getSSTables();
    }
    public long[] getRecentSSTablesPerReadHistogram()
    {
        return recentSSTablesPerRead.get(true);
    }
    public long[] getSSTablesPerReadHistogram()
    {
        return sstablesPerRead.get(false);
    }
    public long getReadCount()
    {
        return readStats.getOpCount();
    }
    public double getRecentReadLatencyMicros()
    {
        return readStats.getRecentLatencyMicros();
    }
    public long[] getLifetimeReadLatencyHistogramMicros()
    {
        return readStats.getTotalLatencyHistogramMicros();
    }
    public long[] getRecentReadLatencyHistogramMicros()
    {
        return readStats.getRecentLatencyHistogramMicros();
    }
    public long getTotalReadLatencyMicros()
    {
        return readStats.getTotalLatencyMicros();
    }
    public int getPendingTasks()
    {
        return Table.flusherLock.getQueueLength();
    }
    public long getWriteCount()
    {
        return writeStats.getOpCount();
    }
    public long getTotalWriteLatencyMicros()
    {
        return writeStats.getTotalLatencyMicros();
    }
    public double getRecentWriteLatencyMicros()
    {
        return writeStats.getRecentLatencyMicros();
    }
    public long[] getLifetimeWriteLatencyHistogramMicros()
    {
        return writeStats.getTotalLatencyHistogramMicros();
    }
    public long[] getRecentWriteLatencyHistogramMicros()
    {
        return writeStats.getRecentLatencyHistogramMicros();
    }
    public ColumnFamily getColumnFamily(DecoratedKey key, QueryPath path, ByteBuffer start, ByteBuffer finish, boolean reversed, int limit)
    {
        return getColumnFamily(QueryFilter.getSliceFilter(key, path, start, finish, reversed, limit));
    }
    public ColumnFamily getColumnFamily(QueryFilter filter)
    {
        return getColumnFamily(filter, gcBefore());
    }
    public int gcBefore()
    {
        return (int) (System.currentTimeMillis() / 1000) - metadata.getGcGraceSeconds();
    }
    private ColumnFamily cacheRow(DecoratedKey key)
    {
        ColumnFamily cached;
        if ((cached = ssTables.getRowCache().get(key)) == null)
        {
            cached = getTopLevelColumns(QueryFilter.getIdentityFilter(key, new QueryPath(columnFamily)), Integer.MIN_VALUE);
            if (cached == null)
            {
                return null;
            }
            for (IColumn column : cached.getSortedColumns())
            {
                if (cached.isSuper())
                {
                    if (!column.name().hasArray())
                    {
                        cached.deepCopyColumn(column);
                    }
                    else
                    {
                        SuperColumn superColumn = (SuperColumn) column;
                        for (IColumn subColumn : column.getSubColumns())
                        {
                            if (!subColumn.name().hasArray() || !subColumn.value().hasArray())
                            {
                                superColumn.remove(subColumn.name());
                                superColumn.addColumn(subColumn.deepCopy());
                            }
                        }
                    }
                }
                else if (!column.name().hasArray() || !column.value().hasArray())
                {
                    cached.deepCopyColumn(column);
                }
            }
            ssTables.getRowCache().put(key, cached);
        }
        return cached;
    }
    private ColumnFamily getColumnFamily(QueryFilter filter, int gcBefore)
    {
        assert columnFamily.equals(filter.getColumnFamilyName()) : filter.getColumnFamilyName();
        long start = System.nanoTime();
        try
        {
            if (ssTables.getRowCache().getCapacity() == 0)
            {
                ColumnFamily cf = getTopLevelColumns(filter, gcBefore);
                return cf.isSuper() ? removeDeleted(cf, gcBefore) : removeDeletedCF(cf, gcBefore);
            }
            ColumnFamily cached = cacheRow(filter.key);
            if (cached == null)
                return null;
            return filterColumnFamily(cached, filter, gcBefore);
        }
        finally
        {
            readStats.addNano(System.nanoTime() - start);
        }
    }
    ColumnFamily filterColumnFamily(ColumnFamily cached, QueryFilter filter, int gcBefore)
    {
        if (filter.filter instanceof SliceQueryFilter)
        {
            SliceQueryFilter sliceFilter = (SliceQueryFilter) filter.filter;
            if (sliceFilter.start.remaining() == 0 && sliceFilter.finish.remaining() == 0)
            {
                if (cached.isSuper() && filter.path.superColumnName != null)
                {
                    IColumn sc = cached.getColumn(filter.path.superColumnName);
                    if (sc == null || sliceFilter.count >= sc.getSubColumns().size())
                    {
                        ColumnFamily cf = cached.cloneMeShallow();
                        if (sc != null)
                            cf.addColumn(sc);
                        return removeDeleted(cf, gcBefore);
                    }
                }
                else
                {
                    if (sliceFilter.count >= cached.getColumnCount())
                    {
                        removeDeletedColumnsOnly(cached, gcBefore);                    
                        return removeDeletedCF(cached, gcBefore);
                    }
                }
            }
        }
        IColumnIterator ci = filter.getMemtableColumnIterator(cached, null, getComparator());
        ColumnFamily cf = null;
        try
        {
            cf = ci.getColumnFamily().cloneMeShallow();
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
        filter.collectCollatedColumns(cf, ci, gcBefore);
        return cf.isSuper() ? removeDeleted(cf, gcBefore) : removeDeletedCF(cf, gcBefore);
    }
    private ColumnFamily getTopLevelColumns(QueryFilter filter, int gcBefore)
    {
        List<IColumnIterator> iterators = new ArrayList<IColumnIterator>();
        final ColumnFamily returnCF = ColumnFamily.create(metadata);
        try
        {
            IColumnIterator iter;
            int sstablesToIterate = 0;
            if (!reverseReadWriteOrder())
            {
                iter = filter.getMemtableColumnIterator(getMemtableThreadSafe(), getComparator());
                if (iter != null)
                {
                    returnCF.delete(iter.getColumnFamily());
                    iterators.add(iter);
                }
                for (Memtable memtable : memtablesPendingFlush)
                {
                    iter = filter.getMemtableColumnIterator(memtable, getComparator());
                    if (iter != null)
                    {
                        returnCF.delete(iter.getColumnFamily());
                        iterators.add(iter);
                    }
                }
                for (SSTableReader sstable : ssTables)
                {
                    iter = filter.getSSTableColumnIterator(sstable);
                    if (iter.getColumnFamily() != null)
                    {
                        returnCF.delete(iter.getColumnFamily());
                        iterators.add(iter);
                    }
                    sstablesToIterate++;
                }
            }
            else
            {
                for (SSTableReader sstable : ssTables)
                {
                    iter = filter.getSSTableColumnIterator(sstable);
                    if (iter.getColumnFamily() != null)
                    {
                        returnCF.delete(iter.getColumnFamily());
                        iterators.add(iter);
                    }
                    sstablesToIterate++;
                }
                for (Memtable memtable : memtablesPendingFlush)
                {
                    iter = filter.getMemtableColumnIterator(memtable, getComparator());
                    if (iter != null)
                    {
                        returnCF.delete(iter.getColumnFamily());
                        iterators.add(iter);
                    }
                }
                iter = filter.getMemtableColumnIterator(getMemtableThreadSafe(), getComparator());
                if (iter != null)
                {
                    returnCF.delete(iter.getColumnFamily());
                    iterators.add(iter);
                }
            }
            recentSSTablesPerRead.add(sstablesToIterate);
            sstablesPerRead.add(sstablesToIterate);
            Comparator<IColumn> comparator = filter.filter.getColumnComparator(getComparator());
            Iterator collated = IteratorUtils.collatedIterator(comparator, iterators);
            filter.collectCollatedColumns(returnCF, collated, gcBefore);
            return returnCF;
        }
        catch (IOException e)
        {
            throw new IOError(e);
        }
        finally
        {
            for (IColumnIterator ci : iterators)
            {
                try
                {
                    ci.close();
                }
                catch (Throwable th)
                {
                    logger.error("error closing " + ci, th);
                }
            }
        }
    }
    public List<Row> getRangeSlice(ByteBuffer superColumn, final AbstractBounds range, int maxResults, IFilter columnFilter)
    throws ExecutionException, InterruptedException
    {
        assert range instanceof Bounds
               || (!((Range)range).isWrapAround() || range.right.equals(StorageService.getPartitioner().getMinimumToken()))
               : range;
        List<Row> rows = new ArrayList<Row>();
        DecoratedKey startWith = new DecoratedKey(range.left, null);
        DecoratedKey stopAt = new DecoratedKey(range.right, null);
        QueryFilter filter = new QueryFilter(null, new QueryPath(columnFamily, superColumn, null), columnFilter);
        Collection<Memtable> memtables = new ArrayList<Memtable>();
        Collection<SSTableReader> sstables = new ArrayList<SSTableReader>();
        if (!reverseReadWriteOrder())
        {
            memtables.add(getMemtableThreadSafe());
            memtables.addAll(memtablesPendingFlush);
            Iterables.addAll(sstables, ssTables);
        }
        else
        {
            Iterables.addAll(sstables, ssTables);
            memtables.addAll(memtablesPendingFlush);
            memtables.add(getMemtableThreadSafe());
        }
        RowIterator iterator = RowIteratorFactory.getIterator(memtables, sstables, startWith, stopAt, filter, getComparator(), this);
        int gcBefore = (int)(System.currentTimeMillis() / 1000) - metadata.getGcGraceSeconds();
        try
        {
            boolean first = true; 
            while (iterator.hasNext())
            {
                Row current = iterator.next();
                DecoratedKey key = current.key;
                if (!stopAt.isEmpty() && stopAt.compareTo(key) < 0)
                    return rows;
                if(range instanceof Bounds || !first || !key.equals(startWith))
                {
                    rows.add(current.cf != null && current.cf.isSuper()
                             ? new Row(current.key, removeDeleted(current.cf, gcBefore))
                             : current);
                    if (logger.isDebugEnabled())
                        logger.debug("scanned " + key);
                }
                first = false;
                if (rows.size() >= maxResults)
                    return rows;
            }
        }
        finally
        {
            try
            {
                iterator.close();
            }
            catch (IOException e)
            {
                throw new IOError(e);
            }
        }
        return rows;
    }
    public List<Row> scan(IndexClause clause, AbstractBounds range, IFilter dataFilter)
    {
        IndexExpression primary = highestSelectivityPredicate(clause);
        ColumnFamilyStore indexCFS = getIndexedColumnFamilyStore(primary.column_name);
        if (logger.isDebugEnabled())
            logger.debug("Primary scan clause is " + getComparator().getString(primary.column_name));
        assert indexCFS != null;
        DecoratedKey indexKey = indexCFS.partitioner.decorateKey(primary.value);
        IFilter firstFilter = dataFilter;
        NamesQueryFilter extraFilter = null;
        if (clause.expressions.size() > 1)
        {
            if (dataFilter instanceof SliceQueryFilter)
            {
                if (getMaxRowSize() < DatabaseDescriptor.getColumnIndexSize())
                {
                    logger.debug("Expanding slice filter to entire row to cover additional expressions");
                    firstFilter = new SliceQueryFilter(ByteBufferUtil.EMPTY_BYTE_BUFFER,
                                                       ByteBufferUtil.EMPTY_BYTE_BUFFER,
                                                       ((SliceQueryFilter) dataFilter).reversed,
                                                       Integer.MAX_VALUE);
                }
                else
                {
                    logger.debug("adding extraFilter to cover additional expressions");
                    SortedSet<ByteBuffer> columns = new TreeSet<ByteBuffer>(getComparator());
                    for (IndexExpression expr : clause.expressions)
                    {
                        if (expr == primary)
                            continue;
                        columns.add(expr.column_name);
                    }
                    extraFilter = new NamesQueryFilter(columns);
                }
            }
            else
            {
                logger.debug("adding columns to firstFilter to cover additional expressions");
                assert dataFilter instanceof NamesQueryFilter;
                SortedSet<ByteBuffer> columns = new TreeSet<ByteBuffer>(getComparator());
                for (IndexExpression expr : clause.expressions)
                {
                    if (expr == primary || ((NamesQueryFilter) dataFilter).columns.contains(expr.column_name))
                        continue;
                    columns.add(expr.column_name);
                }
                if (columns.size() > 0)
                {
                    columns.addAll(((NamesQueryFilter) dataFilter).columns);
                    firstFilter = new NamesQueryFilter(columns);
                }
            }
        }
        List<Row> rows = new ArrayList<Row>();
        ByteBuffer startKey = clause.start_key;
        QueryPath path = new QueryPath(columnFamily);
        outer:
        while (true)
        {
            if (logger.isDebugEnabled())
                logger.debug(String.format("Scanning index row %s:%s starting with %s",
                                           indexCFS.columnFamily, indexKey, indexCFS.getComparator().getString(startKey)));
            QueryFilter indexFilter = QueryFilter.getSliceFilter(indexKey,
                                                                 new QueryPath(indexCFS.getColumnFamilyName()),
                                                                 startKey,
                                                                 ByteBufferUtil.EMPTY_BYTE_BUFFER,
                                                                 false,
                                                                 clause.count);
            ColumnFamily indexRow = indexCFS.getColumnFamily(indexFilter);
            logger.debug("fetched {}", indexRow);
            if (indexRow == null)
                break;
            ByteBuffer dataKey = null;
            int n = 0;
            for (IColumn column : indexRow.getSortedColumns())
            {
                if (column.isMarkedForDelete())
                    continue;
                dataKey = column.name();
                n++;
                DecoratedKey dk = partitioner.decorateKey(dataKey);
                if (!range.right.equals(partitioner.getMinimumToken()) && range.right.compareTo(dk.token) < 0)
                    break outer;
                if (!range.contains(dk.token))
                    continue;
                ColumnFamily data = getColumnFamily(new QueryFilter(dk, path, firstFilter));
                logger.debug("fetched data row {}", data);
                if (extraFilter != null)
                {
                    for (IndexExpression expr : clause.expressions)
                    {
                        if (expr != primary && data.getColumn(expr.column_name) == null)
                        {
                            data.addAll(getColumnFamily(new QueryFilter(dk, path, extraFilter)));
                            break;
                        }
                    }
                }
                if (satisfies(data, clause, primary))
                {
                    logger.debug("row {} satisfies all clauses", data);
                    if (firstFilter != dataFilter)
                    {
                        ColumnFamily expandedData = data;
                        data = expandedData.cloneMeShallow();
                        IColumnIterator iter = dataFilter.getMemtableColumnIterator(expandedData, dk, getComparator());
                        new QueryFilter(dk, path, dataFilter).collectCollatedColumns(data, iter, gcBefore());
                    }
                    rows.add(new Row(dk, data));
                }
                if (rows.size() == clause.count)
                    break outer;
            }
            if (n < clause.count || startKey.equals(dataKey))
                break;
            startKey = dataKey;
        }
        return rows;
    }
    private IndexExpression highestSelectivityPredicate(IndexClause clause)
    {
        IndexExpression best = null;
        int bestMeanCount = Integer.MAX_VALUE;
        for (IndexExpression expression : clause.expressions)
        {
            ColumnFamilyStore cfs = getIndexedColumnFamilyStore(expression.column_name);
            if (cfs == null || !expression.op.equals(IndexOperator.EQ))
                continue;
            int columns = cfs.getMeanColumns();
            if (columns < bestMeanCount)
            {
                best = expression;
                bestMeanCount = columns;
            }
        }
        return best;
    }
    private static boolean satisfies(ColumnFamily data, IndexClause clause, IndexExpression first)
    {
        for (IndexExpression expression : clause.expressions)
        {
            if (expression == first)
                continue;
            IColumn column = data.getColumn(expression.column_name);
            if (column == null)
                return false;
            int v = data.getComparator().compare(column.value(), expression.value);
            if (!satisfies(v, expression.op))
                return false;
        }
        return true;
    }
    private static boolean satisfies(int comparison, IndexOperator op)
    {
        switch (op)
        {
            case EQ:
                return comparison == 0;
            case GTE:
                return comparison >= 0;
            case GT:
                return comparison > 0;
            case LTE:
                return comparison <= 0;
            case LT:
                return comparison < 0;
            default:
                throw new IllegalStateException();
        }
    }
    public AbstractType getComparator()
    {
        return metadata.comparator;
    }
    public void snapshot(String snapshotName)
    {
        try
        {
            forceBlockingFlush();
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException(e);
        }
        catch (InterruptedException e)
        {
            throw new AssertionError(e);
        }
        for (SSTableReader ssTable : ssTables)
        {
            try
            {
                File dataDirectory = ssTable.descriptor.directory.getParentFile();
                String snapshotDirectoryPath = Table.getSnapshotPath(dataDirectory.getAbsolutePath(), table.name, snapshotName);
                FileUtils.createDirectory(snapshotDirectoryPath);
                ssTable.createLinks(snapshotDirectoryPath);
                if (logger.isDebugEnabled())
                    logger.debug("Snapshot for " + table + " keyspace data file " + ssTable.getFilename() +
                        " created in " + snapshotDirectoryPath);
            }
            catch (IOException e)
            {
                throw new IOError(e);
            }
        }
    }
    public boolean hasUnreclaimedSpace()
    {
        return ssTables.getLiveSize() < ssTables.getTotalSize();
    }
    public long getTotalDiskSpaceUsed()
    {
        return ssTables.getTotalSize();
    }
    public long getLiveDiskSpaceUsed()
    {
        return ssTables.getLiveSize();
    }
    public int getLiveSSTableCount()
    {
        return ssTables.size();
    }
    public ColumnFamily getRawCachedRow(DecoratedKey key)
    {
        return ssTables.getRowCache().getCapacity() == 0 ? null : ssTables.getRowCache().getInternal(key);
    }
    void invalidateCachedRow(DecoratedKey key)
    {
        ssTables.getRowCache().remove(key);
    }
    public void forceMajorCompaction() throws InterruptedException, ExecutionException
    {
        CompactionManager.instance.performMajor(this);
    }
    public void invalidateRowCache()
    {
        ssTables.getRowCache().clear();
    }
    public void invalidateKeyCache()
    {
        ssTables.getKeyCache().clear();
    }
    public int getRowCacheCapacity()
    {
        return ssTables.getRowCache().getCapacity();
    }
    public int getKeyCacheCapacity()
    {
        return ssTables.getKeyCache().getCapacity();
    }
    public int getRowCacheSize()
    {
        return ssTables.getRowCache().getSize();
    }
    public int getKeyCacheSize()
    {
        return ssTables.getKeyCache().getSize();
    }
    public static Iterable<ColumnFamilyStore> all()
    {
        Iterable<ColumnFamilyStore>[] stores = new Iterable[DatabaseDescriptor.getTables().size()];
        int i = 0;
        for (Table table : Table.all())
        {
            stores[i++] = table.getColumnFamilyStores();
        }
        return Iterables.concat(stores);
    }
    public Iterable<DecoratedKey> allKeySamples()
    {
        Collection<SSTableReader> sstables = getSSTables();
        Iterable<DecoratedKey>[] samples = new Iterable[sstables.size()];
        int i = 0;
        for (SSTableReader sstable: sstables)
        {
            samples[i++] = sstable.getKeySamples();
        }
        return Iterables.concat(samples);
    }
    void clearUnsafe()
    {
        memtable.clearUnsafe();
        ssTables.clearUnsafe();
    }
    public Set<Memtable> getMemtablesPendingFlush()
    {
        return memtablesPendingFlush;
    }
    public Future<?> truncate() throws IOException
    {
        try
        {
            forceBlockingFlush();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
        final long truncatedAt = System.currentTimeMillis();
        snapshot(Table.getTimestampedSnapshotName("before-truncate"));
        Runnable runnable = new WrappedRunnable()
        {
            public void runMayThrow() throws InterruptedException, IOException
            {
                for (ColumnFamilyStore cfs : Iterables.concat(indexedColumns.values(), Arrays.asList(ColumnFamilyStore.this)))
                {
                    List<SSTableReader> truncatedSSTables = new ArrayList<SSTableReader>();
                    for (SSTableReader sstable : cfs.getSSTables())
                    {
                        if (!sstable.newSince(truncatedAt))
                            truncatedSSTables.add(sstable);
                    }
                    cfs.markCompacted(truncatedSSTables);
                }
                invalidateRowCache();
            }
        };
        return postFlushExecutor.submit(runnable);
    }
    public void renameSSTables(String newCfName) throws IOException
    {
        IOException mostRecentProblem = null;
        for (File existing : DefsTable.getFiles(table.name, columnFamily))
        {
            try
            {
                String newFileName = existing.getName().replaceFirst("\\w+-", newCfName + "-");
                FileUtils.renameWithConfirm(existing, new File(existing.getParent(), newFileName));
            }
            catch (IOException ex)
            {
                mostRecentProblem = ex;
            }
        }
        if (mostRecentProblem != null)
            throw new IOException("One or more IOExceptions encountered while renaming files. Most recent problem is included.", mostRecentProblem);
        for (ColumnFamilyStore indexedCfs : indexedColumns.values())
        {
            indexedCfs.renameSSTables(indexedCfs.columnFamily.replace(columnFamily, newCfName));
        }
    }
    public long getBloomFilterFalsePositives()
    {
        long count = 0L;
        for (SSTableReader sstable: getSSTables())
        {
            count += sstable.getBloomFilterFalsePositiveCount();
        }
        return count;
    }
    public long getRecentBloomFilterFalsePositives()
    {
        long count = 0L;
        for (SSTableReader sstable: getSSTables())
        {
            count += sstable.getRecentBloomFilterFalsePositiveCount();
        }
        return count;
    }
    public double getBloomFilterFalseRatio()
    {
        long falseCount = 0L;
        long trueCount = 0L;
        for (SSTableReader sstable: getSSTables())
        {
            falseCount += sstable.getBloomFilterFalsePositiveCount();
            trueCount += sstable.getBloomFilterTruePositiveCount();
        }
        if (falseCount == 0L && trueCount == 0L)
            return 0d;
        return (double) falseCount / (trueCount + falseCount);
    }
    public double getRecentBloomFilterFalseRatio()
    {
        long falseCount = 0L;
        long trueCount = 0L;
        for (SSTableReader sstable: getSSTables())
        {
            falseCount += sstable.getRecentBloomFilterFalsePositiveCount();
            trueCount += sstable.getRecentBloomFilterTruePositiveCount();
        }
        if (falseCount == 0L && trueCount == 0L)
            return 0d;
        return (double) falseCount / (trueCount + falseCount);
    }
    public SortedSet<ByteBuffer> getIndexedColumns()
    {
        return indexedColumns.keySet();
    }
    public ColumnFamilyStore getIndexedColumnFamilyStore(ByteBuffer column)
    {
        return indexedColumns.get(column);
    }
    public ColumnFamily newIndexedColumnFamily(ByteBuffer column)
    {
        return ColumnFamily.create(indexedColumns.get(column).metadata);
    }
    public DecoratedKey<LocalToken> getIndexKeyFor(ByteBuffer name, ByteBuffer value)
    {
        return indexedColumns.get(name).partitioner.decorateKey(value);
    }
    @Override
    public String toString()
    {
        return "ColumnFamilyStore(" +
               "table='" + table + '\'' +
               ", columnFamily='" + columnFamily + '\'' +
               ')';
    }
    public int getMinimumCompactionThreshold()
    {
        return minCompactionThreshold.value();
    }
    public void setMinimumCompactionThreshold(int minCompactionThreshold)
    {
        if ((minCompactionThreshold > this.maxCompactionThreshold.value()) && this.maxCompactionThreshold.value() != 0) {
            throw new RuntimeException("The min_compaction_threshold cannot be larger than the max.");
        }
        this.minCompactionThreshold.set(minCompactionThreshold);
    }
    public int getMaximumCompactionThreshold()
    {
        return maxCompactionThreshold.value();
    }
    public void setMaximumCompactionThreshold(int maxCompactionThreshold)
    {
        if (maxCompactionThreshold < this.minCompactionThreshold.value()) {
            throw new RuntimeException("The max_compaction_threshold cannot be smaller than the min.");
        }
        this.maxCompactionThreshold.set(maxCompactionThreshold);
    }
    public void disableAutoCompaction()
    {
        minCompactionThreshold.set(0);
        maxCompactionThreshold.set(0);
    }
    public int getMemtableFlushAfterMins()
    {
        return memtime.value();
    }
    public void setMemtableFlushAfterMins(int time)
    {
        if (time <= 0) {
            throw new RuntimeException("MemtableFlushAfterMins must be greater than 0.");
        }
        this.memtime.set(time);
    }
    public int getMemtableThroughputInMB()
    {
        return memsize.value();
    }
    public void setMemtableThroughputInMB(int size)
    {
        if (size <= 0) {
            throw new RuntimeException("MemtableThroughputInMB must be greater than 0.");
        }
        this.memsize.set(size);
    }
    public double getMemtableOperationsInMillions()
    {
        return memops.value();
    }
    public void setMemtableOperationsInMillions(double ops)
    {
        if (ops <= 0) {
            throw new RuntimeException("MemtableOperationsInMillions must be greater than 0.0.");
        }
        this.memops.set(ops);
    }
    public long estimateKeys()
    {
        return ssTables.estimatedKeys();
    }
    public long[] getEstimatedRowSizeHistogram()
    {
        long[] histogram = new long[90];
        for (SSTableReader sstable : ssTables)
        {
            long[] rowSize = sstable.getEstimatedRowSize().get(false);
            for (int i = 0; i < histogram.length; i++)
                histogram[i] += rowSize[i];
        }
        return histogram;
    }
    public long[] getEstimatedColumnCountHistogram()
    {
        long[] histogram = new long[90];
        for (SSTableReader sstable : ssTables)
        {
            long[] columnSize = sstable.getEstimatedColumnCount().get(false);
            for (int i = 0; i < histogram.length; i++)
                histogram[i] += columnSize[i];
        }
        return histogram;
    }
    public boolean isIndexBuilt()
    {
        return SystemTable.isIndexBuilt(table.name, columnFamily);
    }
    public List<String> getBuiltIndexes()
    {
        List<String> indexes = new ArrayList<String>();
        for (ColumnFamilyStore cfs : indexedColumns.values())
        {
            if (cfs.isIndexBuilt())
            {
                indexes.add(cfs.columnFamily); 
            }
        }
        return indexes;
    }
    public boolean isIndex()
    {
        return partitioner instanceof LocalPartitioner;
    }
}
