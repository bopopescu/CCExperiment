package org.apache.maven.repository.legacy.resolver.transform;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.installer.ArtifactInstallationException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.RepositoryRequest;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
@Component( role = ArtifactTransformationManager.class )
public class DefaultArtifactTransformationManager
    implements ArtifactTransformationManager
{
    @Requirement( role = ArtifactTransformation.class, hints = { "release", "latest", "snapshot" } )
    private List<ArtifactTransformation> artifactTransformations;
    public void transformForResolve( Artifact artifact, RepositoryRequest request )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        for ( ArtifactTransformation transform : artifactTransformations )
        {
            transform.transformForResolve( artifact, request );
        }
    }
    public void transformForResolve( Artifact artifact, List<ArtifactRepository> remoteRepositories,
                                     ArtifactRepository localRepository )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        for ( ArtifactTransformation transform : artifactTransformations )
        {
            transform.transformForResolve( artifact, remoteRepositories, localRepository );
        }
    }
    public void transformForInstall( Artifact artifact, ArtifactRepository localRepository )
        throws ArtifactInstallationException
    {
        for ( ArtifactTransformation transform : artifactTransformations )
        {
            transform.transformForInstall( artifact, localRepository );
        }
    }
    public void transformForDeployment( Artifact artifact, ArtifactRepository remoteRepository,
                                        ArtifactRepository localRepository )
        throws ArtifactDeploymentException
    {
        for ( ArtifactTransformation transform : artifactTransformations )
        {
            transform.transformForDeployment( artifact, remoteRepository, localRepository );
        }
    }
    public List getArtifactTransformations()
    {
        return artifactTransformations;
    }
}
