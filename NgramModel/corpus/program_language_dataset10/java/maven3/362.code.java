package org.apache.maven.lifecycle;
public class MissingProjectException
    extends Exception
{
    public MissingProjectException( String message )
    {
        super( message );
    }
}
