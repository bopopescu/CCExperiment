package org.apache.maven.lifecycle;
import junit.framework.TestCase;
import org.apache.maven.lifecycle.internal.ExecutionPlanItem;
import org.apache.maven.lifecycle.internal.stub.DefaultLifecyclesStub;
import org.apache.maven.lifecycle.internal.stub.LifecycleExecutionPlanCalculatorStub;
import org.apache.maven.model.Plugin;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
public class MavenExecutionPlanTest
    extends TestCase
{
    public void testFindFirstWithMatchingSchedule()
        throws Exception
    {
        final List<Scheduling> cycles = DefaultLifecyclesStub.getSchedulingList();
        final Schedule schedule = cycles.get( 0 ).getSchedules().get( 0 );
        assertNotNull( schedule );
    }
    public void testForceAllComplete()
        throws Exception
    {
        MavenExecutionPlan plan = LifecycleExecutionPlanCalculatorStub.getProjectAExceutionPlan();
        plan.forceAllComplete();
        final Iterator<ExecutionPlanItem> planItemIterator = plan.iterator();
        assertTrue( planItemIterator.next().isDone() );
        assertTrue( planItemIterator.next().isDone() );
    }
    public void testFindLastInPhase()
        throws Exception
    {
        MavenExecutionPlan plan = LifecycleExecutionPlanCalculatorStub.getProjectAExceutionPlan();
        ExecutionPlanItem expected = plan.findLastInPhase( "package" );
        ExecutionPlanItem beerPhase = plan.findLastInPhase( "BEER" );  
        assertEquals( expected, beerPhase );
        assertNotNull( expected );
    }
    public void testThreadSafeMojos()
        throws Exception
    {
        MavenExecutionPlan plan = LifecycleExecutionPlanCalculatorStub.getProjectAExceutionPlan();
        final Set<Plugin> unSafePlugins = plan.getNonThreadSafePlugins();
        assertEquals( plan.size() - 1, unSafePlugins.size() );
    }
    public void testFindLastWhenFirst()
        throws Exception
    {
        MavenExecutionPlan plan = LifecycleExecutionPlanCalculatorStub.getProjectAExceutionPlan();
        ExecutionPlanItem beerPhase = plan.findLastInPhase(
            LifecycleExecutionPlanCalculatorStub.VALIDATE.getPhase() );  
        assertNull( beerPhase );
    }
    public void testFindLastInPhaseMisc()
        throws Exception
    {
        MavenExecutionPlan plan = LifecycleExecutionPlanCalculatorStub.getProjectAExceutionPlan();
        assertNull( plan.findLastInPhase( "pacXkage" ) );
        assertNotNull( plan.findLastInPhase( LifecycleExecutionPlanCalculatorStub.INITIALIZE.getPhase() ) );
    }
}
