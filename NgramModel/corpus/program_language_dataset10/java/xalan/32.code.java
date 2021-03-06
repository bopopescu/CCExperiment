import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;
public class Examples
{
  public static void main(String argv[])
          throws TransformerException, TransformerConfigurationException, IOException, SAXException,
                 ParserConfigurationException, FileNotFoundException
  {
    System.out.println("\n\n==== exampleSimple ====");
    try {
        exampleSimple1("xml/foo.xml", "xsl/foo.xsl");
    } catch( Exception ex ) { 
        handleException(ex);
    } 
    System.out.println("\n\n==== exampleSimple2 (see foo.out) ====");
    try {
        exampleSimple2("xml/foo.xml", "xsl/foo.xsl");
    } catch( Exception ex ) { 
        handleException(ex);
    } 
    System.out.println("\n\n==== exampleFromStream ====");
    try {
        exampleFromStream("xml/foo.xml", "xsl/foo.xsl");
    } catch( Exception ex ) { 
        handleException(ex);
    } 
    System.out.println("\n\n==== exampleFromReader ====");
    try {
        exampleFromReader("xml/foo.xml", "xsl/foo.xsl");
    } catch( Exception ex ) { 
        handleException(ex);
    } 
    System.out.println("\n\n==== exampleUseTemplatesObj ====");
    try {
        exampleUseTemplatesObj("xml/foo.xml", "xml/baz.xml", "xsl/foo.xsl");
    } catch( Exception ex ) { 
        handleException(ex);
    } 
    System.out.println("\n\n==== exampleContentHandlerToContentHandler ====");
    try {
        exampleContentHandlerToContentHandler("xml/foo.xml", "xsl/foo.xsl");
    } catch( Exception ex ) { 
        handleException(ex);
    } 
    System.out.println("\n\n==== exampleXMLReader ====");
    try {
        exampleXMLReader("xml/foo.xml", "xsl/foo.xsl");
    } catch( Exception ex ) { 
        handleException(ex);
    } 
    System.out.println("\n\n==== exampleXMLFilter ====");
    try {
        exampleXMLFilter("xml/foo.xml", "xsl/foo.xsl");
    } catch( Exception ex ) { 
        handleException(ex);
    } 
    System.out.println("\n\n==== exampleXMLFilterChain ====");
    try {
        exampleXMLFilterChain("xml/foo.xml", "xsl/foo.xsl", "xsl/foo2.xsl", "xsl/foo3.xsl");
    } catch( Exception ex ) { 
        handleException(ex);
    } 
    System.out.println("\n\n==== exampleDOM2DOM ====");
    try {
        exampleDOM2DOM("xml/foo.xml", "xsl/foo.xsl");
    } catch( Exception ex ) { 
        handleException(ex);
    } 
    System.out.println("\n\n==== exampleParam ====");
    try {
        exampleParam("xml/foo.xml", "xsl/foo.xsl");
    } catch( Exception ex ) { 
        handleException(ex);
    } 
    System.out.println("\n\n==== exampleTransformerReuse ====");
    try {
        exampleTransformerReuse("xml/foo.xml", "xsl/foo.xsl");
    } catch( Exception ex ) { 
        handleException(ex);
    } 
    System.out.println("\n\n==== exampleOutputProperties ====");
    try {
        exampleOutputProperties("xml/foo.xml", "xsl/foo.xsl");
    } catch( Exception ex ) { 
        handleException(ex);
    } 
    System.out.println("\n\n==== exampleUseAssociated ====");
    try {
        exampleUseAssociated("xml/foo.xml");
    } catch( Exception ex ) { 
        handleException(ex);
    } 
    System.out.println("\n\n==== exampleContentHandler2DOM ====");
    try {
        exampleContentHandler2DOM("xml/foo.xml", "xsl/foo.xsl");
    } catch( Exception ex ) { 
        handleException(ex);
    } 
    System.out.println("\n\n==== exampleAsSerializer ====");
    try {
        exampleAsSerializer("xml/foo.xml", "xsl/foo.xsl");
    } catch( Exception ex ) { 
        handleException(ex);
    } 
    System.out.println("\n\n==== exampleContentHandler2DOM ====");
    try {
        exampleContentHandler2DOM("xml/foo.xml", "xsl/foo.xsl");
    } catch( Exception ex ) { 
        handleException(ex);
    } 
    System.out.println("\n==== done! ====");
  }
  public static void exampleSimple1(String sourceID, String xslID)
    throws TransformerException, TransformerConfigurationException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    Transformer transformer 
      = tfactory.newTransformer(new StreamSource(xslID));
    transformer.transform( new StreamSource(sourceID),
                           new StreamResult(new OutputStreamWriter(System.out)));
  }
  public static void exampleSimple2(String sourceID, String xslID)
    throws TransformerException, TransformerConfigurationException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    Transformer transformer 
      = tfactory.newTransformer(new StreamSource(xslID));
    transformer.transform( new StreamSource(new File(sourceID)),
                           new StreamResult(new File("foo.out")));
  }
  public static void exampleFromStream(String sourceID, String xslID)
    throws TransformerException, TransformerConfigurationException,
           FileNotFoundException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    InputStream xslIS = new BufferedInputStream(new FileInputStream(xslID));
    StreamSource xslSource = new StreamSource(xslIS);
    xslSource.setSystemId(xslID);
    Transformer transformer = tfactory.newTransformer(xslSource);
    InputStream xmlIS = new BufferedInputStream(new FileInputStream(sourceID));
    StreamSource xmlSource = new StreamSource(xmlIS);
    xmlSource.setSystemId(sourceID);
    transformer.transform( xmlSource, new StreamResult(new OutputStreamWriter(System.out)));
  }
  public static void exampleFromReader(String sourceID, String xslID)
    throws TransformerException, TransformerConfigurationException,
           FileNotFoundException, UnsupportedEncodingException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    Reader xslReader = new BufferedReader(new InputStreamReader(new FileInputStream(xslID), "UTF-8"));
    StreamSource xslSource = new StreamSource(xslReader);
    xslSource.setSystemId(xslID);
    Transformer transformer = tfactory.newTransformer(xslSource);
    Reader xmlReader = new BufferedReader(new InputStreamReader(new FileInputStream(sourceID), "UTF-8"));
    StreamSource xmlSource = new StreamSource(xmlReader);
    xmlSource.setSystemId(sourceID);
    transformer.transform( xmlSource, new StreamResult(new OutputStreamWriter(System.out)));
  }
  public static void exampleUseTemplatesObj(String sourceID1, 
                                    String sourceID2, 
                                    String xslID)
          throws TransformerException, TransformerConfigurationException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    Templates templates = tfactory.newTemplates(new StreamSource(xslID));
    Transformer transformer1 = templates.newTransformer();
    Transformer transformer2 = templates.newTransformer();
    System.out.println("\n\n----- transform of "+sourceID1+" -----");
    transformer1.transform(new StreamSource(sourceID1),
                          new StreamResult(new OutputStreamWriter(System.out)));
    System.out.println("\n\n----- transform of "+sourceID2+" -----");
    transformer2.transform(new StreamSource(sourceID2),
                          new StreamResult(new OutputStreamWriter(System.out)));
  }
  public static void exampleContentHandlerToContentHandler(String sourceID, 
                                                           String xslID)
          throws TransformerException, 
                 TransformerConfigurationException, 
                 SAXException, IOException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    if (tfactory.getFeature(SAXSource.FEATURE))
    {
      SAXTransformerFactory stfactory = ((SAXTransformerFactory) tfactory);
      TransformerHandler handler 
        = stfactory.newTransformerHandler(new StreamSource(xslID));
      Result result = new SAXResult(new ExampleContentHandler());
      handler.setResult(result);
      XMLReader reader=null;
      try {
	  javax.xml.parsers.SAXParserFactory factory=
	      javax.xml.parsers.SAXParserFactory.newInstance();
	  factory.setNamespaceAware( true );
	  javax.xml.parsers.SAXParser jaxpParser=
	      factory.newSAXParser();
	  reader=jaxpParser.getXMLReader();
      } catch( javax.xml.parsers.ParserConfigurationException ex ) {
	  throw new org.xml.sax.SAXException( ex );
      } catch( javax.xml.parsers.FactoryConfigurationError ex1 ) {
	  throw new org.xml.sax.SAXException( ex1.toString() );
      } catch( NoSuchMethodError ex2 ) {
      }
      if( reader==null ) reader = XMLReaderFactory.createXMLReader();
      reader.setContentHandler(handler);
      reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
      reader.parse(sourceID);
    }
    else
    {
      System.out.println(
        "Can't do exampleContentHandlerToContentHandler because tfactory is not a SAXTransformerFactory");
    }
  }
  public static void exampleXMLReader(String sourceID, String xslID)
          throws TransformerException, TransformerConfigurationException, SAXException, IOException    
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    if(tfactory.getFeature(SAXSource.FEATURE))
    {
      XMLReader reader 
        = ((SAXTransformerFactory) tfactory).newXMLFilter(new StreamSource(xslID));
      reader.setContentHandler(new ExampleContentHandler());
      reader.parse(new InputSource(sourceID));
    }
    else
      System.out.println("tfactory does not support SAX features!");
  }
  public static void exampleXMLFilter(String sourceID, String xslID)
          throws TransformerException, TransformerConfigurationException, SAXException, IOException    
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    XMLReader reader=null;
    try {
	javax.xml.parsers.SAXParserFactory factory=
	    javax.xml.parsers.SAXParserFactory.newInstance();
	  factory.setNamespaceAware( true );
	  javax.xml.parsers.SAXParser jaxpParser=
	    factory.newSAXParser();
	reader=jaxpParser.getXMLReader();
    } catch( javax.xml.parsers.ParserConfigurationException ex ) {
	throw new org.xml.sax.SAXException( ex );
    } catch( javax.xml.parsers.FactoryConfigurationError ex1 ) {
	throw new org.xml.sax.SAXException( ex1.toString() );
    } catch( NoSuchMethodError ex2 ) {
    }
    if( reader==null ) reader = XMLReaderFactory.createXMLReader();
    reader.setContentHandler(new ExampleContentHandler());
    try
    {
      reader.setFeature("http://xml.org/sax/features/namespace-prefixes",
                        true);
      reader.setFeature("http://apache.org/xml/features/validation/dynamic",
                        true);
    }
    catch (SAXException se)
    {
    }
    XMLFilter filter 
      = ((SAXTransformerFactory) tfactory).newXMLFilter(new StreamSource(xslID));
    filter.setParent(reader);
    filter.parse(new InputSource(sourceID));
  }
  public static void exampleXMLFilterChain(
                                           String sourceID, String xslID_1, 
                                           String xslID_2, String xslID_3)
    throws TransformerException, TransformerConfigurationException, SAXException, IOException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    Templates stylesheet1 = tfactory.newTemplates(new StreamSource(xslID_1));
    Transformer transformer1 = stylesheet1.newTransformer();
    if (tfactory.getFeature(SAXSource.FEATURE))
    {
      SAXTransformerFactory stf = (SAXTransformerFactory)tfactory;
      XMLReader reader=null;
      try {
	  javax.xml.parsers.SAXParserFactory factory=
	      javax.xml.parsers.SAXParserFactory.newInstance();
	  factory.setNamespaceAware( true );
	  javax.xml.parsers.SAXParser jaxpParser=
	      factory.newSAXParser();
	  reader=jaxpParser.getXMLReader();
      } catch( javax.xml.parsers.ParserConfigurationException ex ) {
	  throw new org.xml.sax.SAXException( ex );
      } catch( javax.xml.parsers.FactoryConfigurationError ex1 ) {
	  throw new org.xml.sax.SAXException( ex1.toString() );
      } catch( NoSuchMethodError ex2 ) {
      }
      if( reader==null ) reader = XMLReaderFactory.createXMLReader();
      XMLFilter filter1 = stf.newXMLFilter(new StreamSource(xslID_1));
      XMLFilter filter2 = stf.newXMLFilter(new StreamSource(xslID_2));
      XMLFilter filter3 = stf.newXMLFilter(new StreamSource(xslID_3));
      if (null != filter1) 
      {
        filter1.setParent(reader);
        filter2.setParent(filter1);
        filter3.setParent(filter2);
        filter3.setContentHandler(new ExampleContentHandler());
        filter3.parse(new InputSource(sourceID));
      }
      else
      {
        System.out.println(
                           "Can't do exampleXMLFilter because "+
                           "tfactory doesn't support asXMLFilter()");
      }
    }
    else
    {
      System.out.println(
                         "Can't do exampleXMLFilter because "+
                         "tfactory is not a SAXTransformerFactory");
    }
  }
  public static Node exampleDOM2DOM(String sourceID, String xslID)
    throws TransformerException, TransformerConfigurationException, SAXException, IOException,
    ParserConfigurationException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    if (tfactory.getFeature(DOMSource.FEATURE))
    {
      Templates templates;
      {
        DocumentBuilderFactory dfactory =
          DocumentBuilderFactory.newInstance();
        dfactory.setNamespaceAware(true);
        DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
        org.w3c.dom.Document outNode = docBuilder.newDocument();
        Node doc = docBuilder.parse(new InputSource(xslID));
        DOMSource dsource = new DOMSource(doc);
        dsource.setSystemId(xslID);
        templates = tfactory.newTemplates(dsource);
      }
      Transformer transformer = templates.newTransformer();
      DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
      dfactory.setNamespaceAware(true);
      DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
      org.w3c.dom.Document outNode = docBuilder.newDocument();
      Node doc = docBuilder.parse(new InputSource(sourceID));
      transformer.transform(new DOMSource(doc), new DOMResult(outNode));
      Transformer serializer = tfactory.newTransformer();
      serializer.transform(new DOMSource(outNode), new StreamResult(new OutputStreamWriter(System.out)));
      return outNode;
    }
    else
    {
      throw new org.xml.sax
        .SAXNotSupportedException("DOM node processing not supported!");
    }
  } 
  public static void exampleParam(String sourceID, 
                                  String xslID)
    throws TransformerException, TransformerConfigurationException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    Templates templates = tfactory.newTemplates(new StreamSource(xslID));
    Transformer transformer1 = templates.newTransformer();
    Transformer transformer2 = templates.newTransformer();
    transformer1.setParameter("a-param",
                              "hello to you!");
    transformer1.transform(new StreamSource(sourceID),
                           new StreamResult(new OutputStreamWriter(System.out)));
    System.out.println("\n=========");
    transformer2.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer2.transform(new StreamSource(sourceID),
                           new StreamResult(new OutputStreamWriter(System.out)));
  }
  public static void exampleTransformerReuse(String sourceID, String xslID)
    throws TransformerException, TransformerConfigurationException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    Transformer transformer 
      = tfactory.newTransformer(new StreamSource(xslID));
    transformer.setParameter("a-param",
                              "hello to you!");
    transformer.transform( new StreamSource(sourceID),
                           new StreamResult(new OutputStreamWriter(System.out)));
    System.out.println("\n=========\n");
    transformer.setParameter("a-param",
                              "hello to me!");
    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
    transformer.transform( new StreamSource(sourceID),
                           new StreamResult(new OutputStreamWriter(System.out)));
  }
  public static void exampleOutputProperties(String sourceID, String xslID)
    throws TransformerException, TransformerConfigurationException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    Templates templates = tfactory.newTemplates(new StreamSource(xslID));
    Properties oprops = templates.getOutputProperties();
    oprops.put(OutputKeys.INDENT, "yes");
    Transformer transformer = templates.newTransformer();
    transformer.setOutputProperties(oprops);
    transformer.transform(new StreamSource(sourceID),
                          new StreamResult(new OutputStreamWriter(System.out)));
  }
  public static void exampleUseAssociated(String sourceID)
    throws TransformerException, TransformerConfigurationException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    if (tfactory instanceof SAXTransformerFactory)
    {
      SAXTransformerFactory stf = ((SAXTransformerFactory) tfactory);
      Source sources =
        stf.getAssociatedStylesheet(new StreamSource(sourceID),
          null, null, null);
      if(null != sources)
      {
        Transformer transformer = tfactory.newTransformer(sources);
        transformer.transform(new StreamSource(sourceID),
                              new StreamResult(new OutputStreamWriter(System.out)));
      }
      else
      {
        System.out.println("Can't find the associated stylesheet!");
      }
    }
  }
  public static void exampleContentHandler2DOM(String sourceID, String xslID)
          throws TransformerException, TransformerConfigurationException, SAXException, IOException, ParserConfigurationException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance();
    if (tfactory.getFeature(SAXSource.FEATURE)
        && tfactory.getFeature(DOMSource.FEATURE))
    {
      SAXTransformerFactory sfactory = (SAXTransformerFactory) tfactory;
      DocumentBuilderFactory dfactory 
        = DocumentBuilderFactory.newInstance();
      DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
      org.w3c.dom.Document outNode = docBuilder.newDocument();
      TransformerHandler handler 
        = sfactory.newTransformerHandler(new StreamSource(xslID));
      handler.setResult(new DOMResult(outNode));
      XMLReader reader=null;
      try {
	  javax.xml.parsers.SAXParserFactory factory=
	      javax.xml.parsers.SAXParserFactory.newInstance();
	  factory.setNamespaceAware( true );
	  javax.xml.parsers.SAXParser jaxpParser=
	      factory.newSAXParser();
	  reader=jaxpParser.getXMLReader();
      } catch( javax.xml.parsers.ParserConfigurationException ex ) {
	  throw new org.xml.sax.SAXException( ex );
      } catch( javax.xml.parsers.FactoryConfigurationError ex1 ) {
	  throw new org.xml.sax.SAXException( ex1.toString() );
      } catch( NoSuchMethodError ex2 ) {
      }
      if( reader==null ) reader= XMLReaderFactory.createXMLReader();
      reader.setContentHandler(handler);
      reader.setProperty("http://xml.org/sax/properties/lexical-handler",
                         handler);
      reader.parse(sourceID);
      exampleSerializeNode(outNode);
    }
    else
    {
      System.out.println(
        "Can't do exampleContentHandlerToContentHandler because tfactory is not a SAXTransformerFactory");
    }
  }
  public static void exampleSerializeNode(Node node)
    throws TransformerException, TransformerConfigurationException, SAXException, IOException,
    ParserConfigurationException
  {
    TransformerFactory tfactory = TransformerFactory.newInstance(); 
    Transformer serializer = tfactory.newTransformer();
    serializer.setOutputProperty(OutputKeys.INDENT, "yes");
    serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    serializer.transform(new DOMSource(node), 
                         new StreamResult(new OutputStreamWriter(System.out)));
  }  
  public static void exampleAsSerializer(String sourceID, String xslID)
    throws TransformerException, TransformerConfigurationException, SAXException, IOException,
    ParserConfigurationException
  {
    DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
    org.w3c.dom.Document outNode = docBuilder.newDocument();
    Node doc = docBuilder.parse(new InputSource(sourceID));
    TransformerFactory tfactory = TransformerFactory.newInstance(); 
    Transformer serializer = tfactory.newTransformer();
    Properties oprops = new Properties();
    oprops.put("method", "html");
    serializer.setOutputProperties(oprops);
    serializer.transform(new DOMSource(doc), 
                         new StreamResult(new OutputStreamWriter(System.out)));
  }
  private static void  handleException( Exception ex ) {
    System.out.println("EXCEPTION: " );
    ex.printStackTrace();
    if( ex instanceof TransformerConfigurationException ) {
      System.out.println();
      System.out.println("Internal exception: " );
      Throwable ex1=((TransformerConfigurationException)ex).getException();
      ex1.printStackTrace();
      if( ex1 instanceof SAXException ) {
	  Exception ex2=((SAXException)ex1).getException();
	  System.out.println("Internal sub-exception: " );
	  ex2.printStackTrace();
      }
    }
  }
}
