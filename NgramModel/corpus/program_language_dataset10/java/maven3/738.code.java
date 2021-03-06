package org.apache.maven.model.io;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.WriterFactory;
@Component( role = ModelWriter.class )
public class DefaultModelWriter
    implements ModelWriter
{
    public void write( File output, Map<String, Object> options, Model model )
        throws IOException
    {
        if ( output == null )
        {
            throw new IllegalArgumentException( "output file missing" );
        }
        if ( model == null )
        {
            throw new IllegalArgumentException( "model missing" );
        }
        output.getParentFile().mkdirs();
        write( WriterFactory.newXmlWriter( output ), options, model );
    }
    public void write( Writer output, Map<String, Object> options, Model model )
        throws IOException
    {
        if ( output == null )
        {
            throw new IllegalArgumentException( "output writer missing" );
        }
        if ( model == null )
        {
            throw new IllegalArgumentException( "model missing" );
        }
        try
        {
            MavenXpp3Writer w = new MavenXpp3Writer();
            w.write( output, model );
        }
        finally
        {
            IOUtil.close( output );
        }
    }
    public void write( OutputStream output, Map<String, Object> options, Model model )
        throws IOException
    {
        if ( output == null )
        {
            throw new IllegalArgumentException( "output stream missing" );
        }
        if ( model == null )
        {
            throw new IllegalArgumentException( "model missing" );
        }
        try
        {
            String encoding = model.getModelEncoding();
            if ( encoding == null || encoding.length() <= 0 )
            {
                encoding = "UTF-8";
            }
            write( new OutputStreamWriter( output, encoding ), options, model );
        }
        finally
        {
            IOUtil.close( output );
        }
    }
}
