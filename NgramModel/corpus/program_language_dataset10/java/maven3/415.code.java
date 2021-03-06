package org.apache.maven.plugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.graph.DependencyFilter;
import org.sonatype.aether.repository.LocalRepository;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.repository.WorkspaceRepository;
@Component( role = PluginArtifactsCache.class )
public class DefaultPluginArtifactsCache
    implements PluginArtifactsCache
{
    private static class CacheKey
        implements Key
    {
        private final Plugin plugin;
        private final WorkspaceRepository workspace;
        private final LocalRepository localRepo;
        private final List<RemoteRepository> repositories;
        private final DependencyFilter filter;
        private final int hashCode;
        public CacheKey( Plugin plugin, DependencyFilter extensionFilter, List<RemoteRepository> repositories,
                         RepositorySystemSession session )
        {
            this.plugin = plugin.clone();
            workspace = CacheUtils.getWorkspace( session );
            this.localRepo = session.getLocalRepository();
            this.repositories = new ArrayList<RemoteRepository>( repositories.size() );
            for ( RemoteRepository repository : repositories )
            {
                if ( repository.isRepositoryManager() )
                {
                    this.repositories.addAll( repository.getMirroredRepositories() );
                }
                else
                {
                    this.repositories.add( repository );
                }
            }
            this.filter = extensionFilter;
            int hash = 17;
            hash = hash * 31 + CacheUtils.pluginHashCode( plugin );
            hash = hash * 31 + hash( workspace );
            hash = hash * 31 + hash( localRepo );
            hash = hash * 31 + CacheUtils.repositoriesHashCode( repositories );
            hash = hash * 31 + hash( extensionFilter );
            this.hashCode = hash;
        }
        @Override
        public String toString()
        {
            return plugin.getId();
        }
        @Override
        public int hashCode()
        {
            return hashCode;
        }
        private static int hash( Object obj )
        {
            return obj != null ? obj.hashCode() : 0;
        }
        @Override
        public boolean equals( Object o )
        {
            if ( o == this )
            {
                return true;
            }
            if ( !( o instanceof CacheKey ) )
            {
                return false;
            }
            CacheKey that = (CacheKey) o;
            return CacheUtils.pluginEquals( plugin, that.plugin ) && eq( workspace, that.workspace )
                && eq( localRepo, that.localRepo ) && CacheUtils.repositoriesEquals( repositories, that.repositories )
                && eq( filter, that.filter );
        }
        private static <T> boolean eq( T s1, T s2 )
        {
            return s1 != null ? s1.equals( s2 ) : s2 == null;
        }
    }
    protected final Map<Key, CacheRecord> cache = new HashMap<Key, CacheRecord>();
    public Key createKey( Plugin plugin, DependencyFilter extensionFilter, List<RemoteRepository> repositories,
                          RepositorySystemSession session )
    {
        return new CacheKey( plugin, extensionFilter, repositories, session );
    }
    public CacheRecord get( Key key )
        throws PluginResolutionException
    {
        CacheRecord cacheRecord = cache.get( key );
        if ( cacheRecord != null && cacheRecord.exception != null )
        {
            throw cacheRecord.exception;
        }
        return cacheRecord;
    }
    public CacheRecord put( Key key, List<Artifact> pluginArtifacts )
    {
        if ( pluginArtifacts == null )
        {
            throw new NullPointerException();
        }
        assertUniqueKey( key );
        CacheRecord record =
            new CacheRecord( Collections.unmodifiableList( new ArrayList<Artifact>( pluginArtifacts ) ) );
        cache.put( key, record );
        return record;
    }
    protected void assertUniqueKey( Key key )
    {
        if ( cache.containsKey( key ) )
        {
            throw new IllegalStateException( "Duplicate artifact resolution result for plugin " + key );
        }
    }
    public CacheRecord put( Key key, PluginResolutionException exception )
    {
        if ( exception == null )
        {
            throw new NullPointerException();
        }
        assertUniqueKey( key );
        CacheRecord record = new CacheRecord( exception );
        cache.put( key, record );
        return record;
    }
    public void flush()
    {
        cache.clear();
    }
    protected static int pluginHashCode( Plugin plugin )
    {
        return CacheUtils.pluginHashCode( plugin );
    }
    protected static boolean pluginEquals( Plugin a, Plugin b )
    {
        return CacheUtils.pluginEquals( a, b );
    }
    public void register( MavenProject project, CacheRecord record )
    {
    }
}
