package org.apache.maven.lifecycle.internal.stub;
import org.apache.maven.plugin.version.PluginVersionRequest;
import org.apache.maven.plugin.version.PluginVersionResolutionException;
import org.apache.maven.plugin.version.PluginVersionResolver;
import org.apache.maven.plugin.version.PluginVersionResult;
import org.sonatype.aether.repository.ArtifactRepository;
public class PluginVersionResolverStub
    implements PluginVersionResolver
{
    public PluginVersionResult resolve( PluginVersionRequest request )
        throws PluginVersionResolutionException
    {
        return new PluginVersionResult()
        {
            public String getVersion()
            {
                return "0.42";
            }
            public ArtifactRepository getRepository()
            {
                return null;
            }
        };
    }
}
