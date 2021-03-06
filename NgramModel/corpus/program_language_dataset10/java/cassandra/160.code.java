package org.apache.cassandra.db;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.io.ICompactSerializer;
import org.apache.cassandra.service.StorageService;
import org.apache.cassandra.utils.ByteBufferUtil;
public class Row
{
    private static Logger logger_ = LoggerFactory.getLogger(Row.class);
    private static RowSerializer serializer = new RowSerializer();
    static RowSerializer serializer()
    {
        return serializer;
    }
    public final DecoratedKey key;
    public final ColumnFamily cf;
    public Row(DecoratedKey key, ColumnFamily cf)
    {
        assert key != null;
        this.key = key;
        this.cf = cf;
    }
    @Override
    public String toString()
    {
        return "Row(" +
               "key=" + key +
               ", cf=" + cf +
               ')';
    }
}
class RowSerializer implements ICompactSerializer<Row>
{
    public void serialize(Row row, DataOutputStream dos) throws IOException
    {
        ByteBufferUtil.writeWithShortLength(row.key.key, dos);
        ColumnFamily.serializer().serialize(row.cf, dos);
    }
    public Row deserialize(DataInputStream dis) throws IOException
    {
        return new Row(StorageService.getPartitioner().decorateKey(ByteBufferUtil.readWithShortLength(dis)),
                       ColumnFamily.serializer().deserialize(dis));
    }
}
