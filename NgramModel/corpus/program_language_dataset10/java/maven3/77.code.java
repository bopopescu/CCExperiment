package org.apache.maven.artifact.resolver;
import org.apache.maven.artifact.Artifact;
@Deprecated
public interface ResolutionListenerForDepMgmt
{
    void manageArtifactVersion( Artifact artifact,
                                Artifact replacement );
    void manageArtifactScope( Artifact artifact,
                              Artifact replacement );
    void manageArtifactSystemPath( Artifact artifact,
                                   Artifact replacement );
}