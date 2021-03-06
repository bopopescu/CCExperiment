package org.apache.maven.repository.legacy;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.InputData;
import org.apache.maven.wagon.OutputData;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.StreamWagon;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.resource.Resource;
import org.codehaus.plexus.component.annotations.Component;
@Component(role=Wagon.class,hint="string")
public class StringWagon
    extends StreamWagon
{
    private Map<String, String> expectedContent = new HashMap<String, String>();
    public void addExpectedContent( String resourceName, String expectedContent )
    {
        this.expectedContent.put( resourceName, expectedContent );
    }
    public String[] getSupportedProtocols()
    {
        return new String[] { "string" };
    }
    @Override
    public void closeConnection()
        throws ConnectionException
    {
    }
    @Override
    public void fillInputData( InputData inputData )
        throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException
    {
        Resource resource = inputData.getResource();
        String content = expectedContent.get( resource.getName() );
        if ( content != null )
        {
            resource.setContentLength( content.length() );
            resource.setLastModified( System.currentTimeMillis() );
            try
            {
                inputData.setInputStream( new ByteArrayInputStream( content.getBytes( "UTF-8" ) ) );
            }
            catch ( UnsupportedEncodingException e )
            {
                throw new Error( "broken JVM", e );
            }
        }
        else
        {
            throw new ResourceDoesNotExistException( "No content provided for " + resource.getName() );
        }
    }
    @Override
    public void fillOutputData( OutputData outputData )
        throws TransferFailedException
    {
        outputData.setOutputStream( new ByteArrayOutputStream() );
    }
    @Override
    protected void openConnectionInternal()
        throws ConnectionException, AuthenticationException
    {
    }
    public void clearExpectedContent()
    {
        expectedContent.clear();        
    }
}
