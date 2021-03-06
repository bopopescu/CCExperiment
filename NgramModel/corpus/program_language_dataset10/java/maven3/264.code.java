package org.apache.maven;
import java.io.FileNotFoundException;
import org.codehaus.plexus.logging.Logger;
import org.sonatype.aether.AbstractRepositoryListener;
import org.sonatype.aether.RepositoryEvent;
import org.sonatype.aether.transfer.MetadataNotFoundException;
class LoggingRepositoryListener
    extends AbstractRepositoryListener
{
    private final Logger logger;
    public LoggingRepositoryListener( Logger logger )
    {
        this.logger = logger;
    }
    @Override
    public void artifactInstalling( RepositoryEvent event )
    {
        logger.info( "Installing " + event.getArtifact().getFile() + " to " + event.getFile() );
    }
    @Override
    public void metadataInstalling( RepositoryEvent event )
    {
        logger.debug( "Installing " + event.getMetadata() + " to " + event.getFile() );
    }
    @Override
    public void metadataResolved( RepositoryEvent event )
    {
        Exception e = event.getException();
        if ( e != null )
        {
            if ( e instanceof MetadataNotFoundException )
            {
                logger.debug( e.getMessage() );
            }
            else if ( logger.isDebugEnabled() )
            {
                logger.warn( e.getMessage(), e );
            }
            else
            {
                logger.warn( e.getMessage() );
            }
        }
    }
    @Override
    public void metadataInvalid( RepositoryEvent event )
    {
        Exception exception = event.getException();
        StringBuilder buffer = new StringBuilder( 256 );
        buffer.append( "The metadata " );
        if ( event.getMetadata().getFile() != null )
        {
            buffer.append( event.getMetadata().getFile() );
        }
        else
        {
            buffer.append( event.getMetadata() );
        }
        if ( exception instanceof FileNotFoundException )
        {
            buffer.append( " is inaccessible" );
        }
        else
        {
            buffer.append( " is invalid" );
        }
        if ( exception != null )
        {
            buffer.append( ": " );
            buffer.append( exception.getMessage() );
        }
        if ( logger.isDebugEnabled() )
        {
            logger.warn( buffer.toString(), exception );
        }
        else
        {
            logger.warn( buffer.toString() );
        }
    }
    @Override
    public void artifactDescriptorInvalid( RepositoryEvent event )
    {
        StringBuilder buffer = new StringBuilder( 256 );
        buffer.append( "The POM for " );
        buffer.append( event.getArtifact() );
        buffer.append( " is invalid, transitive dependencies (if any) will not be available" );
        if ( logger.isDebugEnabled() )
        {
            logger.warn( buffer + ": " + event.getException().getMessage() );
        }
        else
        {
            logger.warn( buffer + ", enable debug logging for more details" );
        }
    }
    @Override
    public void artifactDescriptorMissing( RepositoryEvent event )
    {
        logger.warn( "The POM for " + event.getArtifact() + " is missing, no dependency information available" );
    }
}
