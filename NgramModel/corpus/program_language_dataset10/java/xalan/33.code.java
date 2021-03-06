import java.io.IOException;
import java.io.OutputStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.SAXException;
public class UseStylesheetParam
{
  public static void main(String[] args)
	throws TransformerException, TransformerConfigurationException, 
         SAXException, IOException	   
	{
    if(args.length != 1)
    {
      System.err.println("Please pass one string to this program");
      return;
    }
    String paramValue = args[0];
   	TransformerFactory tFactory = TransformerFactory.newInstance();
    Transformer transformer = tFactory.newTransformer(new StreamSource("foo.xsl"));
    transformer.setParameter("param1",	
               							 paramValue  );
    transformer.transform(new StreamSource("foo.xml"), new StreamResult(new OutputStreamWriter(System.out)));
  }   
}
