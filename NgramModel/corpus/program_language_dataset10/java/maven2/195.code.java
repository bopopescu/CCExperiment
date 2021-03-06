package org.apache.maven.usability.diagnostics;
public final class DiagnosisUtils
{
    private DiagnosisUtils()
    {
    }
    public static boolean containsInCausality( Throwable error, Class test )
    {
        Throwable cause = error;
        while ( cause != null )
        {
            if ( test.isInstance( cause ) )
            {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
    public static Throwable getRootCause( Throwable error )
    {
        Throwable cause = error;
        while ( true )
        {
            Throwable nextCause = cause.getCause();
            if ( nextCause == null )
            {
                break;
            }
            else
            {
                cause = nextCause;
            }
        }
        return cause;
    }
    public static Throwable getFromCausality( Throwable error, Class targetClass )
    {
        Throwable cause = error;
        while ( cause != null )
        {
            if ( targetClass.isInstance( cause ) )
            {
                return cause;
            }
            cause = cause.getCause();
        }
        return null;
    }
    public static void appendRootCauseIfPresentAndUnique( Throwable error, StringBuffer message,
                                                          boolean includeTypeInfo )
    {
        if ( error == null )
        {
            return;
        }
        Throwable root = getRootCause( error );
        if ( root != null && !root.equals( error ) )
        {
            String rootMsg = root.getMessage();
            if ( rootMsg != null && ( error.getMessage() == null || error.getMessage().indexOf( rootMsg ) < 0 ) )
            {
                message.append( "\n" ).append( rootMsg );
                if ( includeTypeInfo )
                {
                    message.append( "\nRoot error type: " ).append( root.getClass().getName() );
                }
            }
        }
    }
}
