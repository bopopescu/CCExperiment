package org.apache.maven.project;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.Repository;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
@Deprecated
public final class ProjectUtils
{
    private ProjectUtils()
    {
    }
    public static List<ArtifactRepository> buildArtifactRepositories( List<Repository> repositories,
                                                                      ArtifactRepositoryFactory artifactRepositoryFactory,
                                                                      PlexusContainer c )
        throws InvalidRepositoryException
    {
        List<ArtifactRepository> remoteRepositories = new ArrayList<ArtifactRepository>();
        for ( Repository r : repositories )
        {
            remoteRepositories.add( buildArtifactRepository( r, artifactRepositoryFactory, c ) );
        }
        return remoteRepositories;
    }
    public static ArtifactRepository buildDeploymentArtifactRepository( DeploymentRepository repo,
                                                                        ArtifactRepositoryFactory artifactRepositoryFactory,
                                                                        PlexusContainer c )
        throws InvalidRepositoryException
    {
        return buildArtifactRepository( repo, artifactRepositoryFactory, c );
    }
    public static ArtifactRepository buildArtifactRepository( Repository repo,
                                                              ArtifactRepositoryFactory artifactRepositoryFactory,
                                                              PlexusContainer c )
        throws InvalidRepositoryException
    {
        RepositorySystem repositorySystem = rs( c );
        MavenExecutionRequest executionRequest = er( c );
        ArtifactRepository repository = repositorySystem.buildArtifactRepository( repo );
        if ( executionRequest != null )
        {
            repositorySystem.injectMirror( Arrays.asList( repository ), executionRequest.getMirrors() );
            repositorySystem.injectProxy( Arrays.asList( repository ), executionRequest.getProxies() );
            repositorySystem.injectAuthentication( Arrays.asList( repository ), executionRequest.getServers() );
        }
        return repository;
    }
    private static RepositorySystem rs( PlexusContainer c )
    {
        try
        {
            return c.lookup( RepositorySystem.class );
        }
        catch ( ComponentLookupException e )
        {
            throw new IllegalStateException( e );
        }
    }
    private static MavenExecutionRequest er( PlexusContainer c )
    {
        try
        {
            LegacySupport legacySupport = c.lookup( LegacySupport.class );
            if ( legacySupport.getSession() != null )
            {
                return legacySupport.getSession().getRequest();
            }
            else
            {
                return null;
            }
        }
        catch ( ComponentLookupException e )
        {
            throw new IllegalStateException( e );
        }
    }
}
