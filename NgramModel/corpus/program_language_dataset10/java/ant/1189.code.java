package org.example.junit;
import junit.framework.TestCase;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
public class XmlParserTest extends TestCase {
    public XmlParserTest(String s) {
        super(s);
    }
    public void testXercesIsPresent() throws SAXException {
        XMLReader xerces;
        xerces = XMLReaderFactory.createXMLReader(
                        "org.apache.xerces.parsers.SAXParser");
        assertNotNull(xerces);
    }
    public void testXercesHandlesSchema() throws SAXException {
        XMLReader xerces;
        xerces = XMLReaderFactory.createXMLReader("org.apache.xerces.parsers.SAXParser");
        xerces.setFeature("http://apache.org/xml/features/validation/schema",
                true);
    }
    public void testParserHandlesSchema() throws SAXException {
        XMLReader xerces;
        xerces = XMLReaderFactory.createXMLReader();
        xerces.setFeature("http://apache.org/xml/features/validation/schema",
                true);
    }
}
