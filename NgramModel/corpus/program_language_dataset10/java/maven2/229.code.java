package org.apache.maven.model;
import junit.framework.TestCase;
public class ReportSetTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new ReportSet().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new ReportSet().equals( null ) );
        new ReportSet().equals( new ReportSet() );
    }
    public void testEqualsIdentity()
    {
        ReportSet thing = new ReportSet();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new ReportSet().toString() );
    }
}
