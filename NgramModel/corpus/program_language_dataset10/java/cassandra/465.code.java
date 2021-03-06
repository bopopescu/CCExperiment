package org.apache.cassandra.db;
import static junit.framework.Assert.assertEquals;
import static org.apache.cassandra.Util.addMutation;
import static org.apache.cassandra.Util.column;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.ExecutionException;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.Util;
import org.apache.cassandra.db.commitlog.CommitLog;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.junit.Test;
public class NameSortTest extends CleanupHelper
{
    @Test
    public void testNameSort1() throws IOException, ExecutionException, InterruptedException
    {
        testNameSort(1);
    }
    @Test
    public void testNameSort10() throws IOException, ExecutionException, InterruptedException
    {
        testNameSort(10);
    }
    @Test
    public void testNameSort100() throws IOException, ExecutionException, InterruptedException
    {
        testNameSort(100);
    }
    private void testNameSort(int N) throws IOException, ExecutionException, InterruptedException
    {
        Table table = Table.open("Keyspace1");
        for (int i = 0; i < N; ++i)
        {
            ByteBuffer key = ByteBuffer.wrap(Integer.toString(i).getBytes());
            RowMutation rm;
            for (int j = 0; j < 8; ++j)
            {
                ByteBuffer bytes = ByteBuffer.wrap(j % 2 == 0 ? "a".getBytes() : "b".getBytes());
                rm = new RowMutation("Keyspace1", key);
                rm.add(new QueryPath("Standard1", null, ByteBuffer.wrap(("Column-" + j).getBytes())), bytes, j);
                rm.applyUnsafe();
            }
            for (int j = 0; j < 8; ++j)
            {
                rm = new RowMutation("Keyspace1", key);
                for (int k = 0; k < 4; ++k)
                {
                    String value = (j + k) % 2 == 0 ? "a" : "b";
                    addMutation(rm, "Super1", "SuperColumn-" + j, k, value, k);
                }
                rm.applyUnsafe();
            }
        }
        validateNameSort(table, N);
        table.getColumnFamilyStore("Standard1").forceBlockingFlush();
        table.getColumnFamilyStore("Super1").forceBlockingFlush();
        validateNameSort(table, N);
    }
    private void validateNameSort(Table table, int N) throws IOException
    {
        for (int i = 0; i < N; ++i)
        {
            DecoratedKey key = Util.dk(Integer.toString(i));
            ColumnFamily cf;
            cf = Util.getColumnFamily(table, key, "Standard1");
            Collection<IColumn> columns = cf.getSortedColumns();
            for (IColumn column : columns)
            {
                String name = ByteBufferUtil.string(column.name());
                int j = Integer.valueOf(name.substring(name.length() - 1));
                byte[] bytes = j % 2 == 0 ? "a".getBytes() : "b".getBytes();
                assertEquals(new String(bytes), ByteBufferUtil.string(column.value()));
            }
            cf = Util.getColumnFamily(table, key, "Super1");
            assert cf != null : "key " + key + " is missing!";
            Collection<IColumn> superColumns = cf.getSortedColumns();
            assert superColumns.size() == 8 : cf;
            for (IColumn superColumn : superColumns)
            {
                int j = Integer.valueOf(ByteBufferUtil.string(superColumn.name()).split("-")[1]);
                Collection<IColumn> subColumns = superColumn.getSubColumns();
                assert subColumns.size() == 4;
                for (IColumn subColumn : subColumns)
                {
                    long k = subColumn.name().getLong(subColumn.name().position());
                    byte[] bytes = (j + k) % 2 == 0 ? "a".getBytes() : "b".getBytes();
                    assertEquals(new String(bytes), ByteBufferUtil.string(subColumn.value()));
                }
            }
        }
    }
}
