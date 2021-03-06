package org.apache.cassandra;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.cassandra.service.CassandraDaemon;
import org.junit.AfterClass;
import org.junit.BeforeClass;
public class EmbeddedServer extends CleanupHelper
{
    protected static CassandraDaemon daemon = null;
    enum GatewayService
    {
        Thrift
    }
    public static GatewayService getDaemonGatewayService()
    {
        return GatewayService.Thrift;
    }
    static ExecutorService executor = Executors.newSingleThreadExecutor();
    @BeforeClass
    public static void startCassandra() throws IOException
    {
        executor.submit(new Runnable()
        {
            public void run()
            {
                switch (getDaemonGatewayService())
                {
                    case Thrift:
                    default:
                        daemon = new org.apache.cassandra.thrift.CassandraDaemon();
                }
                daemon.activate();
            }
        });
        try
        {
            TimeUnit.SECONDS.sleep(3);
        }
        catch (InterruptedException e)
        {
            throw new AssertionError(e);
        }
    }
    @AfterClass
    public static void stopCassandra() throws Exception
    {
        if (daemon != null)
        {
            daemon.deactivate();
        }
        executor.shutdown();
        executor.shutdownNow();
    }
}
