package org.apache.maven.lifecycle;
import org.apache.maven.lifecycle.internal.BuilderCommon;
import org.apache.maven.lifecycle.internal.ExecutionPlanItem;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.project.MavenProject;
import java.util.ArrayList;
import java.util.List;
public class DefaultSchedules
{
    List<Scheduling> schedules;
    @SuppressWarnings( { "UnusedDeclaration" } )
    public DefaultSchedules()
    {
    }
    public DefaultSchedules( List<Scheduling> schedules )
    {
        this.schedules = schedules;
    }
    public List<ExecutionPlanItem> createExecutionPlanItem( MavenProject mavenProject, List<MojoExecution> executions )
    {
        BuilderCommon.attachToThread( mavenProject );
        List<ExecutionPlanItem> result = new ArrayList<ExecutionPlanItem>();
        for ( MojoExecution mojoExecution : executions )
        {
            String lifeCyclePhase = mojoExecution.getLifecyclePhase();
            final Scheduling scheduling = getScheduling( "default" );
            Schedule schedule = null;
            if ( scheduling != null )
            {
                schedule = scheduling.getSchedule( mojoExecution );
                if ( schedule == null )
                {
                    schedule = scheduling.getSchedule( lifeCyclePhase );
                }
            }
            result.add( new ExecutionPlanItem( mojoExecution, schedule ) );
        }
        return result;
    }
    Scheduling getScheduling( String lifecyclePhaseName )
    {
        for ( Scheduling schedule : schedules )
        {
            if ( lifecyclePhaseName.equals( schedule.getLifecycle() ) )
            {
                return schedule;
            }
        }
        return null;
    }
    public List<Scheduling> getSchedules()
    {
        return schedules;
    }
}