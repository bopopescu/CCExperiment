package org.apache.maven.classrealm;
import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.classrealm.ClassRealmRequest.RealmType;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.MutablePlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.classworlds.realm.ClassRealm;
import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.aether.artifact.Artifact;
@Component( role = ClassRealmManager.class )
public class DefaultClassRealmManager
    implements ClassRealmManager
{
    @Requirement
    private Logger logger;
    @Requirement
    protected PlexusContainer container;
    private ClassRealm mavenRealm;
    private ClassWorld getClassWorld()
    {
        return ( (MutablePlexusContainer) container ).getClassWorld();
    }
    private ClassRealm newRealm( String id )
    {
        ClassWorld world = getClassWorld();
        synchronized ( world )
        {
            String realmId = id;
            Random random = new Random();
            while ( true )
            {
                try
                {
                    ClassRealm classRealm = world.newRealm( realmId, null );
                    if ( logger.isDebugEnabled() )
                    {
                        logger.debug( "Created new class realm " + realmId );
                    }
                    return classRealm;
                }
                catch ( DuplicateRealmException e )
                {
                    realmId = id + '-' + random.nextInt();
                }
            }
        }
    }
    public synchronized ClassRealm getMavenApiRealm()
    {
        if ( mavenRealm == null )
        {
            mavenRealm = newRealm( "maven.api" );
            List<ClassRealmConstituent> constituents = new ArrayList<ClassRealmConstituent>();
            List<String> parentImports = new ArrayList<String>();
            Map<String, ClassLoader> foreignImports = new HashMap<String, ClassLoader>();
            importMavenApi( foreignImports );
            callDelegates( mavenRealm, RealmType.Core, mavenRealm.getParentClassLoader(), parentImports,
                           foreignImports, constituents );
            wireRealm( mavenRealm, parentImports, foreignImports );
            populateRealm( mavenRealm, constituents );
        }
        return mavenRealm;
    }
    private void importMavenApi( Map<String, ClassLoader> imports )
    {
        ClassRealm coreRealm = getCoreRealm();
        imports.put( "org.apache.maven.*", coreRealm );
        imports.put( "org.apache.maven.artifact", coreRealm );
        imports.put( "org.apache.maven.classrealm", coreRealm );
        imports.put( "org.apache.maven.cli", coreRealm );
        imports.put( "org.apache.maven.configuration", coreRealm );
        imports.put( "org.apache.maven.exception", coreRealm );
        imports.put( "org.apache.maven.execution", coreRealm );
        imports.put( "org.apache.maven.lifecycle", coreRealm );
        imports.put( "org.apache.maven.model", coreRealm );
        imports.put( "org.apache.maven.monitor", coreRealm );
        imports.put( "org.apache.maven.plugin", coreRealm );
        imports.put( "org.apache.maven.profiles", coreRealm );
        imports.put( "org.apache.maven.project", coreRealm );
        imports.put( "org.apache.maven.reporting", coreRealm );
        imports.put( "org.apache.maven.repository", coreRealm );
        imports.put( "org.apache.maven.rtinfo", coreRealm );
        imports.put( "org.apache.maven.settings", coreRealm );
        imports.put( "org.apache.maven.toolchain", coreRealm );
        imports.put( "org.apache.maven.usability", coreRealm );
        imports.put( "org.apache.maven.wagon.*", coreRealm );
        imports.put( "org.apache.maven.wagon.authentication", coreRealm );
        imports.put( "org.apache.maven.wagon.authorization", coreRealm );
        imports.put( "org.apache.maven.wagon.events", coreRealm );
        imports.put( "org.apache.maven.wagon.observers", coreRealm );
        imports.put( "org.apache.maven.wagon.proxy", coreRealm );
        imports.put( "org.apache.maven.wagon.repository", coreRealm );
        imports.put( "org.apache.maven.wagon.resource", coreRealm );
        imports.put( "org.sonatype.aether.*", coreRealm );
        imports.put( "org.sonatype.aether.artifact", coreRealm );
        imports.put( "org.sonatype.aether.collection", coreRealm );
        imports.put( "org.sonatype.aether.deployment", coreRealm );
        imports.put( "org.sonatype.aether.graph", coreRealm );
        imports.put( "org.sonatype.aether.impl", coreRealm );
        imports.put( "org.sonatype.aether.installation", coreRealm );
        imports.put( "org.sonatype.aether.metadata", coreRealm );
        imports.put( "org.sonatype.aether.repository", coreRealm );
        imports.put( "org.sonatype.aether.resolution", coreRealm );
        imports.put( "org.sonatype.aether.spi", coreRealm );
        imports.put( "org.sonatype.aether.transfer", coreRealm );
        imports.put( "org.sonatype.aether.version", coreRealm );
        imports.put( "org.codehaus.plexus.classworlds", coreRealm );
        imports.put( "org.codehaus.classworlds", coreRealm );
        imports.put( "org.codehaus.plexus.*", coreRealm );
        imports.put( "org.codehaus.plexus.component", coreRealm );
        imports.put( "org.codehaus.plexus.configuration", coreRealm );
        imports.put( "org.codehaus.plexus.container", coreRealm );
        imports.put( "org.codehaus.plexus.context", coreRealm );
        imports.put( "org.codehaus.plexus.lifecycle", coreRealm );
        imports.put( "org.codehaus.plexus.logging", coreRealm );
        imports.put( "org.codehaus.plexus.personality", coreRealm );
        imports.put( "org.codehaus.plexus.util.xml.Xpp3Dom", coreRealm );
        imports.put( "org.codehaus.plexus.util.xml.pull.XmlPullParser", coreRealm );
        imports.put( "org.codehaus.plexus.util.xml.pull.XmlPullParserException", coreRealm );
        imports.put( "org.codehaus.plexus.util.xml.pull.XmlSerializer", coreRealm );
    }
    private ClassRealm createRealm( String baseRealmId, RealmType type, ClassLoader parent, List<String> parentImports,
                                    Map<String, ClassLoader> foreignImports, List<Artifact> artifacts )
    {
        Set<String> artifactIds = new LinkedHashSet<String>();
        List<ClassRealmConstituent> constituents = new ArrayList<ClassRealmConstituent>();
        if ( artifacts != null )
        {
            for ( Artifact artifact : artifacts )
            {
                artifactIds.add( getId( artifact ) );
                if ( artifact.getFile() != null )
                {
                    constituents.add( new ArtifactClassRealmConstituent( artifact ) );
                }
            }
        }
        if ( parentImports != null )
        {
            parentImports = new ArrayList<String>( parentImports );
        }
        else
        {
            parentImports = new ArrayList<String>();
        }
        if ( foreignImports != null )
        {
            foreignImports = new TreeMap<String, ClassLoader>( foreignImports );
        }
        else
        {
            foreignImports = new TreeMap<String, ClassLoader>();
        }
        ClassRealm classRealm = newRealm( baseRealmId );
        if ( parent != null )
        {
            classRealm.setParentClassLoader( parent );
        }
        callDelegates( classRealm, type, parent, parentImports, foreignImports, constituents );
        wireRealm( classRealm, parentImports, foreignImports );
        Set<String> includedIds = populateRealm( classRealm, constituents );
        if ( logger.isDebugEnabled() )
        {
            artifactIds.removeAll( includedIds );
            for ( String id : artifactIds )
            {
                logger.debug( "  Excluded: " + id );
            }
        }
        return classRealm;
    }
    public ClassRealm getCoreRealm()
    {
        return container.getContainerRealm();
    }
    public ClassRealm createProjectRealm( Model model, List<Artifact> artifacts )
    {
        if ( model == null )
        {
            throw new IllegalArgumentException( "model missing" );
        }
        ClassLoader parent = getMavenApiRealm();
        return createRealm( getKey( model ), RealmType.Project, parent, null, null, artifacts );
    }
    private static String getKey( Model model )
    {
        return "project>" + model.getGroupId() + ":" + model.getArtifactId() + ":" + model.getVersion();
    }
    public ClassRealm createExtensionRealm( Plugin plugin, List<Artifact> artifacts )
    {
        if ( plugin == null )
        {
            throw new IllegalArgumentException( "extension plugin missing" );
        }
        ClassLoader parent = ClassLoader.getSystemClassLoader();
        Map<String, ClassLoader> foreignImports =
            Collections.<String, ClassLoader> singletonMap( "", getMavenApiRealm() );
        return createRealm( getKey( plugin, true ), RealmType.Extension, parent, null, foreignImports, artifacts );
    }
    public ClassRealm createPluginRealm( Plugin plugin, ClassLoader parent, List<String> parentImports,
                                         Map<String, ClassLoader> foreignImports, List<Artifact> artifacts )
    {
        if ( plugin == null )
        {
            throw new IllegalArgumentException( "plugin missing" );
        }
        if ( parent == null )
        {
            parent = ClassLoader.getSystemClassLoader();
        }
        return createRealm( getKey( plugin, false ), RealmType.Plugin, parent, parentImports, foreignImports, artifacts );
    }
    private static String getKey( Plugin plugin, boolean extension )
    {
        String version = ArtifactUtils.toSnapshotVersion( plugin.getVersion() );
        return ( extension ? "extension>" : "plugin>" ) + plugin.getGroupId() + ":" + plugin.getArtifactId() + ":"
            + version;
    }
    private static String getId( Artifact artifact )
    {
        return getId( artifact.getGroupId(), artifact.getArtifactId(), artifact.getExtension(),
                      artifact.getClassifier(), artifact.getBaseVersion() );
    }
    private static String getId( ClassRealmConstituent constituent )
    {
        return getId( constituent.getGroupId(), constituent.getArtifactId(), constituent.getType(),
                      constituent.getClassifier(), constituent.getVersion() );
    }
    private static String getId( String gid, String aid, String type, String cls, String ver )
    {
        return gid + ':' + aid + ':' + type + ( StringUtils.isNotEmpty( cls ) ? ':' + cls : "" ) + ':' + ver;
    }
    private List<ClassRealmManagerDelegate> getDelegates()
    {
        try
        {
            return container.lookupList( ClassRealmManagerDelegate.class );
        }
        catch ( ComponentLookupException e )
        {
            logger.error( "Failed to lookup class realm delegates: " + e.getMessage(), e );
            return Collections.emptyList();
        }
    }
    private void callDelegates( ClassRealm classRealm, RealmType type, ClassLoader parent, List<String> parentImports,
                                Map<String, ClassLoader> foreignImports, List<ClassRealmConstituent> constituents )
    {
        List<ClassRealmManagerDelegate> delegates = getDelegates();
        if ( !delegates.isEmpty() )
        {
            ClassRealmRequest request =
                new DefaultClassRealmRequest( type, parent, parentImports, foreignImports, constituents );
            for ( ClassRealmManagerDelegate delegate : delegates )
            {
                try
                {
                    delegate.setupRealm( classRealm, request );
                }
                catch ( Exception e )
                {
                    logger.error( delegate.getClass().getName() + " failed to setup class realm " + classRealm + ": "
                        + e.getMessage(), e );
                }
            }
        }
    }
    private Set<String> populateRealm( ClassRealm classRealm, List<ClassRealmConstituent> constituents )
    {
        Set<String> includedIds = new LinkedHashSet<String>();
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Populating class realm " + classRealm.getId() );
        }
        for ( ClassRealmConstituent constituent : constituents )
        {
            File file = constituent.getFile();
            String id = getId( constituent );
            includedIds.add( id );
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "  Included: " + id );
            }
            try
            {
                classRealm.addURL( file.toURI().toURL() );
            }
            catch ( MalformedURLException e )
            {
                logger.error( e.getMessage(), e );
            }
        }
        return includedIds;
    }
    private void wireRealm( ClassRealm classRealm, List<String> parentImports, Map<String, ClassLoader> foreignImports )
    {
        if ( foreignImports != null && !foreignImports.isEmpty() )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Importing foreign packages into class realm " + classRealm.getId() );
            }
            for ( Map.Entry<String, ClassLoader> entry : foreignImports.entrySet() )
            {
                ClassLoader importedRealm = entry.getValue();
                String imp = entry.getKey();
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "  Imported: " + imp + " < " + getId( importedRealm ) );
                }
                classRealm.importFrom( importedRealm, imp );
            }
        }
        if ( parentImports != null && !parentImports.isEmpty() )
        {
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Importing parent packages into class realm " + classRealm.getId() );
            }
            for ( String imp : parentImports )
            {
                if ( logger.isDebugEnabled() )
                {
                    logger.debug( "  Imported: " + imp + " < " + getId( classRealm.getParentClassLoader() ) );
                }
                classRealm.importFromParent( imp );
            }
        }
    }
    private String getId( ClassLoader classLoader )
    {
        if ( classLoader instanceof ClassRealm )
        {
            return ( (ClassRealm) classLoader ).getId();
        }
        return String.valueOf( classLoader );
    }
}
