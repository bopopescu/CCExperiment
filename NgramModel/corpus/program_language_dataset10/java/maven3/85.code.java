package org.apache.maven.execution;
import org.apache.maven.artifact.versioning.ArtifactVersion;
@Deprecated
public interface RuntimeInformation
{
    ArtifactVersion getApplicationVersion();
}
