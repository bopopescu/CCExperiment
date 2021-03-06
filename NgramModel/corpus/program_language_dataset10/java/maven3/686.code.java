package org.apache.maven.model;
import junit.framework.TestCase;
public class PluginTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Plugin().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Plugin().equals( null ) );
        new Plugin().equals( new Plugin() );
    }
    public void testEqualsIdentity()
    {
        Plugin thing = new Plugin();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Plugin().toString() );
    }
}
