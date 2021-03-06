package org.apache.maven.usability;
import org.apache.maven.artifact.manager.WagonManager;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.usability.diagnostics.DiagnosisUtils;
import org.apache.maven.usability.diagnostics.ErrorDiagnoser;
import java.io.IOException;
public class ArtifactResolverDiagnoser
    implements ErrorDiagnoser
{
    private WagonManager wagonManager;
    public boolean canDiagnose( Throwable error )
    {
        return DiagnosisUtils.containsInCausality( error, ArtifactResolutionException.class );
    }
    public String diagnose( Throwable error )
    {
        ArtifactResolutionException exception =
            (ArtifactResolutionException) DiagnosisUtils.getFromCausality( error, ArtifactResolutionException.class );
        StringBuffer message = new StringBuffer();
        message.append( "Failed to resolve artifact." );
        message.append( "\n\n" );
        message.append( exception.getMessage() );
        IOException ioe = (IOException) DiagnosisUtils.getFromCausality( exception, IOException.class );
        if ( ioe != null && ioe.getMessage() != null && exception.getMessage().indexOf( ioe.getMessage() ) < 0 )
        {
            message.append( "\n\nCaused by I/O exception: " ).append( ioe.getMessage() );
        }
        if ( !wagonManager.isOnline() )
        {
            message.append( "\n" ).append( SystemWarnings.getOfflineWarning() );
        }
        message.append( "\n" );
        return message.toString();
    }
}
