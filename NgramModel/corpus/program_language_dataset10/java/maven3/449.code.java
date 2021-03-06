package org.apache.maven.plugin.prefix;
import java.util.Collections;
import java.util.List;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.repository.RemoteRepository;
public class DefaultPluginPrefixRequest
    implements PluginPrefixRequest
{
    private String prefix;
    private List<String> pluginGroups = Collections.emptyList();
    private Model pom;
    private List<RemoteRepository> repositories = Collections.emptyList();
    private RepositorySystemSession session;
    public DefaultPluginPrefixRequest()
    {
    }
    public DefaultPluginPrefixRequest( String prefix, MavenSession session )
    {
        setPrefix( prefix );
        setRepositorySession( session.getRepositorySession() );
        MavenProject project = session.getCurrentProject();
        if ( project != null )
        {
            setRepositories( project.getRemotePluginRepositories() );
            setPom( project.getModel() );
        }
        setPluginGroups( session.getPluginGroups() );
    }
    public String getPrefix()
    {
        return prefix;
    }
    public DefaultPluginPrefixRequest setPrefix( String prefix )
    {
        this.prefix = prefix;
        return this;
    }
    public List<String> getPluginGroups()
    {
        return pluginGroups;
    }
    public DefaultPluginPrefixRequest setPluginGroups( List<String> pluginGroups )
    {
        if ( pluginGroups != null )
        {
            this.pluginGroups = pluginGroups;
        }
        else
        {
            this.pluginGroups = Collections.emptyList();
        }
        return this;
    }
    public Model getPom()
    {
        return pom;
    }
    public DefaultPluginPrefixRequest setPom( Model pom )
    {
        this.pom = pom;
        return this;
    }
    public List<RemoteRepository> getRepositories()
    {
        return repositories;
    }
    public DefaultPluginPrefixRequest setRepositories( List<RemoteRepository> repositories )
    {
        if ( repositories != null )
        {
            this.repositories = repositories;
        }
        else
        {
            this.repositories = Collections.emptyList();
        }
        return this;
    }
    public RepositorySystemSession getRepositorySession()
    {
        return session;
    }
    public DefaultPluginPrefixRequest setRepositorySession( RepositorySystemSession session )
    {
        this.session = session;
        return this;
    }
}
