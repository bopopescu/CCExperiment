package org.apache.maven.repository;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.settings.Mirror;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
@Component( role = MirrorSelector.class )
public class DefaultMirrorSelector
    implements MirrorSelector
{
    private static final String WILDCARD = "*";
    private static final String EXTERNAL_WILDCARD = "external:*";
    public Mirror getMirror( ArtifactRepository repository, List<Mirror> mirrors )
    {
        String repoId = repository.getId();
        if ( repoId != null && mirrors != null )
        {
            for ( Mirror mirror : mirrors )
            {
                if ( repoId.equals( mirror.getMirrorOf() ) && matchesLayout( repository, mirror ) )
                {
                    return mirror;
                }
            }
            for ( Mirror mirror : mirrors )
            {
                if ( matchPattern( repository, mirror.getMirrorOf() ) && matchesLayout( repository, mirror ) )
                {
                    return mirror;
                }
            }
        }
        return null;
    }
    static boolean matchPattern( ArtifactRepository originalRepository, String pattern )
    {
        boolean result = false;
        String originalId = originalRepository.getId();
        if ( WILDCARD.equals( pattern ) || pattern.equals( originalId ) )
        {
            result = true;
        }
        else
        {
            String[] repos = pattern.split( "," );
            for ( String repo : repos )
            {
                if ( repo.length() > 1 && repo.startsWith( "!" ) )
                {
                    if ( repo.substring( 1 ).equals( originalId ) )
                    {
                        result = false;
                        break;
                    }
                }
                else if ( repo.equals( originalId ) )
                {
                    result = true;
                    break;
                }
                else if ( EXTERNAL_WILDCARD.equals( repo ) && isExternalRepo( originalRepository ) )
                {
                    result = true;
                }
                else if ( WILDCARD.equals( repo ) )
                {
                    result = true;
                }
            }
        }
        return result;
    }
    static boolean isExternalRepo( ArtifactRepository originalRepository )
    {
        try
        {
            URL url = new URL( originalRepository.getUrl() );
            return !( url.getHost().equals( "localhost" ) || url.getHost().equals( "127.0.0.1" )
                            || url.getProtocol().equals( "file" ) );
        }
        catch ( MalformedURLException e )
        {
            return false;
        }
    }
    static boolean matchesLayout( ArtifactRepository repository, Mirror mirror )
    {
        return matchesLayout( repository.getLayout().getId(), mirror.getMirrorOfLayouts() );
    }
    static boolean matchesLayout( String repoLayout, String mirrorLayout )
    {
        boolean result = false;
        if ( StringUtils.isEmpty( mirrorLayout ) || WILDCARD.equals( mirrorLayout ) )
        {
            result = true;
        }
        else if ( mirrorLayout.equals( repoLayout ) )
        {
            result = true;
        }
        else
        {
            String[] layouts = mirrorLayout.split( "," );
            for ( String layout : layouts )
            {
                if ( layout.length() > 1 && layout.startsWith( "!" ) )
                {
                    if ( layout.substring( 1 ).equals( repoLayout ) )
                    {
                        result = false;
                        break;
                    }
                }
                else if ( layout.equals( repoLayout ) )
                {
                    result = true;
                    break;
                }
                else if ( WILDCARD.equals( layout ) )
                {
                    result = true;
                }
            }
        }
        return result;
    }
}
