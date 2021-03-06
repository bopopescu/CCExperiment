package org.apache.maven.project;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
class ReactorModelPool
{
    private final Map<CacheKey, File> pomFiles = new HashMap<CacheKey, File>();
    public File get( String groupId, String artifactId, String version )
    {
        return pomFiles.get( new CacheKey( groupId, artifactId, version ) );
    }
    public void put( String groupId, String artifactId, String version, File pomFile )
    {
        pomFiles.put( new CacheKey( groupId, artifactId, version ), pomFile );
    }
    private static final class CacheKey
    {
        private final String groupId;
        private final String artifactId;
        private final String version;
        private final int hashCode;
        public CacheKey( String groupId, String artifactId, String version )
        {
            this.groupId = ( groupId != null ) ? groupId : "";
            this.artifactId = ( artifactId != null ) ? artifactId : "";
            this.version = ( version != null ) ? version : "";
            int hash = 17;
            hash = hash * 31 + this.groupId.hashCode();
            hash = hash * 31 + this.artifactId.hashCode();
            hash = hash * 31 + this.version.hashCode();
            hashCode = hash;
        }
        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            if ( !( obj instanceof CacheKey ) )
            {
                return false;
            }
            CacheKey that = (CacheKey) obj;
            return artifactId.equals( that.artifactId ) && groupId.equals( that.groupId )
                && version.equals( that.version );
        }
        @Override
        public int hashCode()
        {
            return hashCode;
        }
        @Override
        public String toString()
        {
            StringBuilder buffer = new StringBuilder( 96 );
            buffer.append( groupId ).append( ':' ).append( artifactId ).append( ':' ).append( version );
            return buffer.toString();
        }
    }
}
