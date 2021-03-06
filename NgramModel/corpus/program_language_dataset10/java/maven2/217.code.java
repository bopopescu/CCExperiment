package org.apache.maven.model;
import junit.framework.TestCase;
public class OrganizationTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Organization().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Organization().equals( null ) );
        new Organization().equals( new Organization() );
    }
    public void testEqualsIdentity()
    {
        Organization thing = new Organization();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Organization().toString() );
    }
}
