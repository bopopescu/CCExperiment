package org.apache.maven.artifact.repository.metadata;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
public class SnapshotArtifactRepositoryMetadata
    extends AbstractRepositoryMetadata
{
    private Artifact artifact;
    public SnapshotArtifactRepositoryMetadata( Artifact artifact )
    {
        super( createMetadata( artifact, null ) );
        this.artifact = artifact;
    }
    public SnapshotArtifactRepositoryMetadata( Artifact artifact,
                                               Snapshot snapshot )
    {
        super( createMetadata( artifact, createVersioning( snapshot ) ) );
        this.artifact = artifact;
    }
    public boolean storedInGroupDirectory()
    {
        return false;
    }
    public boolean storedInArtifactVersionDirectory()
    {
        return true;
    }
    public String getGroupId()
    {
        return artifact.getGroupId();
    }
    public String getArtifactId()
    {
        return artifact.getArtifactId();
    }
    public String getBaseVersion()
    {
        return artifact.getBaseVersion();
    }
    public Object getKey()
    {
        return "snapshot " + artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getBaseVersion();
    }
    public boolean isSnapshot()
    {
        return artifact.isSnapshot();
    }
    public int getNature()
    {
        return isSnapshot() ? SNAPSHOT : RELEASE;
    }
    public ArtifactRepository getRepository()
    {
        return artifact.getRepository();
    }
    public void setRepository( ArtifactRepository remoteRepository )
    {
        artifact.setRepository( remoteRepository );
    }
}
