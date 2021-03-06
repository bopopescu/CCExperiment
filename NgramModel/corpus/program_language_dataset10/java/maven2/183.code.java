package org.apache.maven.cli;
import junit.framework.TestCase;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.providers.file.FileWagon;
import org.apache.maven.wagon.repository.Repository;
import org.apache.maven.wagon.resource.Resource;
public abstract class AbstractConsoleDownloadMonitorTest
    extends TestCase
{
    protected AbstractConsoleDownloadMonitor monitor;
    public AbstractConsoleDownloadMonitorTest()
    {
        super();
    }
    public void setMonitor( AbstractConsoleDownloadMonitor monitor )
    {
        this.monitor = monitor;
    }
    public AbstractConsoleDownloadMonitor getMonitor()
    {
        return monitor;
    }
    public void testTransferInitiated()
        throws Exception
    {
        monitor.transferInitiated( new TransferEventMock() );
    }
    public void testTransferStarted()
        throws Exception
    {
        monitor.transferStarted( new TransferEventMock() );
    }
    public void testTransferProgress()
        throws Exception
    {
        byte[] buffer = new byte[1000];
        monitor.transferProgress( new TransferEventMock(), buffer, 1000 );
    }
    public void testTransferCompleted()
        throws Exception
    {
        monitor.transferCompleted( new TransferEventMock() );
    }
    public void testTransferError()
        throws Exception
    {
        monitor.transferError( new TransferEventMock( new RuntimeException() ) );
    }
    public void testDebug()
        throws Exception
    {
        monitor.debug( "msg" );
    }
    static class TransferEventMock
        extends TransferEvent
    {
        public TransferEventMock()
            throws ConnectionException, AuthenticationException
        {
            super( new FileWagon(), new Resource(), TransferEvent.TRANSFER_INITIATED, TransferEvent.REQUEST_GET );
            getResource().setContentLength( 100000 );
            getResource().setName( "foo.bar" );
            Repository repository = new Repository();
            getWagon().connect( repository );
        }
        public TransferEventMock( Resource resource, int length )
            throws ConnectionException, AuthenticationException
        {
            super( new FileWagon(), resource, TransferEvent.TRANSFER_INITIATED, TransferEvent.REQUEST_GET );
            getResource().setContentLength( length );
            Repository repository = new Repository();
            getWagon().connect( repository );
        }
        public TransferEventMock( Exception exception )
            throws ConnectionException, AuthenticationException
        {
            super( new FileWagon(), new Resource(), exception, TransferEvent.REQUEST_GET );
            getResource().setContentLength( 100000 );
            getResource().setName( "foo.bar" );
            Repository repository = new Repository();
            getWagon().connect( repository );
        }
    }
}
