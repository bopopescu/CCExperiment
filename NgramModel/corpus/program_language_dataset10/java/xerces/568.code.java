package org.apache.xerces.parsers;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.XMLSchemaValidator;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.util.SymbolTable;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
public class StandardParserConfiguration
    extends DTDConfiguration {
    protected static final String NORMALIZE_DATA =
    Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_NORMALIZED_VALUE;
    protected static final String SCHEMA_ELEMENT_DEFAULT =
    Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_ELEMENT_DEFAULT;
    protected static final String SCHEMA_AUGMENT_PSVI =
    Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_AUGMENT_PSVI;
    protected static final String XMLSCHEMA_VALIDATION = 
    Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_VALIDATION_FEATURE;
    protected static final String XMLSCHEMA_FULL_CHECKING = 
    Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_FULL_CHECKING;
    protected static final String GENERATE_SYNTHETIC_ANNOTATIONS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.GENERATE_SYNTHETIC_ANNOTATIONS_FEATURE;
    protected static final String VALIDATE_ANNOTATIONS =
        Constants.XERCES_FEATURE_PREFIX + Constants.VALIDATE_ANNOTATIONS_FEATURE;
    protected static final String HONOUR_ALL_SCHEMALOCATIONS = 
        Constants.XERCES_FEATURE_PREFIX + Constants.HONOUR_ALL_SCHEMALOCATIONS_FEATURE;
    protected static final String NAMESPACE_GROWTH = 
        Constants.XERCES_FEATURE_PREFIX + Constants.NAMESPACE_GROWTH_FEATURE;
    protected static final String TOLERATE_DUPLICATES = 
        Constants.XERCES_FEATURE_PREFIX + Constants.TOLERATE_DUPLICATES_FEATURE;
    protected static final String IGNORE_XSI_TYPE =
        Constants.XERCES_FEATURE_PREFIX + Constants.IGNORE_XSI_TYPE_FEATURE;
    protected static final String ID_IDREF_CHECKING =
        Constants.XERCES_FEATURE_PREFIX + Constants.ID_IDREF_CHECKING_FEATURE;
    protected static final String UNPARSED_ENTITY_CHECKING =
        Constants.XERCES_FEATURE_PREFIX + Constants.UNPARSED_ENTITY_CHECKING_FEATURE;
    protected static final String IDENTITY_CONSTRAINT_CHECKING =
        Constants.XERCES_FEATURE_PREFIX + Constants.IDC_CHECKING_FEATURE;
    protected static final String SCHEMA_VALIDATOR =
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_VALIDATOR_PROPERTY;
    protected static final String SCHEMA_LOCATION =
    Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_LOCATION;
    protected static final String SCHEMA_NONS_LOCATION =
    Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_NONS_LOCATION;
    protected static final String ROOT_TYPE_DEF =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ROOT_TYPE_DEFINITION_PROPERTY;
    protected static final String ROOT_ELEMENT_DECL =
        Constants.XERCES_PROPERTY_PREFIX + Constants.ROOT_ELEMENT_DECLARATION_PROPERTY;
    protected static final String SCHEMA_DV_FACTORY = 
        Constants.XERCES_PROPERTY_PREFIX + Constants.SCHEMA_DV_FACTORY_PROPERTY;
    protected XMLSchemaValidator fSchemaValidator;
    public StandardParserConfiguration() {
        this(null, null, null);
    } 
    public StandardParserConfiguration(SymbolTable symbolTable) {
        this(symbolTable, null, null);
    } 
    public StandardParserConfiguration(SymbolTable symbolTable,
                                       XMLGrammarPool grammarPool) {
        this(symbolTable, grammarPool, null);
    } 
    public StandardParserConfiguration(SymbolTable symbolTable,
                                       XMLGrammarPool grammarPool,
                                       XMLComponentManager parentSettings) {
        super(symbolTable, grammarPool, parentSettings);
        final String[] recognizedFeatures = {
            NORMALIZE_DATA,
            SCHEMA_ELEMENT_DEFAULT,
            SCHEMA_AUGMENT_PSVI,
            GENERATE_SYNTHETIC_ANNOTATIONS,
            VALIDATE_ANNOTATIONS,
            HONOUR_ALL_SCHEMALOCATIONS,
            NAMESPACE_GROWTH,
            TOLERATE_DUPLICATES,
            XMLSCHEMA_VALIDATION,
            XMLSCHEMA_FULL_CHECKING,
            IGNORE_XSI_TYPE,
            ID_IDREF_CHECKING,
            IDENTITY_CONSTRAINT_CHECKING,
            UNPARSED_ENTITY_CHECKING,
        };
        addRecognizedFeatures(recognizedFeatures);
        setFeature(SCHEMA_ELEMENT_DEFAULT, true);
        setFeature(NORMALIZE_DATA, true);
        setFeature(SCHEMA_AUGMENT_PSVI, true);
        setFeature(GENERATE_SYNTHETIC_ANNOTATIONS, false);
        setFeature(VALIDATE_ANNOTATIONS, false);
        setFeature(HONOUR_ALL_SCHEMALOCATIONS, false);
        setFeature(NAMESPACE_GROWTH, false);
        setFeature(TOLERATE_DUPLICATES, false);
        setFeature(IGNORE_XSI_TYPE, false);
        setFeature(ID_IDREF_CHECKING, true);
        setFeature(IDENTITY_CONSTRAINT_CHECKING, true);
        setFeature(UNPARSED_ENTITY_CHECKING, true);
        final String[] recognizedProperties = {
            SCHEMA_LOCATION,
            SCHEMA_NONS_LOCATION,
            ROOT_TYPE_DEF,
            ROOT_ELEMENT_DECL,
            SCHEMA_DV_FACTORY,
        };
        addRecognizedProperties(recognizedProperties);
    } 
    protected void configurePipeline() {
        super.configurePipeline();
        if ( getFeature(XMLSCHEMA_VALIDATION )) {
            if (fSchemaValidator == null) {
                fSchemaValidator = new XMLSchemaValidator(); 
                fProperties.put(SCHEMA_VALIDATOR, fSchemaValidator);
                addComponent(fSchemaValidator);
                if (fErrorReporter.getMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN) == null) {
                    XSMessageFormatter xmft = new XSMessageFormatter();
                    fErrorReporter.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, xmft);
                }
            }
            fLastComponent = fSchemaValidator;
            fNamespaceBinder.setDocumentHandler(fSchemaValidator);
            fSchemaValidator.setDocumentHandler(fDocumentHandler);
            fSchemaValidator.setDocumentSource(fNamespaceBinder);
        } 
    } 
    protected void checkFeature(String featureId)
        throws XMLConfigurationException {
        if (featureId.startsWith(Constants.XERCES_FEATURE_PREFIX)) {
            final int suffixLength = featureId.length() - Constants.XERCES_FEATURE_PREFIX.length();
            if (suffixLength == Constants.SCHEMA_VALIDATION_FEATURE.length() && 
                featureId.endsWith(Constants.SCHEMA_VALIDATION_FEATURE)) {
                return;
            }
            if (suffixLength == Constants.SCHEMA_FULL_CHECKING.length() &&
                featureId.endsWith(Constants.SCHEMA_FULL_CHECKING)) {
                return;
            }
            if (suffixLength == Constants.SCHEMA_NORMALIZED_VALUE.length() && 
                featureId.endsWith(Constants.SCHEMA_NORMALIZED_VALUE)) {
                return;
            } 
            if (suffixLength == Constants.SCHEMA_ELEMENT_DEFAULT.length() && 
                featureId.endsWith(Constants.SCHEMA_ELEMENT_DEFAULT)) {
                return;
            }
        }
        super.checkFeature(featureId);
    } 
    protected void checkProperty(String propertyId)
        throws XMLConfigurationException {
        if (propertyId.startsWith(Constants.XERCES_PROPERTY_PREFIX)) {
            final int suffixLength = propertyId.length() - Constants.XERCES_PROPERTY_PREFIX.length();
            if (suffixLength == Constants.SCHEMA_LOCATION.length() && 
                propertyId.endsWith(Constants.SCHEMA_LOCATION)) {
                return;
            }
            if (suffixLength == Constants.SCHEMA_NONS_LOCATION.length() && 
                propertyId.endsWith(Constants.SCHEMA_NONS_LOCATION)) {
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
} 
