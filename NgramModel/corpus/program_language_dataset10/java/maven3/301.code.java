package org.apache.maven.artifact.resolver;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.codehaus.plexus.component.annotations.Component;
@Component( role = ResolutionErrorHandler.class )
public class DefaultResolutionErrorHandler
    implements ResolutionErrorHandler
{
    public void throwErrors( ArtifactResolutionRequest request, ArtifactResolutionResult result )
        throws ArtifactResolutionException
    {
        if ( result.hasMetadataResolutionExceptions() )
        {
            throw result.getMetadataResolutionException( 0 );
        }
        if ( result.hasCircularDependencyExceptions() )
        {
            throw result.getCircularDependencyException( 0 );
        }
        if ( result.hasVersionRangeViolations() )
        {
            throw result.getVersionRangeViolation( 0 );
        }
        if ( result.hasErrorArtifactExceptions() )
        {
            throw result.getErrorArtifactExceptions().get( 0 );
        }
        if ( result.hasMissingArtifacts() )
        {
            throw new MultipleArtifactsNotFoundException( request.getArtifact(), toList( result.getArtifacts() ),
                                                          result.getMissingArtifacts(),
                                                          request.getRemoteRepositories() );
        }
        if ( result.hasExceptions() )
        {
            throw new ArtifactResolutionException( "Unknown error during artifact resolution, " + request + ", "
                + result.getExceptions(), request.getArtifact(), request.getRemoteRepositories() );
        }
    }
    private static <T> List<T> toList( Collection<T> items )
    {
        return ( items != null ) ? new ArrayList<T>( items ) : null;
    }
}
