package org.apache.maven.artifact.metadata;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
@Deprecated
public class ResolutionGroup
    extends org.apache.maven.repository.legacy.metadata.ResolutionGroup
{
    public ResolutionGroup( Artifact pomArtifact, Set<Artifact> artifacts,
                            List<ArtifactRepository> resolutionRepositories )
    {
        super( pomArtifact, artifacts, resolutionRepositories );
    }
    public ResolutionGroup( Artifact pomArtifact, Artifact relocatedArtifact, Set<Artifact> artifacts,
                            Map<String, Artifact> managedVersions, List<ArtifactRepository> resolutionRepositories )
    {
        super( pomArtifact, relocatedArtifact, artifacts, managedVersions, resolutionRepositories );
    }
}
