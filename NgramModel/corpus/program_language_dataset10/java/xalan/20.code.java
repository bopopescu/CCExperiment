package servlet;
import java.io.*;
import org.xml.sax.*;
import org.apache.xml.utils.DefaultErrorHandler;
public class ApplyXSLTListener extends DefaultErrorHandler implements ErrorHandler
{
    private ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    public PrintWriter out = null;
    public ApplyXSLTListener()
    {
      out = new PrintWriter(new BufferedOutputStream(outStream), true);
    }
    public void warning(SAXParseException spe)
    {
	out.println("Parser Warning: " + spe.getMessage());
    }
    public void error(SAXParseException spe)
    {
	out.println("Parser Error: " + spe.getMessage());
    }
    public void fatalError(SAXParseException spe)
    throws SAXException
    {
	out.println("Parser Fatal Error: " + spe.getMessage());
	throw spe;
    }
    public String getMessage()
    {
	return outStream.toString();
    }
}
