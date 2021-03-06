package org.apache.maven.project.inheritance.t10;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import org.apache.maven.model.Build;
import org.apache.maven.model.MailingList;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.inheritance.AbstractProjectInheritanceTestCase;
import org.apache.maven.artifact.Artifact;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.logging.LoggerManager;
import org.codehaus.plexus.logging.Logger;
public class ProjectInheritanceTest
    extends AbstractProjectInheritanceTestCase
{
    public void testDependencyManagementOverridesTransitiveDependencyVersion()
        throws Exception
    {
        File localRepo = getLocalRepositoryPath();
        File pom0 = new File( localRepo, "p0/pom.xml" );
        File pom0Basedir = pom0.getParentFile();
        File pom1 = new File( pom0Basedir, "p1/pom.xml" );
        MavenProject project0 = getProjectWithDependencies( pom0 );
        MavenProject project1 = getProjectWithDependencies( pom1 );
        assertEquals( pom0Basedir, project1.getParent().getBasedir() );
        System.out.println("Project " + project1.getId() + " " + project1);
        Map map = project1.getArtifactMap();
        assertNotNull("No artifacts", map);
        assertTrue("No Artifacts", map.size() > 0);
        assertTrue("Set size should be 3, is " + map.size(), map.size() == 3);
        Artifact a = (Artifact) map.get("maven-test:maven-test-a");
        Artifact b = (Artifact) map.get("maven-test:maven-test-b");
        Artifact c = (Artifact) map.get("maven-test:maven-test-c");
        assertTrue("Incorrect scope for " + a.getDependencyConflictId(), a.getScope().equals("test"));
        assertTrue("Incorrect scope for " + b.getDependencyConflictId(), b.getScope().equals("runtime"));
        assertTrue("Incorrect scope for " + c.getDependencyConflictId(), c.getScope().equals("runtime"));
    }
}