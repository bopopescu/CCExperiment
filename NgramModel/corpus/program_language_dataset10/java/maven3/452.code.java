package org.apache.maven.plugin.prefix;
public interface PluginPrefixResolver
{
    PluginPrefixResult resolve( PluginPrefixRequest request )
        throws NoPluginFoundForPrefixException;
}
