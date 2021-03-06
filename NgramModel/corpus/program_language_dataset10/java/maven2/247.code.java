package org.apache.maven.plugin.logging;
import java.io.PrintWriter;
import java.io.StringWriter;
public class SystemStreamLog
    implements Log
{
    public void debug( CharSequence content )
    {
        print( "debug", content );
    }
    public void debug( CharSequence content, Throwable error )
    {
        print( "debug", content, error );
    }
    public void debug( Throwable error )
    {
        print( "debug", error );
    }
    public void info( CharSequence content )
    {
        print( "info", content );
    }
    public void info( CharSequence content, Throwable error )
    {
        print( "info", content, error );
    }
    public void info( Throwable error )
    {
        print( "info", error );
    }
    public void warn( CharSequence content )
    {
        print( "warn", content );
    }
    public void warn( CharSequence content, Throwable error )
    {
        print( "warn", content, error );
    }
    public void warn( Throwable error )
    {
        print( "warn", error );
    }
    public void error( CharSequence content )
    {
        System.err.println( "[error] " + content.toString() );
    }
    public void error( CharSequence content, Throwable error )
    {
        StringWriter sWriter = new StringWriter();
        PrintWriter pWriter = new PrintWriter( sWriter );
        error.printStackTrace( pWriter );
        System.err.println( "[error] " + content.toString() + "\n\n" + sWriter.toString() );
    }
    public void error( Throwable error )
    {
        StringWriter sWriter = new StringWriter();
        PrintWriter pWriter = new PrintWriter( sWriter );
        error.printStackTrace( pWriter );
        System.err.println( "[error] " + sWriter.toString() );
    }
    public boolean isDebugEnabled()
    {
        return false;
    }
    public boolean isInfoEnabled()
    {
        return true;
    }
    public boolean isWarnEnabled()
    {
        return true;
    }
    public boolean isErrorEnabled()
    {
        return true;
    }
    private void print( String prefix, CharSequence content )
    {
        System.out.println( "[" + prefix + "] " + content.toString() );
    }
    private void print( String prefix, Throwable error )
    {
        StringWriter sWriter = new StringWriter();
        PrintWriter pWriter = new PrintWriter( sWriter );
        error.printStackTrace( pWriter );
        System.out.println( "[" + prefix + "] " + sWriter.toString() );
    }
    private void print( String prefix, CharSequence content, Throwable error )
    {
        StringWriter sWriter = new StringWriter();
        PrintWriter pWriter = new PrintWriter( sWriter );
        error.printStackTrace( pWriter );
        System.out.println( "[" + prefix + "] " + content.toString() + "\n\n" + sWriter.toString() );
    }
}