package jaxp;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
public class JAXPSpecTest extends DefaultHandler {
    public static void main(String[] args) throws Exception {
        JAXPSpecTest jaxpTest = new JAXPSpecTest();
        if (args.length == 0) {
            JAXPSpecTest.printUsage();
        }
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if(arg.equals("testSchemaLanguageSAX"))
                jaxpTest.testSchemaLanguageSAX();
            else if(arg.equals("testSchemaSourceSAX"))
                jaxpTest.testSchemaSourceSAX();
            else if(arg.equals("testSchemaLanguageDOM"))
                jaxpTest.testSchemaLanguageDOM();
            else if(arg.equals("testSchemaSourceDOM"))
                jaxpTest.testSchemaSourceDOM();
            else
                JAXPSpecTest.printUsage();
        }
    }
    public void testSchemaLanguageSAX() throws Exception{
        System.out.println(" Running JAXPSpecTest.testSchemaLanguageSAX ");
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setValidating(false);
        SAXParser saxParser = spf.newSAXParser();
        saxParser.setProperty(
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
        "http://www.w3.org/2001/XMLSchema");
        saxParser.parse("tests/jaxp/data/personal-schema.xml",this);
        System.out.println(" JAXPSpecTest.testSchemaLanguageSAX Passed ");
    }
    public void testSchemaSourceSAX() throws Exception{
        try{
            System.out.println(" Running JAXPSpecTest.testSchemaSourceSAX ");
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setValidating(true);
            SAXParser saxParser = spf.newSAXParser();
            saxParser.setProperty(
            "http://java.sun.com/xml/jaxp/properties/schemaSource",
            "tests/jaxp/data/personal-schema.xsd");
            saxParser.parse("tests/jaxp/data/personal-schema.xml",this);
        }catch(SAXNotSupportedException ne){
            System.out.println(" JAXPSpecTest.testSchemaSourceSAX Passed");
        }
    }
    public void testSchemaLanguageDOM() throws Exception {
        System.out.println(" Running JAXPSpecTest.testSchemaLanguageDOM ");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setAttribute(
        "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
        "http://www.w3.org/2001/XMLSchema");
        DocumentBuilder docBuilder = dbf.newDocumentBuilder();
        docBuilder.setErrorHandler(this);
        Document document = docBuilder.parse(
        new File("tests/jaxp/data/personal-schema.xml"));
        System.out.println(" JAXPSpecTest.testSchemaLanguageDOM Passed");
    }
    public void testSchemaSourceDOM() throws Exception {
        try{
            System.out.println(" Running JAXPSpecTest.testSchemaSourceDOM ");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setValidating(true);
            dbf.setAttribute(
            "http://java.sun.com/xml/jaxp/properties/schemaSource",
            "tests/jaxp/data/personal-schema.xsd");
            DocumentBuilder docBuilder = dbf.newDocumentBuilder();
            docBuilder.setErrorHandler(this);
            Document document = docBuilder.parse(
            "tests/jaxp/data/personal-schema.xml");
        } catch (IllegalArgumentException e) {
            System.out.println(" JAXPSpecTest.testSchemaSourceDOM Passed");
        }
    }
    public void warning(SAXParseException ex) throws SAXException {
        printError("Warning", ex);
    }
    public void error(SAXParseException ex) throws SAXException {
        printError("Error", ex);
    }
    public void fatalError(SAXParseException ex) throws SAXException {
        printError("Fatal Error", ex);
        throw ex;
    }
    private void printError(String type, SAXParseException ex) {
        System.err.print("[");
        System.err.print(type);
        System.err.print("] ");
        String systemId = ex.getSystemId();
        if (systemId != null) {
            int index = systemId.lastIndexOf('/');
            if (index != -1)
                systemId = systemId.substring(index + 1);
            System.err.print(systemId);
        }
        System.err.print(':');
        System.err.print(ex.getLineNumber());
        System.err.print(':');
        System.err.print(ex.getColumnNumber());
        System.err.print(": ");
        System.err.print(ex.getMessage());
        System.err.println();
        System.err.flush();
    }
    private static void printUsage(){
        System.err.println("Usage : JAXPSpecTest testSchemaLanguageSAX testSchemaSourceSAX testSchemaLanguageDOM testSchemaSourceDOM  ... ");
    }
}
