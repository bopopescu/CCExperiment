package org.apache.xerces.parsers;
import java.io.IOException;
import java.util.Locale;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLDTDScannerImpl;
import org.apache.xerces.impl.XMLDocumentScannerImpl;
import org.apache.xerces.impl.XMLEntityManager;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.impl.XMLNSDocumentScannerImpl;
import org.apache.xerces.impl.dv.DTDDVFactory;
import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.impl.validation.ValidationManager;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponent;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.apache.xerces.xni.parser.XMLDTDScanner;
import org.apache.xerces.xni.parser.XMLDocumentScanner;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.apache.xerces.xni.parser.XMLPullParserConfiguration;
public class NonValidatingConfiguration
    extends BasicParserConfiguration 
    implements XMLPullParserConfiguration {
    protected static final String WARN_ON_DUPLICATE_ATTDEF =
        Constants.XERCES_FEATURE_PREFIX + Constants.WARN_ON_DUPLICATE_ATTDEF_FEATURE;
    protected static final String WARN_ON_DUPLICATE_ENTITYDEF =
        Constants.XERCES_FEATURE_PREFIX + Constants.WARN_ON_DUPLICATE_ENTITYDEF_FEATURE;
    protected static final String WARN_ON_UNDECLARED_ELEMDEF =
        Constants.XERCES_FEATURE_PREFIX + Constants.WARN_ON_UNDECLARED_ELEMDEF_FEATURE;
    protected static final String ALLOW_JAVA_ENCODINGS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.ALLOW_JAVA_ENCODINGS_FEATURE;
    protected static final String CONTINUE_AFTER_FATAL_ERROR = 
        Constants.XERCES_FEATURE_PREFIX + Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE;
    protected static final String LOAD_EXTERNAL_DTD =
        Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE;
    protected static final String NOTIFY_BUILTIN_REFS =
        Constants.XERCES_FEATURE_PREFIX + Constants.NOTIFY_BUILTIN_REFS_FEATURE;
    protected static final String NOTIFY_CHAR_REFS =
        Constants.XERCES_FEATURE_PREFIX + Constants.NOTIFY_CHAR_REFS_FEATURE;
    protected static final String NORMALIZE_DATA =
    Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_NORMALIZED_VALUE;
    protected static final String SCHEMA_ELEMENT_DEFAULT =
    Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_ELEMENT_DEFAULT;
    protected static final String ERROR_REPORTER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ERROR_REPORTER_PROPERTY;
    protected static final String ENTITY_MANAGER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.ENTITY_MANAGER_PROPERTY;
    protected static final String DOCUMENT_SCANNER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DOCUMENT_SCANNER_PROPERTY;
    protected static final String DTD_SCANNER = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_SCANNER_PROPERTY;
    protected static final String XMLGRAMMAR_POOL = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;
    protected static final String DTD_VALIDATOR = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DTD_VALIDATOR_PROPERTY;
    protected static final String NAMESPACE_BINDER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.NAMESPACE_BINDER_PROPERTY;
    protected static final String DATATYPE_VALIDATOR_FACTORY = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.DATATYPE_VALIDATOR_FACTORY_PROPERTY;
    protected static final String VALIDATION_MANAGER =
        Constants.XERCES_PROPERTY_PREFIX + Constants.VALIDATION_MANAGER_PROPERTY;
    protected static final String SCHEMA_VALIDATOR =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_VALIDATOR_PROPERTY;
    protected static final String LOCALE =
        Constants.XERCES_PROPERTY_PREFIX + Constants.LOCALE_PROPERTY;
    private static final boolean PRINT_EXCEPTION_STACK_TRACE = false;
    protected XMLGrammarPool fGrammarPool;
    protected DTDDVFactory fDatatypeValidatorFactory;
    protected XMLErrorReporter fErrorReporter;
    protected XMLEntityManager fEntityManager;
    protected XMLDocumentScanner fScanner;
    protected XMLInputSource fInputSource;
    protected XMLDTDScanner fDTDScanner;
    protected ValidationManager fValidationManager;
    private XMLNSDocumentScannerImpl fNamespaceScanner;
    private XMLDocumentScannerImpl fNonNSScanner;
	protected boolean fConfigUpdated = false;
    protected XMLLocator fLocator;
    protected boolean fParseInProgress = false;
    public NonValidatingConfiguration() {
        this(null, null, null);
    } 
    public NonValidatingConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    } 
    public NonValidatingConfiguration(SymbolTable symbolTable,
                                       XMLGrammarPool grammarPool) {
        this(symbolTable, grammarPool, null);
    } 
    public NonValidatingConfiguration(SymbolTable symbolTable,
                                       XMLGrammarPool grammarPool,
                                       XMLComponentManager parentSettings) {
        super(symbolTable, parentSettings);
        final String[] recognizedFeatures = {        	
        	PARSER_SETTINGS,
			NAMESPACES,
            CONTINUE_AFTER_FATAL_ERROR,
        };
        addRecognizedFeatures(recognizedFeatures);
        fFeatures.put(CONTINUE_AFTER_FATAL_ERROR, Boolean.FALSE);
		fFeatures.put(PARSER_SETTINGS, Boolean.TRUE);
		fFeatures.put(NAMESPACES, Boolean.TRUE);
        final String[] recognizedProperties = {
            ERROR_REPORTER,             
            ENTITY_MANAGER, 
            DOCUMENT_SCANNER,
            DTD_SCANNER,
            DTD_VALIDATOR,
            NAMESPACE_BINDER,
            XMLGRAMMAR_POOL,   
            DATATYPE_VALIDATOR_FACTORY,
            VALIDATION_MANAGER,
            LOCALE
        };
        addRecognizedProperties(recognizedProperties);
        fGrammarPool = grammarPool;
        if(fGrammarPool != null){
			fProperties.put(XMLGRAMMAR_POOL, fGrammarPool);
        }
        fEntityManager = createEntityManager();
		fProperties.put(ENTITY_MANAGER, fEntityManager);
        addComponent(fEntityManager);
        fErrorReporter = createErrorReporter();
        fErrorReporter.setDocumentLocator(fEntityManager.getEntityScanner());
		fProperties.put(ERROR_REPORTER, fErrorReporter);
        addComponent(fErrorReporter);
        fDTDScanner = createDTDScanner();
        if (fDTDScanner != null) {
			fProperties.put(DTD_SCANNER, fDTDScanner);
            if (fDTDScanner instanceof XMLComponent) {
                addComponent((XMLComponent)fDTDScanner);
            }
        }
        fDatatypeValidatorFactory = createDatatypeValidatorFactory();
        if (fDatatypeValidatorFactory != null) {
			fProperties.put(DATATYPE_VALIDATOR_FACTORY,
                        fDatatypeValidatorFactory);
        }
        fValidationManager = createValidationManager();
        if (fValidationManager != null) {
			fProperties.put(VALIDATION_MANAGER, fValidationManager);
        }
        if (fErrorReporter.getMessageFormatter(XMLMessageFormatter.XML_DOMAIN) == null) {
            XMLMessageFormatter xmft = new XMLMessageFormatter();
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN, xmft);
            fErrorReporter.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN, xmft);
        }
		fConfigUpdated = false;
        try {
            setLocale(Locale.getDefault());
        }
        catch (XNIException e) {
        }
    } 
	public void setFeature(String featureId, boolean state)
		throws XMLConfigurationException {
		fConfigUpdated = true;
		super.setFeature(featureId, state);
	}
	public Object getProperty(String propertyId)
	    throws XMLConfigurationException {
	    if (LOCALE.equals(propertyId)) {
	        return getLocale();
	    }
	    return super.getProperty(propertyId);
	}
	public void setProperty(String propertyId, Object value)
	    throws XMLConfigurationException {
	    fConfigUpdated = true;
        if (LOCALE.equals(propertyId)) {
            setLocale((Locale) value);
        }
	    super.setProperty(propertyId, value);
	}
    public void setLocale(Locale locale) throws XNIException {
        super.setLocale(locale);
        fErrorReporter.setLocale(locale);
    } 
	public boolean getFeature(String featureId)
		throws XMLConfigurationException {
		if (featureId.equals(PARSER_SETTINGS)){
			return fConfigUpdated;
		}
		return super.getFeature(featureId);
	} 
    public void setInputSource(XMLInputSource inputSource)
        throws XMLConfigurationException, IOException {
        fInputSource = inputSource;
    } 
    public boolean parse(boolean complete) throws XNIException, IOException {
        if (fInputSource !=null) {
            try {
                reset();
                fScanner.setInputSource(fInputSource);
                fInputSource = null;
            } 
            catch (XNIException ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw ex;
            } 
            catch (IOException ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw ex;
            } 
            catch (RuntimeException ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw ex;
            }
            catch (Exception ex) {
                if (PRINT_EXCEPTION_STACK_TRACE)
                    ex.printStackTrace();
                throw new XNIException(ex);
            }
        }
        try {
            return fScanner.scanDocument(complete);
        } 
        catch (XNIException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } 
        catch (IOException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } 
        catch (RuntimeException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        }
        catch (Exception ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw new XNIException(ex);
        }
    } 
    public void cleanup() {
        fEntityManager.closeReaders();
    }
    public void parse(XMLInputSource source) throws XNIException, IOException {
        if (fParseInProgress) {
            throw new XNIException("FWK005 parse may not be called while parsing.");
        }
        fParseInProgress = true;
        try {
            setInputSource(source);
            parse(true);
        } 
        catch (XNIException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        } 
        catch (IOException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        }
        catch (RuntimeException ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw ex;
        }              
        catch (Exception ex) {
            if (PRINT_EXCEPTION_STACK_TRACE)
                ex.printStackTrace();
            throw new XNIException(ex);
        }
        finally {
            fParseInProgress = false;
            this.cleanup();
        }
    } 
    protected void reset() throws XNIException {
        if (fValidationManager != null)
            fValidationManager.reset();
        configurePipeline();
        super.reset();
    } 
    protected void configurePipeline() {
        if (fFeatures.get(NAMESPACES) == Boolean.TRUE) {
            if (fNamespaceScanner == null) {
                fNamespaceScanner = new XMLNSDocumentScannerImpl();
                addComponent((XMLComponent)fNamespaceScanner);
            }
            fProperties.put(DOCUMENT_SCANNER, fNamespaceScanner);
            fNamespaceScanner.setDTDValidator(null);
            fScanner = fNamespaceScanner;
        } 
        else {
            if (fNonNSScanner == null) {
                fNonNSScanner = new XMLDocumentScannerImpl();
                addComponent((XMLComponent)fNonNSScanner);
            }
            fProperties.put(DOCUMENT_SCANNER, fNonNSScanner);
            fScanner = fNonNSScanner;
        }
        fScanner.setDocumentHandler(fDocumentHandler);
        fLastComponent = fScanner;
        if (fDTDScanner != null) {
                fDTDScanner.setDTDHandler(fDTDHandler);
                fDTDScanner.setDTDContentModelHandler(fDTDContentModelHandler);
        }
    } 
    protected void checkFeature(String featureId)
        throws XMLConfigurationException {
        if (featureId.startsWith(Constants.XERCES_FEATURE_PREFIX)) {
            final int suffixLength = featureId.length() - Constants.XERCES_FEATURE_PREFIX.length();
            if (suffixLength == Constants.DYNAMIC_VALIDATION_FEATURE.length() && 
                featureId.endsWith(Constants.DYNAMIC_VALIDATION_FEATURE)) {
                return;
            }
            if (suffixLength == Constants.DEFAULT_ATTRIBUTE_VALUES_FEATURE.length() && 
                featureId.endsWith(Constants.DEFAULT_ATTRIBUTE_VALUES_FEATURE)) {
                short type = XMLConfigurationException.NOT_SUPPORTED;
                throw new XMLConfigurationException(type, featureId);
            }
            if (suffixLength == Constants.VALIDATE_CONTENT_MODELS_FEATURE.length() && 
                featureId.endsWith(Constants.VALIDATE_CONTENT_MODELS_FEATURE)) {
                short type = XMLConfigurationException.NOT_SUPPORTED;
                throw new XMLConfigurationException(type, featureId);
            }
            if (suffixLength == Constants.LOAD_DTD_GRAMMAR_FEATURE.length() && 
                featureId.endsWith(Constants.LOAD_DTD_GRAMMAR_FEATURE)) {
                return;
            }
            if (suffixLength == Constants.LOAD_EXTERNAL_DTD_FEATURE.length() && 
                featureId.endsWith(Constants.LOAD_EXTERNAL_DTD_FEATURE)) {
                return;
            }
            if (suffixLength == Constants.VALIDATE_DATATYPES_FEATURE.length() && 
                featureId.endsWith(Constants.VALIDATE_DATATYPES_FEATURE)) {
                short type = XMLConfigurationException.NOT_SUPPORTED;
                throw new XMLConfigurationException(type, featureId);
            }
        }
        super.checkFeature(featureId);
    } 
    protected void checkProperty(String propertyId)
        throws XMLConfigurationException {
        if (propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            final int suffixLength = propertyId.length() - Constants.XERCES_PROPERTY_PREFIX.length();
            if (suffixLength == Constants.DTD_SCANNER_PROPERTY.length() && 
                propertyId.endsWith(Constants.DTD_SCANNER_PROPERTY)) {
                return;
            }
        }
        if (propertyId.startsWith(Constants.JAXP_PROPERTY_PREFIX)) {
            final int suffixLength = propertyId.length() - Constants.JAXP_PROPERTY_PREFIX.length();
            if (suffixLength == Constants.SCHEMA_SOURCE.length() && 
                propertyId.endsWith(Constants.SCHEMA_SOURCE)) {
                return;
            }
        }
        super.checkProperty(propertyId);
    } 
    protected XMLEntityManager createEntityManager() {
        return new XMLEntityManager();
    } 
    protected XMLErrorReporter createErrorReporter() {
        return new XMLErrorReporter();
    } 
    protected XMLDocumentScanner createDocumentScanner() {
        return null;
    } 
    protected XMLDTDScanner createDTDScanner() {
        return new XMLDTDScannerImpl();
    } 
    protected DTDDVFactory createDatatypeValidatorFactory() {
        return DTDDVFactory.getInstance();
    } 
    protected ValidationManager createValidationManager(){
        return new ValidationManager();
    }
} 
