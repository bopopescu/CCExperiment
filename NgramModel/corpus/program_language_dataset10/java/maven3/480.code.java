package org.apache.maven.project;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
class ExtensionDescriptorBuilder
{
    private String getExtensionDescriptorLocation()
    {
        return "META-INF/maven/extension.xml";
    }
    public ExtensionDescriptor build( File extensionJar )
        throws IOException
    {
        ExtensionDescriptor extensionDescriptor = null;
        if ( extensionJar.isFile() )
        {
            JarFile pluginJar = new JarFile( extensionJar, false );
            try
            {
                ZipEntry pluginDescriptorEntry = pluginJar.getEntry( getExtensionDescriptorLocation() );
                if ( pluginDescriptorEntry != null )
                {
                    InputStream is = pluginJar.getInputStream( pluginDescriptorEntry );
                    extensionDescriptor = build( is );
                }
            }
            finally
            {
                pluginJar.close();
            }
        }
        else
        {
            File pluginXml = new File( extensionJar, getExtensionDescriptorLocation() );
            if ( pluginXml.canRead() )
            {
                InputStream is = new BufferedInputStream( new FileInputStream( pluginXml ) );
                try
                {
                    extensionDescriptor = build( is );
                }
                finally
                {
                    IOUtil.close( is );
                }
            }
        }
        return extensionDescriptor;
    }
    ExtensionDescriptor build( InputStream is )
        throws IOException
    {
        ExtensionDescriptor extensionDescriptor = new ExtensionDescriptor();
        Xpp3Dom dom;
        try
        {
            dom = Xpp3DomBuilder.build( ReaderFactory.newXmlReader( is ) );
        }
        catch ( XmlPullParserException e )
        {
            throw (IOException) new IOException( e.getMessage() ).initCause( e );
        }
        finally
        {
            IOUtil.close( is );
        }
        if ( !"extension".equals( dom.getName() ) )
        {
            throw new IOException( "Unexpected root element \"" + dom.getName() + "\", expected \"extension\"" );
        }
        extensionDescriptor.setExportedPackages( parseStrings( dom.getChild( "exportedPackages" ) ) );
        extensionDescriptor.setExportedArtifacts( parseStrings( dom.getChild( "exportedArtifacts" ) ) );
        return extensionDescriptor;
    }
    private List<String> parseStrings( Xpp3Dom dom )
    {
        List<String> strings = null;
        if ( dom != null )
        {
            strings = new ArrayList<String>();
            for ( Xpp3Dom child : dom.getChildren() )
            {
                String string = child.getValue();
                if ( string != null )
                {
                    string = string.trim();
                    if ( string.length() > 0 )
                    {
                        strings.add( string );
                    }
                }
            }
        }
        return strings;
    }
}
