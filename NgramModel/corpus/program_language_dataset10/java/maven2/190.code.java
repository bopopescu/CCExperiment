package org.apache.maven.usability;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.codehaus.plexus.PlexusTestCase;
public class InvalidArtifactDiagnoserTest
    extends PlexusTestCase
{
    private InvalidArtifactDiagnoser diagnoser = new InvalidArtifactDiagnoser();
    public void testShouldDiagnoseArtifactWithMissingGroupId() throws Throwable
    {
        testDiagnosis( "Test diagnosis for missing groupId", null, "test-artifact", "1.0", "jar" );
    }
    public void testShouldDiagnoseArtifactWithMissingArtifactId() throws Throwable
    {
        testDiagnosis( "Test diagnosis for missing artifactId", "test.group.id", null, "1.0", "jar" );
    }
    public void testShouldDiagnoseArtifactWithMissingVersion() throws Throwable
    {
        testDiagnosis( "Test diagnosis for missing version", "test.group.id", "test-artifact", null, "jar" );
    }
    public void testShouldDiagnoseArtifactWithMissingType() throws Throwable
    {
        testDiagnosis( "Test diagnosis for missing type", "test.group.id", "test-artifact", "1.0", null );
    }
    public void testShouldDiagnoseArtifactWithMissingGroupIdAndArtifactId() throws Throwable
    {
        testDiagnosis( "Test diagnosis for missing groupId and artifactId", null, null, "1.0", "jar" );
    }
    private void testDiagnosis( String testHeader, String groupId, String artifactId, String version, String type )
        throws Throwable
    {
        System.out.println( "------------------------------------------------------------" );
        System.out.println( "|  " + testHeader );
        System.out.println( "------------------------------------------------------------" );
        System.out.println();
        try
        {
            createArtifact( groupId, artifactId, version, type );
            fail( "artifact creation did not fail; nothing to diagnose." );
        }
        catch ( Throwable error )
        {
            assertTrue( "Unexpected error while constructing artifact: " + error, diagnoser.canDiagnose( error ) );
            if ( diagnoser.canDiagnose( error ) )
            {
                System.out.println( diagnoser.diagnose( error ) );
            }
            else
            {
                throw error;
            }
        }
    }
    private Artifact createArtifact( String groupId, String artifactId, String version, String type )
        throws Exception
    {
        ArtifactFactory artifactFactory = (ArtifactFactory) lookup( ArtifactFactory.ROLE );
        return artifactFactory.createBuildArtifact( groupId, artifactId, version, type );
    }
}
