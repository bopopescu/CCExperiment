package org.apache.maven.model;
import junit.framework.TestCase;
public class ExtensionTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Extension().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Extension().equals( null ) );
        new Extension().equals( new Extension() );
    }
    public void testEqualsIdentity()
    {
        Extension thing = new Extension();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Extension().toString() );
    }
}
