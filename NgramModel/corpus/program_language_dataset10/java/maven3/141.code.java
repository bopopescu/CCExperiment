package org.apache.maven.repository.legacy.resolver;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ResolutionListener;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.repository.legacy.resolver.conflict.ConflictResolver;
@Deprecated
public interface LegacyArtifactCollector
{
    ArtifactResolutionResult collect( Set<Artifact> artifacts, Artifact originatingArtifact, Map managedVersions,
                                      ArtifactResolutionRequest repositoryRequest, ArtifactMetadataSource source,
                                      ArtifactFilter filter, List<ResolutionListener> listeners,
                                      List<ConflictResolver> conflictResolvers );
    ArtifactResolutionResult collect( Set<Artifact> artifacts, Artifact originatingArtifact, Map managedVersions,
                                      ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories,
                                      ArtifactMetadataSource source, ArtifactFilter filter,
                                      List<ResolutionListener> listeners, List<ConflictResolver> conflictResolvers );
    @Deprecated
    ArtifactResolutionResult collect( Set<Artifact> artifacts, Artifact originatingArtifact, Map managedVersions,
                                      ArtifactRepository localRepository, List<ArtifactRepository> remoteRepositories,
                                      ArtifactMetadataSource source, ArtifactFilter filter,
                                      List<ResolutionListener> listeners );
}
