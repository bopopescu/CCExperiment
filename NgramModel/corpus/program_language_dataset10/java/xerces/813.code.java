package schema.config;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import junit.framework.Assert;
import org.apache.xerces.xs.ItemPSVI;
import org.xml.sax.SAXException;
public class RootSimpleTypeDefinitionTest extends BaseTest {
    private QName typeString;
    private QName typeNonNegInt;
    private final static String INVALID_TYPE_ERROR = "cvc-type.3.1.3";
    private final static String MININCLUSIVE_DERIVATION_ERROR = "cvc-minInclusive-valid";
    public static void main(String[] args) {
        junit.textui.TestRunner.run(RootSimpleTypeDefinitionTest.class);
    }
    protected String getXMLDocument() {
        return "simpleType.xml";
    }
    protected String getSchemaFile() {
        return "base.xsd";
    }
    protected String[] getRelevantErrorIDs() {
        return new String[] { INVALID_TYPE_ERROR, MININCLUSIVE_DERIVATION_ERROR };
    }
    public RootSimpleTypeDefinitionTest(String name) {
        super(name);
        String ns = "x" + XMLConstants.W3C_XML_SCHEMA_NS_URI;
        ns = ns.substring(1);
        typeString = new QName(ns, "string", "xsd");
        typeNonNegInt = new QName(ns, "nonNegativeInteger", "xsd");
    }
    public void testSettingSimpleType() throws Exception {
        try {
            fValidator.setProperty(ROOT_TYPE, typeString);
        } catch (SAXException e1) {
            Assert.fail("Problem setting property: " + e1.getMessage());
        }
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        assertValidity(ItemPSVI.VALIDITY_VALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementNull(fRootNode.getElementDeclaration());
        assertTypeName("string", fRootNode.getTypeDefinition().getName());
    }
    public void testSettingInvalidSimpleType() throws Exception {
        try {
            fValidator.setProperty(ROOT_TYPE, typeNonNegInt);
        } catch (SAXException e1) {
            Assert.fail("Problem setting property: " + e1.getMessage());
        }
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        assertError(INVALID_TYPE_ERROR);
        assertError(MININCLUSIVE_DERIVATION_ERROR);
        assertValidity(ItemPSVI.VALIDITY_INVALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementNull(fRootNode.getElementDeclaration());
        assertTypeName("nonNegativeInteger", fRootNode.getTypeDefinition().getName());
    }
}
