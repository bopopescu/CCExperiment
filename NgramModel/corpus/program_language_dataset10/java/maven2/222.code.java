package org.apache.maven.model;
import junit.framework.TestCase;
public class PluginManagementTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new PluginManagement().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new PluginManagement().equals( null ) );
        new PluginManagement().equals( new PluginManagement() );
    }
    public void testEqualsIdentity()
    {
        PluginManagement thing = new PluginManagement();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new PluginManagement().toString() );
    }
}
