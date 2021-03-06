package org.apache.maven;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import org.apache.maven.artifact.manager.DefaultWagonManager;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.manager.WagonProviderMapping;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.resolver.DefaultArtifactResolver;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.execution.BuildFailure;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.execution.ReactorManager;
import org.apache.maven.execution.RuntimeInformation;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.lifecycle.LifecycleExecutor;
import org.apache.maven.model.Profile;
import org.apache.maven.monitor.event.DefaultEventDispatcher;
import org.apache.maven.monitor.event.EventDispatcher;
import org.apache.maven.monitor.event.MavenEvents;
import org.apache.maven.profiles.ProfileManager;
import org.apache.maven.profiles.activation.ProfileActivationException;
import org.apache.maven.project.DuplicateProjectException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.MissingProjectException;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.artifact.MavenMetadataSource;
import org.apache.maven.reactor.MavenExecutionException;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Proxy;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.usability.SystemWarnings;
import org.apache.maven.usability.diagnostics.ErrorDiagnostics;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLifecycleException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.dag.CycleDetectedException;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;
public class DefaultMaven
    extends AbstractLogEnabled
    implements Maven, Contextualizable
{
    protected MavenProjectBuilder projectBuilder;
    protected LifecycleExecutor lifecycleExecutor;
    protected PlexusContainer container;
    protected ErrorDiagnostics errorDiagnostics;
    protected RuntimeInformation runtimeInformation;
    protected MavenMetadataSource mavenMetadataSource;
    private static final long MB = 1024 * 1024;
    private static final int MS_PER_SEC = 1000;
    private static final int SEC_PER_MIN = 60;
    public void execute( MavenExecutionRequest request )
        throws MavenExecutionException
    {
        EventDispatcher dispatcher = request.getEventDispatcher();
        String event = MavenEvents.REACTOR_EXECUTION;
        dispatcher.dispatchStart( event, request.getBaseDirectory() );
        ReactorManager rm;
        try
        {
            rm = doExecute( request, dispatcher );
        }
        catch ( LifecycleExecutionException e )
        {
            dispatcher.dispatchError( event, request.getBaseDirectory(), e );
            logError( e, request.isShowErrors() );
            stats( request.getStartTime() );
            line();
            throw new MavenExecutionException( e.getMessage(), e );
        }
        catch ( BuildFailureException e )
        {
            dispatcher.dispatchError( event, request.getBaseDirectory(), e );
            logFailure( e, request.isShowErrors() );
            stats( request.getStartTime() );
            line();
            throw new MavenExecutionException( e.getMessage(), e );
        }
        catch ( Throwable t )
        {
            dispatcher.dispatchError( event, request.getBaseDirectory(), t );
            logFatal( t );
            stats( request.getStartTime() );
            line();
            throw new MavenExecutionException( "Error executing project within the reactor", t );
        }
        logReactorSummary( rm );
        if ( rm.hasBuildFailures() )
        {
            logErrors( rm, request.isShowErrors() );
            if ( !ReactorManager.FAIL_NEVER.equals( rm.getFailureBehavior() ) )
            {
                dispatcher.dispatchError( event, request.getBaseDirectory(), null );
                getLogger().info( "BUILD ERRORS" );
                line();
                stats( request.getStartTime() );
                line();
                throw new MavenExecutionException( "Some builds failed" );
            }
            else
            {
                getLogger().info( " + Ignoring failures" );
            }
        }
        logSuccess( rm );
        stats( request.getStartTime() );
        line();
        dispatcher.dispatchEnd( event, request.getBaseDirectory() );
    }
    private void logErrors( ReactorManager rm, boolean showErrors )
    {
        for ( Iterator it = rm.getSortedProjects().iterator(); it.hasNext(); )
        {
            MavenProject project = (MavenProject) it.next();
            if ( rm.hasBuildFailure( project ) )
            {
                BuildFailure buildFailure = rm.getBuildFailure( project );
                getLogger().info(
                    "Error for project: " + project.getName() + " (during " + buildFailure.getTask() + ")" );
                line();
                logDiagnostics( buildFailure.getCause() );
                logTrace( buildFailure.getCause(), showErrors );
            }
        }
        if ( !showErrors )
        {
            getLogger().info( "For more information, run Maven with the -e switch" );
            line();
        }
    }
    private ReactorManager doExecute( MavenExecutionRequest request, EventDispatcher dispatcher )
        throws MavenExecutionException, BuildFailureException, LifecycleExecutionException
    {
        try
        {
            resolveParameters( request.getSettings(), request.getExecutionProperties() );
        }
        catch ( ComponentLookupException e )
        {
            throw new MavenExecutionException( "Unable to configure Maven for execution", e );
        }
        catch ( ComponentLifecycleException e )
        {
            throw new MavenExecutionException( "Unable to configure Maven for execution", e );
        }
        catch ( SettingsConfigurationException e )
        {
            throw new MavenExecutionException( "Unable to configure Maven for execution", e );
        }
        ProfileManager globalProfileManager = request.getGlobalProfileManager();
        globalProfileManager.loadSettingsProfiles( request.getSettings() );
        getLogger().info( "Scanning for projects..." );
        request.getProjectBuilderConfiguration()
               .setMetadataSource( new MavenMetadataSource( mavenMetadataSource, request.getProjectBuilderConfiguration() ) );
        boolean foundProjects = true;
        List projects = getProjects( request );
        if ( projects.isEmpty() )
        {
            projects.add( getSuperProject( request ) );
            foundProjects = false;
        }
        ReactorManager rm;
        try
        {
            String resumeFrom = request.getResumeFrom();
            List projectList = request.getSelectedProjects();
            String makeBehavior = request.getMakeBehavior();
            rm = new ReactorManager( projects, projectList, resumeFrom, makeBehavior );
            rm.setFailureBehavior( request.getFailureBehavior() );
        }
        catch ( CycleDetectedException e )
        {
            throw new BuildFailureException(
                "The projects in the reactor contain a cyclic reference: " + e.getMessage(), e );
        }
        catch ( DuplicateProjectException e )
        {
            throw new BuildFailureException( e.getMessage(), e );
        }
        catch ( MissingProjectException e )
        {
            throw new BuildFailureException( e.getMessage(), e );
        }
        validateActivatedProfiles( globalProfileManager, projects );
        if ( rm.hasMultipleProjects() )
        {
            getLogger().info( "Reactor build order: " );
            for ( Iterator i = rm.getSortedProjects().iterator(); i.hasNext(); )
            {
                MavenProject project = (MavenProject) i.next();
                getLogger().info( "  " + project.getName() );
            }
        }
        MavenSession session = createSession( request, rm );
        session.setUsingPOMsFromFilesystem( foundProjects );
        lifecycleExecutor.execute( session, rm, dispatcher );
        return rm;
    }
    private void validateActivatedProfiles( ProfileManager globalProfileManager, List projects )
    {
        if ( globalProfileManager != null )
        {
            Set activeProfileIds = new HashSet();
            for ( Iterator i = projects.iterator(); i.hasNext(); )
            {
                MavenProject project = (MavenProject) i.next();
                do
                {
                    for ( Iterator j = project.getActiveProfiles().iterator(); j.hasNext(); )
                    {
                        activeProfileIds.add( ( (Profile) j.next() ).getId() );
                    }
                    project = project.getParent();
                }
                while ( project != null );
            }
            for ( Iterator i = globalProfileManager.getExplicitlyActivatedIds().iterator(); i.hasNext(); )
            {
                String explicitProfileId = (String) i.next();
                if ( !activeProfileIds.contains( explicitProfileId ) )
                {
                    getLogger().warn( "\n\tProfile with id: \'" + explicitProfileId + "\' has not been activated.\n" );
                }
            }
        }
    }
    private MavenProject getSuperProject( MavenExecutionRequest request )
        throws MavenExecutionException
    {
        MavenProject superProject;
        try
        {
            superProject = projectBuilder.buildStandaloneSuperProject( request.getLocalRepository(), request.getGlobalProfileManager() );
        }
        catch ( ProjectBuildingException e )
        {
            throw new MavenExecutionException( e.getMessage(), e );
        }
        return superProject;
    }
    private List getProjects( MavenExecutionRequest request )
        throws MavenExecutionException, BuildFailureException
    {
        List projects;
        try
        {
            List files = getProjectFiles( request );
            projects = collectProjects( files, request, !request.isReactorActive() );
        }
        catch ( IOException e )
        {
            throw new MavenExecutionException( "Error processing projects for the reactor: " + e.getMessage(), e );
        }
        catch ( ArtifactResolutionException e )
        {
            throw new MavenExecutionException( e.getMessage(), e );
        }
        catch ( ProjectBuildingException e )
        {
            throw new MavenExecutionException( e.getMessage(), e );
        }
        catch ( ProfileActivationException e )
        {
            throw new MavenExecutionException( e.getMessage(), e );
        }
        return projects;
    }
    private void logReactorSummaryLine( String name, String status )
    {
        logReactorSummaryLine( name, status, -1 );
    }
    private void logReactorSummaryLine( String name, String status, long time )
    {
        StringBuffer messageBuffer = new StringBuffer();
        messageBuffer.append( name );
        int dotCount = 54;
        dotCount -= name.length();
        messageBuffer.append( " " );
        for ( int i = 0; i < dotCount; i++ )
        {
            messageBuffer.append( '.' );
        }
        messageBuffer.append( " " );
        messageBuffer.append( status );
        if ( time >= 0 )
        {
            messageBuffer.append( " [" );
            messageBuffer.append( getFormattedTime( time ) );
            messageBuffer.append( "]" );
        }
        getLogger().info( messageBuffer.toString() );
    }
    private static String getFormattedTime( long time )
    {
        String pattern = "s.SSS's'";
        if ( time / 60000L > 0 )
        {
            pattern = "m:s" + pattern;
            if ( time / 3600000L > 0 )
            {
                pattern = "H:m" + pattern;
            }
        }
        DateFormat fmt = new SimpleDateFormat( pattern );
        fmt.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
        return fmt.format( new Date( time ) );
    }
    private List collectProjects( List files, MavenExecutionRequest request, boolean isRoot )
        throws ArtifactResolutionException, ProjectBuildingException, ProfileActivationException,
        MavenExecutionException, BuildFailureException
    {
        List projects = new ArrayList( files.size() );
        for ( Iterator iterator = files.iterator(); iterator.hasNext(); )
        {
            File file = (File) iterator.next();
            boolean usingReleasePom = false;
            if ( RELEASE_POMv4.equals( file.getName() ) )
            {
                getLogger().info( "NOTE: Using release-pom: " + file + " in reactor build." );
                usingReleasePom = true;
            }
            MavenProject project = getProject( file, request );
            if ( isRoot )
            {
                project.setExecutionRoot( true );
            }
            if ( ( project.getPrerequisites() != null ) && ( project.getPrerequisites().getMaven() != null ) )
            {
                DefaultArtifactVersion version = new DefaultArtifactVersion( project.getPrerequisites().getMaven() );
                if ( runtimeInformation.getApplicationVersion().compareTo( version ) < 0 )
                {
                    throw new BuildFailureException( "Unable to build project '" + project.getFile()
                        + "; it requires Maven version " + version.toString() );
                }
            }
            if ( ( project.getModules() != null ) && !project.getModules().isEmpty() && request.isRecursive() )
            {
                project.setPackaging( "pom" );
                File basedir = file.getParentFile();
                List moduleFiles = new ArrayList( project.getModules().size() );
                for ( Iterator i = project.getModules().iterator(); i.hasNext(); )
                {
                    String name = (String) i.next();
                    if ( StringUtils.isEmpty( StringUtils.trim( name ) ) )
                    {
                        getLogger().warn(
                            "Empty module detected. Please check you don't have any empty module definitions in your POM." );
                        continue;
                    }
                    File moduleFile = new File( basedir, name );
                    if ( moduleFile.exists() && moduleFile.isDirectory() )
                    {
                        if ( usingReleasePom )
                        {
                            moduleFile = new File( basedir, name + "/" + Maven.RELEASE_POMv4 );
                        }
                        else
                        {
                            moduleFile = new File( basedir, name + "/" + Maven.POMv4 );
                        }
                    }
                    if ( Os.isFamily( "windows" ) )
                    {
                        try
                        {
                            moduleFile = moduleFile.getCanonicalFile();
                        }
                        catch ( IOException e )
                        {
                            throw new MavenExecutionException( "Unable to canonicalize file name " + moduleFile, e );
                        }
                    }
                    else
                    {
                        moduleFile = new File( moduleFile.toURI().normalize() );
                    }
                    moduleFiles.add( moduleFile );
                }
                List collectedProjects =
                    collectProjects( moduleFiles, request, false );
                projects.addAll( collectedProjects );
                project.setCollectedProjects( collectedProjects );
            }
            projects.add( project );
        }
        return projects;
    }
    public MavenProject getProject( File pom, ArtifactRepository localRepository, Settings settings,
                                    Properties userProperties, ProfileManager globalProfileManager )
        throws ProjectBuildingException, ArtifactResolutionException, ProfileActivationException
    {
        MavenExecutionRequest request = new DefaultMavenExecutionRequest(
                                                                      localRepository,
                                                                      settings,
                                                                      new DefaultEventDispatcher(),
                                                                      Collections.EMPTY_LIST,
                                                                      pom.getParentFile()
                                                                         .getAbsolutePath(),
                                                                      globalProfileManager,
                                                                      globalProfileManager.getRequestProperties(),
                                                                      new Properties(), false );
        return getProject( pom, request );
    }
    public MavenProject getProject( File pom, MavenExecutionRequest request )
        throws ProjectBuildingException, ArtifactResolutionException, ProfileActivationException
    {
        if ( pom.exists() )
        {
            if ( pom.length() == 0 )
            {
                throw new ProjectBuildingException( "unknown", "The file " + pom.getAbsolutePath()
                    + " you specified has zero length." );
            }
        }
        return projectBuilder.build( pom, request.getProjectBuilderConfiguration() );
    }
    protected MavenSession createSession( MavenExecutionRequest request,
                                          ReactorManager rpm )
    {
        return new MavenSession( container, request, rpm );
    }
    private void resolveParameters( Settings settings, Properties executionProperties )
        throws ComponentLookupException, ComponentLifecycleException, SettingsConfigurationException
    {
        WagonManager wagonManager = (WagonManager) container.lookup( WagonManager.ROLE );
        try
        {
            if ( settings.isOffline() )
            {
                getLogger().info( SystemWarnings.getOfflineWarning() );
                wagonManager.setOnline( false );
            }
            try
            {
                DefaultWagonManager wm = (DefaultWagonManager) wagonManager;
                String oldUserAgent = wm.getHttpUserAgent();
                int firstSpace = oldUserAgent == null ? -1 : oldUserAgent.indexOf( " " );
                StringBuffer buffer = new StringBuffer();
                buffer.append( "Apache-Maven/" );
                ArtifactVersion version = runtimeInformation.getApplicationVersion();
                if ( version != null )
                {
                    buffer.append( version.getMajorVersion() );
                    buffer.append( '.' );
                    buffer.append( version.getMinorVersion() );
                }
                else
                {
                    buffer.append( "unknown" );
                }
                buffer.append( ' ' );
                if ( firstSpace > -1 )
                {
                    buffer.append( oldUserAgent.substring( firstSpace + 1 ) );
                    buffer.append( ' ' );
                    buffer.append( oldUserAgent.substring( 0, firstSpace ) );
                }
                else
                {
                    buffer.append( oldUserAgent );
                }
                wm.setHttpUserAgent( buffer.toString() );
            }
            catch ( ClassCastException e )
            {
            }
            SecDispatcher sd = null;
            try
            {
                Proxy proxy = settings.getActiveProxy();
                try
                {
                    sd = (SecDispatcher) container.lookup( SecDispatcher.ROLE, "maven" );
                }
                catch ( Exception e )
                {
                    getLogger().warn( "Security features are disabled. Cannot find plexus component " + SecDispatcher.ROLE + ":maven" );
                    line();
                }
                if ( proxy != null )
                {
                    if ( proxy.getHost() == null )
                    {
                        throw new SettingsConfigurationException( "Proxy in settings.xml has no host" );
                    }
                    String pass = proxy.getPassword();
                    if ( sd != null )
                    {
                        try
                        {
                            pass = sd.decrypt( pass );
                        }
                        catch ( SecDispatcherException e )
                        {
                            reportSecurityConfigurationError( "password for proxy '" + proxy.getId() + "'", e );
                        }
                    }
                    wagonManager.addProxy( proxy.getProtocol(), proxy.getHost(), proxy.getPort(), proxy.getUsername(),
                                           pass, proxy.getNonProxyHosts() );
                }
                for ( Iterator i = settings.getServers().iterator(); i.hasNext(); )
                {
                    Server server = (Server) i.next();
                    String passWord = server.getPassword();
                    if ( sd != null )
                    {
                        try
                        {
                            passWord = sd.decrypt( passWord );
                        }
                        catch ( SecDispatcherException e )
                        {
                            reportSecurityConfigurationError( "password for server '" + server.getId() + "'", e );
                        }
                    }
                    String passPhrase = server.getPassphrase();
                    if ( sd != null )
                    {
                        try
                        {
                            passPhrase = sd.decrypt( passPhrase );
                        }
                        catch ( SecDispatcherException e )
                        {
                            reportSecurityConfigurationError( "passphrase for server '" + server.getId() + "'", e );
                        }
                    }
                    wagonManager.addAuthenticationInfo( server.getId(), server.getUsername(), passWord,
                                                        server.getPrivateKey(), passPhrase );
                    wagonManager.addPermissionInfo( server.getId(), server.getFilePermissions(),
                                                    server.getDirectoryPermissions() );
                    if ( server.getConfiguration() != null )
                    {
                        wagonManager.addConfiguration( server.getId(), (Xpp3Dom) server.getConfiguration() );
                    }
                }
                for ( Iterator i = settings.getMirrors().iterator(); i.hasNext(); )
                {
                    Mirror mirror = (Mirror) i.next();
                    wagonManager.addMirror( mirror.getId(), mirror.getMirrorOf(), mirror.getUrl() );
                }
            }
            finally
            {
                if ( sd != null )
                {
                    container.release( sd );
                }
            }
        }
        finally
        {
            container.release( wagonManager );
        }
        WagonProviderMapping mapping = (WagonProviderMapping) container.lookup( WagonProviderMapping.ROLE );
        try
        {
            mapping.setWagonProvider( "http", "lightweight" );
            mapping.setWagonProvider( "https", "lightweight" );
            for ( Object k: executionProperties.keySet() )
            {
                String key = (String) k;
                if ( key.startsWith( "maven.wagon.provider." ) )
                {
                    String provider = executionProperties.getProperty( key );
                    key = key.substring( "maven.wagon.provider.".length() );
                    mapping.setWagonProvider( key, provider );
                }
            }
        }
        finally
        {
            container.release( mapping );
        }
        String numThreads = System.getProperty( "maven.artifact.threads" );
        if ( numThreads != null )
        {
            int threads = 0;
            try
            {
                threads = Integer.valueOf( numThreads ).intValue();
                if ( threads < 1 )
                {
                    getLogger().warn( "Invalid number of threads '" + threads + "' will be ignored" );
                }
            }
            catch ( NumberFormatException e )
            {
                getLogger().warn( "Invalid number of threads '" + numThreads + "' will be ignored: " + e.getMessage() );
            }
            if ( threads > 0 )
            {
                DefaultArtifactResolver artifactResolver = (DefaultArtifactResolver) container.lookup( ArtifactResolver.ROLE );
                try
                {
                    artifactResolver.configureNumberOfThreads( threads );
                    getLogger().debug( "Resolution thread pool size set to: " + threads );
                }
                finally
                {
                    container.release( artifactResolver );
                }
            }
        }
    }
    private void reportSecurityConfigurationError( String affectedConfiguration, SecDispatcherException e )
    {
        Throwable cause = e;
        String msg = "Not decrypting " + affectedConfiguration + " due to exception in security handler.";
        while ( cause.getCause() != null )
        {
            cause = cause.getCause();
        }
        if ( cause instanceof FileNotFoundException )
        {
            msg += "\nEnsure that you have configured your master password file (and relocation if appropriate)\nSee the installation instructions for details.";
        }
        getLogger().warn( msg + "\nCause: " + cause.getMessage() );
        getLogger().debug( "Full trace follows", e );
    }
    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }
    protected void logFatal( Throwable error )
    {
        line();
        getLogger().error( "FATAL ERROR" );
        line();
        logDiagnostics( error );
        logTrace( error, true );
    }
    protected void logError( Exception e, boolean showErrors )
    {
        line();
        getLogger().error( "BUILD ERROR" );
        line();
        logDiagnostics( e );
        logTrace( e, showErrors );
        if ( !showErrors )
        {
            getLogger().info( "For more information, run Maven with the -e switch" );
            line();
        }
    }
    protected void logFailure( BuildFailureException e, boolean showErrors )
    {
        line();
        getLogger().error( "BUILD FAILURE" );
        line();
        logDiagnostics( e );
        logTrace( e, showErrors );
        if ( !showErrors )
        {
            getLogger().info( "For more information, run Maven with the -e switch" );
            line();
        }
    }
    private void logTrace( Throwable t, boolean showErrors )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Trace", t );
            line();
        }
        else if ( showErrors )
        {
            getLogger().info( "Trace", t );
            line();
        }
    }
    private void logDiagnostics( Throwable t )
    {
        String message = null;
        if ( errorDiagnostics != null )
        {
            message = errorDiagnostics.diagnose( t );
        }
        if ( message == null )
        {
            message = t.getMessage();
        }
        getLogger().info( message );
        line();
    }
    protected void logSuccess( ReactorManager rm )
    {
        line();
        getLogger().info( "BUILD SUCCESSFUL" );
        line();
    }
    private void logReactorSummary( ReactorManager rm )
    {
        if ( rm.hasMultipleProjects() && rm.executedMultipleProjects() )
        {
            getLogger().info( "" );
            getLogger().info( "" );
            line();
            getLogger().info( "Reactor Summary:" );
            line();
            for ( Iterator it = rm.getSortedProjects().iterator(); it.hasNext(); )
            {
                MavenProject project = (MavenProject) it.next();
                if ( rm.hasBuildFailure( project ) )
                {
                    logReactorSummaryLine( project.getName(), "FAILED", rm.getBuildFailure( project ).getTime() );
                }
                else if ( rm.isBlackListed( project ) )
                {
                    logReactorSummaryLine( project.getName(), "SKIPPED (dependency build failed or was skipped)" );
                }
                else if ( rm.hasBuildSuccess( project ) )
                {
                    logReactorSummaryLine( project.getName(), "SUCCESS", rm.getBuildSuccess( project ).getTime() );
                }
                else
                {
                    logReactorSummaryLine( project.getName(), "NOT BUILT" );
                }
            }
            line();
        }
    }
    protected void stats( Date start )
    {
        Date finish = new Date();
        long time = finish.getTime() - start.getTime();
        getLogger().info( "Total time: " + formatTime( time ) );
        getLogger().info( "Finished at: " + finish );
        System.gc();
        Runtime r = Runtime.getRuntime();
        getLogger().info(
            "Final Memory: " + ( r.totalMemory() - r.freeMemory() ) / MB + "M/" + r.totalMemory() / MB + "M" );
    }
    protected void line()
    {
        getLogger().info( "------------------------------------------------------------------------" );
    }
    protected static String formatTime( long ms )
    {
        long secs = ms / MS_PER_SEC;
        long min = secs / SEC_PER_MIN;
        secs = secs % SEC_PER_MIN;
        String msg = "";
        if ( min > 1 )
        {
            msg = min + " minutes ";
        }
        else if ( min == 1 )
        {
            msg = "1 minute ";
        }
        if ( secs > 1 )
        {
            msg += secs + " seconds";
        }
        else if ( secs == 1 )
        {
            msg += "1 second";
        }
        else if ( min == 0 )
        {
            msg += "< 1 second";
        }
        return msg;
    }
    private List getProjectFiles( MavenExecutionRequest request )
        throws IOException
    {
        List files = Collections.EMPTY_LIST;
        File userDir = new File( System.getProperty( "user.dir" ) );
        if ( request.isReactorActive() )
        {
            String includes = System.getProperty( "maven.reactor.includes", "**/" + POMv4 + ",**/" + RELEASE_POMv4 );
            String excludes = System.getProperty( "maven.reactor.excludes", POMv4 + "," + RELEASE_POMv4 );
            files = FileUtils.getFiles( userDir, includes, excludes );
            filterOneProjectFilePerDirectory( files );
            Collections.sort( files );
        }
        else if ( request.getPomFile() != null )
        {
            File projectFile = new File( request.getPomFile() ).getAbsoluteFile();
            if ( projectFile.exists() )
            {
                files = Collections.singletonList( projectFile );
            }
        }
        else
        {
            File projectFile = new File( userDir, RELEASE_POMv4 );
            if ( !projectFile.exists() )
            {
                projectFile = new File( userDir, POMv4 );
            }
            if ( projectFile.exists() )
            {
                files = Collections.singletonList( projectFile );
            }
        }
        return files;
    }
    private void filterOneProjectFilePerDirectory( List files )
    {
        List releaseDirs = new ArrayList();
        for ( Iterator it = files.iterator(); it.hasNext(); )
        {
            File projectFile = (File) it.next();
            if ( RELEASE_POMv4.equals( projectFile.getName() ) )
            {
                releaseDirs.add( projectFile.getParentFile() );
            }
        }
        for ( Iterator it = files.iterator(); it.hasNext(); )
        {
            File projectFile = (File) it.next();
            if ( !RELEASE_POMv4.equals( projectFile.getName() ) && releaseDirs.contains( projectFile.getParentFile() ) )
            {
                it.remove();
            }
        }
    }
}
