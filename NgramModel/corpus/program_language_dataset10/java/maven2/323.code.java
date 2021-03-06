package org.apache.maven.project;
import org.apache.maven.model.Build;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginContainer;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.Reporting;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
public class ModelUtilsTest
    extends TestCase
{
    public void testShouldUseMainPluginDependencyVersionOverManagedDepVersion()
    {
        Plugin mgtPlugin = createPlugin( "group", "artifact", "1", Collections.EMPTY_MAP );
        Dependency mgtDep = createDependency( "g", "a", "2" );
        mgtPlugin.addDependency( mgtDep );
        Plugin plugin = createPlugin( "group", "artifact", "1", Collections.EMPTY_MAP );
        Dependency dep = createDependency( "g", "a", "1" );
        plugin.addDependency( dep );
        ModelUtils.mergePluginDefinitions( plugin, mgtPlugin, false );
        assertEquals( dep.getVersion(), ((Dependency) plugin.getDependencies().get( 0 ) ).getVersion() );
    }
    private Dependency createDependency( String gid,
                                         String aid,
                                         String ver )
    {
        Dependency dep = new Dependency();
        dep.setGroupId( gid );
        dep.setArtifactId( aid );
        dep.setVersion( ver );
        return dep;
    }
    public void testShouldNotInheritPluginWithInheritanceSetToFalse()
    {
        PluginContainer parent = new PluginContainer();
        Plugin parentPlugin = createPlugin( "group", "artifact", "1.0", Collections.EMPTY_MAP );
        parentPlugin.setInherited( "false" );
        parent.addPlugin( parentPlugin );
        PluginContainer child = new PluginContainer();
        child.addPlugin( createPlugin( "group3", "artifact3", "1.0", Collections.EMPTY_MAP ) );
        ModelUtils.mergePluginLists( child, parent, true );
        List results = child.getPlugins();
        assertEquals( 1, results.size() );
        Plugin result1 = (Plugin) results.get( 0 );
        assertEquals( "group3", result1.getGroupId() );
        assertEquals( "artifact3", result1.getArtifactId() );
    }
    public void testShouldPreserveChildOrderingOfPluginsAfterParentMerge()
    {
        PluginContainer parent = new PluginContainer();
        parent.addPlugin( createPlugin( "group", "artifact", "1.0", Collections.EMPTY_MAP ) );
        parent.addPlugin( createPlugin( "group2", "artifact2", "1.0", Collections.singletonMap( "key", "value" ) ) );
        PluginContainer child = new PluginContainer();
        child.addPlugin( createPlugin( "group3", "artifact3", "1.0", Collections.EMPTY_MAP ) );
        child.addPlugin( createPlugin( "group2", "artifact2", "1.0", Collections.singletonMap( "key2", "value2" ) ) );
        ModelUtils.mergePluginLists( child, parent, true );
        List results = child.getPlugins();
        assertEquals( 3, results.size() );
        Plugin result1 = (Plugin) results.get( 0 );
        assertEquals( "group", result1.getGroupId() );
        assertEquals( "artifact", result1.getArtifactId() );
        Plugin result2 = (Plugin) results.get( 1 );
        assertEquals( "group3", result2.getGroupId() );
        assertEquals( "artifact3", result2.getArtifactId() );
        Plugin result3 = (Plugin) results.get( 2 );
        assertEquals( "group2", result3.getGroupId() );
        assertEquals( "artifact2", result3.getArtifactId() );
        Xpp3Dom result3Config = (Xpp3Dom) result3.getConfiguration();
        assertNotNull( result3Config );
        assertNotNull( result3Config.getChild( "key" ) );
        assertNotNull( result3Config.getChild( "key2" ) );
        assertEquals( "value", result3Config.getChild( "key" ).getValue() );
        assertEquals( "value2", result3Config.getChild( "key2" ).getValue() );
    }
    public void testShouldPreserveChildOrderingOfReportsAfterParentMerge()
    {
        Reporting parent = new Reporting();
        parent.addPlugin( createReportPlugin( "group", "artifact", "1.0", Collections.EMPTY_MAP ) );
        parent.addPlugin( createReportPlugin( "group2", "artifact2", "1.0", Collections.singletonMap( "key", "value" ) ) );
        Reporting child = new Reporting();
        child.addPlugin( createReportPlugin( "group3", "artifact3", "1.0", Collections.EMPTY_MAP ) );
        child.addPlugin( createReportPlugin( "group2", "artifact2", "1.0", Collections.singletonMap( "key2", "value2" ) ) );
        ModelUtils.mergeReportPluginLists( child, parent, true );
        List results = child.getPlugins();
        assertEquals( 3, results.size() );
        ReportPlugin result1 = (ReportPlugin) results.get( 0 );
        assertEquals( "group", result1.getGroupId() );
        assertEquals( "artifact", result1.getArtifactId() );
        ReportPlugin result2 = (ReportPlugin) results.get( 1 );
        assertEquals( "group3", result2.getGroupId() );
        assertEquals( "artifact3", result2.getArtifactId() );
        ReportPlugin result3 = (ReportPlugin) results.get( 2 );
        assertEquals( "group2", result3.getGroupId() );
        assertEquals( "artifact2", result3.getArtifactId() );
        Xpp3Dom result3Config = (Xpp3Dom) result3.getConfiguration();
        assertNotNull( result3Config );
        assertNotNull( result3Config.getChild( "key" ) );
        assertNotNull( result3Config.getChild( "key2" ) );
        assertEquals( "value2", result3Config.getChild( "key2" ).getValue() );
    }
    private Xpp3Dom createConfiguration( Map configuration )
    {
        Xpp3Dom config = new Xpp3Dom( "configuration" );
        if( configuration != null )
        {
            for ( Iterator it = configuration.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry entry = (Map.Entry) it.next();
                Xpp3Dom param = new Xpp3Dom( String.valueOf( entry.getKey() ) );
                param.setValue( String.valueOf( entry.getValue() ) );
                config.addChild( param );
            }
        }
        return config;
    }
    private Plugin createPlugin( String groupId, String artifactId, String version, Map configuration )
    {
        Plugin plugin = new Plugin();
        plugin.setGroupId( groupId );
        plugin.setArtifactId( artifactId );
        plugin.setVersion( version );
        plugin.setConfiguration( createConfiguration( configuration ) );
        return plugin;
    }
    private ReportPlugin createReportPlugin( String groupId, String artifactId, String version, Map configuration )
    {
        ReportPlugin plugin = new ReportPlugin();
        plugin.setGroupId( groupId );
        plugin.setArtifactId( artifactId );
        plugin.setVersion( version );
        plugin.setConfiguration( createConfiguration( configuration ) );
        return plugin;
    }
    public void testShouldInheritOnePluginWithExecution()
    {
        Plugin parent = new Plugin();
        parent.setArtifactId( "testArtifact" );
        parent.setGroupId( "testGroup" );
        parent.setVersion( "1.0" );
        PluginExecution parentExecution = new PluginExecution();
        parentExecution.setId( "testExecution" );
        parent.addExecution( parentExecution );
        Plugin child = new Plugin();
        child.setArtifactId( "testArtifact" );
        child.setGroupId( "testGroup" );
        child.setVersion( "1.0" );
        ModelUtils.mergePluginDefinitions( child, parent, false );
        assertEquals( 1, child.getExecutions().size() );
    }
    public void testShouldMergeInheritedPluginHavingExecutionWithLocalPlugin()
    {
        Plugin parent = new Plugin();
        parent.setArtifactId( "testArtifact" );
        parent.setGroupId( "testGroup" );
        parent.setVersion( "1.0" );
        PluginExecution parentExecution = new PluginExecution();
        parentExecution.setId( "testExecution" );
        parent.addExecution( parentExecution );
        Plugin child = new Plugin();
        child.setArtifactId( "testArtifact" );
        child.setGroupId( "testGroup" );
        child.setVersion( "1.0" );
        PluginExecution childExecution = new PluginExecution();
        childExecution.setId( "testExecution2" );
        child.addExecution( childExecution );
        ModelUtils.mergePluginDefinitions( child, parent, false );
        assertEquals( 2, child.getExecutions().size() );
    }
    public void testShouldMergeOnePluginWithInheritExecutionWithoutDuplicatingPluginInList()
    {
        Plugin parent = new Plugin();
        parent.setArtifactId( "testArtifact" );
        parent.setGroupId( "testGroup" );
        parent.setVersion( "1.0" );
        PluginExecution parentExecution = new PluginExecution();
        parentExecution.setId( "testExecution" );
        parent.addExecution( parentExecution );
        Build parentContainer = new Build();
        parentContainer.addPlugin( parent );
        Plugin child = new Plugin();
        child.setArtifactId( "testArtifact" );
        child.setGroupId( "testGroup" );
        child.setVersion( "1.0" );
        Build childContainer = new Build();
        childContainer.addPlugin( child );
        ModelUtils.mergePluginLists( childContainer, parentContainer, true );
        List plugins = childContainer.getPlugins();
        assertEquals( 1, plugins.size() );
        Plugin plugin = (Plugin) plugins.get( 0 );
        assertEquals( 1, plugin.getExecutions().size() );
    }
    public void testShouldMergePluginWithDifferentExecutionFromParentWithoutDuplicatingPluginInList()
    {
        Plugin parent = new Plugin();
        parent.setArtifactId( "testArtifact" );
        parent.setGroupId( "testGroup" );
        parent.setVersion( "1.0" );
        PluginExecution parentExecution = new PluginExecution();
        parentExecution.setId( "testExecution" );
        parent.addExecution( parentExecution );
        Build parentContainer = new Build();
        parentContainer.addPlugin( parent );
        Plugin child = new Plugin();
        child.setArtifactId( "testArtifact" );
        child.setGroupId( "testGroup" );
        child.setVersion( "1.0" );
        PluginExecution childExecution = new PluginExecution();
        childExecution.setId( "testExecution2" );
        child.addExecution( childExecution );
        Build childContainer = new Build();
        childContainer.addPlugin( child );
        ModelUtils.mergePluginLists( childContainer, parentContainer, true );
        List plugins = childContainer.getPlugins();
        assertEquals( 1, plugins.size() );
        Plugin plugin = (Plugin) plugins.get( 0 );
        assertEquals( 2, plugin.getExecutions().size() );
    }
    public void testShouldNOTMergeInheritedPluginHavingInheritEqualFalse()
    {
        Plugin parent = new Plugin();
        parent.setArtifactId( "testArtifact" );
        parent.setGroupId( "testGroup" );
        parent.setVersion( "1.0" );
        parent.setInherited( "false" );
        PluginExecution parentExecution = new PluginExecution();
        parentExecution.setId( "testExecution" );
        parent.addExecution( parentExecution );
        Plugin child = new Plugin();
        child.setArtifactId( "testArtifact" );
        child.setGroupId( "testGroup" );
        child.setVersion( "1.0" );
        ModelUtils.mergePluginDefinitions( child, parent, true );
        assertEquals( 0, child.getExecutions().size() );
    }
    public void testShouldKeepOriginalPluginOrdering()
    {
        Plugin parentPlugin1 = new Plugin();
        parentPlugin1.setArtifactId( "testArtifact" );
        parentPlugin1.setGroupId( "zzz" );  
        parentPlugin1.setVersion( "1.0" );
        PluginExecution parentExecution1 = new PluginExecution();
        parentExecution1.setId( "testExecution" );
        parentPlugin1.addExecution( parentExecution1 );
        Plugin parentPlugin2 = new Plugin();
        parentPlugin2.setArtifactId( "testArtifact" );
        parentPlugin2.setGroupId( "yyy" );
        parentPlugin2.setVersion( "1.0" );
        PluginExecution parentExecution2 = new PluginExecution();
        parentExecution2.setId( "testExecution" );
        parentPlugin2.addExecution( parentExecution2 );
        PluginContainer parentContainer = new PluginContainer();
        parentContainer.addPlugin(parentPlugin1);
        parentContainer.addPlugin(parentPlugin2);
        Plugin childPlugin1 = new Plugin();
        childPlugin1.setArtifactId( "testArtifact" );
        childPlugin1.setGroupId( "bbb" );
        childPlugin1.setVersion( "1.0" );
        PluginExecution childExecution1 = new PluginExecution();
        childExecution1.setId( "testExecution" );
        childPlugin1.addExecution( childExecution1 );
        Plugin childPlugin2 = new Plugin();
        childPlugin2.setArtifactId( "testArtifact" );
        childPlugin2.setGroupId( "aaa" );
        childPlugin2.setVersion( "1.0" );
        PluginExecution childExecution2 = new PluginExecution();
        childExecution2.setId( "testExecution" );
        childPlugin2.addExecution( childExecution2 );
        PluginContainer childContainer = new PluginContainer();
        childContainer.addPlugin(childPlugin1);
        childContainer.addPlugin(childPlugin2);
        ModelUtils.mergePluginLists(childContainer, parentContainer, true);
        assertEquals( 4, childContainer.getPlugins().size() );
        assertSame(parentPlugin1, childContainer.getPlugins().get(0));
        assertSame(parentPlugin2, childContainer.getPlugins().get(1));
        assertSame(childPlugin1, childContainer.getPlugins().get(2));
        assertSame(childPlugin2, childContainer.getPlugins().get(3));
    }
    public void testShouldKeepOriginalPluginExecutionOrdering()
    {
        Plugin parent = new Plugin();
        parent.setArtifactId( "testArtifact" );
        parent.setGroupId( "testGroup" );
        parent.setVersion( "1.0" );
        PluginExecution parentExecution1 = new PluginExecution();
        parentExecution1.setId( "zzz" );  
        PluginExecution parentExecution2 = new PluginExecution();
        parentExecution2.setId( "yyy" );  
        parent.addExecution( parentExecution1 );
        parent.addExecution( parentExecution2 );
        Dependency dep = new Dependency();
        dep.setGroupId( "depGroupId" );
        dep.setArtifactId( "depArtifactId" );
        dep.setVersion( "depVersion" );
        parent.setDependencies( Collections.singletonList( dep ) );
        Plugin child = new Plugin();
        child.setArtifactId( "testArtifact" );
        child.setGroupId( "testGroup" );
        child.setVersion( "1.0" );
        PluginExecution childExecution1 = new PluginExecution();
        childExecution1.setId( "bbb" );
        PluginExecution childExecution2 = new PluginExecution();
        childExecution2.setId( "aaa" );
        child.addExecution( childExecution1 );
        child.addExecution( childExecution2 );
        ModelUtils.mergePluginDefinitions( child, parent, false );
        assertEquals( 4, child.getExecutions().size() );
        assertSame(parentExecution1, child.getExecutions().get(0));
        assertSame(parentExecution2, child.getExecutions().get(1));
        assertSame(childExecution1, child.getExecutions().get(2));
        assertSame(childExecution2, child.getExecutions().get(3));
        assertEquals( 1, child.getDependencies().size() );
        Dependency dep2 = (Dependency) child.getDependencies().get( 0 );
        assertEquals( dep.getManagementKey(), dep2.getManagementKey() );
    }
    public void testShouldMergeTwoPluginDependenciesOnMergeDupePluginDefs()
    {
        PluginContainer first = new PluginContainer();
        Plugin fPlugin = createPlugin( "g", "a", "1", Collections.EMPTY_MAP );
        Dependency fDep = new Dependency();
        fDep.setGroupId( "group" );
        fDep.setArtifactId( "artifact" );
        fDep.setVersion( "1" );
        first.addPlugin( fPlugin );
        fPlugin.addDependency( fDep );
        Plugin sPlugin = createPlugin( "g", "a", "1", Collections.EMPTY_MAP );
        Dependency sDep = new Dependency();
        sDep.setGroupId( "group" );
        sDep.setArtifactId( "artifact2" );
        sDep.setVersion( "1" );
        first.addPlugin( sPlugin );
        sPlugin.addDependency( sDep );
        ModelUtils.mergeDuplicatePluginDefinitions( first );
        assertEquals( 2, ((Plugin)first.getPlugins().get( 0 ) ).getDependencies().size() );
    }
    public void testShouldNotMergePluginExecutionWhenExecInheritedIsFalseAndTreatAsInheritanceIsTrue()
    {
        String gid = "group";
        String aid = "artifact";
        String ver = "1";
        PluginContainer parent = new PluginContainer();
        Plugin pParent = createPlugin( gid, aid, ver, Collections.EMPTY_MAP );
        pParent.setInherited( Boolean.toString( true ) );
        PluginExecution eParent = new PluginExecution();
        String testId = "test";
        eParent.setId( testId );
        eParent.addGoal( "run" );
        eParent.setPhase( "initialize" );
        eParent.setInherited( Boolean.toString( false ) );
        pParent.addExecution( eParent );
        parent.addPlugin( pParent );
        PluginContainer child = new PluginContainer();
        Plugin pChild = createPlugin( gid, aid, ver, Collections.EMPTY_MAP );
        PluginExecution eChild = new PluginExecution();
        eChild.setId( "child-specified" );
        eChild.addGoal( "child" );
        eChild.setPhase( "compile" );
        pChild.addExecution( eChild );
        child.addPlugin( pChild );
        ModelUtils.mergePluginDefinitions( pChild, pParent, true );
        Map executionMap = pChild.getExecutionsAsMap();
        assertNull( "test execution should not be inherited from parent.", executionMap.get( testId ) );
    }
    public void testShouldNotMergePluginExecutionWhenPluginInheritedIsFalseAndTreatAsInheritanceIsTrue()
    {
        String gid = "group";
        String aid = "artifact";
        String ver = "1";
        PluginContainer parent = new PluginContainer();
        Plugin pParent = createPlugin( gid, aid, ver, Collections.EMPTY_MAP );
        pParent.setInherited( Boolean.toString( false ) );
        PluginExecution eParent = new PluginExecution();
        String testId = "test";
        eParent.setId( testId );
        eParent.addGoal( "run" );
        eParent.setPhase( "initialize" );
        eParent.setInherited( Boolean.toString( true ) );
        pParent.addExecution( eParent );
        parent.addPlugin( pParent );
        PluginContainer child = new PluginContainer();
        Plugin pChild = createPlugin( gid, aid, ver, Collections.EMPTY_MAP );
        PluginExecution eChild = new PluginExecution();
        eChild.setId( "child-specified" );
        eChild.addGoal( "child" );
        eChild.setPhase( "compile" );
        pChild.addExecution( eChild );
        child.addPlugin( pChild );
        ModelUtils.mergePluginDefinitions( pChild, pParent, true );
        Map executionMap = pChild.getExecutionsAsMap();
        assertNull( "test execution should not be inherited from parent.", executionMap.get( testId ) );
    }
    public void testShouldMergePluginExecutionWhenExecInheritedIsTrueAndTreatAsInheritanceIsTrue()
    {
        String gid = "group";
        String aid = "artifact";
        String ver = "1";
        PluginContainer parent = new PluginContainer();
        Plugin pParent = createPlugin( gid, aid, ver, Collections.EMPTY_MAP );
        pParent.setInherited( Boolean.toString( true ) );
        PluginExecution eParent = new PluginExecution();
        String testId = "test";
        eParent.setId( testId );
        eParent.addGoal( "run" );
        eParent.setPhase( "initialize" );
        eParent.setInherited( Boolean.toString( true ) );
        pParent.addExecution( eParent );
        parent.addPlugin( pParent );
        PluginContainer child = new PluginContainer();
        Plugin pChild = createPlugin( gid, aid, ver, Collections.EMPTY_MAP );
        PluginExecution eChild = new PluginExecution();
        eChild.setId( "child-specified" );
        eChild.addGoal( "child" );
        eChild.setPhase( "compile" );
        pChild.addExecution( eChild );
        child.addPlugin( pChild );
        ModelUtils.mergePluginDefinitions( pChild, pParent, true );
        Map executionMap = pChild.getExecutionsAsMap();
        assertNotNull( "test execution should be inherited from parent.", executionMap.get( testId ) );
    }
}
