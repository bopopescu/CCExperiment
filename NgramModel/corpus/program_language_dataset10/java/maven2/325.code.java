package org.apache.maven.project;
import org.apache.maven.artifact.Artifact;
import java.io.File;
import java.util.Iterator;
public class ProjectClasspathTest
    extends AbstractMavenProjectTestCase
{
    private String dir = "projects/scope/";
    public void testProjectClasspath()
        throws Exception
    {
        File f = getFileForClasspathResource( dir + "project-with-scoped-dependencies.xml" );
        TestProjectBuilder builder = (TestProjectBuilder) getContainer().lookup( MavenProjectBuilder.ROLE, "test" );
        TestArtifactResolver testArtifactResolver = (TestArtifactResolver) getContainer().lookup( TestArtifactResolver.class.getName() );
        builder.setArtifactResolver( testArtifactResolver );
        builder.setArtifactMetadataSource( testArtifactResolver.source() );
        MavenProject project = getProjectWithDependencies( f );
        Artifact artifact;
        assertNotNull( "Test project can't be null!", project );
        checkArtifactIdScope( project, "provided", "provided" );
        checkArtifactIdScope( project, "test", "test" );
        checkArtifactIdScope( project, "compile", "compile" );
        checkArtifactIdScope( project, "runtime", "runtime" );
        checkArtifactIdScope( project, "default", "compile" );
        artifact = getArtifact( project, "maven-test-test", "scope-provided" );
        assertNull( "Check no provided dependencies are transitive", artifact );
        artifact = getArtifact( project, "maven-test-test", "scope-test" );
        assertNull( "Check no test dependencies are transitive", artifact );
        artifact = getArtifact( project, "maven-test-test", "scope-compile" );
        assertEquals( "Check scope", "test", artifact.getScope() );
        artifact = getArtifact( project, "maven-test-test", "scope-default" );
        assertEquals( "Check scope", "test", artifact.getScope() );
        artifact = getArtifact( project, "maven-test-test", "scope-runtime" );
        assertEquals( "Check scope", "test", artifact.getScope() );
        checkGroupIdScope( project, "provided", "maven-test-provided" );
        artifact = getArtifact( project, "maven-test-provided", "scope-runtime" );
        assertEquals( "Check scope", "provided", artifact.getScope() );
        checkGroupIdScope( project, "runtime", "maven-test-runtime" );
        artifact = getArtifact( project, "maven-test-runtime", "scope-runtime" );
        assertEquals( "Check scope", "runtime", artifact.getScope() );
        checkGroupIdScope( project, "compile", "maven-test-compile" );
        artifact = getArtifact( project, "maven-test-compile", "scope-runtime" );
        assertEquals( "Check scope", "runtime", artifact.getScope() );
        checkGroupIdScope( project, "compile", "maven-test-default" );
        artifact = getArtifact( project, "maven-test-default", "scope-runtime" );
        assertEquals( "Check scope", "runtime", artifact.getScope() );
    }
    private void checkGroupIdScope( MavenProject project, String scopeValue, String groupId )
    {
        Artifact artifact;
        artifact = getArtifact( project, groupId, "scope-compile" );
        assertEquals( "Check scope", scopeValue, artifact.getScope() );
        artifact = getArtifact( project, groupId, "scope-test" );
        assertNull( "Check test dependency is not transitive", artifact );
        artifact = getArtifact( project, groupId, "scope-provided" );
        assertNull( "Check provided dependency is not transitive", artifact );
        artifact = getArtifact( project, groupId, "scope-default" );
        assertEquals( "Check scope", scopeValue, artifact.getScope() );
    }
    private void checkArtifactIdScope( MavenProject project, String scope, String scopeValue )
    {
        String artifactId = "scope-" + scope;
        Artifact artifact = getArtifact( project, "maven-test", artifactId );
        assertEquals( "Check scope", scopeValue, artifact.getScope() );
    }
    private Artifact getArtifact( MavenProject project, String groupId, String artifactId )
    {
        for ( Iterator i = project.getArtifacts().iterator(); i.hasNext(); )
        {
            Artifact a = (Artifact) i.next();
            if ( artifactId.equals( a.getArtifactId() ) && a.getGroupId().equals( groupId ) )
            {
                return a;
            }
        }
        return null;
    }
}
