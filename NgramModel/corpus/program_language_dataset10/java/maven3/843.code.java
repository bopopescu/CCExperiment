package org.apache.maven.settings.validation;
import java.util.List;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.SettingsProblem.Severity;
import org.apache.maven.settings.building.SettingsProblemCollector;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
@Component( role = SettingsValidator.class )
public class DefaultSettingsValidator
    implements SettingsValidator
{
    private static final String ID_REGEX = "[A-Za-z0-9_\\-.]+";
    private static final String ILLEGAL_FS_CHARS = "\\/:\"<>|?*";
    private static final String ILLEGAL_REPO_ID_CHARS = ILLEGAL_FS_CHARS;
    public void validate( Settings settings, SettingsProblemCollector problems )
    {
        if ( settings.isUsePluginRegistry() )
        {
            addViolation( problems, Severity.WARNING, "usePluginRegistry", null, "is deprecated and has no effect." );
        }
        List<String> pluginGroups = settings.getPluginGroups();
        if ( pluginGroups != null )
        {
            for ( int i = 0; i < pluginGroups.size(); i++ )
            {
                String pluginGroup = pluginGroups.get( i ).trim();
                if ( StringUtils.isBlank( pluginGroup ) )
                {
                    addViolation( problems, Severity.ERROR, "pluginGroups.pluginGroup[" + i + "]", null,
                                  "must not be empty" );
                }
                else if ( !pluginGroup.matches( ID_REGEX ) )
                {
                    addViolation( problems, Severity.ERROR, "pluginGroups.pluginGroup[" + i + "]", null,
                                  "must denote a valid group id and match the pattern " + ID_REGEX );
                }
            }
        }
        List<Server> servers = settings.getServers();
        if ( servers != null )
        {
            for ( int i = 0; i < servers.size(); i++ )
            {
                Server server = servers.get( i );
                validateStringNotEmpty( problems, "servers.server[" + i + "].id", server.getId(), null );
            }
        }
        List<Mirror> mirrors = settings.getMirrors();
        if ( mirrors != null )
        {
            for ( Mirror mirror : mirrors )
            {
                validateStringNotEmpty( problems, "mirrors.mirror.id", mirror.getId(), mirror.getUrl() );
                validateBannedCharacters( problems, "mirrors.mirror.id", Severity.WARNING, mirror.getId(), null,
                                          ILLEGAL_REPO_ID_CHARS );
                if ( "local".equals( mirror.getId() ) )
                {
                    addViolation( problems, Severity.WARNING, "mirrors.mirror.id", null, "must not be 'local'"
                        + ", this identifier is reserved for the local repository"
                        + ", using it for other repositories will corrupt your repository metadata." );
                }
                validateStringNotEmpty( problems, "mirrors.mirror.url", mirror.getUrl(), mirror.getId() );
                validateStringNotEmpty( problems, "mirrors.mirror.mirrorOf", mirror.getMirrorOf(), mirror.getId() );
            }
        }
        List<Profile> profiles = settings.getProfiles();
        if ( profiles != null )
        {
            for ( Profile profile : profiles )
            {
                validateRepositories( problems, profile.getRepositories(), "repositories.repository" );
                validateRepositories( problems, profile.getPluginRepositories(),
                                      "pluginRepositories.pluginRepository" );
            }
        }
    }
    private void validateRepositories( SettingsProblemCollector problems, List<Repository> repositories, String prefix )
    {
        for ( Repository repository : repositories )
        {
            validateStringNotEmpty( problems, prefix + ".id", repository.getId(), repository.getUrl() );
            validateBannedCharacters( problems, prefix + ".id", Severity.WARNING, repository.getId(), null,
                                      ILLEGAL_REPO_ID_CHARS );
            if ( "local".equals( repository.getId() ) )
            {
                addViolation( problems, Severity.WARNING, prefix + ".id", null, "must not be 'local'"
                    + ", this identifier is reserved for the local repository"
                    + ", using it for other repositories will corrupt your repository metadata." );
            }
            validateStringNotEmpty( problems, prefix + ".url", repository.getUrl(), repository.getId() );
            if ( "legacy".equals( repository.getLayout() ) )
            {
                addViolation( problems, Severity.WARNING, prefix + ".layout", repository.getId(),
                              "uses the unsupported value 'legacy', artifact resolution might fail." );
            }
        }
    }
    private boolean validateStringNotEmpty( SettingsProblemCollector problems, String fieldName, String string,
                                            String sourceHint )
    {
        if ( !validateNotNull( problems, fieldName, string, sourceHint ) )
        {
            return false;
        }
        if ( string.length() > 0 )
        {
            return true;
        }
        addViolation( problems, Severity.ERROR, fieldName, sourceHint, "is missing" );
        return false;
    }
    private boolean validateNotNull( SettingsProblemCollector problems, String fieldName, Object object,
                                     String sourceHint )
    {
        if ( object != null )
        {
            return true;
        }
        addViolation( problems, Severity.ERROR, fieldName, sourceHint, "is missing" );
        return false;
    }
    private boolean validateBannedCharacters( SettingsProblemCollector problems, String fieldName, Severity severity,
                                              String string, String sourceHint, String banned )
    {
        if ( string != null )
        {
            for ( int i = string.length() - 1; i >= 0; i-- )
            {
                if ( banned.indexOf( string.charAt( i ) ) >= 0 )
                {
                    addViolation( problems, severity, fieldName, sourceHint,
                                  "must not contain any of these characters " + banned + " but found "
                                      + string.charAt( i ) );
                    return false;
                }
            }
        }
        return true;
    }
    private void addViolation( SettingsProblemCollector problems, Severity severity, String fieldName,
                               String sourceHint, String message )
    {
        StringBuilder buffer = new StringBuilder( 256 );
        buffer.append( '\'' ).append( fieldName ).append( '\'' );
        if ( sourceHint != null )
        {
            buffer.append( " for " ).append( sourceHint );
        }
        buffer.append( ' ' ).append( message );
        problems.add( severity, buffer.toString(), -1, -1, null );
    }
}
