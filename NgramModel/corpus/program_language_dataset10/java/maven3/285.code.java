package org.apache.maven.artifact.repository;
import java.io.File;
import java.util.Collections;
import java.util.List;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.metadata.ArtifactMetadata;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.repository.Proxy;
public class MavenArtifactRepository
    implements ArtifactRepository
{
    private String id;
    private String url;
    private String basedir;
    private String protocol;
    private ArtifactRepositoryLayout layout;
    private ArtifactRepositoryPolicy snapshots;
    private ArtifactRepositoryPolicy releases;
    private Authentication authentication;
    private Proxy proxy;
    public MavenArtifactRepository()
    {
    }
    public MavenArtifactRepository( String id, String url, ArtifactRepositoryLayout layout,
                                    ArtifactRepositoryPolicy snapshots, ArtifactRepositoryPolicy releases )
    {
        this.id = id;
        this.url = url;
        this.layout = layout;
        this.snapshots = snapshots;
        this.releases = releases;
        this.protocol = protocol( url );
        this.basedir = basedir( url );
    }
    public String pathOf( Artifact artifact )
    {
        return layout.pathOf( artifact );
    }
    public String pathOfRemoteRepositoryMetadata( ArtifactMetadata artifactMetadata )
    {
        return layout.pathOfRemoteRepositoryMetadata( artifactMetadata );
    }
    public String pathOfLocalRepositoryMetadata( ArtifactMetadata metadata, ArtifactRepository repository )
    {
        return layout.pathOfLocalRepositoryMetadata( metadata, repository );
    }
    public void setLayout( ArtifactRepositoryLayout layout )
    {
        this.layout = layout;
    }
    public ArtifactRepositoryLayout getLayout()
    {
        return layout;
    }
    public void setSnapshotUpdatePolicy( ArtifactRepositoryPolicy snapshots )
    {
        this.snapshots = snapshots;
    }
    public ArtifactRepositoryPolicy getSnapshots()
    {
        return snapshots;
    }
    public void setReleaseUpdatePolicy( ArtifactRepositoryPolicy releases )
    {
        this.releases = releases;
    }
    public ArtifactRepositoryPolicy getReleases()
    {
        return releases;
    }
    public String getKey()
    {
        return getId();
    }
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append( "       id: " ).append( getId() ).append( "\n" );
        sb.append( "      url: " ).append( getUrl() ).append( "\n" );
        sb.append( "   layout: " ).append( layout != null ? layout.getId() : "none" ).append( "\n" );
        if ( snapshots != null )
        {
            sb.append( "snapshots: [enabled => " ).append( snapshots.isEnabled() );
            sb.append( ", update => " ).append( snapshots.getUpdatePolicy() ).append( "]\n" );
        }
        if ( releases != null )
        {
            sb.append( " releases: [enabled => " ).append( releases.isEnabled() );
            sb.append( ", update => " ).append( releases.getUpdatePolicy() ).append( "]\n" );
        }
        return sb.toString();
    }
    public Artifact find( Artifact artifact )
    {
        File artifactFile = new File( getBasedir(), pathOf( artifact ) );
        artifact.setFile( artifactFile );
        return artifact;
    }
    public List<String> findVersions( Artifact artifact )
    {
        return Collections.emptyList();
    }
    public String getId()
    {
        return id;
    }
    public String getUrl()
    {
        return url;
    }
    public String getBasedir()
    {
        return basedir;
    }
    public String getProtocol()
    {
        return protocol;
    }
    public void setId( String id )
    {
        this.id = id;
    }
    public void setUrl( String url )
    {
        this.url = url;
        this.protocol = protocol( url );
        this.basedir = basedir( url );
    }
    private static String protocol( final String url )
    {
        final int pos = url.indexOf( ":" );
        if ( pos == -1 )
        {
            return "";
        }
        return url.substring( 0, pos ).trim();
    }
    private String basedir( String url )
    {
        String retValue = null;
        if ( protocol.equalsIgnoreCase( "file" ) )
        {
            retValue = url.substring( protocol.length() + 1 );
            retValue = decode( retValue );
            if ( retValue.startsWith( "//" ) )
            {
                retValue = retValue.substring( 2 );
                if ( retValue.length() >= 2 && ( retValue.charAt( 1 ) == '|' || retValue.charAt( 1 ) == ':' ) )
                {
                    retValue = retValue.charAt( 0 ) + ":" + retValue.substring( 2 );
                }
                else
                {
                    int index = retValue.indexOf( "/" );
                    if ( index >= 0 )
                    {
                        retValue = retValue.substring( index + 1 );
                    }
                    if ( retValue.length() >= 2 && ( retValue.charAt( 1 ) == '|' || retValue.charAt( 1 ) == ':' ) )
                    {
                        retValue = retValue.charAt( 0 ) + ":" + retValue.substring( 2 );
                    }
                    else if ( index >= 0 )
                    {
                        retValue = "/" + retValue;
                    }
                }
            }
            if ( retValue.length() >= 2 && retValue.charAt( 1 ) == '|' )
            {
                retValue = retValue.charAt( 0 ) + ":" + retValue.substring( 2 );
            }
            retValue = new File( retValue ).getPath();
        }
        if ( retValue == null )
        {
            retValue = "/";
        }
        return retValue.trim();
    }
    private static String decode( String url )
    {
        String decoded = url;
        if ( url != null )
        {
            int pos = -1;
            while ( ( pos = decoded.indexOf( '%', pos + 1 ) ) >= 0 )
            {
                if ( pos + 2 < decoded.length() )
                {
                    String hexStr = decoded.substring( pos + 1, pos + 3 );
                    char ch = (char) Integer.parseInt( hexStr, 16 );
                    decoded = decoded.substring( 0, pos ) + ch + decoded.substring( pos + 3 );
                }
            }
        }
        return decoded;
    }
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ( ( getId() == null ) ? 0 : getId().hashCode() );
        return result;
    }
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        ArtifactRepository other = (ArtifactRepository) obj;
        return eq( getId(), other.getId() );
    }
    protected static <T> boolean eq( T s1, T s2 )
    {
        return s1 != null ? s1.equals( s2 ) : s2 == null;
    }
    public Authentication getAuthentication()
    {
        return authentication;
    }
    public void setAuthentication( Authentication authentication )
    {
        this.authentication = authentication;
    }
    public Proxy getProxy()
    {
        return proxy;
    }
    public void setProxy( Proxy proxy )
    {
        this.proxy = proxy;
    }
    public boolean isBlacklisted()
    {
        return false;
    }
    public void setBlacklisted( boolean blackListed )
    {
    }
    public boolean isUniqueVersion()
    {
        return true;
    }
    public boolean isProjectAware()
    {
        return false;
    }
}
