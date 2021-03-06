package org.apache.maven.artifact.metadata;
import org.apache.maven.artifact.Artifact;
public class ArtifactMetadataRetrievalException
    extends Exception
{
    private Artifact artifact;
    public ArtifactMetadataRetrievalException( String message )
    {
        this( message, null, null );
    }
    public ArtifactMetadataRetrievalException( Throwable cause )
    {
        this( null, cause, null );
    }
    public ArtifactMetadataRetrievalException( String message, Throwable cause )
    {
        this( message, cause, null );
    }
    public ArtifactMetadataRetrievalException( String message, Throwable cause, Artifact artifact )
    {
        super( message, cause );
        this.artifact = artifact;
    }
    public Artifact getArtifact()
    {
        return artifact;
    }
}
