package org.apache.maven.execution;
import java.util.List;
import org.apache.maven.project.MavenProject;
public interface ProjectDependencyGraph
{
    List<MavenProject> getSortedProjects();
    List<MavenProject> getDownstreamProjects( MavenProject project, boolean transitive );
    List<MavenProject> getUpstreamProjects( MavenProject project, boolean transitive );
}
