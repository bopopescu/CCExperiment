package org.apache.maven.lifecycle.internal;
import org.apache.maven.lifecycle.DefaultLifecycles;
import org.apache.maven.lifecycle.LifeCyclePluginAnalyzer;
import org.apache.maven.lifecycle.Lifecycle;
import org.apache.maven.lifecycle.mapping.LifecycleMapping;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
@Component( role = LifeCyclePluginAnalyzer.class )
public class DefaultLifecyclePluginAnalyzer
    implements LifeCyclePluginAnalyzer
{
    @Requirement( role = LifecycleMapping.class )
    private Map<String, LifecycleMapping> lifecycleMappings;
    @Requirement
    private DefaultLifecycles defaultLifeCycles;
    @Requirement
    private Logger logger;
    public DefaultLifecyclePluginAnalyzer()
    {
    }
    public Set<Plugin> getPluginsBoundByDefaultToAllLifecycles( String packaging )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Looking up lifecyle mappings for packaging " + packaging + " from " +
                Thread.currentThread().getContextClassLoader() );
        }
        LifecycleMapping lifecycleMappingForPackaging = lifecycleMappings.get( packaging );
        if ( lifecycleMappingForPackaging == null )
        {
            return null;
        }
        Map<Plugin, Plugin> plugins = new LinkedHashMap<Plugin, Plugin>();
        for ( Lifecycle lifecycle : getOrderedLifecycles() )
        {
            org.apache.maven.lifecycle.mapping.Lifecycle lifecycleConfiguration =
                lifecycleMappingForPackaging.getLifecycles().get( lifecycle.getId() );
            Map<String, String> phaseToGoalMapping = null;
            if ( lifecycleConfiguration != null )
            {
                phaseToGoalMapping = lifecycleConfiguration.getPhases();
            }
            else if ( lifecycle.getDefaultPhases() != null )
            {
                phaseToGoalMapping = lifecycle.getDefaultPhases();
            }
            if ( phaseToGoalMapping != null )
            {
                for ( Map.Entry<String, String> goalsForLifecyclePhase : phaseToGoalMapping.entrySet() )
                {
                    String phase = goalsForLifecyclePhase.getKey();
                    String goals = goalsForLifecyclePhase.getValue();
                    if ( goals != null )
                    {
                        parseLifecyclePhaseDefinitions( plugins, phase, goals );
                    }
                }
            }
        }
        return plugins.keySet();
    }
    private List<Lifecycle> getOrderedLifecycles()
    {
        List<Lifecycle> lifecycles = new ArrayList<Lifecycle>( defaultLifeCycles.getLifeCycles() );
        Collections.sort( lifecycles, new Comparator<Lifecycle>()
        {
            public int compare( Lifecycle l1, Lifecycle l2 )
            {
                return l1.getId().compareTo( l2.getId() );
            }
        } );
        return lifecycles;
    }
    private void parseLifecyclePhaseDefinitions( Map<Plugin, Plugin> plugins, String phase, String goals )
    {
        String[] mojos = StringUtils.split( goals, "," );
        for ( int i = 0; i < mojos.length; i++ )
        {
            GoalSpec gs = parseGoalSpec( mojos[i].trim() );
            if ( gs == null )
            {
                logger.warn( "Ignored invalid goal specification '" + mojos[i] + "' from lifecycle mapping for phase "
                    + phase );
                continue;
            }
            Plugin plugin = new Plugin();
            plugin.setGroupId( gs.groupId );
            plugin.setArtifactId( gs.artifactId );
            plugin.setVersion( gs.version );
            Plugin existing = plugins.get( plugin );
            if ( existing != null )
            {
                if ( existing.getVersion() == null )
                {
                    existing.setVersion( plugin.getVersion() );
                }
                plugin = existing;
            }
            else
            {
                plugins.put( plugin, plugin );
            }
            PluginExecution execution = new PluginExecution();
            execution.setId( getExecutionId( plugin, gs.goal ) );
            execution.setPhase( phase );
            execution.setPriority( i - mojos.length );
            execution.getGoals().add( gs.goal );
            plugin.getExecutions().add( execution );
        }
    }
    private GoalSpec parseGoalSpec( String goalSpec )
    {
        GoalSpec gs = new GoalSpec();
        String[] p = StringUtils.split( goalSpec.trim(), ":" );
        if ( p.length == 3 )
        {
            gs.groupId = p[0];
            gs.artifactId = p[1];
            gs.goal = p[2];
        }
        else if ( p.length == 4 )
        {
            gs.groupId = p[0];
            gs.artifactId = p[1];
            gs.version = p[2];
            gs.goal = p[3];
        }
        else
        {
            gs = null;
        }
        return gs;
    }
    private String getExecutionId( Plugin plugin, String goal )
    {
        Set<String> existingIds = new HashSet<String>();
        for ( PluginExecution execution : plugin.getExecutions() )
        {
            existingIds.add( execution.getId() );
        }
        String base = "default-" + goal;
        String id = base;
        for ( int index = 1; existingIds.contains( id ); index++ )
        {
            id = base + '-' + index;
        }
        return id;
    }
    static class GoalSpec
    {
        String groupId;
        String artifactId;
        String version;
        String goal;
    }
}
