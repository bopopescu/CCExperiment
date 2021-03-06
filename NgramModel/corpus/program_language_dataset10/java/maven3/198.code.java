package org.apache.maven.artifact.resolver.filter;
import java.util.Arrays;
import org.apache.maven.artifact.Artifact;
import junit.framework.TestCase;
public class OrArtifactFilterTest
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
        OrArtifactFilter filter1 = new OrArtifactFilter();
        OrArtifactFilter filter2 = new OrArtifactFilter( Arrays.asList( newSubFilter() ) );
        assertFalse( filter1.equals( null ) );
        assertTrue( filter1.equals( filter1 ) );
        assertEquals( filter1.hashCode(), filter1.hashCode() );
        assertFalse( filter1.equals( filter2 ) );
        assertFalse( filter2.equals( filter1 ) );
    }
}
