package org.apache.maven.plugin.descriptor;
import org.codehaus.plexus.configuration.PlexusConfigurationException;
public class InvalidPluginDescriptorException
    extends PlexusConfigurationException
{
    public InvalidPluginDescriptorException( String message, Throwable cause )
    {
        super( message, cause );
    }
    public InvalidPluginDescriptorException( String message )
    {
        super( message );
    }
}
