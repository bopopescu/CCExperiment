package org.apache.maven.artifact.deployer;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataDeploymentException;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataManager;
import org.apache.maven.artifact.transform.ArtifactTransformationManager;
import org.apache.maven.wagon.TransferFailedException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.FileUtils;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
public class DefaultArtifactDeployer
    extends AbstractLogEnabled
    implements ArtifactDeployer
{
    private WagonManager wagonManager;
    private ArtifactTransformationManager transformationManager;
    private RepositoryMetadataManager repositoryMetadataManager;
    public void deploy( String basedir, String finalName, Artifact artifact, ArtifactRepository deploymentRepository,
                        ArtifactRepository localRepository )
        throws ArtifactDeploymentException
    {
        String extension = artifact.getArtifactHandler().getExtension();
        File source = new File( basedir, finalName + "." + extension );
        deploy( source, artifact, deploymentRepository, localRepository );
    }
    public void deploy( File source, Artifact artifact, ArtifactRepository deploymentRepository,
                        ArtifactRepository localRepository )
        throws ArtifactDeploymentException
    {
        if ( !wagonManager.isOnline() )
        {
            throw new ArtifactDeploymentException( "System is offline. Cannot deploy artifact: " + artifact + "." );
        }
        boolean useArtifactFile = false;
        File oldArtifactFile = artifact.getFile();
        if ( "pom".equals( artifact.getType() ) )
        {
            artifact.setFile( source );
            useArtifactFile = true;
        }
        try
        {
            transformationManager.transformForDeployment( artifact, deploymentRepository, localRepository );
            if ( useArtifactFile )
            {
                source = artifact.getFile();
                artifact.setFile( oldArtifactFile );
            }
            File artifactFile = new File( localRepository.getBasedir(), localRepository.pathOf( artifact ) );
            if ( !artifactFile.equals( source ) )
            {
                FileUtils.copyFile( source, artifactFile );
            }
            wagonManager.putArtifact( source, artifact, deploymentRepository );
            for ( Iterator i = artifact.getMetadataList().iterator(); i.hasNext(); )
            {
                ArtifactMetadata metadata = (ArtifactMetadata) i.next();
                repositoryMetadataManager.deploy( metadata, localRepository, deploymentRepository );
            }
        }
        catch ( TransferFailedException e )
        {
            throw new ArtifactDeploymentException( "Error deploying artifact: " + e.getMessage(), e );
        }
        catch ( IOException e )
        {
            throw new ArtifactDeploymentException( "Error deploying artifact: " + e.getMessage(), e );
        }
        catch ( RepositoryMetadataDeploymentException e )
        {
            throw new ArtifactDeploymentException( "Error installing artifact's metadata: " + e.getMessage(), e );
        }
    }
}
