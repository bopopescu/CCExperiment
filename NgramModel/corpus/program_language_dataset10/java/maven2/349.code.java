package org.apache.maven.project.injection;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Exclusion;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginManagement;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
public class DefaultModelDefaultsInjectorTest
    extends TestCase
{
    public void testShouldConstructWithNoParams()
    {
        new DefaultModelDefaultsInjector();
    }
    public void testShouldMergePluginManagementVersionIntoPlugin()
    {
        String groupId = "org.apache.maven.plugins";
        String artifactId = "maven-test-plugin";
        Model model = new Model();
        Build build = new Build();
        Plugin targetPlugin = new Plugin();
        targetPlugin.setGroupId( groupId );
        targetPlugin.setArtifactId( artifactId );
        build.addPlugin( targetPlugin );
        PluginManagement pMgmt = new PluginManagement();
        Plugin managedPlugin = new Plugin();
        managedPlugin.setGroupId( groupId );
        managedPlugin.setArtifactId( artifactId );
        managedPlugin.setVersion( "10.0.0" );
        pMgmt.addPlugin( managedPlugin );
        build.setPluginManagement( pMgmt );
        model.setBuild( build );
        new DefaultModelDefaultsInjector().injectDefaults( model );
        Map pMap = model.getBuild().getPluginsAsMap();
        Plugin result = (Plugin) pMap.get( groupId + ":" + artifactId );
        assertNotNull( result );
        assertEquals( managedPlugin.getVersion(), result.getVersion() );
    }
    public void testShouldKeepPluginVersionOverPluginManagementVersion()
    {
        String groupId = "org.apache.maven.plugins";
        String artifactId = "maven-test-plugin";
        Model model = new Model();
        Build build = new Build();
        Plugin targetPlugin = new Plugin();
        targetPlugin.setGroupId( groupId );
        targetPlugin.setArtifactId( artifactId );
        targetPlugin.setVersion( "9.0.0" );
        build.addPlugin( targetPlugin );
        PluginManagement pMgmt = new PluginManagement();
        Plugin managedPlugin = new Plugin();
        managedPlugin.setGroupId( groupId );
        managedPlugin.setArtifactId( artifactId );
        managedPlugin.setVersion( "10.0.0" );
        pMgmt.addPlugin( managedPlugin );
        build.setPluginManagement( pMgmt );
        model.setBuild( build );
        new DefaultModelDefaultsInjector().injectDefaults( model );
        Map pMap = model.getBuild().getPluginsAsMap();
        Plugin result = (Plugin) pMap.get( groupId + ":" + artifactId );
        assertNotNull( result );
        assertEquals( targetPlugin.getVersion(), result.getVersion() );
    }
    public void testShouldMergeManagedDependencyOfTypeEJBToDependencyList()
    {
        Model model = new Model();
        Dependency managedDep = new Dependency();
        managedDep.setGroupId( "group" );
        managedDep.setArtifactId( "artifact" );
        managedDep.setVersion( "1.0" );
        managedDep.setType( "ejb" );
        DependencyManagement depMgmt = new DependencyManagement();
        depMgmt.addDependency( managedDep );
        model.setDependencyManagement( depMgmt );
        Dependency dep = new Dependency();
        dep.setGroupId( "group" );
        dep.setArtifactId( "artifact" );
        dep.setType( "ejb" );
        model.addDependency( dep );
        new DefaultModelDefaultsInjector().injectDefaults( model );
        List resultingDeps = model.getDependencies();
        assertEquals( 1, resultingDeps.size() );
        Dependency result = (Dependency) resultingDeps.get( 0 );
        assertEquals( "1.0", result.getVersion() );
    }
    public void testShouldSucceedInMergingDependencyWithDependency()
    {
        Model model = new Model();
        Dependency dep = new Dependency();
        dep.setGroupId( "myGroup" );
        dep.setArtifactId( "myArtifact" );
        model.addDependency( dep );
        Dependency def = new Dependency();
        def.setGroupId( dep.getGroupId() );
        def.setArtifactId( dep.getArtifactId() );
        def.setVersion( "1.0.1" );
        def.setScope( "scope" );
        DependencyManagement depMgmt = new DependencyManagement();
        depMgmt.addDependency( def );
        model.setDependencyManagement( depMgmt );
        new DefaultModelDefaultsInjector().injectDefaults( model );
        List deps = model.getDependencies();
        assertEquals( 1, deps.size() );
        Dependency result = (Dependency) deps.get( 0 );
        assertEquals( def.getVersion(), result.getVersion() );
    }
    public void testShouldMergeDependencyExclusionsFromDefaultsToDependency()
    {
        Model model = new Model();
        Dependency dep = new Dependency();
        dep.setGroupId( "myGroup" );
        dep.setArtifactId( "myArtifact" );
        model.addDependency( dep );
        Dependency def = new Dependency();
        def.setGroupId( dep.getGroupId() );
        def.setArtifactId( dep.getArtifactId() );
        def.setVersion( "1.0.1" );
        def.setScope( "scope" );
        Exclusion exc = new Exclusion();
        exc.setArtifactId( "mydep" );
        exc.setGroupId( "mygrp" );
        def.addExclusion( exc );
        DependencyManagement depMgmt = new DependencyManagement();
        depMgmt.addDependency( def );
        model.setDependencyManagement( depMgmt );
        new DefaultModelDefaultsInjector().injectDefaults( model );
        List deps = model.getDependencies();
        assertEquals( 1, deps.size() );
        Dependency result = (Dependency) deps.get( 0 );
        assertEquals( def.getVersion(), result.getVersion() );
        List resultExclusions = result.getExclusions();
        assertNotNull( resultExclusions );
        assertEquals( 1, resultExclusions.size() );
        Exclusion resultExclusion = (Exclusion) resultExclusions.get( 0 );
        assertEquals( "mydep", resultExclusion.getArtifactId() );
        assertEquals( "mygrp", resultExclusion.getGroupId() );
    }
    public void testShouldMergeDefaultUrlAndArtifactWhenDependencyDoesntSupplyVersion()
    {
        Model model = new Model();
        Dependency dep = new Dependency();
        dep.setGroupId( "myGroup" );
        dep.setArtifactId( "myArtifact" );
        model.addDependency( dep );
        Dependency def = new Dependency();
        def.setGroupId( dep.getGroupId() );
        def.setArtifactId( dep.getArtifactId() );
        def.setVersion( "1.0.1" );
        DependencyManagement depMgmt = new DependencyManagement();
        depMgmt.addDependency( def );
        model.setDependencyManagement( depMgmt );
        new DefaultModelDefaultsInjector().injectDefaults( model );
        List deps = model.getDependencies();
        assertEquals( 1, deps.size() );
        Dependency result = (Dependency) deps.get( 0 );
        assertEquals( def.getVersion(), result.getVersion() );
    }
    public void testShouldNotMergeDefaultUrlOrArtifactWhenDependencySuppliesVersion()
    {
        Model model = new Model();
        Dependency dep = new Dependency();
        dep.setGroupId( "myGroup" );
        dep.setArtifactId( "myArtifact" );
        dep.setVersion( "1.0.1" );
        model.addDependency( dep );
        Dependency def = new Dependency();
        def.setGroupId( dep.getGroupId() );
        def.setArtifactId( dep.getArtifactId() );
        DependencyManagement depMgmt = new DependencyManagement();
        depMgmt.addDependency( def );
        model.setDependencyManagement( depMgmt );
        new DefaultModelDefaultsInjector().injectDefaults( model );
        List deps = model.getDependencies();
        assertEquals( 1, deps.size() );
        Dependency result = (Dependency) deps.get( 0 );
        assertEquals( dep.getVersion(), result.getVersion() );
    }
    public void testShouldMergeDefaultPropertiesWhenDependencyDoesntSupplyProperties()
    {
        Model model = new Model();
        Dependency dep = new Dependency();
        dep.setGroupId( "myGroup" );
        dep.setArtifactId( "myArtifact" );
        dep.setVersion( "1.0.1" );
        model.addDependency( dep );
        Dependency def = new Dependency();
        def.setGroupId( dep.getGroupId() );
        def.setArtifactId( dep.getArtifactId() );
        DependencyManagement depMgmt = new DependencyManagement();
        depMgmt.addDependency( def );
        model.setDependencyManagement( depMgmt );
        new DefaultModelDefaultsInjector().injectDefaults( model );
        List deps = model.getDependencies();
        assertEquals( 1, deps.size() );
    }
    public void testShouldNotMergeDefaultPropertiesWhenDependencySuppliesProperties()
    {
        Model model = new Model();
        Dependency dep = new Dependency();
        dep.setGroupId( "myGroup" );
        dep.setArtifactId( "myArtifact" );
        dep.setVersion( "1.0.1" );
        model.addDependency( dep );
        Dependency def = new Dependency();
        def.setGroupId( dep.getGroupId() );
        def.setArtifactId( dep.getArtifactId() );
        DependencyManagement depMgmt = new DependencyManagement();
        depMgmt.addDependency( def );
        model.setDependencyManagement( depMgmt );
        new DefaultModelDefaultsInjector().injectDefaults( model );
        List deps = model.getDependencies();
        assertEquals( 1, deps.size() );
    }
    public void testShouldMergeDefaultScopeWhenDependencyDoesntSupplyScope()
    {
        Model model = new Model();
        Dependency dep = new Dependency();
        dep.setGroupId( "myGroup" );
        dep.setArtifactId( "myArtifact" );
        dep.setVersion( "1.0.1" );
        dep.setScope( "scope" );
        model.addDependency( dep );
        Dependency def = new Dependency();
        def.setGroupId( dep.getGroupId() );
        def.setArtifactId( dep.getArtifactId() );
        DependencyManagement depMgmt = new DependencyManagement();
        depMgmt.addDependency( def );
        model.setDependencyManagement( depMgmt );
        new DefaultModelDefaultsInjector().injectDefaults( model );
        List deps = model.getDependencies();
        assertEquals( 1, deps.size() );
        Dependency result = (Dependency) deps.get( 0 );
        assertEquals( "scope", result.getScope() );
    }
    public void testShouldNotMergeDefaultScopeWhenDependencySuppliesScope()
    {
        Model model = new Model();
        Dependency dep = new Dependency();
        dep.setGroupId( "myGroup" );
        dep.setArtifactId( "myArtifact" );
        dep.setVersion( "1.0.1" );
        dep.setScope( "scope" );
        model.addDependency( dep );
        Dependency def = new Dependency();
        def.setGroupId( dep.getGroupId() );
        def.setArtifactId( dep.getArtifactId() );
        def.setScope( "default" );
        DependencyManagement depMgmt = new DependencyManagement();
        depMgmt.addDependency( def );
        model.setDependencyManagement( depMgmt );
        new DefaultModelDefaultsInjector().injectDefaults( model );
        List deps = model.getDependencies();
        assertEquals( 1, deps.size() );
        Dependency result = (Dependency) deps.get( 0 );
        assertEquals( "scope", result.getScope() );
    }
    public void testShouldRejectDependencyWhereNoVersionIsFoundAfterDefaultsInjection()
    {
        Model model = new Model();
        Dependency dep = new Dependency();
        dep.setGroupId( "myGroup" );
        dep.setArtifactId( "myArtifact" );
        model.addDependency( dep );
        Dependency def = new Dependency();
        def.setGroupId( dep.getGroupId() );
        def.setArtifactId( dep.getArtifactId() );
        DependencyManagement depMgmt = new DependencyManagement();
        depMgmt.addDependency( def );
        model.setDependencyManagement( depMgmt );
        new DefaultModelDefaultsInjector().injectDefaults( model );
        Dependency dependency = (Dependency) model.getDependencies().get( 0 );
        assertNull( "check version is null", dependency.getVersion() );
    }
}
