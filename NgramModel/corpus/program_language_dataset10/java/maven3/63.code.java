package org.apache.maven.artifact.repository;
import java.util.Arrays;
import java.util.List;
import org.apache.maven.artifact.UnknownRepositoryLayoutException;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
@Component( role = ArtifactRepositoryFactory.class )
public class DefaultArtifactRepositoryFactory
    implements ArtifactRepositoryFactory
{
    @Requirement
    private org.apache.maven.repository.legacy.repository.ArtifactRepositoryFactory factory;
    @Requirement
    private LegacySupport legacySupport;
    @Requirement
    private RepositorySystem repositorySystem;
    public ArtifactRepositoryLayout getLayout( String layoutId )
        throws UnknownRepositoryLayoutException
    {
        return factory.getLayout( layoutId );
    }
    public ArtifactRepository createDeploymentArtifactRepository( String id, String url, String layoutId,
                                                                  boolean uniqueVersion )
        throws UnknownRepositoryLayoutException
    {
        return injectSession( factory.createDeploymentArtifactRepository( id, url, layoutId, uniqueVersion ), false );
    }
    public ArtifactRepository createDeploymentArtifactRepository( String id, String url,
                                                                  ArtifactRepositoryLayout repositoryLayout,
                                                                  boolean uniqueVersion )
    {
        return injectSession( factory.createDeploymentArtifactRepository( id, url, repositoryLayout, uniqueVersion ),
                              false );
    }
    public ArtifactRepository createArtifactRepository( String id, String url, String layoutId,
                                                        ArtifactRepositoryPolicy snapshots,
                                                        ArtifactRepositoryPolicy releases )
        throws UnknownRepositoryLayoutException
    {
        return injectSession( factory.createArtifactRepository( layoutId, url, layoutId, snapshots, releases ), true );
    }
    public ArtifactRepository createArtifactRepository( String id, String url,
                                                        ArtifactRepositoryLayout repositoryLayout,
                                                        ArtifactRepositoryPolicy snapshots,
                                                        ArtifactRepositoryPolicy releases )
    {
        return injectSession( factory.createArtifactRepository( id, url, repositoryLayout, snapshots, releases ), true );
    }
    public void setGlobalUpdatePolicy( String updatePolicy )
    {
        factory.setGlobalUpdatePolicy( updatePolicy );
    }
    public void setGlobalChecksumPolicy( String checksumPolicy )
    {
        factory.setGlobalChecksumPolicy( checksumPolicy );
    }
    private ArtifactRepository injectSession( ArtifactRepository repository, boolean mirrors )
    {
        MavenSession session = legacySupport.getSession();
        if ( session != null && repository != null && !isLocalRepository( repository ) )
        {
            MavenExecutionRequest request = session.getRequest();
            if ( request != null )
            {
                List<ArtifactRepository> repositories = Arrays.asList( repository );
                if ( mirrors )
                {
                    repositorySystem.injectMirror( repositories, request.getMirrors() );
                }
                repositorySystem.injectProxy( repositories, request.getProxies() );
                repositorySystem.injectAuthentication( repositories, request.getServers() );
            }
        }
        return repository;
    }
    private boolean isLocalRepository( ArtifactRepository repository )
    {
        return "local".equals( repository.getId() );
    }
}
