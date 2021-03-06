package org.apache.maven.execution;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
public interface ExecutionEvent
{
    enum Type
    {
        ProjectDiscoveryStarted,
        SessionStarted,
        SessionEnded,
        ProjectSkipped,
        ProjectStarted,
        ProjectSucceeded,
        ProjectFailed,
        MojoSkipped,
        MojoStarted,
        MojoSucceeded,
        MojoFailed,
        ForkStarted,
        ForkSucceeded,
        ForkFailed,
        ForkedProjectStarted,
        ForkedProjectSucceeded,
        ForkedProjectFailed,
    }
    Type getType();
    MavenSession getSession();
    MavenProject getProject();
    MojoExecution getMojoExecution();
    Exception getException();
}
