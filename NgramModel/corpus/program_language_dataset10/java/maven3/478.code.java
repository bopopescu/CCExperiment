package org.apache.maven.project;
import java.io.File;
public class DuplicateProjectException
    extends Exception
{
    private final String projectId;
    private final File existingProjectFile;
    private final File conflictingProjectFile;
    public DuplicateProjectException( String message )
    {
        this( null, null, null, message );
    }
    public DuplicateProjectException( String message, Exception e )
    {
        super( message, e );
        this.projectId = null;
        this.existingProjectFile = null;
        this.conflictingProjectFile = null;
    }
    public DuplicateProjectException( String projectId, File existingProjectFile, File conflictingProjectFile,
                                      String message )
    {
        super( message );
        this.projectId = projectId;
        this.existingProjectFile = existingProjectFile;
        this.conflictingProjectFile = conflictingProjectFile;
    }
    public String getProjectId()
    {
        return projectId;
    }
    public File getExistingProjectFile()
    {
        return existingProjectFile;
    }
    public File getConflictingProjectFile()
    {
        return conflictingProjectFile;
    }
}
