package org.apache.maven.artifact.resolver.filter;
import java.util.Arrays;
import org.apache.maven.artifact.Artifact;
import junit.framework.TestCase;
public class AndArtifactFilterTest
    extends TestCase
{
    private ArtifactFilter newSubFilter()
    {
        return new ArtifactFilter()
        {
            public boolean include( Artifact artifact )
            {
                return false;
            }
        };
    }
    public void testEquals()
    {
        AndArtifactFilter filter1 = new AndArtifactFilter();
        AndArtifactFilter filter2 = new AndArtifactFilter( Arrays.asList( newSubFilter() ) );
        assertFalse( filter1.equals( null ) );
        assertTrue( filter1.equals( filter1 ) );
        assertEquals( filter1.hashCode(), filter1.hashCode() );
        assertFalse( filter1.equals( filter2 ) );
        assertFalse( filter2.equals( filter1 ) );
    }
}
