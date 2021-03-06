package org.apache.maven.artifact.installer;
import java.io.File;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.LegacyLocalRepositoryManager;
import org.apache.maven.artifact.repository.metadata.ArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.MetadataBridge;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotArtifactRepositoryMetadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.plugin.LegacySupport;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.sonatype.aether.RepositorySystem;
import org.sonatype.aether.installation.InstallRequest;
import org.sonatype.aether.installation.InstallationException;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.util.DefaultRepositorySystemSession;
import org.sonatype.aether.util.artifact.SubArtifact;
@Component( role = ArtifactInstaller.class )
public class DefaultArtifactInstaller
    extends AbstractLogEnabled
    implements ArtifactInstaller
{
    @Requirement
    private RepositorySystem repoSystem;
    @Requirement
    private LegacySupport legacySupport;
    @Deprecated
    public void install( String basedir, String finalName, Artifact artifact, ArtifactRepository localRepository )
        throws ArtifactInstallationException
    {
        String extension = artifact.getArtifactHandler().getExtension();
        File source = new File( basedir, finalName + "." + extension );
        install( source, artifact, localRepository );
    }
    public void install( File source, Artifact artifact, ArtifactRepository localRepository )
        throws ArtifactInstallationException
    {
        DefaultRepositorySystemSession session =
            new DefaultRepositorySystemSession( legacySupport.getRepositorySession() );
        session.setLocalRepositoryManager( LegacyLocalRepositoryManager.wrap( localRepository, repoSystem ) );
        InstallRequest request = new InstallRequest();
        org.sonatype.aether.artifact.Artifact mainArtifact = RepositoryUtils.toArtifact( artifact );
        mainArtifact = mainArtifact.setFile( source );
        request.addArtifact( mainArtifact );
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
        try
        {
            repoSystem.install( session, request );
        }
        catch ( InstallationException e )
        {
            throw new ArtifactInstallationException( e.getMessage(), e );
        }
        if ( artifact.isSnapshot() )
        {
            Snapshot snapshot = new Snapshot();
            snapshot.setLocalCopy( true );
            artifact.addMetadata( new SnapshotArtifactRepositoryMetadata( artifact, snapshot ) );
        }
        Versioning versioning = new Versioning();
        versioning.updateTimestamp();
        versioning.addVersion( artifact.getBaseVersion() );
        if ( artifact.isRelease() )
        {
            versioning.setRelease( artifact.getBaseVersion() );
        }
        artifact.addMetadata( new ArtifactRepositoryMetadata( artifact, versioning ) );
    }
}
