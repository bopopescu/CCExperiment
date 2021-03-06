package org.apache.maven.model;
import junit.framework.TestCase;
public class DeploymentRepositoryTest
    extends TestCase
{
    public void testHashCodeNullSafe()
    {
        new DeploymentRepository().hashCode();
    }
    public void testEqualsNullSafe()
    {
        assertFalse( new DeploymentRepository().equals( null ) );
        new DeploymentRepository().equals( new DeploymentRepository() );
    }
    public void testEqualsIdentity()
    {
        DeploymentRepository thing = new DeploymentRepository();
        assertTrue( thing.equals( thing ) );
    }
    public void testToStringNullSafe()
    {
        assertNotNull( new DeploymentRepository().toString() );
    }
}
