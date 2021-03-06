package org.apache.maven.project.artifact;
import java.io.File;
import org.apache.maven.artifact.ArtifactUtils;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.InvalidProjectVersionException;
public class InvalidDependencyVersionException
    extends InvalidProjectVersionException
{
    private Dependency dependency;
    public InvalidDependencyVersionException( String projectId, Dependency dependency, File pomFile,
                                              InvalidVersionSpecificationException cause )
    {
        super( projectId, formatLocationInPom( dependency ), dependency.getVersion(), pomFile, cause );
        this.dependency = dependency;
    }
    private static String formatLocationInPom( Dependency dependency )
    {
        return "Dependency: " + ArtifactUtils.versionlessKey( dependency.getGroupId(), dependency.getArtifactId() );
    }
    public Dependency getDependency()
    {
        return dependency;
    }
}
