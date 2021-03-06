package org.apache.maven.project.injection;
import org.apache.maven.model.Build;
import org.apache.maven.model.BuildBase;
import org.apache.maven.model.ConfigurationContainer;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginContainer;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.PluginManagement;
import org.apache.maven.model.Profile;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.ReportSet;
import org.apache.maven.model.Reporting;
import org.apache.maven.project.ModelUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
public class DefaultProfileInjector
    implements ProfileInjector
{
    public void inject( Profile profile, Model model )
    {
        model.setDependencies( injectDependencies( profile.getDependencies(), model.getDependencies() ) );
        injectModules( profile, model );
        model.setRepositories( ModelUtils.mergeRepositoryLists( profile.getRepositories(), model.getRepositories() ) );
        model.setPluginRepositories( ModelUtils.mergeRepositoryLists( profile.getPluginRepositories(), model
            .getPluginRepositories() ) );
        injectReporting( profile, model );
        injectDependencyManagement( profile, model );
        injectDistributionManagement( profile, model );
        injectBuild( profile, model );
        Properties props = new Properties();
        props.putAll( model.getProperties() );
        props.putAll( profile.getProperties() );
        model.setProperties( props );
    }
    private void injectBuild( Profile profile, Model model )
    {
        BuildBase profileBuild = profile.getBuild();
        Build modelBuild = model.getBuild();
        if ( profileBuild != null )
        {
            if ( modelBuild == null )
            {
                modelBuild = new Build();
                model.setBuild( modelBuild );
            }
            if ( profileBuild.getDirectory() != null )
            {
                modelBuild.setDirectory( profileBuild.getDirectory() );
            }
            if ( profileBuild.getDefaultGoal() != null )
            {
                modelBuild.setDefaultGoal( profileBuild.getDefaultGoal() );
            }
            if ( profileBuild.getFinalName() != null )
            {
                modelBuild.setFinalName( profileBuild.getFinalName() );
            }
            ModelUtils.mergeFilterLists( modelBuild.getFilters(), profileBuild.getFilters() );
            ModelUtils.mergeResourceLists( modelBuild.getResources(), profileBuild.getResources() );
            ModelUtils.mergeResourceLists( modelBuild.getTestResources(), profileBuild.getTestResources() );
            injectPlugins( profileBuild, modelBuild );
            PluginManagement profilePM = profileBuild.getPluginManagement();
            PluginManagement modelPM = modelBuild.getPluginManagement();
            if ( modelPM == null )
            {
                modelBuild.setPluginManagement( profilePM );
            }
            else
            {
                injectPlugins( profilePM, modelPM );
            }
        }
    }
    protected void injectPlugins( PluginContainer profileContainer, PluginContainer modelContainer )
    {
        if ( ( profileContainer == null ) || ( modelContainer == null ) )
        {
            return;
        }
        List modelPlugins = modelContainer.getPlugins();
        if ( modelPlugins == null )
        {
            modelContainer.setPlugins( profileContainer.getPlugins() );
        }
        else if ( profileContainer.getPlugins() != null )
        {
            List mergedPlugins = new ArrayList();
            Map profilePlugins = profileContainer.getPluginsAsMap();
            for ( Iterator it = modelPlugins.iterator(); it.hasNext(); )
            {
                Plugin modelPlugin = (Plugin) it.next();
                Plugin profilePlugin = (Plugin) profilePlugins.get( modelPlugin.getKey() );
                if ( ( profilePlugin != null ) && !mergedPlugins.contains( profilePlugin ) )
                {
                    Plugin mergedPlugin = modelPlugin;
                    injectPluginDefinition( profilePlugin, modelPlugin );
                    mergedPlugins.add( mergedPlugin );
                }
            }
            List results = ModelUtils.orderAfterMerge( mergedPlugins, modelPlugins, profileContainer.getPlugins() );
            modelContainer.setPlugins( results );
            modelContainer.flushPluginMap();
        }
    }
    private void injectPluginDefinition( Plugin profilePlugin, Plugin modelPlugin )
    {
        if ( ( profilePlugin == null ) || ( modelPlugin == null ) )
        {
            return;
        }
        if ( profilePlugin.isExtensions() )
        {
            modelPlugin.setExtensions( true );
        }
        if ( profilePlugin.getVersion() != null )
        {
            modelPlugin.setVersion( profilePlugin.getVersion() );
        }
        modelPlugin.setDependencies( injectDependencies( profilePlugin.getDependencies(), modelPlugin.getDependencies() ) );
        injectConfigurationContainer( profilePlugin, modelPlugin );
        List modelExecutions = modelPlugin.getExecutions();
        if ( ( modelExecutions == null ) || modelExecutions.isEmpty() )
        {
            modelPlugin.setExecutions( profilePlugin.getExecutions() );
        }
        else
        {
            Map executions = new LinkedHashMap();
            Map profileExecutions = profilePlugin.getExecutionsAsMap();
            for ( Iterator it = modelExecutions.iterator(); it.hasNext(); )
            {
                PluginExecution modelExecution = (PluginExecution) it.next();
                PluginExecution profileExecution = (PluginExecution) profileExecutions.get( modelExecution.getId() );
                if ( profileExecution != null )
                {
                    injectConfigurationContainer( profileExecution, modelExecution );
                    if ( profileExecution.getPhase() != null )
                    {
                        modelExecution.setPhase( profileExecution.getPhase() );
                    }
                    List profileGoals = profileExecution.getGoals();
                    List modelGoals = modelExecution.getGoals();
                    List goals = new ArrayList();
                    if ( ( modelGoals != null ) && !modelGoals.isEmpty() )
                    {
                        goals.addAll( modelGoals );
                    }
                    if ( profileGoals != null )
                    {
                        for ( Iterator goalIterator = profileGoals.iterator(); goalIterator.hasNext(); )
                        {
                            String goal = (String) goalIterator.next();
                            if ( !goals.contains( goal ) )
                            {
                                goals.add( goal );
                            }
                        }
                    }
                    modelExecution.setGoals( goals );
                }
                executions.put( modelExecution.getId(), modelExecution );
            }
            for ( Iterator it = profileExecutions.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry entry = (Map.Entry) it.next();
                String id = (String) entry.getKey();
                if ( !executions.containsKey( id ) )
                {
                    executions.put( id, entry.getValue() );
                }
            }
            modelPlugin.setExecutions( new ArrayList( executions.values() ) );
            modelPlugin.flushExecutionMap();
        }
    }
    private Xpp3Dom merge( Xpp3Dom dominant, Xpp3Dom recessive )
    {
        Xpp3Dom dominantCopy = ( dominant == null ) ? null : new Xpp3Dom( dominant );
        return Xpp3Dom.mergeXpp3Dom( dominantCopy, recessive );
    }
    private void injectConfigurationContainer( ConfigurationContainer profileContainer,
                                               ConfigurationContainer modelContainer )
    {
        Xpp3Dom configuration = (Xpp3Dom) profileContainer.getConfiguration();
        Xpp3Dom parentConfiguration = (Xpp3Dom) modelContainer.getConfiguration();
        configuration = merge( configuration, parentConfiguration );
        modelContainer.setConfiguration( configuration );
    }
    private void injectModules( Profile profile, Model model )
    {
        List modules = new ArrayList();
        List modelModules = model.getModules();
        if ( ( modelModules != null ) && !modelModules.isEmpty() )
        {
            modules.addAll( modelModules );
        }
        List profileModules = profile.getModules();
        if ( profileModules != null )
        {
            for ( Iterator it = profileModules.iterator(); it.hasNext(); )
            {
                String module = (String) it.next();
                if ( !modules.contains( module ) )
                {
                    modules.add( module );
                }
            }
        }
        model.setModules( modules );
    }
    private void injectDistributionManagement( Profile profile, Model model )
    {
        DistributionManagement pDistMgmt = profile.getDistributionManagement();
        DistributionManagement mDistMgmt = model.getDistributionManagement();
        if ( mDistMgmt == null )
        {
            model.setDistributionManagement( pDistMgmt );
        }
        else if ( pDistMgmt != null )
        {
            if ( pDistMgmt.getRepository() != null )
            {
                mDistMgmt.setRepository( pDistMgmt.getRepository() );
            }
            if ( pDistMgmt.getSnapshotRepository() != null )
            {
                mDistMgmt.setSnapshotRepository( pDistMgmt.getSnapshotRepository() );
            }
            if ( StringUtils.isNotEmpty( pDistMgmt.getDownloadUrl() ) )
            {
                mDistMgmt.setDownloadUrl( pDistMgmt.getDownloadUrl() );
            }
            if ( pDistMgmt.getRelocation() != null )
            {
                mDistMgmt.setRelocation( pDistMgmt.getRelocation() );
            }
            if ( pDistMgmt.getSite() != null )
            {
                mDistMgmt.setSite( pDistMgmt.getSite() );
            }
        }
    }
    private void injectDependencyManagement( Profile profile, Model model )
    {
        DependencyManagement modelDepMgmt = model.getDependencyManagement();
        DependencyManagement profileDepMgmt = profile.getDependencyManagement();
        if ( profileDepMgmt != null )
        {
            if ( modelDepMgmt == null )
            {
                model.setDependencyManagement( profileDepMgmt );
            }
            else
            {
                Map depsMap = new LinkedHashMap();
                List deps = modelDepMgmt.getDependencies();
                if ( deps != null )
                {
                    for ( Iterator it = deps.iterator(); it.hasNext(); )
                    {
                        Dependency dependency = (Dependency) it.next();
                        depsMap.put( dependency.getManagementKey(), dependency );
                    }
                }
                deps = profileDepMgmt.getDependencies();
                if ( deps != null )
                {
                    for ( Iterator it = deps.iterator(); it.hasNext(); )
                    {
                        Dependency dependency = (Dependency) it.next();
                        depsMap.put( dependency.getManagementKey(), dependency );
                    }
                }
                modelDepMgmt.setDependencies( new ArrayList( depsMap.values() ) );
            }
        }
    }
    private void injectReporting( Profile profile, Model model )
    {
        Reporting profileReporting = profile.getReporting();
        Reporting modelReporting = model.getReporting();
        if ( profileReporting != null )
        {
            if ( modelReporting == null )
            {
                model.setReporting( profileReporting );
            }
            else
            {
                if ( StringUtils.isEmpty( modelReporting.getOutputDirectory() ) )
                {
                    modelReporting.setOutputDirectory( profileReporting.getOutputDirectory() );
                }
                Map mergedReportPlugins = new LinkedHashMap();
                Map profileReportersByKey = profileReporting.getReportPluginsAsMap();
                List modelReportPlugins = modelReporting.getPlugins();
                if ( modelReportPlugins != null )
                {
                    for ( Iterator it = modelReportPlugins.iterator(); it.hasNext(); )
                    {
                        ReportPlugin modelReportPlugin = (ReportPlugin) it.next();
                        String inherited = modelReportPlugin.getInherited();
                        if ( StringUtils.isEmpty( inherited ) || Boolean.valueOf( inherited ).booleanValue() )
                        {
                            ReportPlugin profileReportPlugin = (ReportPlugin) profileReportersByKey
                                .get( modelReportPlugin.getKey() );
                            ReportPlugin mergedReportPlugin = modelReportPlugin;
                            if ( profileReportPlugin != null )
                            {
                                mergedReportPlugin = profileReportPlugin;
                                mergeReportPlugins( profileReportPlugin, modelReportPlugin );
                            }
                            else if ( StringUtils.isEmpty( inherited ) )
                            {
                                mergedReportPlugin.unsetInheritanceApplied();
                            }
                            mergedReportPlugins.put( mergedReportPlugin.getKey(), mergedReportPlugin );
                        }
                    }
                }
                for ( Iterator it = profileReportersByKey.entrySet().iterator(); it.hasNext(); )
                {
                    Map.Entry entry = (Map.Entry) it.next();
                    String key = (String) entry.getKey();
                    if ( !mergedReportPlugins.containsKey( key ) )
                    {
                        mergedReportPlugins.put( key, entry.getValue() );
                    }
                }
                modelReporting.setPlugins( new ArrayList( mergedReportPlugins.values() ) );
                modelReporting.flushReportPluginMap();
            }
        }
    }
    private void mergeReportPlugins( ReportPlugin dominant, ReportPlugin recessive )
    {
        if ( StringUtils.isEmpty( recessive.getVersion() ) )
        {
            recessive.setVersion( dominant.getVersion() );
        }
        Xpp3Dom dominantConfig = (Xpp3Dom) dominant.getConfiguration();
        Xpp3Dom recessiveConfig = (Xpp3Dom) recessive.getConfiguration();
        recessive.setConfiguration( merge( dominantConfig, recessiveConfig ) );
        Map mergedReportSets = new LinkedHashMap();
        Map dominantReportSetsById = dominant.getReportSetsAsMap();
        for ( Iterator it = recessive.getReportSets().iterator(); it.hasNext(); )
        {
            ReportSet recessiveReportSet = (ReportSet) it.next();
            ReportSet dominantReportSet = (ReportSet) dominantReportSetsById.get( recessiveReportSet.getId() );
            ReportSet merged = recessiveReportSet;
            if ( dominantReportSet != null )
            {
                merged = recessiveReportSet;
                Xpp3Dom dominantRSConfig = (Xpp3Dom) dominantReportSet.getConfiguration();
                Xpp3Dom mergedRSConfig = (Xpp3Dom) merged.getConfiguration();
                merged.setConfiguration( merge( dominantRSConfig, mergedRSConfig ) );
                List mergedReports = merged.getReports();
                if ( mergedReports == null )
                {
                    mergedReports = new ArrayList();
                    merged.setReports( mergedReports );
                }
                List dominantRSReports = dominantReportSet.getReports();
                if ( dominantRSReports != null )
                {
                    for ( Iterator reportIterator = dominantRSReports.iterator(); reportIterator.hasNext(); )
                    {
                        String report = (String) reportIterator.next();
                        if ( !mergedReports.contains( report ) )
                        {
                            mergedReports.add( report );
                        }
                    }
                }
                mergedReportSets.put( merged.getId(), merged );
            }
        }
        for ( Iterator rsIterator = dominantReportSetsById.entrySet().iterator(); rsIterator.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) rsIterator.next();
            String key = (String) entry.getKey();
            if ( !mergedReportSets.containsKey( key ) )
            {
                mergedReportSets.put( key, entry.getValue() );
            }
        }
        recessive.setReportSets( new ArrayList( mergedReportSets.values() ) );
        recessive.flushReportSetMap();
    }
    private List injectDependencies( List profileDeps, List modelDeps )
    {
        Map depsMap = new LinkedHashMap();
        if ( modelDeps != null )
        {
            for ( Iterator it = modelDeps.iterator(); it.hasNext(); )
            {
                Dependency dependency = (Dependency) it.next();
                depsMap.put( dependency.getManagementKey(), dependency );
            }
        }
        if ( profileDeps != null )
        {
            for ( Iterator it = profileDeps.iterator(); it.hasNext(); )
            {
                Dependency dependency = (Dependency) it.next();
                depsMap.put( dependency.getManagementKey(), dependency );
            }
        }
        return new ArrayList( depsMap.values() );
    }
}
