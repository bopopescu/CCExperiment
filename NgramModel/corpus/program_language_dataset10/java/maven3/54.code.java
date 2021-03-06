package org.apache.maven.artifact.deployer;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.DefaultArtifactRepository;
import org.apache.maven.artifact.repository.LegacyLocalRepositoryManager;
import org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.MetadataBridge;
import org.apache.maven.artifact.repository.metadata.SnapshotArtifactRepositoryMetadata;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.deployment.DeployRequest;
import org.sonatype.aether.deployment.DeployResult;
import org.sonatype.aether.deployment.DeploymentException;
import org.sonatype.aether.metadata.MergeableMetadata;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.util.artifact.SubArtifact;
@Component( role = ArtifactDeployer.class, instantiationStrategy = "per-lookup" )
public class DefaultArtifactDeployer
    extends AbstractLogEnabled
    implements ArtifactDeployer
{
    @Requirement
    private RepositorySystem repoSystem;
    @Requirement
    private LegacySupport legacySupport;
    private Map<Object, MergeableMetadata> relatedMetadata = new ConcurrentHashMap<Object, MergeableMetadata>();
    @Deprecated
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
        DefaultRepositorySystemSession session =
            new DefaultRepositorySystemSession( legacySupport.getRepositorySession() );
        session.setLocalRepositoryManager( LegacyLocalRepositoryManager.wrap( localRepository, repoSystem ) );
        DeployRequest request = new DeployRequest();
        org.sonatype.aether.artifact.Artifact mainArtifact = RepositoryUtils.toArtifact( artifact );
        mainArtifact = mainArtifact.setFile( source );
        request.addArtifact( mainArtifact );
        String versionKey = artifact.getGroupId() + ':' + artifact.getArtifactId();
        String snapshotKey = null;
        if ( artifact.isSnapshot() )
        {
            snapshotKey = versionKey + ':' + artifact.getBaseVersion();
            request.addMetadata( relatedMetadata.get( snapshotKey ) );
        }
        request.addMetadata( relatedMetadata.get( versionKey ) );
        for ( ArtifactMetadata metadata : artifact.getMetadataList() )
        {
            if ( metadata instanceof ProjectArtifactMetadata )
            {
                org.sonatype.aether.artifact.Artifact pomArtifact = new SubArtifact( mainArtifact, "", "pom" );
                pomArtifact = pomArtifact.setFile( ( (ProjectArtifactMetadata) metadata ).getFile() );
                request.addArtifact( pomArtifact );
            }
            else if ( metadata instanceof SnapshotArtifactRepositoryMetadata
                || metadata instanceof ArtifactRepositoryMetadata )
            {
            }
            else
            {
                request.addMetadata( new MetadataBridge( metadata ) );
            }
        }
        RemoteRepository remoteRepo = RepositoryUtils.toRepo( deploymentRepository );
        if ( deploymentRepository instanceof DefaultArtifactRepository
            && deploymentRepository.getAuthentication() == null )
        {
            remoteRepo.setAuthentication( session.getAuthenticationSelector().getAuthentication( remoteRepo ) );
            remoteRepo.setProxy( session.getProxySelector().getProxy( remoteRepo ) );
        }
        request.setRepository( remoteRepo );
        DeployResult result;
        try
        {
            result = repoSystem.deploy( session, request );
        }
        catch ( DeploymentException e )
        {
            throw new ArtifactDeploymentException( e.getMessage(), e );
        }
        for ( Object metadata : result.getMetadata() )
        {
            if ( metadata.getClass().getName().endsWith( ".internal.VersionsMetadata" ) )
            {
                relatedMetadata.put( versionKey, (MergeableMetadata) metadata );
            }
            if ( snapshotKey != null && metadata.getClass().getName().endsWith( ".internal.RemoteSnapshotMetadata" ) )
            {
                relatedMetadata.put( snapshotKey, (MergeableMetadata) metadata );
            }
        }
        artifact.setResolvedVersion( result.getArtifacts().iterator().next().getVersion() );
    }
}
