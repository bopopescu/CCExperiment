package org.apache.maven.repository.internal;
import org.apache.maven.model.building.ModelCache;
import org.sonatype.aether.RepositoryCache;
import org.sonatype.aether.RepositorySystemSession;
class DefaultModelCache
    implements ModelCache
{
    private final RepositorySystemSession session;
    private final RepositoryCache cache;
    public static ModelCache newInstance( RepositorySystemSession session )
    {
        if ( session.getCache() == null )
        {
            return null;
        }
        else
        {
            return new DefaultModelCache( session );
        }
    }
    private DefaultModelCache( RepositorySystemSession session )
    {
        this.session = session;
        this.cache = session.getCache();
    }
    public Object get( String groupId, String artifactId, String version, String tag )
    {
        return cache.get( session, new Key( groupId, artifactId, version, tag ) );
    }
    public void put( String groupId, String artifactId, String version, String tag, Object data )
    {
        cache.put( session, new Key( groupId, artifactId, version, tag ), data );
    }
    static class Key
    {
        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String tag;
        private final int hash;
        public Key( String groupId, String artifactId, String version, String tag )
        {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.tag = tag;
            int h = 17;
            h = h * 31 + this.groupId.hashCode();
            h = h * 31 + this.artifactId.hashCode();
            h = h * 31 + this.version.hashCode();
            h = h * 31 + this.tag.hashCode();
            hash = h;
        }
        @Override
        public boolean equals( Object obj )
        {
            if ( this == obj )
            {
                return true;
            }
            if ( null == obj || !getClass().equals( obj.getClass() ) )
            {
                return false;
            }
            Key that = (Key) obj;
            return artifactId.equals( that.artifactId ) && groupId.equals( that.groupId )
                && version.equals( that.version ) && tag.equals( that.tag );
        }
        @Override
        public int hashCode()
        {
            return hash;
        }
    }
}
