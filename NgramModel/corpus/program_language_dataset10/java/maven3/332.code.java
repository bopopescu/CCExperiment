package org.apache.maven.exception;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.lifecycle.LifecycleExecutionException;
import org.apache.maven.model.building.ModelProblem;
import org.apache.maven.model.building.ModelProblemUtils;
import org.apache.maven.plugin.AbstractMojoExecutionException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginExecutionException;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingResult;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
@Component( role = ExceptionHandler.class )
public class DefaultExceptionHandler
    implements ExceptionHandler
{
    public ExceptionSummary handleException( Throwable exception )
    {
        return handle( "", exception );
    }
    private ExceptionSummary handle( String message, Throwable exception )
    {
        String reference = getReference( exception );
        List<ExceptionSummary> children = null;
        if ( exception instanceof ProjectBuildingException )
        {
            List<ProjectBuildingResult> results = ( (ProjectBuildingException) exception ).getResults();
            children = new ArrayList<ExceptionSummary>();
            for ( ProjectBuildingResult result : results )
            {
                ExceptionSummary child = handle( result );
                if ( child != null )
                {
                    children.add( child );
                }
            }
            message = "The build could not read " + children.size() + " project" + ( children.size() == 1 ? "" : "s" );
        }
        else
        {
            message = getMessage( message, exception );
        }
        return new ExceptionSummary( exception, message, reference, children );
    }
    private ExceptionSummary handle( ProjectBuildingResult result )
    {
        List<ExceptionSummary> children = new ArrayList<ExceptionSummary>();
        for ( ModelProblem problem : result.getProblems() )
        {
            ExceptionSummary child = handle( problem, result.getProjectId() );
            if ( child != null )
            {
                children.add( child );
            }
        }
        if ( children.isEmpty() )
        {
            return null;
        }
        String message =
            "\nThe project " + result.getProjectId() + " (" + result.getPomFile() + ") has "
                + children.size() + " error" + ( children.size() == 1 ? "" : "s" );
        return new ExceptionSummary( null, message, null, children );
    }
    private ExceptionSummary handle( ModelProblem problem, String projectId )
    {
        if ( ModelProblem.Severity.ERROR.compareTo( problem.getSeverity() ) >= 0 )
        {
            String message = problem.getMessage();
            String location = ModelProblemUtils.formatLocation( problem, projectId );
            if ( StringUtils.isNotEmpty( location ) )
            {
                message += " @ " + location;
            }
            return handle( message, problem.getException() );
        }
        else
        {
            return null;
        }
    }
    private String getReference( Throwable exception )
    {
        String reference = "";
        if ( exception != null )
        {
            if ( exception instanceof MojoExecutionException )
            {
                reference = MojoExecutionException.class.getSimpleName();
            }
            else if ( exception instanceof MojoFailureException )
            {
                reference = MojoFailureException.class.getSimpleName();
            }
            else if ( exception instanceof LinkageError )
            {
                reference = LinkageError.class.getSimpleName();
            }
            else if ( exception instanceof PluginExecutionException )
            {
                reference = getReference( exception.getCause() );
                if ( StringUtils.isEmpty( reference ) )
                {
                    reference = exception.getClass().getSimpleName();
                }
            }
            else if ( exception instanceof LifecycleExecutionException )
            {
                reference = getReference( exception.getCause() );
            }
            else if ( isNoteworthyException( exception ) )
            {
                reference = exception.getClass().getSimpleName();
            }
        }
        if ( StringUtils.isNotEmpty( reference ) && !reference.startsWith( "http:" ) )
        {
            reference = "http://cwiki.apache.org/confluence/display/MAVEN/" + reference;
        }
        return reference;
    }
    private boolean isNoteworthyException( Throwable exception )
    {
        if ( exception == null )
        {
            return false;
        }
        else if ( exception instanceof Error )
        {
            return true;
        }
        else if ( exception instanceof RuntimeException )
        {
            return false;
        }
        else if ( exception.getClass().getName().startsWith( "java" ) )
        {
            return false;
        }
        return true;
    }
    private String getMessage( String message, Throwable exception )
    {
        String fullMessage = ( message != null ) ? message : "";
        for ( Throwable t = exception; t != null; t = t.getCause() )
        {
            String exceptionMessage = t.getMessage();
            if ( t instanceof AbstractMojoExecutionException )
            {
                String longMessage = ( (AbstractMojoExecutionException) t ).getLongMessage();
                if ( StringUtils.isNotEmpty( longMessage ) )
                {
                    if ( StringUtils.isEmpty( exceptionMessage ) || longMessage.contains( exceptionMessage ) )
                    {
                        exceptionMessage = longMessage;
                    }
                    else if ( !exceptionMessage.contains( longMessage ) )
                    {
                        exceptionMessage = join( exceptionMessage, '\n' + longMessage );
                    }
                }
            }
            if ( StringUtils.isEmpty( exceptionMessage ) )
            {
                exceptionMessage = t.getClass().getSimpleName();
            }
            if ( t instanceof UnknownHostException && !fullMessage.contains( "host" ) )
            {
                fullMessage = join( fullMessage, "Unknown host " + exceptionMessage );
            }
            else if ( !fullMessage.contains( exceptionMessage ) )
            {
                fullMessage = join( fullMessage, exceptionMessage );
            }
        }
        return fullMessage.trim();
    }
    private String join( String message1, String message2 )
    {
        String message = "";
        if ( StringUtils.isNotEmpty( message1 ) )
        {
            message = message1.trim();
        }
        if ( StringUtils.isNotEmpty( message2 ) )
        {
            if ( StringUtils.isNotEmpty( message ) )
            {
                if ( message.endsWith( "." ) || message.endsWith( "!" ) || message.endsWith( ":" ) )
                {
                    message += " ";
                }
                else
                {
                    message += ": ";
                }
            }
            message += message2;
        }
        return message;
    }
}
