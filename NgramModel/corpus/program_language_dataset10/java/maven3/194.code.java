package org.apache.maven.artifact.resolver;
import java.io.File;
import java.io.InputStream;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.TransferListener;
import org.apache.maven.wagon.providers.file.FileWagon;
import org.apache.maven.wagon.resource.Resource;
public class TestFileWagon
    extends FileWagon
{
    private TestTransferListener testTransferListener;
    private boolean insideGet;
    protected void getTransfer( Resource resource, 
                                File destination, 
                                InputStream input, 
                                boolean closeInput, 
                                int maxSize )
        throws TransferFailedException
    {
        addTransfer( "getTransfer " + resource.getName() );
        super.getTransfer( resource, destination, input, closeInput, maxSize );
    }
    public void get( String resourceName, File destination )
        throws TransferFailedException, 
               ResourceDoesNotExistException, 
               AuthorizationException
    {
        addTransfer( "get " + resourceName );
        insideGet = true;
        super.get( resourceName, destination );
        insideGet = false;
    }
    private void addTransfer( String resourceName )
    {
        if ( testTransferListener != null )
        {
            testTransferListener.addTransfer( resourceName );
        }
    }
    public boolean getIfNewer( String resourceName, File destination, long timestamp )
        throws TransferFailedException, 
               ResourceDoesNotExistException, 
               AuthorizationException
    {
        if ( !insideGet )
        {
            addTransfer( "getIfNewer " + resourceName );
        }
        return super.getIfNewer( resourceName, destination, timestamp );
    }
    public void addTransferListener( TransferListener listener )
    {
        if ( listener instanceof TestTransferListener )
        {
            testTransferListener = (TestTransferListener) listener;
        }
        super.addTransferListener( listener );
    }
}
