package org.apache.maven.artifact.repository.metadata.io;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Map;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Reader;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
@Component( role = MetadataReader.class )
public class DefaultMetadataReader
    implements MetadataReader
{
    public Metadata read( File input, Map<String, ?> options )
        throws IOException
    {
        if ( input == null )
        {
            throw new IllegalArgumentException( "input file missing" );
        }
        Metadata metadata = read( ReaderFactory.newXmlReader( input ), options );
        return metadata;
    }
    public Metadata read( Reader input, Map<String, ?> options )
        throws IOException
    {
        if ( input == null )
        {
            throw new IllegalArgumentException( "input reader missing" );
        }
        try
        {
            MetadataXpp3Reader r = new MetadataXpp3Reader();
            return r.read( input, isStrict( options ) );
        }
        catch ( XmlPullParserException e )
        {
            throw new MetadataParseException( e.getMessage(), e.getLineNumber(), e.getColumnNumber(), e );
        }
        finally
        {
            IOUtil.close( input );
        }
    }
    public Metadata read( InputStream input, Map<String, ?> options )
        throws IOException
    {
        if ( input == null )
        {
            throw new IllegalArgumentException( "input stream missing" );
        }
        try
        {
            MetadataXpp3Reader r = new MetadataXpp3Reader();
            return r.read( input, isStrict( options ) );
        }
        catch ( XmlPullParserException e )
        {
            throw new MetadataParseException( e.getMessage(), e.getLineNumber(), e.getColumnNumber(), e );
        }
        finally
        {
            IOUtil.close( input );
        }
    }
    private boolean isStrict( Map<String, ?> options )
    {
        Object value = ( options != null ) ? options.get( IS_STRICT ) : null;
        return value == null || Boolean.parseBoolean( value.toString() );
    }
}
