package org.apache.maven.plugin.descriptor;
public class DuplicateMojoDescriptorException
    extends InvalidPluginDescriptorException
{
    public DuplicateMojoDescriptorException( String goalPrefix, String goal, String existingImplementation, String newImplementation )
    {
        super( "Goal: " + goal + " already exists in the plugin descriptor for prefix: " + goalPrefix + "\nExisting implementation is: " + existingImplementation + "\nConflicting implementation is: " + newImplementation );
    }
}
