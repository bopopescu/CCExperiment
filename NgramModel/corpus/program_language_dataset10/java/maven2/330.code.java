package org.apache.maven.project.canonical;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.AbstractMavenProjectTestCase;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import java.io.File;
import java.util.Iterator;
import java.util.List;
public class CanonicalProjectBuilderTest
    extends AbstractMavenProjectTestCase
{
    public void testProjectBuilder()
        throws Exception
    {
        File f = getFileForClasspathResource( "canonical-pom.xml" );
        MavenProject project = getProject( f );
        assertEquals( "4.0.0", project.getModelVersion() );
        List plugins = project.getBuildPlugins();
        String key = "org.apache.maven.plugins:maven-plexus-plugin";
        Plugin plugin = null;
        for ( Iterator it = plugins.iterator(); it.hasNext(); )
        {
            Plugin check = (Plugin) it.next();
            if ( key.equals( check.getKey() ) )
            {
                plugin = check;
                break;
            }
        }
        assertNotNull( plugin );
        assertEquals( "1.0", plugin.getVersion() );
        Xpp3Dom configuration = (Xpp3Dom) plugin.getConfiguration();
        assertEquals( "src/conf/plexus.conf", configuration.getChild( "plexusConfiguration" ).getValue() );
        assertEquals( "src/conf/plexus.properties",
                      configuration.getChild( "plexusConfigurationPropertiesFile" ).getValue() );
        assertEquals( "Continuum", configuration.getChild( "plexusApplicationName" ).getValue() );
        List executions = plugin.getExecutions();
        PluginExecution execution = (PluginExecution) executions.get( 0 );
        String g0 = (String) execution.getGoals().get( 0 );
        assertEquals( "plexus:runtime", g0 );
        configuration = (Xpp3Dom) execution.getConfiguration();
        assertEquals( "ContinuumPro", configuration.getChild( "plexusApplicationName" ).getValue() );
    }
}
