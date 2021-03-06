package org.apache.maven.artifact;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.maven.artifact.versioning.VersionRange;
import junit.framework.TestCase;
public class ArtifactUtilsTest
    extends TestCase
{
    private Artifact newArtifact( String aid )
    {
        return new DefaultArtifact( "group", aid, VersionRange.createFromVersion( "1.0" ), "test", "jar", "tests", null );
    }
    public void testIsSnapshot()
    {
        assertEquals( false, ArtifactUtils.isSnapshot( null ) );
        assertEquals( false, ArtifactUtils.isSnapshot( "" ) );
        assertEquals( false, ArtifactUtils.isSnapshot( "1.2.3" ) );
        assertEquals( true, ArtifactUtils.isSnapshot( "1.2.3-SNAPSHOT" ) );
        assertEquals( true, ArtifactUtils.isSnapshot( "1.2.3-snapshot" ) );
        assertEquals( true, ArtifactUtils.isSnapshot( "1.2.3-20090413.094722-2" ) );
    }
    public void testToSnapshotVersion()
    {
        assertEquals( "1.2.3", ArtifactUtils.toSnapshotVersion( "1.2.3" ) );
        assertEquals( "1.2.3-SNAPSHOT", ArtifactUtils.toSnapshotVersion( "1.2.3-SNAPSHOT" ) );
        assertEquals( "1.2.3-SNAPSHOT", ArtifactUtils.toSnapshotVersion( "1.2.3-20090413.094722-2" ) );
    }
    public void testArtifactMapByVersionlessIdOrdering()
        throws Exception
    {
        List<Artifact> list = new ArrayList<Artifact>();
        list.add( newArtifact( "b" ) );
        list.add( newArtifact( "a" ) );
        list.add( newArtifact( "c" ) );
        list.add( newArtifact( "e" ) );
        list.add( newArtifact( "d" ) );
        Map<String, Artifact> map = ArtifactUtils.artifactMapByVersionlessId( list );
        assertNotNull( map );
        assertEquals( list, new ArrayList<Artifact>( map.values() ) );
    }
}
