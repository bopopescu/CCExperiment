package org.apache.cassandra.db;
import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.cassandra.SchemaLoader;
import org.apache.cassandra.Util;
import org.apache.cassandra.db.filter.QueryPath;
import org.apache.cassandra.io.util.DataOutputBuffer;
import org.junit.Test;
import org.apache.cassandra.utils.ByteBufferUtil;
public class ReadMessageTest extends SchemaLoader
{
    @Test
    public void testMakeReadMessage() throws IOException
    {
        ArrayList<ByteBuffer> colList = new ArrayList<ByteBuffer>();
        colList.add(ByteBufferUtil.bytes("col1"));
        colList.add(ByteBufferUtil.bytes("col2"));
        ReadCommand rm, rm2;
        DecoratedKey dk = Util.dk("row1");
        rm = new SliceByNamesReadCommand("Keyspace1", dk.key, new QueryPath("Standard1"), colList);
        rm2 = serializeAndDeserializeReadMessage(rm);
        assert rm2.toString().equals(rm.toString());
        rm = new SliceFromReadCommand("Keyspace1", dk.key, new QueryPath("Standard1"), ByteBufferUtil.EMPTY_BYTE_BUFFER, ByteBufferUtil.EMPTY_BYTE_BUFFER, true, 2);
        rm2 = serializeAndDeserializeReadMessage(rm);
        assert rm2.toString().equals(rm.toString());
        rm = new SliceFromReadCommand("Keyspace1", dk.key, new QueryPath("Standard1"), ByteBufferUtil.bytes("a"), ByteBufferUtil.bytes("z"), true, 5);
        rm2 = serializeAndDeserializeReadMessage(rm);
        assertEquals(rm2.toString(), rm.toString());
        rm = new SliceFromReadCommand("Keyspace1", dk.key, new QueryPath("Standard1"), ByteBufferUtil.EMPTY_BYTE_BUFFER, ByteBufferUtil.EMPTY_BYTE_BUFFER, true, 2);
        rm2 = serializeAndDeserializeReadMessage(rm);
        assert rm2.toString().equals(rm.toString());
        rm = new SliceFromReadCommand("Keyspace1", dk.key, new QueryPath("Standard1"), ByteBufferUtil.bytes("a"), ByteBufferUtil.bytes("z"), true, 5);
        rm2 = serializeAndDeserializeReadMessage(rm);
        assertEquals(rm2.toString(), rm.toString());
    }
    private ReadCommand serializeAndDeserializeReadMessage(ReadCommand rm) throws IOException
    {
        ReadCommandSerializer rms = ReadCommand.serializer();
        DataOutputBuffer dos = new DataOutputBuffer();
        ByteArrayInputStream bis;
        rms.serialize(rm, dos);
        bis = new ByteArrayInputStream(dos.getData(), 0, dos.getLength());
        return rms.deserialize(new DataInputStream(bis));
    }
    @Test
    public void testGetColumn() throws IOException, ColumnFamilyNotDefinedException
    {
        Table table = Table.open("Keyspace1");
        RowMutation rm;
        DecoratedKey dk = Util.dk("key1");
        rm = new RowMutation("Keyspace1", dk.key);
        rm.add(new QueryPath("Standard1", null, ByteBufferUtil.bytes("Column1")), ByteBufferUtil.bytes("abcd"), 0);
        rm.apply();
        ReadCommand command = new SliceByNamesReadCommand("Keyspace1", dk.key, new QueryPath("Standard1"), Arrays.asList(ByteBufferUtil.bytes("Column1")));
        Row row = command.getRow(table);
        IColumn col = row.cf.getColumn(ByteBufferUtil.bytes("Column1"));
        assert Arrays.equals(col.value().array(), "abcd".getBytes());  
    }
}
