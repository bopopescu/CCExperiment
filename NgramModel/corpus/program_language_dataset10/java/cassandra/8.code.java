package org.apache.cassandra.hadoop.pig;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import org.apache.cassandra.db.Column;
import org.apache.cassandra.db.IColumn;
import org.apache.cassandra.db.SuperColumn;
import org.apache.cassandra.hadoop.*;
import org.apache.cassandra.thrift.SlicePredicate;
import org.apache.cassandra.thrift.SliceRange;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.pig.LoadFunc;
import org.apache.pig.backend.hadoop.executionengine.mapReduceLayer.PigSplit;
import org.apache.pig.data.DefaultDataBag;
import org.apache.pig.data.DataByteArray;
import org.apache.pig.data.Tuple;
import org.apache.pig.data.TupleFactory;
public class CassandraStorage extends LoadFunc
{
    public final static String PIG_RPC_PORT = "PIG_RPC_PORT";
    public final static String PIG_INITIAL_ADDRESS = "PIG_INITIAL_ADDRESS";
    public final static String PIG_PARTITIONER = "PIG_PARTITIONER";
    private final static ByteBuffer BOUND = FBUtilities.EMPTY_BYTE_BUFFER;
    private final static int LIMIT = 1024;
    private Configuration conf;
    private RecordReader reader;
    @Override
    public Tuple getNext() throws IOException
    {
        try
        {
            if (!reader.nextKeyValue())
                return null;
            ByteBuffer key = (ByteBuffer)reader.getCurrentKey();
            SortedMap<ByteBuffer,IColumn> cf = (SortedMap<ByteBuffer,IColumn>)reader.getCurrentValue();
            assert key != null && cf != null;
	    Tuple tuple = TupleFactory.getInstance().newTuple(2);
            ArrayList<Tuple> columns = new ArrayList<Tuple>();
            tuple.set(0, new DataByteArray(key.array(), key.position()+key.arrayOffset(), key.limit()+key.arrayOffset()));
            for (Map.Entry<ByteBuffer, IColumn> entry : cf.entrySet())
            {                    
                columns.add(columnToTuple(entry.getKey(), entry.getValue()));
            }
            tuple.set(1, new DefaultDataBag(columns));
            return tuple;
        }
        catch (InterruptedException e)
        {
            throw new IOException(e.getMessage());
        }
    }
    private Tuple columnToTuple(ByteBuffer name, IColumn col) throws IOException
    {
        Tuple pair = TupleFactory.getInstance().newTuple(2);
        pair.set(0, new DataByteArray(name.array(), name.position()+name.arrayOffset(), name.limit()+name.arrayOffset()));
        if (col instanceof Column)
        {
            pair.set(1, new DataByteArray(col.value().array(), 
                                          col.value().position()+col.value().arrayOffset(),
                                          col.value().limit()+col.value().arrayOffset()));
            return pair;
        }
        ArrayList<Tuple> subcols = new ArrayList<Tuple>();
        for (IColumn subcol : ((SuperColumn)col).getSubColumns())
            subcols.add(columnToTuple(subcol.name(), subcol));
        pair.set(1, new DefaultDataBag(subcols));
        return pair;
    }
    @Override
    public InputFormat getInputFormat()
    {
        ColumnFamilyInputFormat inputFormat = new ColumnFamilyInputFormat();
        return inputFormat;
    }
    @Override
    public void prepareToRead(RecordReader reader, PigSplit split)
    {
        this.reader = reader;
    }
    @Override
    public void setLocation(String location, Job job) throws IOException
    {
        String ksname, cfname;
        try
        {
            if (!location.startsWith("cassandra://"))
                throw new Exception("Bad scheme.");
            String[] parts = location.split("/+");
            ksname = parts[1];
            cfname = parts[2];
        }
        catch (Exception e)
        {
            throw new IOException("Expected 'cassandra://<keyspace>/<columnfamily>': " + e.getMessage());
        }
        SliceRange range = new SliceRange(BOUND, BOUND, false, LIMIT);
        SlicePredicate predicate = new SlicePredicate().setSlice_range(range);
        conf = job.getConfiguration();
        ConfigHelper.setInputSlicePredicate(conf, predicate);
        ConfigHelper.setInputColumnFamily(conf, ksname, cfname);
        if (System.getenv(PIG_RPC_PORT) != null)
            ConfigHelper.setRpcPort(conf, System.getenv(PIG_RPC_PORT));
        if (System.getenv(PIG_INITIAL_ADDRESS) != null)
            ConfigHelper.setInitialAddress(conf, System.getenv(PIG_INITIAL_ADDRESS));
        if (System.getenv(PIG_PARTITIONER) != null)
            ConfigHelper.setPartitioner(conf, System.getenv(PIG_PARTITIONER));
    }
    @Override
    public String relativeToAbsolutePath(String location, Path curDir) throws IOException
    {
        return location;
    }
}
