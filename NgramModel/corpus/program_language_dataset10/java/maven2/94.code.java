package org.apache.maven.artifact.transform;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataResolutionException;
import org.apache.maven.artifact.repository.metadata.SnapshotArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import java.util.List;
public abstract class AbstractVersionTransformation
    extends AbstractLogEnabled
    implements ArtifactTransformation
{
    protected RepositoryMetadataManager repositoryMetadataManager;
    protected WagonManager wagonManager;
    protected String resolveVersion( Artifact artifact, ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories )
        throws RepositoryMetadataResolutionException
    {
        RepositoryMetadata metadata;
        if ( !artifact.isSnapshot() || Artifact.LATEST_VERSION.equals( artifact.getBaseVersion() ) )
        {
            metadata = new ArtifactRepositoryMetadata( artifact );
        }
        else
        {
            metadata = new SnapshotArtifactRepositoryMetadata( artifact );
        }
        repositoryMetadataManager.resolve( metadata, remoteRepositories, localRepository );
        artifact.addMetadata( metadata );
        Metadata repoMetadata = metadata.getMetadata();
        String version = null;
        if ( repoMetadata != null && repoMetadata.getVersioning() != null )
        {
            version = constructVersion( repoMetadata.getVersioning(), artifact.getBaseVersion() );
        }
        if ( version == null )
        {
            version = artifact.getBaseVersion();
        }
        if ( getLogger().isDebugEnabled() )
        {
            if ( !version.equals( artifact.getBaseVersion() ) )
            {
                String message = artifact.getArtifactId() + ": resolved to version " + version;
                if ( artifact.getRepository() != null )
                {
                    message += " from repository " + artifact.getRepository().getId();
                }
                else
                {
                    message += " from local repository";
                }
                getLogger().debug( message );
            }
            else
            {
                getLogger().debug( artifact.getArtifactId() + ": using locally installed snapshot" );
            }
        }
        return version;
    }
    protected abstract String constructVersion( Versioning versioning, String baseVersion );
}
