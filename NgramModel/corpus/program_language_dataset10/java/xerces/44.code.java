package xni.parser;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;
import org.apache.xerces.xni.XMLDTDContentModelHandler;
import org.apache.xerces.xni.XMLDTDHandler;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
public abstract class AbstractConfiguration 
    implements XMLParserConfiguration {
    protected final Vector fRecognizedFeatures = new Vector();
    protected final Vector fRecognizedProperties = new Vector();
    protected final Hashtable fFeatures = new Hashtable();
    protected final Hashtable fProperties = new Hashtable();
    protected XMLEntityResolver fEntityResolver;
    protected XMLErrorHandler fErrorHandler;
    protected XMLDocumentHandler fDocumentHandler;
    protected XMLDTDHandler fDTDHandler;
    protected XMLDTDContentModelHandler fDTDContentModelHandler;
    protected Locale fLocale;
    protected final Vector fComponents = new Vector();
    public void addRecognizedFeatures(String[] featureIds) {
        int length = featureIds != null ? featureIds.length : 0;
        for (int i = 0; i < length; i++) {
            String featureId = featureIds[i];
            if (!fRecognizedFeatures.contains(featureId)) {
                fRecognizedFeatures.addElement(featureId);
            }
        }
    } 
    public void setFeature(String featureId, boolean state)
        throws XMLConfigurationException {
        if (!fRecognizedFeatures.contains(featureId)) {
            short type = XMLConfigurationException.NOT_RECOGNIZED;
            throw new XMLConfigurationException(type, featureId);
        }
        fFeatures.put(featureId, state ? Boolean.TRUE : Boolean.FALSE);
        int length = fComponents.size();
        for (int i = 0; i < length; i++) {
            XMLComponent component = (XMLComponent)fComponents.elementAt(i);
            component.setFeature(featureId, state);
        }
    } 
    public boolean getFeature(String featureId) 
        throws XMLConfigurationException {
        if (!fRecognizedFeatures.contains(featureId)) {
            short type = XMLConfigurationException.NOT_RECOGNIZED;
            throw new XMLConfigurationException(type, featureId);
        }
        Boolean state = (Boolean)fFeatures.get(featureId);
        return state != null ? state.booleanValue() : false;
    } 
    public void addRecognizedProperties(String[] propertyIds) {
        int length = propertyIds != null ? propertyIds.length : 0;
        for (int i = 0; i < length; i++) {
            String propertyId = propertyIds[i];
            if (!fRecognizedProperties.contains(propertyId)) {
                fRecognizedProperties.addElement(propertyId);
            }
        }
    } 
    public void setProperty(String propertyId, Object value) 
        throws XMLConfigurationException {
        if (!fRecognizedProperties.contains(propertyId)) {
            short type = XMLConfigurationException.NOT_RECOGNIZED;
            throw new XMLConfigurationException(type, propertyId);
        }
        if (value != null) {
            fProperties.put(propertyId, value);
        }
        else {
            fProperties.remove(propertyId);
        }
        int length = fComponents.size();
        for (int i = 0; i < length; i++) {
            XMLComponent component = (XMLComponent)fComponents.elementAt(i);
            component.setProperty(propertyId, value);
        }
    } 
    public Object getProperty(String propertyId) 
        throws XMLConfigurationException {
        if (!fRecognizedProperties.contains(propertyId)) {
            short type = XMLConfigurationException.NOT_RECOGNIZED;
            throw new XMLConfigurationException(type, propertyId);
        }
        Object value = fProperties.get(propertyId);
        return value;
    } 
    public void setEntityResolver(XMLEntityResolver resolver) {
        fEntityResolver = resolver;
    } 
    public XMLEntityResolver getEntityResolver() {
        return fEntityResolver;
    } 
    public void setErrorHandler(XMLErrorHandler handler) {
        fErrorHandler = handler;
    } 
    public XMLErrorHandler getErrorHandler() {
        return fErrorHandler;
    } 
    public void setDocumentHandler(XMLDocumentHandler handler) {
        fDocumentHandler = handler;
    } 
    public XMLDocumentHandler getDocumentHandler() {
        return fDocumentHandler;
    } 
    public void setDTDHandler(XMLDTDHandler handler) {
        fDTDHandler = handler;
    } 
    public XMLDTDHandler getDTDHandler() {
        return fDTDHandler;
    } 
    public void setDTDContentModelHandler(XMLDTDContentModelHandler handler) {
        fDTDContentModelHandler = handler;
    } 
    public XMLDTDContentModelHandler getDTDContentModelHandler() {
        return fDTDContentModelHandler;
    } 
    public abstract void parse(XMLInputSource inputSource) 
        throws IOException, XNIException;
    public void setLocale(Locale locale) {
        fLocale = locale;
    } 
    public Locale getLocale() {
        return fLocale;
    } 
    protected void addComponent(XMLComponent component) {
        if (!fComponents.contains(component)) {
            fComponents.addElement(component);
            addRecognizedFeatures(component.getRecognizedFeatures());
            addRecognizedProperties(component.getRecognizedProperties());
        }
    } 
    protected void resetComponents() 
        throws XMLConfigurationException {
        int length = fComponents.size();
        for (int i = 0; i < length; i++) {
            XMLComponent component = (XMLComponent)fComponents.elementAt(i);
            component.reset(this);
        }
    } 
    protected void openInputSourceStream(XMLInputSource source)
        throws IOException {
        if (source.getCharacterStream() != null) {
            return;
        }
        InputStream stream = source.getByteStream();
        if (stream == null) {
            String systemId = source.getSystemId();
            try {
                URL url = new URL(systemId);
                stream = url.openStream();
            }
            catch (MalformedURLException e) {
                stream = new FileInputStream(systemId);
            }
            source.setByteStream(stream);
        }
    } 
} 
