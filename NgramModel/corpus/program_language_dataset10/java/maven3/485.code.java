package org.apache.maven.project;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.building.ModelProblemUtils;
public class ProjectBuildingException
    extends Exception
{
    private final String projectId;
    private File pomFile;
    private List<ProjectBuildingResult> results;
    public ProjectBuildingException( String projectId, String message, Throwable cause )
    {
        super( createMessage( message, projectId, null ), cause );
        this.projectId = projectId;
    }
    public ProjectBuildingException( String projectId, String message, File pomFile )
    {
        super( createMessage( message, projectId, pomFile ) );
        this.projectId = projectId;
        this.pomFile = pomFile;
    }
    protected ProjectBuildingException( String projectId, String message, File pomFile, Throwable cause )
    {
        super( createMessage( message, projectId, pomFile ), cause );
        this.projectId = projectId;
        this.pomFile = pomFile;
    }
    public ProjectBuildingException( List<ProjectBuildingResult> results )
    {
        super( createMessage( results ) );
        this.projectId = "";
        this.results = results;
    }
    public File getPomFile()
    {
        return pomFile;
    }
    public String getPomLocation()
    {
        if ( getPomFile() != null )
        {
            return getPomFile().getAbsolutePath();
        }
        else
        {
            return "null";
        }
    }
    public String getProjectId()
    {
        return projectId;
    }
    public List<ProjectBuildingResult> getResults()
    {
        return results;
    }
    private static String createMessage( String message, String projectId, File pomFile )
    {
        StringBuilder buffer = new StringBuilder( 256 );
        buffer.append( message );
        buffer.append( " for project " ).append( projectId );
        if ( pomFile != null )
        {
            buffer.append( " at " ).append( pomFile.getAbsolutePath() );
        }
        return buffer.toString();
    }
    private static String createMessage( List<ProjectBuildingResult> results )
    {
        StringWriter buffer = new StringWriter( 1024 );
        PrintWriter writer = new PrintWriter( buffer );
        writer.println( "Some problems were encountered while processing the POMs:" );
        for ( ProjectBuildingResult result : results )
        {
            for ( ModelProblem problem : result.getProblems() )
            {
                writer.print( "[" );
                writer.print( problem.getSeverity() );
                writer.print( "] " );
                writer.print( problem.getMessage() );
                writer.print( " @ " );
                writer.println( ModelProblemUtils.formatLocation( problem, result.getProjectId() ) );
            }
        }
        writer.close();
        return buffer.toString();
    }
}
