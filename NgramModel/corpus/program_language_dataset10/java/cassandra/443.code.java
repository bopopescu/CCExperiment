package org.apache.cassandra.db;
import java.net.InetAddress;
import java.util.*;
import org.apache.cassandra.Util;
import org.junit.Test;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.io.sstable.SSTableReader;
import org.apache.cassandra.io.sstable.SSTableUtils;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.utils.FBUtilities;
import static junit.framework.Assert.assertEquals;
public class LongCompactionSpeedTest extends CleanupHelper
{
    public static final String TABLE1 = "Keyspace1";
    public static final InetAddress LOCAL = FBUtilities.getLocalAddress();
    @Test
    public void testCompactionWide() throws Exception
    {
        testCompaction(2, 1, 200000);
    }
    @Test
    public void testCompactionSlim() throws Exception
    {
        testCompaction(2, 200000, 1);
    }
    @Test
    public void testCompactionMany() throws Exception
    {
        testCompaction(100, 800, 5);
    }
    protected void testCompaction(int sstableCount, int rowsPerSSTable, int colsPerRow) throws Exception
    {
        CompactionManager.instance.disableAutoCompaction();
        Table table = Table.open(TABLE1);
        ColumnFamilyStore store = table.getColumnFamilyStore("Standard1");
        ArrayList<SSTableReader> sstables = new ArrayList<SSTableReader>();
        for (int k = 0; k < sstableCount; k++)
        {
            SortedMap<String,ColumnFamily> rows = new TreeMap<String,ColumnFamily>();
            for (int j = 0; j < rowsPerSSTable; j++)
            {
                String key = String.valueOf(j);
                IColumn[] cols = new IColumn[colsPerRow];
                for (int i = 0; i < colsPerRow; i++)
                {
                    cols[i] = Util.column(String.valueOf(i), String.valueOf(i), k);
                }
                rows.put(key, SSTableUtils.createCF(Long.MIN_VALUE, Integer.MIN_VALUE, cols));
            }
            SSTableReader sstable = SSTableUtils.prepare().write(rows);
            sstables.add(sstable);
            store.addSSTable(sstable);
        }
        Thread.sleep(1000);
        long start = System.currentTimeMillis();
        CompactionManager.instance.doCompaction(store, sstables, (int) (System.currentTimeMillis() / 1000) - DatabaseDescriptor.getCFMetaData(TABLE1, "Standard1").getGcGraceSeconds());
        System.out.println(String.format("%s: sstables=%d rowsper=%d colsper=%d: %d ms",
                                         this.getClass().getName(),
                                         sstableCount,
                                         rowsPerSSTable,
                                         colsPerRow,
                                         System.currentTimeMillis() - start));
    }
}
