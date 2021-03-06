package org.apache.cassandra.db;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import org.apache.cassandra.Util;
import org.junit.Test;
import org.apache.cassandra.io.sstable.SSTableReader;
import org.apache.cassandra.CleanupHelper;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.utils.FBUtilities;
import static junit.framework.Assert.assertEquals;
import org.apache.cassandra.utils.ByteBufferUtil;
public class RowIterationTest extends CleanupHelper
{
    public static final String TABLE1 = "Keyspace2";
    public static final InetAddress LOCAL = FBUtilities.getLocalAddress();
    @Test
    public void testRowIteration() throws IOException, ExecutionException, InterruptedException
    {
        Table table = Table.open(TABLE1);
        ColumnFamilyStore store = table.getColumnFamilyStore("Super3");
        final int ROWS_PER_SSTABLE = 10;
        Set<DecoratedKey> inserted = new HashSet<DecoratedKey>();
        for (int i = 0; i < ROWS_PER_SSTABLE; i++) {
            DecoratedKey key = Util.dk(String.valueOf(i));
            RowMutation rm = new RowMutation(TABLE1, key.key);
            rm.add(new QueryPath("Super3", ByteBufferUtil.bytes("sc"), ByteBuffer.wrap(String.valueOf(i).getBytes())), ByteBuffer.wrap(new byte[ROWS_PER_SSTABLE * 10 - i * 2]), i);
            rm.apply();
            inserted.add(key);
        }
        store.forceBlockingFlush();
        assertEquals(inserted.toString(), inserted.size(), Util.getRangeSlice(store).size());
    }
}
