package org.apache.maven.model.path;
import java.io.File;
public interface PathTranslator
{
    String alignToBaseDirectory( String path, File basedir );
}
