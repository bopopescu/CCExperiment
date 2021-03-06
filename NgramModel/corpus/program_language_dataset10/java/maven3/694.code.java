package org.apache.maven.model;
import junit.framework.TestCase;
public class RepositoryTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new Repository().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new Repository().equals( null ) );
        new Repository().equals( new Repository() );
    }
    public void testEqualsIdentity()
    {
        Repository thing = new Repository();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new Repository().toString() );
    }
}
