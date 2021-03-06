package org.apache.maven.settings.building;
import java.io.File;
import junit.framework.TestCase;
public class DefaultSettingsBuilderFactoryTest
    extends TestCase
{
    private File getSettings( String name )
    {
        return new File( "src/test/resources/settings/factory/" + name + ".xml" ).getAbsoluteFile();
    }
    public void testCompleteWiring()
        throws Exception
    {
        SettingsBuilder builder = new DefaultSettingsBuilderFactory().newInstance();
        assertNotNull( builder );
        DefaultSettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
        request.setSystemProperties( System.getProperties() );
        request.setUserSettingsFile( getSettings( "simple" ) );
        SettingsBuildingResult result = builder.build( request );
        assertNotNull( result );
        assertNotNull( result.getEffectiveSettings() );
    }
}
