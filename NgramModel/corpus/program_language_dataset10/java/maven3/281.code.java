package org.apache.maven.artifact.metadata;
import org.apache.maven.artifact.Artifact;
@Deprecated
public class ArtifactMetadataRetrievalException
    extends org.apache.maven.repository.legacy.metadata.ArtifactMetadataRetrievalException
{
    public ArtifactMetadataRetrievalException( String message, Throwable cause, Artifact artifact )
    {
        super( message, cause, artifact );
    }
}
