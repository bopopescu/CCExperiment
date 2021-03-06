package org.apache.maven.plugin;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.component.repository.exception.ComponentRepositoryException;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
public class PluginContainerException
    extends PluginManagerException
{
    private ClassRealm pluginRealm;
    public PluginContainerException( MojoDescriptor mojoDescriptor, ClassRealm pluginRealm, String message, Throwable e )
    {
        super( mojoDescriptor, message, e );
        this.pluginRealm = pluginRealm;
    }
    public PluginContainerException( MojoDescriptor mojoDescriptor, ClassRealm pluginRealm, String message,
                                     ComponentLookupException e )
    {
        super( mojoDescriptor, message, e );
        this.pluginRealm = pluginRealm;
    }
    public PluginContainerException( Plugin plugin, ClassRealm pluginRealm, String message, Throwable e )
    {
        super( plugin, message, e );
        this.pluginRealm = pluginRealm;
    }
    public PluginContainerException( Plugin plugin, ClassRealm pluginRealm, String message,
                                     PlexusConfigurationException e )
    {
        super( plugin, message, e );
        this.pluginRealm = pluginRealm;
    }
    public PluginContainerException( Plugin plugin, ClassRealm pluginRealm, String message,
                                     ComponentRepositoryException e )
    {
        super( plugin, message, e );
        this.pluginRealm = pluginRealm;
    }
    public ClassRealm getPluginRealm()
    {
        return pluginRealm;
    }
}
