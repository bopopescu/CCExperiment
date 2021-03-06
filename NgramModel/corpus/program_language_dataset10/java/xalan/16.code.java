import java.io.IOException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;
import org.apache.xml.serializer.Serializer;
import org.apache.xml.serializer.SerializerFactory;
import org.apache.xml.serializer.OutputPropertiesFactory;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
public class Pipe
{
	public static void main(String[] args)
	throws TransformerException, TransformerConfigurationException, 
         SAXException, IOException	   
	{
  	TransformerFactory tFactory = TransformerFactory.newInstance();
    if (tFactory.getFeature(SAXSource.FEATURE) && tFactory.getFeature(SAXResult.FEATURE))
    { 
      SAXTransformerFactory saxTFactory = ((SAXTransformerFactory) tFactory);	  
      TransformerHandler tHandler1 = saxTFactory.newTransformerHandler(new StreamSource("foo1.xsl"));
      TransformerHandler tHandler2 = saxTFactory.newTransformerHandler(new StreamSource("foo2.xsl"));
      TransformerHandler tHandler3 = saxTFactory.newTransformerHandler(new StreamSource("foo3.xsl"));
	    XMLReader reader = XMLReaderFactory.createXMLReader();
      reader.setContentHandler(tHandler1);
      reader.setProperty("http://xml.org/sax/properties/lexical-handler", tHandler1);
      tHandler1.setResult(new SAXResult(tHandler2));
      tHandler2.setResult(new SAXResult(tHandler3));
      java.util.Properties xmlProps = OutputPropertiesFactory.getDefaultMethodProperties("xml");
      xmlProps.setProperty("indent", "yes");
      xmlProps.setProperty("standalone", "no");
      Serializer serializer = SerializerFactory.getSerializer(xmlProps);
      serializer.setOutputStream(System.out);
      tHandler3.setResult(new SAXResult(serializer.asContentHandler()));
      reader.parse("foo.xml");
    }
  }
}
