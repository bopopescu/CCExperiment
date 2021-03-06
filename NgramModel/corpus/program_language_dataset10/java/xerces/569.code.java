package org.apache.xerces.parsers;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xinclude.XIncludeHandler;
import org.apache.xerces.xinclude.XIncludeNamespaceSupport;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDocumentSource;
public class XIncludeAwareParserConfiguration extends XML11Configuration {
    protected static final String ALLOW_UE_AND_NOTATION_EVENTS =
        Constants.SAX_FEATURE_PREFIX + Constants.ALLOW_DTD_EVENTS_AFTER_ENDDTD_FEATURE;
    protected static final String XINCLUDE_FIXUP_BASE_URIS =
        Constants.XERCES_FEATURE_PREFIX + Constants.XINCLUDE_FIXUP_BASE_URIS_FEATURE;
    protected static final String XINCLUDE_FIXUP_LANGUAGE =
        Constants.XERCES_FEATURE_PREFIX + Constants.XINCLUDE_FIXUP_LANGUAGE_FEATURE;
    protected static final String XINCLUDE_FEATURE = 
        Constants.XERCES_FEATURE_PREFIX + Constants.XINCLUDE_FEATURE;
    protected static final String XINCLUDE_HANDLER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.XINCLUDE_HANDLER_PROPERTY;
    protected static final String NAMESPACE_CONTEXT =
        Constants.XERCES_PROPERTY_PREFIX + Constants.NAMESPACE_CONTEXT_PROPERTY;
    protected XIncludeHandler fXIncludeHandler;
    protected NamespaceSupport fNonXIncludeNSContext;
    protected XIncludeNamespaceSupport fXIncludeNSContext;
    protected NamespaceContext fCurrentNSContext;
    protected boolean fXIncludeEnabled = false;
    public XIncludeAwareParserConfiguration() {
        this(null, null, null);
    } 
    public XIncludeAwareParserConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    } 
    public XIncludeAwareParserConfiguration(
            SymbolTable symbolTable,
            XMLGrammarPool grammarPool) {
        this(symbolTable, grammarPool, null);
    } 
    public XIncludeAwareParserConfiguration(
            SymbolTable symbolTable,
            XMLGrammarPool grammarPool,
            XMLComponentManager parentSettings) {
        super(symbolTable, grammarPool, parentSettings);
        final String[] recognizedFeatures = {
                ALLOW_UE_AND_NOTATION_EVENTS,
                XINCLUDE_FIXUP_BASE_URIS,
                XINCLUDE_FIXUP_LANGUAGE
        };
        addRecognizedFeatures(recognizedFeatures);
        final String[] recognizedProperties =
        { XINCLUDE_HANDLER, NAMESPACE_CONTEXT };
        addRecognizedProperties(recognizedProperties);
        setFeature(ALLOW_UE_AND_NOTATION_EVENTS, true);
        setFeature(XINCLUDE_FIXUP_BASE_URIS, true);
        setFeature(XINCLUDE_FIXUP_LANGUAGE, true);
        fNonXIncludeNSContext = new NamespaceSupport();
        fCurrentNSContext = fNonXIncludeNSContext;
        setProperty(NAMESPACE_CONTEXT, fNonXIncludeNSContext);
    }
    protected void configurePipeline() {
        super.configurePipeline();
        if (fXIncludeEnabled) {
            if (fXIncludeHandler == null) {
                fXIncludeHandler = new XIncludeHandler();
                setProperty(XINCLUDE_HANDLER, fXIncludeHandler);
                addCommonComponent(fXIncludeHandler);
                fXIncludeHandler.reset(this);
            }
            if (fCurrentNSContext != fXIncludeNSContext) {
                if (fXIncludeNSContext == null) {
                    fXIncludeNSContext = new XIncludeNamespaceSupport();
                }
                fCurrentNSContext = fXIncludeNSContext;
                setProperty(NAMESPACE_CONTEXT, fXIncludeNSContext);
            }
            fDTDScanner.setDTDHandler(fDTDProcessor);
            fDTDProcessor.setDTDSource(fDTDScanner);
            fDTDProcessor.setDTDHandler(fXIncludeHandler);
            fXIncludeHandler.setDTDSource(fDTDProcessor);
            fXIncludeHandler.setDTDHandler(fDTDHandler);
            if (fDTDHandler != null) {
                fDTDHandler.setDTDSource(fXIncludeHandler);
            }
            XMLDocumentSource prev = null;
            if (fFeatures.get(XMLSCHEMA_VALIDATION) == Boolean.TRUE) {
                prev = fSchemaValidator.getDocumentSource();
            }
            else {
                prev = fLastComponent;
                fLastComponent = fXIncludeHandler;
            }
            XMLDocumentHandler next = prev.getDocumentHandler();
            prev.setDocumentHandler(fXIncludeHandler);
            fXIncludeHandler.setDocumentSource(prev);
            if (next != null) {
                fXIncludeHandler.setDocumentHandler(next);
                next.setDocumentSource(fXIncludeHandler);
            }
        }
        else {
            if (fCurrentNSContext != fNonXIncludeNSContext) {
                fCurrentNSContext = fNonXIncludeNSContext;
                setProperty(NAMESPACE_CONTEXT, fNonXIncludeNSContext);
            }
        }
    } 
    protected void configureXML11Pipeline() {
        super.configureXML11Pipeline();
        if (fXIncludeEnabled) {
            if (fXIncludeHandler == null) {
                fXIncludeHandler = new XIncludeHandler();
                setProperty(XINCLUDE_HANDLER, fXIncludeHandler);
                addCommonComponent(fXIncludeHandler);
                fXIncludeHandler.reset(this);
            }
            if (fCurrentNSContext != fXIncludeNSContext) {
                if (fXIncludeNSContext == null) {
                    fXIncludeNSContext = new XIncludeNamespaceSupport();
                }
                fCurrentNSContext = fXIncludeNSContext;
                setProperty(NAMESPACE_CONTEXT, fXIncludeNSContext);
            }
            fXML11DTDScanner.setDTDHandler(fXML11DTDProcessor);
            fXML11DTDProcessor.setDTDSource(fXML11DTDScanner);
            fXML11DTDProcessor.setDTDHandler(fXIncludeHandler);
            fXIncludeHandler.setDTDSource(fXML11DTDProcessor);
            fXIncludeHandler.setDTDHandler(fDTDHandler);
            if (fDTDHandler != null) {
                fDTDHandler.setDTDSource(fXIncludeHandler);
            }
            XMLDocumentSource prev = null;
            if (fFeatures.get(XMLSCHEMA_VALIDATION) == Boolean.TRUE) {
                prev = fSchemaValidator.getDocumentSource();
            }
            else {
                prev = fLastComponent;
                fLastComponent = fXIncludeHandler;
            }
            XMLDocumentHandler next = prev.getDocumentHandler();
            prev.setDocumentHandler(fXIncludeHandler);
            fXIncludeHandler.setDocumentSource(prev);
            if (next != null) {
                fXIncludeHandler.setDocumentHandler(next);
                next.setDocumentSource(fXIncludeHandler);
            }
        }
        else {
            if (fCurrentNSContext != fNonXIncludeNSContext) {
                fCurrentNSContext = fNonXIncludeNSContext;
                setProperty(NAMESPACE_CONTEXT, fNonXIncludeNSContext);
            }
        }
    } 
    public boolean getFeature(String featureId)
        throws XMLConfigurationException {
        if (featureId.equals(PARSER_SETTINGS)) {
            return fConfigUpdated;
        }
        else if (featureId.equals(XINCLUDE_FEATURE)) {
            return fXIncludeEnabled;
        }
        return super.getFeature0(featureId);
    } 
    public void setFeature(String featureId, boolean state)
        throws XMLConfigurationException {
        if (featureId.equals(XINCLUDE_FEATURE)) {
            fXIncludeEnabled = state;
            fConfigUpdated = true;
            return;
        }
        super.setFeature(featureId,state);
    }
}
