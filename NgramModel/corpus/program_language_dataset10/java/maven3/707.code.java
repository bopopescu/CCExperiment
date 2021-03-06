package org.apache.maven.model.building;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
public class FileModelSource
    implements ModelSource
{
    private File pomFile;
    public FileModelSource( File pomFile )
    {
        if ( pomFile == null )
        {
            throw new IllegalArgumentException( "no POM file specified" );
        }
        this.pomFile = pomFile.getAbsoluteFile();
    }
    public InputStream getInputStream()
        throws IOException
    {
        return new FileInputStream( pomFile );
    }
    public String getLocation()
    {
        return pomFile.getPath();
    }
    public File getPomFile()
    {
        return pomFile;
    }
    @Override
    public String toString()
    {
        return getLocation();
    }
}
