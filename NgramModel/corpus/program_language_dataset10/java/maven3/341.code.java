package org.apache.maven.execution;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;
public class DefaultMavenExecutionResult
    implements MavenExecutionResult
{
    private MavenProject project;
    private List<MavenProject> topologicallySortedProjects = Collections.emptyList();
    private DependencyResolutionResult dependencyResolutionResult;
    private List<Throwable> exceptions;
    private Map<MavenProject, BuildSummary> buildSummaries;
    public MavenExecutionResult setProject( MavenProject project )
    {
        this.project = project;
        return this;
    }
    public MavenProject getProject()
    {
        return project;
    }
    public MavenExecutionResult setTopologicallySortedProjects( List<MavenProject> topologicallySortedProjects )
    {
        this.topologicallySortedProjects = topologicallySortedProjects;
        return this;
    }
    public List<MavenProject> getTopologicallySortedProjects()
    {
        return null == topologicallySortedProjects ? Collections.<MavenProject> emptyList() : topologicallySortedProjects;
    }
    public DependencyResolutionResult getDependencyResolutionResult()
    {
        return dependencyResolutionResult;
    }
    public MavenExecutionResult setDependencyResolutionResult( DependencyResolutionResult dependencyResolutionResult )
    {
        this.dependencyResolutionResult = dependencyResolutionResult;
        return this;
    }
    public List<Throwable> getExceptions()
    {
        return exceptions == null ? Collections.<Throwable> emptyList() : exceptions;
    }
    public MavenExecutionResult addException( Throwable t )
    {
        if ( exceptions == null )
        {
            exceptions = new ArrayList<Throwable>();
        }
        exceptions.add( t );
        return this;
    }
    public boolean hasExceptions()
    {
        return !getExceptions().isEmpty();
    }
    public BuildSummary getBuildSummary( MavenProject project )
    {
        return ( buildSummaries != null ) ? buildSummaries.get( project ) : null;
    }
    public void addBuildSummary( BuildSummary summary )
    {
        if ( buildSummaries == null )
        {
            buildSummaries = new IdentityHashMap<MavenProject, BuildSummary>();
        }
        buildSummaries.put( summary.getProject(), summary );
    }
}
