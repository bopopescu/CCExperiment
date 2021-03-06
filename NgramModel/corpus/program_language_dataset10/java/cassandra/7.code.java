package org.apache.cassandra.contrib.utils.service;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import org.apache.cassandra.config.CFMetaData;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.config.KSMetaData;
import org.apache.cassandra.service.EmbeddedCassandraService;
import org.apache.cassandra.thrift.AuthenticationException;
import org.apache.cassandra.thrift.AuthorizationException;
import org.apache.cassandra.thrift.Cassandra;
import org.apache.cassandra.thrift.Column;
import org.apache.cassandra.thrift.ColumnOrSuperColumn;
import org.apache.cassandra.thrift.ColumnParent;
import org.apache.cassandra.thrift.ColumnPath;
import org.apache.cassandra.thrift.ConsistencyLevel;
import org.apache.cassandra.thrift.InvalidRequestException;
import org.apache.cassandra.thrift.NotFoundException;
import org.apache.cassandra.thrift.TimedOutException;
import org.apache.cassandra.thrift.UnavailableException;
import org.apache.cassandra.utils.ByteBufferUtil;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.junit.BeforeClass;
import org.junit.Test;
public class CassandraServiceTest {
    private static EmbeddedCassandraService cassandra;
    private static Thread cassandraRunner;
    private static CassandraServiceDataCleaner cleaner;
    @BeforeClass
    public static void setup() throws TTransportException, IOException,
            InterruptedException, ConfigurationException {
        System.setProperty("cassandra.config", "file:../../test/conf/cassandra.yaml");
        System.setProperty("log4j.configuration", "file:../../test/conf/log4j-junit.properties");
        loadYamlTables();
        initCleaner();
    }
    private static void initCleaner() throws IOException, TTransportException, ConfigurationException {
        cleaner = new CassandraServiceDataCleaner();
        cleaner.prepare();
        cassandra = new EmbeddedCassandraService();
        cassandra.init();
        if ( cassandraRunner == null ) {
            cassandraRunner = new Thread(cassandra);
            cassandraRunner.setDaemon(true);
            cassandraRunner.start();
        }
    }
    private static void loadYamlTables() throws ConfigurationException {
      for (KSMetaData table : DatabaseDescriptor.readTablesFromYaml()) {
        for (CFMetaData cfm : table.cfMetaData().values()) {
          CFMetaData.map(cfm);
        }
        DatabaseDescriptor.setTableDefinition(table, DatabaseDescriptor.getDefsVersion());
      }
    }
    @Test
    public void testInProcessCassandraServer()
            throws UnsupportedEncodingException, InvalidRequestException,
            UnavailableException, TimedOutException, TException,
            NotFoundException, AuthenticationException, AuthorizationException {
        Cassandra.Client client = getClient();
        client.set_keyspace("Keyspace1");        
        String key_user_id = "1";
        long timestamp = System.currentTimeMillis();   
        ColumnParent colParent = new ColumnParent("Standard1");
        Column column = new Column(ByteBufferUtil.bytes("name"), 
                ByteBufferUtil.bytes("Ran"), timestamp);
        client.insert(ByteBufferUtil.bytes(key_user_id), colParent, column, ConsistencyLevel.ONE);
        ColumnPath cp = new ColumnPath("Standard1");
        cp.setColumn(ByteBufferUtil.bytes("name"));
        ColumnOrSuperColumn got = client.get(ByteBufferUtil.bytes(key_user_id), cp,
                ConsistencyLevel.ONE);
        assertNotNull("Got a null ColumnOrSuperColumn", got);
        assertEquals("Ran", new String(got.getColumn().getValue(), "utf-8"));
    }
    private Cassandra.Client getClient() throws TTransportException {
        TTransport tr = new TFramedTransport(new TSocket("localhost", 9170));
        TProtocol proto = new TBinaryProtocol(tr);
        Cassandra.Client client = new Cassandra.Client(proto);
        tr.open();
        return client;
    }
}
