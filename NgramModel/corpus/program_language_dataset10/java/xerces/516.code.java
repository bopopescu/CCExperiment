package org.apache.xerces.jaxp;
import java.util.Hashtable;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import org.apache.xerces.impl.Constants;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
public class SAXParserFactoryImpl extends SAXParserFactory {
    private static final String NAMESPACES_FEATURE =
        Constants.SAX_FEATURE_PREFIX + Constants.NAMESPACES_FEATURE;
    private static final String VALIDATION_FEATURE =
        Constants.SAX_FEATURE_PREFIX + Constants.VALIDATION_FEATURE;
    private static final String XINCLUDE_FEATURE = 
        Constants.XERCES_FEATURE_PREFIX + Constants.XINCLUDE_FEATURE;
    private Hashtable features;
    private Schema grammar;
    private boolean isXIncludeAware;
    private boolean fSecureProcess = false;
    public SAXParser newSAXParser()
        throws ParserConfigurationException {
        SAXParser saxParserImpl;
        try {
            saxParserImpl = new SAXParserImpl(this, features, fSecureProcess);
        } 
        catch (SAXException se) {
            throw new ParserConfigurationException(se.getMessage());
        }
        return saxParserImpl;
    }
    private SAXParserImpl newSAXParserImpl()
        throws ParserConfigurationException, SAXNotRecognizedException, 
        SAXNotSupportedException {
        SAXParserImpl saxParserImpl;
        try {
            saxParserImpl = new SAXParserImpl(this, features);
        } catch (SAXNotSupportedException e) {
            throw e;
        } catch (SAXNotRecognizedException e) {
            throw e;
        } catch (SAXException se) {
            throw new ParserConfigurationException(se.getMessage());
        }
        return saxParserImpl;
    }
    public void setFeature(String name, boolean value)
        throws ParserConfigurationException, SAXNotRecognizedException, 
		SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException();
        }
        if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
            fSecureProcess = value;
            return;
        }
        else if (name.equals(NAMESPACES_FEATURE)) {
            setNamespaceAware(value);
            return;
        }
        else if (name.equals(VALIDATION_FEATURE)) {
            setValidating(value);
            return;
        }
        else if (name.equals(XINCLUDE_FEATURE)) {
            setXIncludeAware(value);
            return;
        }
        if (features == null) {
            features = new Hashtable();
        }
        features.put(name, value ? Boolean.TRUE : Boolean.FALSE);
        try {
            newSAXParserImpl();
        } 
        catch (SAXNotSupportedException e) {
            features.remove(name);
            throw e;
        } 
        catch (SAXNotRecognizedException e) {
            features.remove(name);
            throw e;
        }
    }
    public boolean getFeature(String name)
        throws ParserConfigurationException, SAXNotRecognizedException,
		SAXNotSupportedException {
        if (name == null) {
            throw new NullPointerException();
        }
        if (name.equals(XMLConstants.FEATURE_SECURE_PROCESSING)) {
            return fSecureProcess;
        }
        else if (name.equals(NAMESPACES_FEATURE)) {
            return isNamespaceAware();
        }
        else if (name.equals(VALIDATION_FEATURE)) {
            return isValidating();
        }
        else if (name.equals(XINCLUDE_FEATURE)) {
            return isXIncludeAware();
        }
        return newSAXParserImpl().getXMLReader().getFeature(name);
    }
    public Schema getSchema() {
        return grammar;
    }
    public void setSchema(Schema grammar) {
        this.grammar = grammar;
    }
    public boolean isXIncludeAware() {
        return this.isXIncludeAware;
    }
    public void setXIncludeAware(boolean state) {
        this.isXIncludeAware = state;
    }
}
