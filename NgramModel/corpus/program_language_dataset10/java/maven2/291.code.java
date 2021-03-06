package org.apache.maven.project;
import org.apache.maven.artifact.InvalidRepositoryException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.model.DeploymentRepository;
import org.apache.maven.model.Repository;
import org.apache.maven.model.RepositoryBase;
import org.apache.maven.model.RepositoryPolicy;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
public final class ProjectUtils
{
    private ProjectUtils()
    {
    }
    public static List buildArtifactRepositories( List repositories,
                                                  ArtifactRepositoryFactory artifactRepositoryFactory,
                                                  PlexusContainer container )
        throws InvalidRepositoryException
    {
        List repos = new ArrayList();
        for ( Iterator i = repositories.iterator(); i.hasNext(); )
        {
            Repository mavenRepo = (Repository) i.next();
            ArtifactRepository artifactRepo =
                buildArtifactRepository( mavenRepo, artifactRepositoryFactory, container );
            if ( !repos.contains( artifactRepo ) )
            {
                repos.add( artifactRepo );
            }
        }
        return repos;
    }
    public static ArtifactRepository buildDeploymentArtifactRepository( DeploymentRepository repo,
                                                                        ArtifactRepositoryFactory artifactRepositoryFactory,
                                                                        PlexusContainer container )
        throws InvalidRepositoryException
    {
        if ( repo != null )
        {
            String id = repo.getId();
            String url = repo.getUrl();
            ArtifactRepositoryLayout layout = getRepositoryLayout( repo, container );
            return artifactRepositoryFactory.createDeploymentArtifactRepository( id, url, layout,
                                                                                 repo.isUniqueVersion() );
        }
        else
        {
            return null;
        }
    }
    public static ArtifactRepository buildArtifactRepository( Repository repo,
                                                              ArtifactRepositoryFactory artifactRepositoryFactory,
                                                              PlexusContainer container )
        throws InvalidRepositoryException
    {
        if ( repo != null )
        {
            String id = repo.getId();
            String url = repo.getUrl();
            if ( id == null || id.trim().length() < 1 )
            {
                throw new InvalidRepositoryException( "Repository ID must not be empty (URL is: " + url + ").", new IllegalArgumentException( "repository.id" ) );
            }
            if ( url == null || url.trim().length() < 1 )
            {
                throw new InvalidRepositoryException( "Repository URL must not be empty (ID is: " + id + ").", new IllegalArgumentException( "repository.url" ) );
            }
            ArtifactRepositoryLayout layout = getRepositoryLayout( repo, container );
            ArtifactRepositoryPolicy snapshots = buildArtifactRepositoryPolicy( repo.getSnapshots() );
            ArtifactRepositoryPolicy releases = buildArtifactRepositoryPolicy( repo.getReleases() );
            return artifactRepositoryFactory.createArtifactRepository( id, url, layout, snapshots, releases );
        }
        else
        {
            return null;
        }
    }
    private static ArtifactRepositoryPolicy buildArtifactRepositoryPolicy( RepositoryPolicy policy )
    {
        boolean enabled = true;
        String updatePolicy = null;
        String checksumPolicy = null;
        if ( policy != null )
        {
            enabled = policy.isEnabled();
            if ( policy.getUpdatePolicy() != null )
            {
                updatePolicy = policy.getUpdatePolicy();
            }
            if ( policy.getChecksumPolicy() != null )
            {
                checksumPolicy = policy.getChecksumPolicy();
            }
        }
        return new ArtifactRepositoryPolicy( enabled, updatePolicy, checksumPolicy );
    }
    private static ArtifactRepositoryLayout getRepositoryLayout( RepositoryBase mavenRepo, PlexusContainer container )
        throws InvalidRepositoryException
    {
        String layout = mavenRepo.getLayout();
        ArtifactRepositoryLayout repositoryLayout;
        try
        {
            repositoryLayout = (ArtifactRepositoryLayout) container.lookup( ArtifactRepositoryLayout.ROLE, layout );
        }
        catch ( ComponentLookupException e )
        {
            throw new InvalidRepositoryException( "Cannot find layout implementation corresponding to: \'" + layout
                + "\' for remote repository with id: \'" + mavenRepo.getId() + "\'.", e );
        }
        return repositoryLayout;
    }
}
