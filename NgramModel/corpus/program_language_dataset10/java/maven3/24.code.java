package org.apache.maven.artifact.metadata;
@Deprecated
public interface ArtifactMetadata
    extends org.apache.maven.repository.legacy.metadata.ArtifactMetadata
{
    void merge( ArtifactMetadata metadata );    
}
