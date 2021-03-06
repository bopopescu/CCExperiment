package org.apache.maven.artifact.installer;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import java.io.File;
public interface ArtifactInstaller
{
    String ROLE = ArtifactInstaller.class.getName();
    void install( String basedir, String finalName, Artifact artifact, ArtifactRepository localRepository )
        throws ArtifactInstallationException;
    void install( File source, Artifact artifact, ArtifactRepository localRepository )
        throws ArtifactInstallationException;
}
