package org.apache.maven.toolchain;
import java.util.HashMap;
import java.util.Map;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.apache.maven.toolchain.model.ToolchainModel;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
@Component( role = ToolchainManager.class )
public class DefaultToolchainManager
    implements ToolchainManager
{
    @Requirement
    Logger logger;
    @Requirement( role = ToolchainFactory.class )
    Map<String, ToolchainFactory> factories;
    public Toolchain getToolchainFromBuildContext( String type, MavenSession session )
    {
        Map<String, Object> context = retrieveContext( session );
        ToolchainModel model = (ToolchainModel) context.get( getStorageKey( type ) );
        if ( model != null )
        {
            try
            {
                ToolchainFactory fact = factories.get( type );
                if ( fact != null )
                {
                    return fact.createToolchain( model );
                }
                else
                {
                    logger.error( "Missing toolchain factory for type: " + type
                        + ". Possibly caused by misconfigured project." );
                }
            }
            catch ( MisconfiguredToolchainException ex )
            {
                logger.error( "Misconfigured toolchain.", ex );
            }
        }
        return null;
    }
    Map<String, Object> retrieveContext( MavenSession session )
    {
        Map<String, Object> context = null;
        if ( session != null )
        {
            PluginDescriptor desc = new PluginDescriptor();
            desc.setGroupId( PluginDescriptor.getDefaultPluginGroupId() );
            desc.setArtifactId( PluginDescriptor.getDefaultPluginArtifactId( "toolchains" ) );
            MavenProject current = session.getCurrentProject();
            if ( current != null )
            {
                context = session.getPluginContext( desc, current );
            }
        }
        return ( context != null ) ? context : new HashMap<String, Object>();
    }
    public static final String getStorageKey( String type )
    {
        return "toolchain-" + type; 
    }
}
