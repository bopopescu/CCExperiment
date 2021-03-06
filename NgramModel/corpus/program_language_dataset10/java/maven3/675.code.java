package org.apache.maven.model;
import junit.framework.TestCase;
public class IssueManagementTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new IssueManagement().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new IssueManagement().equals( null ) );
        new IssueManagement().equals( new IssueManagement() );
    }
    public void testEqualsIdentity()
    {
        IssueManagement thing = new IssueManagement();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new IssueManagement().toString() );
    }
}
