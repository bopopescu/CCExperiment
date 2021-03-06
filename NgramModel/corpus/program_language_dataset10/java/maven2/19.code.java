package org.apache.maven.artifact.metadata;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.metadata.RepositoryMetadataStoreException;
public interface ArtifactMetadata
{
    boolean storedInArtifactVersionDirectory();
    boolean storedInGroupDirectory();
    String getGroupId();
    String getArtifactId();
    String getBaseVersion();
    Object getKey();
    String getLocalFilename( ArtifactRepository repository );
    String getRemoteFilename();
    void merge( ArtifactMetadata metadata );
    void storeInLocalRepository( ArtifactRepository localRepository, ArtifactRepository remoteRepository )
        throws RepositoryMetadataStoreException;
    String extendedToString();
}
