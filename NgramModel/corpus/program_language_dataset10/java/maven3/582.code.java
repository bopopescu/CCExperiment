package org.apache.maven.lifecycle.internal.stub;
import org.apache.maven.ProjectDependenciesResolver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.project.DependencyResolutionException;
import org.apache.maven.project.DependencyResolutionRequest;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;
import org.sonatype.aether.graph.Dependency;
import org.sonatype.aether.graph.DependencyNode;
import org.sonatype.aether.util.graph.DefaultDependencyNode;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public class ProjectDependenciesResolverStub
    implements ProjectDependenciesResolver, org.apache.maven.project.ProjectDependenciesResolver
{
    public Set<Artifact> resolve( MavenProject project, Collection<String> scopesToResolve, MavenSession session )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        return new HashSet<Artifact>();
    }
    public Set<Artifact> resolve( MavenProject project, Collection<String> scopesToCollect,
                                  Collection<String> scopesToResolve, MavenSession session )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        return new HashSet<Artifact>();
    }
    public Set<Artifact> resolve( Collection<? extends MavenProject> projects, Collection<String> scopes,
                                  MavenSession session )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        return new HashSet<Artifact>();
    }
    public Set<Artifact> resolve( MavenProject project, Collection<String> scopesToCollect,
                                  Collection<String> scopesToResolve, MavenSession session,
                                  Set<Artifact> ignoreableArtifacts )
        throws ArtifactResolutionException, ArtifactNotFoundException
    {
        return new HashSet<Artifact>();
    }
    public DependencyResolutionResult resolve( DependencyResolutionRequest request )
        throws DependencyResolutionException
    {
        return new DependencyResolutionResult()
        {
            public List<Dependency> getUnresolvedDependencies()
            {
                return Collections.emptyList();
            }
            public List<Dependency> getResolvedDependencies()
            {
                return Collections.emptyList();
            }
            public List<Exception> getResolutionErrors( Dependency dependency )
            {
                return Collections.emptyList();
            }
            public DependencyNode getDependencyGraph()
            {
                return new DefaultDependencyNode( (Dependency) null );
            }
            public List<Dependency> getDependencies()
            {
                return Collections.emptyList();
            }
            public List<Exception> getCollectionErrors()
            {
                return Collections.emptyList();
            }
        };
    }
}
