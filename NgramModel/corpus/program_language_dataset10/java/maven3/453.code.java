package org.apache.maven.plugin.prefix;
import org.sonatype.aether.repository.ArtifactRepository;
public interface PluginPrefixResult
{
    String getGroupId();
    String getArtifactId();
    ArtifactRepository getRepository();
}
