package org.apache.maven.plugin;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryEvent;
import org.codehaus.plexus.component.discovery.ComponentDiscoveryListener;
import org.codehaus.plexus.component.repository.ComponentSetDescriptor;
import java.util.ArrayList;
import java.util.List;
public class MavenPluginValidator
    implements ComponentDiscoveryListener
{
    private final Artifact pluginArtifact;
    private List errors = new ArrayList();
    public MavenPluginValidator( Artifact pluginArtifact )
    {
        this.pluginArtifact = pluginArtifact;
    }
    public void componentDiscovered( ComponentDiscoveryEvent event )
    {
        ComponentSetDescriptor componentSetDescriptor = event.getComponentSetDescriptor();
        if ( componentSetDescriptor instanceof PluginDescriptor )
        {
            PluginDescriptor pluginDescriptor = (PluginDescriptor) componentSetDescriptor;
            if ( !pluginArtifact.getGroupId().equals( pluginDescriptor.getGroupId() ) )
            {
                errors.add( "Plugin's descriptor contains the wrong group ID: " + pluginDescriptor.getGroupId() );
            }
            if ( !pluginArtifact.getArtifactId().equals( pluginDescriptor.getArtifactId() ) )
            {
                errors.add( "Plugin's descriptor contains the wrong artifact ID: " + pluginDescriptor.getArtifactId() );
            }
            if ( !pluginArtifact.getBaseVersion().equals( pluginDescriptor.getVersion() ) )
            {
                errors.add( "Plugin's descriptor contains the wrong version: " + pluginDescriptor.getVersion() );
            }
        }
    }
    public boolean hasErrors()
    {
        return !errors.isEmpty();
    }
    public List getErrors()
    {
        return errors;
    }
}
