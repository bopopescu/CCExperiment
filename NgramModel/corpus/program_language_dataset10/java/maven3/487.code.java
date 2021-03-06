package org.apache.maven.project;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Profile;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.sonatype.aether.RepositorySystemSession;
public interface ProjectBuildingRequest
{
    ProjectBuildingRequest setLocalRepository( ArtifactRepository localRepository );
    ArtifactRepository getLocalRepository();
    ProjectBuildingRequest setRemoteRepositories( List<ArtifactRepository> remoteRepositories );
    List<ArtifactRepository> getRemoteRepositories();
    ProjectBuildingRequest setPluginArtifactRepositories( List<ArtifactRepository> pluginArtifacgRepositories );
    List<ArtifactRepository> getPluginArtifactRepositories();
    ProjectBuildingRequest setSystemProperties( Properties systemProperties );
    Properties getSystemProperties();
    ProjectBuildingRequest setUserProperties( Properties userProperties );
    Properties getUserProperties();
    void setProject(MavenProject mavenProject);
    MavenProject getProject();
    ProjectBuildingRequest setProcessPlugins( boolean processPlugins );
    boolean isProcessPlugins();
    ProjectBuildingRequest setResolveDependencies( boolean resolveDependencies );
    boolean isResolveDependencies();
    ProjectBuildingRequest setValidationLevel( int validationLevel );
    int getValidationLevel();
    void setActiveProfileIds( List<String> activeProfileIds );
    List<String> getActiveProfileIds();
    void setInactiveProfileIds( List<String> inactiveProfileIds );
    List<String> getInactiveProfileIds();
    void addProfile( Profile profile );
    void setProfiles( List<Profile> profiles );
    List<Profile> getProfiles();
    Date getBuildStartTime();
    void setBuildStartTime( Date buildStartTime );
    RepositorySystemSession getRepositorySession();
    ProjectBuildingRequest setRepositorySession( RepositorySystemSession repositorySession );
    ProjectBuildingRequest setRepositoryMerging( RepositoryMerging mode );
    RepositoryMerging getRepositoryMerging();
    enum RepositoryMerging
    {
        POM_DOMINANT,
        REQUEST_DOMINANT,
    }
}
