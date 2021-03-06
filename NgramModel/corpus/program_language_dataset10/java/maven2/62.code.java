package org.apache.maven.artifact.versioning;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import java.util.List;
public class OverConstrainedVersionException
    extends ArtifactResolutionException
{
    public OverConstrainedVersionException( String msg, Artifact artifact )
    {
        super( msg, artifact );
    }
    public OverConstrainedVersionException( String msg, Artifact artifact, List remoteRepositories )
    {
        super( msg, artifact, remoteRepositories );
    }
}
