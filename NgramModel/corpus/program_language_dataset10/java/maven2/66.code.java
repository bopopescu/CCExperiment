package org.apache.maven.artifact;
import org.apache.maven.artifact.handler.ArtifactHandlerMock;
import org.apache.maven.artifact.versioning.VersionRange;
import junit.framework.TestCase;
public class DefaultArtifactTest
    extends TestCase
{
    private DefaultArtifact artifact;
    private DefaultArtifact snapshotArtifact;
    private String groupId = "groupid", artifactId = "artifactId", version = "1.0", scope = "scope", type = "type",
        classifier = "classifier";
    private String snapshotSpecVersion = "1.0-SNAPSHOT";
    private String snapshotResolvedVersion = "1.0-20070606.010101-1";
    private VersionRange versionRange;
    private VersionRange snapshotVersionRange;
    private ArtifactHandlerMock artifactHandler;
    protected void setUp()
        throws Exception
    {
        super.setUp();
        artifactHandler = new ArtifactHandlerMock();
        versionRange = VersionRange.createFromVersion( version );
        artifact = new DefaultArtifact( groupId, artifactId, versionRange, scope, type, classifier, artifactHandler );
        snapshotVersionRange = VersionRange.createFromVersion( snapshotResolvedVersion );
        snapshotArtifact = new DefaultArtifact( groupId, artifactId, snapshotVersionRange, scope, type, classifier, artifactHandler );
    }
    public void testGetVersionReturnsResolvedVersionOnSnapshot()
    {
        assertEquals( snapshotResolvedVersion, snapshotArtifact.getVersion() );
        assertEquals( snapshotSpecVersion, snapshotArtifact.getBaseVersion() );
    }
    public void testGetDependencyConflictId()
    {
        assertEquals( groupId + ":" + artifactId + ":" + type + ":" + classifier, artifact.getDependencyConflictId() );
    }
    public void testGetDependencyConflictIdNullGroupId()
    {
        artifact.setGroupId( null );
        assertEquals( null + ":" + artifactId + ":" + type + ":" + classifier, artifact.getDependencyConflictId() );
    }
    public void testGetDependencyConflictIdNullClassifier()
    {
        artifact = new DefaultArtifact( groupId, artifactId, versionRange, scope, type, null, artifactHandler );
        assertEquals( groupId + ":" + artifactId + ":" + type, artifact.getDependencyConflictId() );
    }
    public void testGetDependencyConflictIdNullScope()
    {
        artifact.setScope( null );
        assertEquals( groupId + ":" + artifactId + ":" + type + ":" + classifier, artifact.getDependencyConflictId() );
    }
    public void testToString()
    {
        assertEquals( groupId + ":" + artifactId + ":" + type + ":" + classifier + ":" + version + ":" + scope,
                      artifact.toString() );
    }
    public void testToStringNullGroupId()
    {
        artifact.setGroupId( null );
        assertEquals( artifactId + ":" + type + ":" + classifier + ":" + version + ":" + scope, artifact.toString() );
    }
    public void testToStringNullClassifier()
    {
        artifact = new DefaultArtifact( groupId, artifactId, versionRange, scope, type, null, artifactHandler );
        assertEquals( groupId + ":" + artifactId + ":" + type + ":" + version + ":" + scope, artifact.toString() );
    }
    public void testToStringNullScope()
    {
        artifact.setScope( null );
        assertEquals( groupId + ":" + artifactId + ":" + type + ":" + classifier + ":" + version, artifact.toString() );
    }
}
