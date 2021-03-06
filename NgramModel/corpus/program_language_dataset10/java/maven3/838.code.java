package org.apache.maven.settings.io;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.xpp3.SettingsXpp3Writer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.WriterFactory;
@Component( role = SettingsWriter.class )
public class DefaultSettingsWriter
    implements SettingsWriter
{
    public void write( File output, Map<String, Object> options, Settings settings )
        throws IOException
    {
        if ( output == null )
        {
            throw new IllegalArgumentException( "output file missing" );
        }
        if ( settings == null )
        {
            throw new IllegalArgumentException( "settings missing" );
        }
        output.getParentFile().mkdirs();
        write( WriterFactory.newXmlWriter( output ), options, settings );
    }
    public void write( Writer output, Map<String, Object> options, Settings settings )
        throws IOException
    {
        if ( output == null )
        {
            throw new IllegalArgumentException( "output writer missing" );
        }
        if ( settings == null )
        {
            throw new IllegalArgumentException( "settings missing" );
        }
        try
        {
            SettingsXpp3Writer w = new SettingsXpp3Writer();
            w.write( output, settings );
        }
        finally
        {
            IOUtil.close( output );
        }
    }
    public void write( OutputStream output, Map<String, Object> options, Settings settings )
        throws IOException
    {
        if ( output == null )
        {
            throw new IllegalArgumentException( "output stream missing" );
        }
        if ( settings == null )
        {
            throw new IllegalArgumentException( "settings missing" );
        }
        try
        {
            String encoding = settings.getModelEncoding();
            if ( encoding == null || encoding.length() <= 0 )
            {
                encoding = "UTF-8";
            }
            write( new OutputStreamWriter( output, encoding ), options, settings );
        }
        finally
        {
            IOUtil.close( output );
        }
    }
}
