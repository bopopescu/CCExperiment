package dom.traversal;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ElementTraversal;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import junit.framework.TestCase;
public abstract class AbstractTestCase extends TestCase {
    private DocumentBuilder fDocumentBuilder;
    protected final void setUp() {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            dbf.setExpandEntityReferences(false);
            fDocumentBuilder = dbf.newDocumentBuilder();
        }
        catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            fail(pce.getMessage());
        }
    }
    protected final void tearDown() {
        fDocumentBuilder = null;
    }
    protected final ElementTraversal parse(String input) {
        try {
            Document doc = fDocumentBuilder.parse(new InputSource(new StringReader(input)));
            DOMImplementation domImpl = doc.getImplementation();
            assertTrue(domImpl.hasFeature("ElementTraversal", "1.0"));
            return toElementTraversal(doc.getDocumentElement());
        } 
        catch (SAXException se) {
            se.printStackTrace();
            fail(se.getMessage());
        } 
        catch (IOException ioe) {
            ioe.printStackTrace();
            fail(ioe.getMessage());
        }
        return null;
    }
    protected final ElementTraversal toElementTraversal(Element e) {
        assertTrue("e instanceof ElementTraversal", e == null || e instanceof ElementTraversal);
        return (ElementTraversal) e;
    }
}
