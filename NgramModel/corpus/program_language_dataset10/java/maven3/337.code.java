package org.apache.maven.execution;
import org.apache.maven.project.MavenProject;
public class BuildSuccess
    extends BuildSummary
{
    public BuildSuccess( MavenProject project, long time )
    {
        super( project, time );
    }
}
