package org.apache.maven.model;
import junit.framework.TestCase;
public class PluginContainerTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new PluginContainer().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new PluginContainer().equals( null ) );
        new PluginContainer().equals( new PluginContainer() );
    }
    public void testEqualsIdentity()
    {
        PluginContainer thing = new PluginContainer();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new PluginContainer().toString() );
    }
}
