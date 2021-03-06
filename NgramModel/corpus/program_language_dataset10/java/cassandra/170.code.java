package org.apache.cassandra.db;
import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.KSMetaData;
import org.apache.cassandra.db.commitlog.CommitLog;
import org.apache.cassandra.db.filter.QueryFilter;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.dht.LocalToken;
import org.apache.cassandra.io.ICompactionInfo;
import org.apache.cassandra.io.sstable.ReducingKeyIterator;
import org.apache.cassandra.io.sstable.SSTableDeletingReference;
import org.apache.cassandra.io.sstable.SSTableReader;
import org.apache.cassandra.io.util.FileUtils;
import org.apache.cassandra.locator.AbstractReplicationStrategy;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.cliffc.high_scale_lib.NonBlockingHashMap;
public class Table
{
    public static final String SYSTEM_TABLE = "system";
    private static final Logger logger = LoggerFactory.getLogger(Table.class);
    private static final String SNAPSHOT_SUBDIR_NAME = "snapshots";
    static final ReentrantReadWriteLock flusherLock = new ReentrantReadWriteLock();
    static
    {
        try
        {
            DatabaseDescriptor.createAllDirectories();
        }
        catch (IOException ex)
        {
            throw new IOError(ex);
        }
    }
    private static final Map<String, Table> instances = new NonBlockingHashMap<String, Table>();
    public final String name;
    public final Map<Integer, ColumnFamilyStore> columnFamilyStores = new HashMap<Integer, ColumnFamilyStore>(); 
    private final Object[] indexLocks;
    private ScheduledFuture<?> flushTask;
    private volatile AbstractReplicationStrategy replicationStrategy;
    public static Table open(String table)
    {
        Table tableInstance = instances.get(table);
        if (tableInstance == null)
        {
            synchronized (Table.class)
            {
                tableInstance = instances.get(table);
                if (tableInstance == null)
                {
                    tableInstance = new Table(table);
                    instances.put(table, tableInstance);
                    for (ColumnFamilyStore cfs : tableInstance.getColumnFamilyStores())
                        cfs.initRowCache();
                }
            }
        }
        return tableInstance;
    }
    public static Lock getFlushLock()
    {
        return flusherLock.writeLock();
    }
    public static Table clear(String table) throws IOException
    {
        synchronized (Table.class)
        {
            Table t = instances.remove(table);
            if (t != null)
            {
                t.flushTask.cancel(false);
                for (ColumnFamilyStore cfs : t.getColumnFamilyStores())
                    t.unloadCf(cfs);
            }
            return t;
        }
    }
    public Collection<ColumnFamilyStore> getColumnFamilyStores()
    {
        return Collections.unmodifiableCollection(columnFamilyStores.values());
    }
    public ColumnFamilyStore getColumnFamilyStore(String cfName)
    {
        Integer id = CFMetaData.getId(name, cfName);
        if (id == null)
            throw new IllegalArgumentException(String.format("Unknown table/cf pair (%s.%s)", name, cfName));
        return columnFamilyStores.get(id);
    }
    public void forceCleanup() throws IOException, ExecutionException, InterruptedException
    {
        if (name.equals(SYSTEM_TABLE))
            throw new UnsupportedOperationException("Cleanup of the system table is neither necessary nor wise");
        List<ColumnFamilyStore> sortedColumnFamilies = new ArrayList<ColumnFamilyStore>(columnFamilyStores.values());
        Collections.sort(sortedColumnFamilies, new Comparator<ColumnFamilyStore>()
        {
            public int compare(ColumnFamilyStore cf1, ColumnFamilyStore cf2)
            {
                long diff = (cf1.getTotalDiskSpaceUsed() - cf2.getTotalDiskSpaceUsed());
                if (diff > 0)
                    return 1;
                if (diff < 0)
                    return -1;
                return cf1.columnFamily.compareTo(cf2.columnFamily);
            }
        });
        for (ColumnFamilyStore cfs : sortedColumnFamilies)
            cfs.forceCleanup();
    }
    public void snapshot(String clientSuppliedName)
    {
        String snapshotName = getTimestampedSnapshotName(clientSuppliedName);
        for (ColumnFamilyStore cfStore : columnFamilyStores.values())
        {
            cfStore.snapshot(snapshotName);
        }
    }
    public static String getTimestampedSnapshotName(String clientSuppliedName)
    {
        String snapshotName = Long.toString(System.currentTimeMillis());
        if (clientSuppliedName != null && !clientSuppliedName.equals(""))
        {
            snapshotName = snapshotName + "-" + clientSuppliedName;
        }
        return snapshotName;
    }
    public void clearSnapshot() throws IOException
    {
        for (String dataDirPath : DatabaseDescriptor.getAllDataFileLocations())
        {
            String snapshotPath = dataDirPath + File.separator + name + File.separator + SNAPSHOT_SUBDIR_NAME;
            File snapshotDir = new File(snapshotPath);
            if (snapshotDir.exists())
            {
                if (logger.isDebugEnabled())
                    logger.debug("Removing snapshot directory " + snapshotPath);
                FileUtils.deleteRecursive(snapshotDir);
            }
        }
    }
    public void forceCompaction() throws IOException, ExecutionException, InterruptedException
    {
        for (ColumnFamilyStore cfStore : columnFamilyStores.values())
            CompactionManager.instance.performMajor(cfStore);
    }
    public List<SSTableReader> getAllSSTables()
    {
        List<SSTableReader> list = new ArrayList<SSTableReader>();
        for (ColumnFamilyStore cfStore : columnFamilyStores.values())
            list.addAll(cfStore.getSSTables());
        return list;
    }
    private Table(String table)
    {
        name = table;
        KSMetaData ksm = DatabaseDescriptor.getKSMetaData(table);
        try
        {
            createReplicationStrategy(ksm);
        }
        catch (ConfigurationException e)
        {
            throw new RuntimeException(e);
        }
        indexLocks = new Object[DatabaseDescriptor.getConcurrentWriters() * 128];
        for (int i = 0; i < indexLocks.length; i++)
            indexLocks[i] = new Object();
        for (String dataDir : DatabaseDescriptor.getAllDataFileLocations())
        {
            try
            {
                String keyspaceDir = dataDir + File.separator + table;
                FileUtils.createDirectory(keyspaceDir);
                File streamingDir = new File(keyspaceDir, "stream");
                if (streamingDir.exists())
                    FileUtils.deleteRecursive(streamingDir);
            }
            catch (IOException ex)
            {
                throw new IOError(ex);
            }
        }
        for (CFMetaData cfm : new ArrayList<CFMetaData>(DatabaseDescriptor.getTableDefinition(table).cfMetaData().values()))
        {
            logger.debug("Initializing {}.{}", name, cfm.cfName);
            initCf(cfm.cfId, cfm.cfName);
        }
        int minCheckMs = Integer.MAX_VALUE;
        for (ColumnFamilyStore cfs : columnFamilyStores.values())
        {
            minCheckMs = Math.min(minCheckMs, cfs.getMemtableFlushAfterMins() * 60 * 1000 / 10);
        }
        Runnable runnable = new Runnable()
        {
            public void run()
            {
                for (ColumnFamilyStore cfs : columnFamilyStores.values())
                {
                    cfs.forceFlushIfExpired();
                }
            }
        };
        flushTask = StorageService.scheduledTasks.scheduleWithFixedDelay(runnable, minCheckMs, minCheckMs, TimeUnit.MILLISECONDS);
    }
    public void createReplicationStrategy(KSMetaData ksm) throws ConfigurationException
    {
        if (replicationStrategy != null)
            StorageService.instance.getTokenMetadata().unregister(replicationStrategy);
        replicationStrategy = AbstractReplicationStrategy.createReplicationStrategy(ksm.name,
                                                                                    ksm.strategyClass,
                                                                                    StorageService.instance.getTokenMetadata(),
                                                                                    DatabaseDescriptor.getEndpointSnitch(),
                                                                                    ksm.strategyOptions);
    }
    public void dropCf(Integer cfId) throws IOException
    {
        assert columnFamilyStores.containsKey(cfId);
        ColumnFamilyStore cfs = columnFamilyStores.remove(cfId);
        if (cfs == null)
            return;
        unloadCf(cfs);
        cfs.removeAllSSTables();
    }
    private void unloadCf(ColumnFamilyStore cfs) throws IOException
    {
        try
        {
            cfs.forceBlockingFlush();
        }
        catch (ExecutionException e)
        {
            throw new IOException(e);
        }
        catch (InterruptedException e)
        {
            throw new IOException(e);
        }
        cfs.unregisterMBean();
    }
    public void initCf(Integer cfId, String cfName)
    {
        assert !columnFamilyStores.containsKey(cfId) : String.format("tried to init %s as %s, but already used by %s",
                                                                     cfName, cfId, columnFamilyStores.get(cfId));
        columnFamilyStores.put(cfId, ColumnFamilyStore.createColumnFamilyStore(this, cfName));
    }
    public void reloadCf(Integer cfId) throws IOException
    {
        ColumnFamilyStore cfs = columnFamilyStores.remove(cfId);
        assert cfs != null;
        unloadCf(cfs);
        initCf(cfId, cfs.getColumnFamilyName());
    }
    public void renameCf(Integer cfId, String newName) throws IOException
    {
        assert columnFamilyStores.containsKey(cfId);
        ColumnFamilyStore cfs = columnFamilyStores.remove(cfId);
        unloadCf(cfs);
        cfs.renameSSTables(newName);
        initCf(cfId, newName);
    }
    public Row getRow(QueryFilter filter) throws IOException
    {
        ColumnFamilyStore cfStore = getColumnFamilyStore(filter.getColumnFamilyName());
        ColumnFamily columnFamily = cfStore.getColumnFamily(filter);
        return new Row(filter.key, columnFamily);
    }
    public void apply(RowMutation mutation, boolean writeCommitLog) throws IOException
    {
        List<Memtable> memtablesToFlush = Collections.emptyList();
        if (logger.isDebugEnabled())
            logger.debug("applying mutation of row {}", ByteBufferUtil.bytesToHex(mutation.key()));
        flusherLock.readLock().lock();
        try
        {
            if (writeCommitLog)
                CommitLog.instance.add(mutation);
            DecoratedKey key = StorageService.getPartitioner().decorateKey(mutation.key());
            for (ColumnFamily cf : mutation.getColumnFamilies())
            {
                ColumnFamilyStore cfs = columnFamilyStores.get(cf.id());
                if (cfs == null)
                {
                    logger.error("Attempting to mutate non-existant column family " + cf.id());
                    continue;
                }
                SortedSet<ByteBuffer> mutatedIndexedColumns = null;
                for (ByteBuffer column : cfs.getIndexedColumns())
                {
                    if (cf.getColumnNames().contains(column) || cf.isMarkedForDelete())
                    {
                        if (mutatedIndexedColumns == null)
                            mutatedIndexedColumns = new TreeSet<ByteBuffer>();
                        mutatedIndexedColumns.add(column);
                        if (logger.isDebugEnabled())
                        {
                            ByteBuffer value = cf.getColumn(column) == null ? null : cf.getColumn(column).value(); 
                            logger.debug(String.format("mutating indexed column %s value %s",
                                                       cf.getComparator().getString(column),
                                                       value == null ? "null" : ByteBufferUtil.bytesToHex(value)));
                        }
                    }
                }
                synchronized (indexLockFor(mutation.key()))
                {
                    ColumnFamily oldIndexedColumns = null;
                    if (mutatedIndexedColumns != null)
                    {
                        oldIndexedColumns = readCurrentIndexedColumns(key, cfs, mutatedIndexedColumns);
                        logger.debug("Pre-mutation index row is {}", oldIndexedColumns);
                        ignoreObsoleteMutations(cf, mutatedIndexedColumns, oldIndexedColumns);
                    }
                    Memtable fullMemtable = cfs.apply(key, cf);
                    if (fullMemtable != null)
                        memtablesToFlush = addFullMemtable(memtablesToFlush, fullMemtable);
                    if (mutatedIndexedColumns != null)
                    {
                        applyIndexUpdates(mutation.key(), cf, cfs, mutatedIndexedColumns, oldIndexedColumns);
                    }
                }
            }
        }
        finally
        {
            flusherLock.readLock().unlock();
        }
        for (Memtable memtable : memtablesToFlush)
            memtable.cfs.maybeSwitchMemtable(memtable, writeCommitLog);
    }
    private static List<Memtable> addFullMemtable(List<Memtable> memtablesToFlush, Memtable fullMemtable)
    {
        if (memtablesToFlush.isEmpty())
            memtablesToFlush = new ArrayList<Memtable>(2);
        memtablesToFlush.add(fullMemtable);
        return memtablesToFlush;
    }
    private static void ignoreObsoleteMutations(ColumnFamily cf, SortedSet<ByteBuffer> mutatedIndexedColumns, ColumnFamily oldIndexedColumns)
    {
        if (oldIndexedColumns == null)
            return;
        ColumnFamily cf2 = cf.cloneMe();
        for (IColumn oldColumn : oldIndexedColumns)
        {
            cf2.addColumn(oldColumn);
        }
        ColumnFamily resolved = ColumnFamilyStore.removeDeleted(cf2, Integer.MAX_VALUE);
        for (IColumn oldColumn : oldIndexedColumns)
        {
            IColumn resolvedColumn = resolved == null ? null : resolved.getColumn(oldColumn.name());
            if (resolvedColumn != null && resolvedColumn.equals(oldColumn))
            {
                if (logger.isDebugEnabled())
                    logger.debug("ignoring obsolete mutation of " + cf.getComparator().getString(oldColumn.name()));
                cf.remove(oldColumn.name());
                mutatedIndexedColumns.remove(oldColumn.name());
                oldIndexedColumns.remove(oldColumn.name());
            }
        }
    }
    private static ColumnFamily readCurrentIndexedColumns(DecoratedKey key, ColumnFamilyStore cfs, SortedSet<ByteBuffer> mutatedIndexedColumns)
    {
        QueryFilter filter = QueryFilter.getNamesFilter(key, new QueryPath(cfs.getColumnFamilyName()), mutatedIndexedColumns);
        return cfs.getColumnFamily(filter);
    }
    private static List<Memtable> applyIndexUpdates(ByteBuffer key,
                                                    ColumnFamily cf,
                                                    ColumnFamilyStore cfs,
                                                    SortedSet<ByteBuffer> mutatedIndexedColumns,
                                                    ColumnFamily oldIndexedColumns)
    {
        List<Memtable> fullMemtables = Collections.emptyList();
        for (ByteBuffer columnName : mutatedIndexedColumns)
        {
            IColumn column = cf.getColumn(columnName);
            if (column == null || column.isMarkedForDelete())
                continue; 
            DecoratedKey<LocalToken> valueKey = cfs.getIndexKeyFor(columnName, column.value());
            ColumnFamily cfi = cfs.newIndexedColumnFamily(columnName);
            if (column instanceof ExpiringColumn)
            {
                ExpiringColumn ec = (ExpiringColumn)column;
                cfi.addColumn(new ExpiringColumn(key, ByteBufferUtil.EMPTY_BYTE_BUFFER, ec.timestamp, ec.getTimeToLive(), ec.getLocalDeletionTime()));
            }
            else
            {
                cfi.addColumn(new Column(key, ByteBufferUtil.EMPTY_BYTE_BUFFER, column.timestamp()));
            }
            if (logger.isDebugEnabled())
                logger.debug("applying index row {}:{}", valueKey, cfi);
            Memtable fullMemtable = cfs.getIndexedColumnFamilyStore(columnName).apply(valueKey, cfi);
            if (fullMemtable != null)
                fullMemtables = addFullMemtable(fullMemtables, fullMemtable);
        }
        if (oldIndexedColumns != null)
        {
            int localDeletionTime = (int) (System.currentTimeMillis() / 1000);
            for (Map.Entry<ByteBuffer, IColumn> entry : oldIndexedColumns.getColumnsMap().entrySet())
            {
                ByteBuffer columnName = entry.getKey();
                IColumn column = entry.getValue();
                if (column.isMarkedForDelete())
                    continue;
                DecoratedKey<LocalToken> valueKey = cfs.getIndexKeyFor(columnName, column.value());
                ColumnFamily cfi = cfs.newIndexedColumnFamily(columnName);
                cfi.addTombstone(key, localDeletionTime, column.timestamp());
                Memtable fullMemtable = cfs.getIndexedColumnFamilyStore(columnName).apply(valueKey, cfi);
                if (logger.isDebugEnabled())
                    logger.debug("applying index tombstones {}:{}", valueKey, cfi);
                if (fullMemtable != null)
                    fullMemtables = addFullMemtable(fullMemtables, fullMemtable);
            }
        }
        return fullMemtables;
    }
    public static void cleanupIndexEntry(ColumnFamilyStore cfs, ByteBuffer key, IColumn column)
    {
        if (column.isMarkedForDelete())
            return;
        int localDeletionTime = (int) (System.currentTimeMillis() / 1000);
        DecoratedKey<LocalToken> valueKey = cfs.getIndexKeyFor(column.name(), column.value());
        ColumnFamily cfi = cfs.newIndexedColumnFamily(column.name());
        cfi.addTombstone(key, localDeletionTime, column.timestamp());
        Memtable fullMemtable = cfs.getIndexedColumnFamilyStore(column.name()).apply(valueKey, cfi);
        if (logger.isDebugEnabled())
            logger.debug("removed index entry for cleaned-up value {}:{}", valueKey, cfi);
        if (fullMemtable != null)
            fullMemtable.cfs.maybeSwitchMemtable(fullMemtable, false);
    }
    public IndexBuilder createIndexBuilder(ColumnFamilyStore cfs, SortedSet<ByteBuffer> columns, ReducingKeyIterator iter)
    {
        return new IndexBuilder(cfs, columns, iter);
    }
    public AbstractReplicationStrategy getReplicationStrategy()
    {
        return replicationStrategy;
    }
    public class IndexBuilder implements ICompactionInfo
    {
        private final ColumnFamilyStore cfs;
        private final SortedSet<ByteBuffer> columns;
        private final ReducingKeyIterator iter;
        public IndexBuilder(ColumnFamilyStore cfs, SortedSet<ByteBuffer> columns, ReducingKeyIterator iter)
        {
            this.cfs = cfs;
            this.columns = columns;
            this.iter = iter;
        }
        public void build()
        {
            while (iter.hasNext())
            {
                DecoratedKey key = iter.next();
                logger.debug("Indexing row {} ", key);
                List<Memtable> memtablesToFlush = Collections.emptyList();
                flusherLock.readLock().lock();
                try
                {
                    synchronized (indexLockFor(key.key))
                    {
                        ColumnFamily cf = readCurrentIndexedColumns(key, cfs, columns);
                        if (cf != null)
                            memtablesToFlush = applyIndexUpdates(key.key, cf, cfs, cf.getColumnNames(), null);
                    }
                }
                finally
                {
                    flusherLock.readLock().unlock();
                }
                for (Memtable memtable : memtablesToFlush)
                    memtable.cfs.maybeSwitchMemtable(memtable, false);
            }
            try
            {
                iter.close();
            }
            catch (IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        public long getTotalBytes()
        {
            return iter.getTotalBytes();
        }
        public long getBytesRead()
        {
            return iter.getBytesRead();
        }
        public String getTaskType()
        {
            return String.format("Secondary index build %s", cfs.columnFamily);
        }
    }
    private Object indexLockFor(ByteBuffer key)
    {
        return indexLocks[Math.abs(key.hashCode() % indexLocks.length)];
    }
    public List<Future<?>> flush() throws IOException
    {
        List<Future<?>> futures = new ArrayList<Future<?>>();
        for (Integer cfId : columnFamilyStores.keySet())
        {
            Future<?> future = columnFamilyStores.get(cfId).forceFlush();
            if (future != null)
                futures.add(future);
        }
        return futures;
    }
    void load(RowMutation rowMutation) throws IOException
    {
        DecoratedKey key = StorageService.getPartitioner().decorateKey(rowMutation.key());
        for (ColumnFamily columnFamily : rowMutation.getColumnFamilies())
        {
            Collection<IColumn> columns = columnFamily.getSortedColumns();
            for (IColumn column : columns)
            {
                ColumnFamilyStore cfStore = columnFamilyStores.get(ByteBufferUtil.toInt(column.name()));
                cfStore.applyBinary(key, column.value());
            }
        }
    }
    public String getDataFileLocation(long expectedCompactedFileSize)
    {
        String path = DatabaseDescriptor.getDataFileLocationForTable(name, expectedCompactedFileSize);
        if (path == null)
        {
            StorageService.instance.requestGC();
            try
            {
                Thread.sleep(SSTableDeletingReference.RETRY_DELAY * 2);
            }
            catch (InterruptedException e)
            {
                throw new AssertionError(e);
            }
            path = DatabaseDescriptor.getDataFileLocationForTable(name, expectedCompactedFileSize);
        }
        return path;
    }
    public static String getSnapshotPath(String dataDirPath, String tableName, String snapshotName)
    {
        return dataDirPath + File.separator + tableName + File.separator + SNAPSHOT_SUBDIR_NAME + File.separator + snapshotName;
    }
    public static Iterable<Table> all()
    {
        Function<String, Table> transformer = new Function<String, Table>()
        {
            public Table apply(String tableName)
            {
                return Table.open(tableName);
            }
        };
        return Iterables.transform(DatabaseDescriptor.getTables(), transformer);
    }
    public void truncate(String cfname) throws InterruptedException, ExecutionException, IOException
    {
        logger.debug("Truncating...");
        ColumnFamilyStore cfs = getColumnFamilyStore(cfname);
        cfs.truncate().get();
        logger.debug("Truncation done.");
    }
}
