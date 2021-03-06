package org.apache.maven.project;
import org.apache.maven.artifact.Artifact;
public class DuplicateArtifactAttachmentException
    extends RuntimeException
{
    private static final String DEFAULT_MESSAGE = "Duplicate artifact attachment detected.";
    private Artifact artifact;
    private final MavenProject project;
    public DuplicateArtifactAttachmentException( MavenProject project, Artifact artifact )
    {
        super( constructMessage( project, artifact ) );
        this.project = project;
        this.artifact = artifact;
    }
    private static String constructMessage( MavenProject project, Artifact artifact )
    {
        return DEFAULT_MESSAGE + " (project: " + project.getId() + "; illegal attachment: " + artifact.getId() + ")";
    }
    public MavenProject getProject()
    {
        return project;
    }
    public Artifact getArtifact()
    {
        return artifact;
    }
}
