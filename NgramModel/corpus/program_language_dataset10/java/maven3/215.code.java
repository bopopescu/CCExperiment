package org.apache.maven.project.inheritance.t00;
import org.apache.maven.model.MailingList;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.inheritance.AbstractProjectInheritanceTestCase;
public class ProjectInheritanceTest
    extends AbstractProjectInheritanceTestCase
{
    public void testProjectInheritance()
        throws Exception
    {
        MavenProject p4 = getProject( projectFile( "p4" ) );
        assertEquals( "p4", p4.getName() );
        assertEquals( "2000", p4.getInceptionYear() );
        assertEquals( "mailing-list", ( (MailingList) p4.getMailingLists().get( 0 ) ).getName() );
        assertEquals( "scm-url/p2/p3/p4", p4.getScm().getUrl() );
        assertEquals( "Codehaus", p4.getOrganization().getName() );
        assertEquals( "4.0.0", p4.getModelVersion() );
        assertEquals( "4.0.0", p4.getModelVersion() );
    }
}
