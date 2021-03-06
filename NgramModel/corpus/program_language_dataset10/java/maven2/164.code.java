package org.apache.maven.plugin.version;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.metadata.ResolutionGroup;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.execution.RuntimeInformation;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.plugin.InvalidPluginException;
import org.apache.maven.plugin.registry.MavenPluginRegistryBuilder;
import org.apache.maven.plugin.registry.PluginRegistry;
import org.apache.maven.plugin.registry.PluginRegistryUtils;
import org.apache.maven.plugin.registry.TrackableBase;
import org.apache.maven.plugin.registry.io.xpp3.PluginRegistryXpp3Writer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.settings.RuntimeInfo;
import org.apache.maven.settings.Settings;
import org.codehaus.plexus.components.interactivity.InputHandler;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
public class DefaultPluginVersionManager
    extends AbstractLogEnabled
    implements PluginVersionManager
{
    private MavenPluginRegistryBuilder mavenPluginRegistryBuilder;
    private ArtifactFactory artifactFactory;
    private InputHandler inputHandler;
    private ArtifactMetadataSource artifactMetadataSource;
    private PluginRegistry pluginRegistry;
    private MavenProjectBuilder mavenProjectBuilder;
    private RuntimeInformation runtimeInformation;
    private Map resolvedMetaVersions = new HashMap();
    public String resolvePluginVersion( String groupId, String artifactId, MavenProject project, Settings settings,
                                        ArtifactRepository localRepository )
        throws PluginVersionResolutionException, InvalidPluginException, PluginVersionNotFoundException
    {
        return resolvePluginVersion( groupId, artifactId, project, settings, localRepository, false );
    }
    public String resolveReportPluginVersion( String groupId, String artifactId, MavenProject project,
                                              Settings settings, ArtifactRepository localRepository )
        throws PluginVersionResolutionException, InvalidPluginException, PluginVersionNotFoundException
    {
        return resolvePluginVersion( groupId, artifactId, project, settings, localRepository, true );
    }
    private String resolvePluginVersion( String groupId, String artifactId, MavenProject project, Settings settings,
                                         ArtifactRepository localRepository, boolean resolveAsReportPlugin )
        throws PluginVersionResolutionException, InvalidPluginException, PluginVersionNotFoundException
    {
        String version = getVersionFromPluginConfig( groupId, artifactId, project, resolveAsReportPlugin );
        if ( version == null )
        {
            if ( project.getProjectReferences() != null )
            {
                String refId = ArtifactUtils.versionlessKey( groupId, artifactId );
                MavenProject ref = (MavenProject) project.getProjectReferences().get( refId );
                if ( ref != null )
                {
                    version = ref.getVersion();
                }
            }
        }
        String updatedVersion = null;
        boolean promptToPersist = false;
        RuntimeInfo settingsRTInfo = settings.getRuntimeInfo();
        Boolean pluginUpdateOverride = settingsRTInfo.getPluginUpdateOverride();
        if ( StringUtils.isEmpty( version ) && settings.isUsePluginRegistry() )
        {
            version = resolveExistingFromPluginRegistry( groupId, artifactId );
            if ( StringUtils.isNotEmpty( version ) )
            {
                if ( Boolean.TRUE.equals( pluginUpdateOverride )
                    || ( !Boolean.FALSE.equals( pluginUpdateOverride ) && shouldCheckForUpdates( groupId, artifactId ) ) )
                {
                    updatedVersion =
                        resolveMetaVersion( groupId, artifactId, project, localRepository, Artifact.LATEST_VERSION );
                    if ( StringUtils.isNotEmpty( updatedVersion ) && !updatedVersion.equals( version ) )
                    {
                        boolean isRejected = checkForRejectedStatus( groupId, artifactId, updatedVersion );
                        promptToPersist = !isRejected;
                        if ( isRejected )
                        {
                            updatedVersion = null;
                        }
                        else
                        {
                            getLogger().info(
                                "Plugin \'" + constructPluginKey( groupId, artifactId ) + "\' has updates." );
                        }
                    }
                }
            }
        }
        boolean forcePersist = false;
        if ( StringUtils.isEmpty( version ) )
        {
            version = resolveMetaVersion( groupId, artifactId, project, localRepository, Artifact.LATEST_VERSION );
            if ( version != null )
            {
                updatedVersion = version;
                forcePersist = true;
                promptToPersist = false;
            }
        }
        if ( StringUtils.isEmpty( version ) )
        {
            version = resolveMetaVersion( groupId, artifactId, project, localRepository, Artifact.RELEASE_VERSION );
            if ( version != null )
            {
                updatedVersion = version;
                forcePersist = true;
                promptToPersist = false;
            }
        }
        if ( StringUtils.isEmpty( version ) && project.getGroupId().equals( groupId )
            && project.getArtifactId().equals( artifactId ) )
        {
            version = project.getVersion();
        }
        if ( StringUtils.isEmpty( version ) )
        {
            throw new PluginVersionNotFoundException( groupId, artifactId );
        }
        if ( settings.isUsePluginRegistry() )
        {
            boolean inInteractiveMode = settings.isInteractiveMode();
            String s = getPluginRegistry( groupId, artifactId ).getAutoUpdate();
            boolean autoUpdate = true;
            if ( s != null )
            {
                autoUpdate = Boolean.valueOf( s ).booleanValue();
            }
            boolean persistUpdate = forcePersist || ( promptToPersist
                && !Boolean.FALSE.equals( pluginUpdateOverride ) && ( inInteractiveMode || autoUpdate ) );
            Boolean applyToAll = settings.getRuntimeInfo().getApplyToAllPluginUpdates();
            promptToPersist =
                promptToPersist && pluginUpdateOverride == null && applyToAll == null && inInteractiveMode;
            if ( promptToPersist )
            {
                persistUpdate = promptToPersistPluginUpdate( version, updatedVersion, groupId, artifactId, settings );
            }
            if ( !Boolean.FALSE.equals( applyToAll ) && persistUpdate )
            {
                updatePluginVersionInRegistry( groupId, artifactId, updatedVersion );
                version = updatedVersion;
            }
            else if ( promptToPersist )
            {
                addNewVersionToRejectedListInExisting( groupId, artifactId, updatedVersion );
            }
        }
        return version;
    }
    private boolean shouldCheckForUpdates( String groupId, String artifactId )
        throws PluginVersionResolutionException
    {
        PluginRegistry pluginRegistry = getPluginRegistry( groupId, artifactId );
        org.apache.maven.plugin.registry.Plugin plugin = getPlugin( groupId, artifactId, pluginRegistry );
        if ( plugin == null )
        {
            return true;
        }
        else
        {
            String lastChecked = plugin.getLastChecked();
            if ( StringUtils.isEmpty( lastChecked ) )
            {
                return true;
            }
            else
            {
                SimpleDateFormat format =
                    new SimpleDateFormat( org.apache.maven.plugin.registry.Plugin.LAST_CHECKED_DATE_FORMAT );
                try
                {
                    Date lastCheckedDate = format.parse( lastChecked );
                    return IntervalUtils.isExpired( pluginRegistry.getUpdateInterval(), lastCheckedDate );
                }
                catch ( ParseException e )
                {
                    getLogger().warn( "Last-checked date for plugin {" + constructPluginKey( groupId, artifactId )
                        + "} is invalid. Checking for updates." );
                    return true;
                }
            }
        }
    }
    private boolean checkForRejectedStatus( String groupId, String artifactId, String version )
        throws PluginVersionResolutionException
    {
        PluginRegistry pluginRegistry = getPluginRegistry( groupId, artifactId );
        org.apache.maven.plugin.registry.Plugin plugin = getPlugin( groupId, artifactId, pluginRegistry );
        return plugin.getRejectedVersions().contains( version );
    }
    private boolean promptToPersistPluginUpdate( String version, String updatedVersion, String groupId,
                                                 String artifactId, Settings settings )
        throws PluginVersionResolutionException
    {
        try
        {
            StringBuffer message = new StringBuffer();
            if ( version != null && version.equals( updatedVersion ) )
            {
                message.append( "Unregistered plugin detected.\n\n" );
            }
            else
            {
                message.append( "New plugin version detected.\n\n" );
            }
            message.append( "Group ID: " ).append( groupId ).append( "\n" );
            message.append( "Artifact ID: " ).append( artifactId ).append( "\n" );
            message.append( "\n" );
            if ( version != null && !version.equals( updatedVersion ) )
            {
                message.append( "Registered Version: " ).append( version ).append( "\n" );
            }
            message.append( "Detected plugin version: " ).append( updatedVersion ).append( "\n" );
            message.append( "\n" );
            message.append( "Would you like to use this new version from now on? ( [Y]es, [n]o, [a]ll, n[o]ne ) " );
            getLogger().info( message.toString() );
            String persistAnswer = inputHandler.readLine();
            boolean shouldPersist = true;
            if ( !StringUtils.isEmpty( persistAnswer ) )
            {
                persistAnswer = persistAnswer.toLowerCase();
                if ( persistAnswer.startsWith( "y" ) )
                {
                    shouldPersist = true;
                }
                else if ( persistAnswer.startsWith( "a" ) )
                {
                    shouldPersist = true;
                    settings.getRuntimeInfo().setApplyToAllPluginUpdates( Boolean.TRUE );
                }
                else if ( persistAnswer.indexOf( "o" ) > -1 )
                {
                    settings.getRuntimeInfo().setApplyToAllPluginUpdates( Boolean.FALSE );
                }
                else if ( persistAnswer.startsWith( "n" ) )
                {
                    shouldPersist = false;
                }
                else
                {
                    shouldPersist = true;
                }
            }
            if ( shouldPersist )
            {
                getLogger().info( "Updating plugin version to " + updatedVersion );
            }
            else
            {
                getLogger().info( "NOT updating plugin version to " + updatedVersion );
            }
            return shouldPersist;
        }
        catch ( IOException e )
        {
            throw new PluginVersionResolutionException( groupId, artifactId, "Can't read user input.", e );
        }
    }
    private void addNewVersionToRejectedListInExisting( String groupId, String artifactId, String rejectedVersion )
        throws PluginVersionResolutionException
    {
        PluginRegistry pluginRegistry = getPluginRegistry( groupId, artifactId );
        org.apache.maven.plugin.registry.Plugin plugin = getPlugin( groupId, artifactId, pluginRegistry );
        String pluginKey = constructPluginKey( groupId, artifactId );
        if ( plugin != null && !TrackableBase.GLOBAL_LEVEL.equals( plugin.getSourceLevel() ) )
        {
            plugin.addRejectedVersion( rejectedVersion );
            writeUserRegistry( groupId, artifactId, pluginRegistry );
            getLogger().warn( "Plugin version: " + rejectedVersion + " added to your rejectedVersions list.\n" +
                "You will not be prompted for this version again.\n\nPlugin: " + pluginKey );
        }
        else
        {
            getLogger().warn(
                "Cannot add rejectedVersion entry for: " + rejectedVersion + ".\n\nPlugin: " + pluginKey );
        }
    }
    private String resolveExistingFromPluginRegistry( String groupId, String artifactId )
        throws PluginVersionResolutionException
    {
        PluginRegistry pluginRegistry = getPluginRegistry( groupId, artifactId );
        org.apache.maven.plugin.registry.Plugin plugin = getPlugin( groupId, artifactId, pluginRegistry );
        String version = null;
        if ( plugin != null )
        {
            version = plugin.getUseVersion();
        }
        return version;
    }
    private org.apache.maven.plugin.registry.Plugin getPlugin( String groupId, String artifactId,
                                                               PluginRegistry pluginRegistry )
    {
        Map pluginsByKey;
        if ( pluginRegistry != null )
        {
            pluginsByKey = pluginRegistry.getPluginsByKey();
        }
        else
        {
            pluginsByKey = new HashMap();
        }
        String pluginKey = constructPluginKey( groupId, artifactId );
        return (org.apache.maven.plugin.registry.Plugin) pluginsByKey.get( pluginKey );
    }
    private String constructPluginKey( String groupId, String artifactId )
    {
        return groupId + ":" + artifactId;
    }
    private String getVersionFromPluginConfig( String groupId, String artifactId, MavenProject project,
                                               boolean resolveAsReportPlugin )
    {
        String version = null;
        if ( resolveAsReportPlugin )
        {
            if ( project.getReportPlugins() != null )
            {
                for ( Iterator it = project.getReportPlugins().iterator(); it.hasNext() && version == null; )
                {
                    ReportPlugin plugin = (ReportPlugin) it.next();
                    if ( groupId.equals( plugin.getGroupId() ) && artifactId.equals( plugin.getArtifactId() ) )
                    {
                        version = plugin.getVersion();
                    }
                }
            }
        }
        else
        {
            if ( project.getBuildPlugins() != null )
            {
                for ( Iterator it = project.getBuildPlugins().iterator(); it.hasNext() && version == null; )
                {
                    Plugin plugin = (Plugin) it.next();
                    if ( groupId.equals( plugin.getGroupId() ) && artifactId.equals( plugin.getArtifactId() ) )
                    {
                        version = plugin.getVersion();
                    }
                }
            }
        }
        return version;
    }
    private void updatePluginVersionInRegistry( String groupId, String artifactId, String version )
        throws PluginVersionResolutionException
    {
        PluginRegistry pluginRegistry = getPluginRegistry( groupId, artifactId );
        org.apache.maven.plugin.registry.Plugin plugin = getPlugin( groupId, artifactId, pluginRegistry );
        if ( plugin != null )
        {
            if ( PluginRegistry.GLOBAL_LEVEL.equals( plugin.getSourceLevel() ) )
            {
                getLogger().warn( "Cannot update registered version for plugin {" + groupId + ":" + artifactId +
                    "}; it is specified in the global registry." );
            }
            else
            {
                plugin.setUseVersion( version );
                SimpleDateFormat format =
                    new SimpleDateFormat( org.apache.maven.plugin.registry.Plugin.LAST_CHECKED_DATE_FORMAT );
                plugin.setLastChecked( format.format( new Date() ) );
            }
        }
        else
        {
            plugin = new org.apache.maven.plugin.registry.Plugin();
            plugin.setGroupId( groupId );
            plugin.setArtifactId( artifactId );
            plugin.setUseVersion( version );
            pluginRegistry.addPlugin( plugin );
            pluginRegistry.flushPluginsByKey();
        }
        writeUserRegistry( groupId, artifactId, pluginRegistry );
    }
    private void writeUserRegistry( String groupId, String artifactId, PluginRegistry pluginRegistry )
    {
        File pluginRegistryFile = pluginRegistry.getRuntimeInfo().getFile();
        PluginRegistry extractedUserRegistry = PluginRegistryUtils.extractUserPluginRegistry( pluginRegistry );
        if ( extractedUserRegistry != null )
        {
            Writer fWriter = null;
            try
            {
                pluginRegistryFile.getParentFile().mkdirs();
                fWriter = WriterFactory.newXmlWriter( pluginRegistryFile );
                PluginRegistryXpp3Writer writer = new PluginRegistryXpp3Writer();
                writer.write( fWriter, extractedUserRegistry );
            }
            catch ( IOException e )
            {
                getLogger().warn(
                    "Cannot rewrite user-level plugin-registry.xml with new plugin version of plugin: \'" + groupId
                        + ":" + artifactId + "\'.", e );
            }
            finally
            {
                IOUtil.close( fWriter );
            }
        }
    }
    private PluginRegistry getPluginRegistry( String groupId, String artifactId )
        throws PluginVersionResolutionException
    {
        if ( pluginRegistry == null )
        {
            try
            {
                pluginRegistry = mavenPluginRegistryBuilder.buildPluginRegistry();
            }
            catch ( IOException e )
            {
                throw new PluginVersionResolutionException( groupId, artifactId,
                                                            "Error reading plugin registry: " + e.getMessage(), e );
            }
            catch ( XmlPullParserException e )
            {
                throw new PluginVersionResolutionException( groupId, artifactId,
                                                            "Error parsing plugin registry: " + e.getMessage(), e );
            }
            if ( pluginRegistry == null )
            {
                pluginRegistry = mavenPluginRegistryBuilder.createUserPluginRegistry();
            }
        }
        return pluginRegistry;
    }
    private String resolveMetaVersion( String groupId, String artifactId, MavenProject project,
                                       ArtifactRepository localRepository, String metaVersionId )
        throws PluginVersionResolutionException, InvalidPluginException
    {
        Artifact artifact = artifactFactory.createProjectArtifact( groupId, artifactId, metaVersionId );
        String key = artifact.getDependencyConflictId();
        if ( resolvedMetaVersions.containsKey( key ) )
        {
            return (String) resolvedMetaVersions.get( key );
        }
        String version = null;
        try
        {
            ResolutionGroup resolutionGroup =
                artifactMetadataSource.retrieve( artifact, localRepository, project.getPluginArtifactRepositories() );
            artifact = resolutionGroup.getPomArtifact();
        }
        catch ( ArtifactMetadataRetrievalException e )
        {
            throw new PluginVersionResolutionException( groupId, artifactId, e.getMessage(), e );
        }
        String artifactVersion = artifact.getVersion();
        if ( artifact.getFile() != null )
        {
            boolean pluginValid = false;
            while ( !pluginValid && artifactVersion != null )
            {
                pluginValid = true;
                MavenProject pluginProject;
                try
                {
                    artifact = artifactFactory.createProjectArtifact( groupId, artifactId, artifactVersion );
                    pluginProject = mavenProjectBuilder.buildFromRepository( artifact,
                                                                             project.getPluginArtifactRepositories(),
                                                                             localRepository, false );
                }
                catch ( ProjectBuildingException e )
                {
                    throw new InvalidPluginException( "Unable to build project information for plugin '"
                        + ArtifactUtils.versionlessKey( groupId, artifactId ) + "': " + e.getMessage(), e );
                }
                if ( pluginProject.getPrerequisites() != null && pluginProject.getPrerequisites().getMaven() != null )
                {
                    DefaultArtifactVersion requiredVersion =
                        new DefaultArtifactVersion( pluginProject.getPrerequisites().getMaven() );
                    if ( runtimeInformation.getApplicationVersion().compareTo( requiredVersion ) < 0 )
                    {
                        getLogger().info( "Ignoring available plugin update: " + artifactVersion
                            + " as it requires Maven version " + requiredVersion );
                        VersionRange vr;
                        try
                        {
                            vr = VersionRange.createFromVersionSpec( "(," + artifactVersion + ")" );
                        }
                        catch ( InvalidVersionSpecificationException e )
                        {
                            throw new PluginVersionResolutionException( groupId, artifactId,
                                                                        "Error getting available plugin versions: "
                                                                            + e.getMessage(), e );
                        }
                        getLogger().debug( "Trying " + vr );
                        try
                        {
                            List versions = artifactMetadataSource.retrieveAvailableVersions( artifact, localRepository,
                                                                                              project.getPluginArtifactRepositories() );
                            ArtifactVersion v = vr.matchVersion( versions );
                            artifactVersion = v != null ? v.toString() : null;
                        }
                        catch ( ArtifactMetadataRetrievalException e )
                        {
                            throw new PluginVersionResolutionException( groupId, artifactId,
                                                                        "Error getting available plugin versions: "
                                                                            + e.getMessage(), e );
                        }
                        if ( artifactVersion != null )
                        {
                            getLogger().debug( "Found " + artifactVersion );
                            pluginValid = false;
                        }
                    }
                }
            }
        }
        if ( !metaVersionId.equals( artifactVersion ) )
        {
            version = artifactVersion;
            resolvedMetaVersions.put( key, version );
        }
        return version;
    }
}
