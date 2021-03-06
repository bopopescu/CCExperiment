package schema.config;
import junit.framework.Assert;
import org.apache.xerces.dom.PSVIElementNSImpl;
import org.apache.xerces.xs.ItemPSVI;
import org.xml.sax.SAXException;
public class IdIdrefCheckingTest extends BaseTest {
    public static final String DUPLICATE_ID = "cvc-id.2";
    public static final String NO_ID_BINDING = "cvc-id.1";
    public static void main(String[] args) {
        junit.textui.TestRunner.run(IdIdrefCheckingTest.class);
    }
    protected String getXMLDocument() {
        return "idIdref.xml";
    }
    protected String getSchemaFile() {
        return "base.xsd";
    }
    protected String[] getRelevantErrorIDs() {
        return new String[] { DUPLICATE_ID, NO_ID_BINDING };
    }
    public IdIdrefCheckingTest(String name) {
        super(name);
    }
    protected void setUp() throws Exception {
        super.setUp();
    }
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    public void testDefault() {
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        checkDefault();
    }
    public void testSetFalse() {
        try {
            fValidator.setFeature(ID_IDREF_CHECKING, false);
        } catch (SAXException e) {
            Assert.fail("Error setting feature.");
        }
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        checkValidResult();
    }
    public void testSetTrue() {
        try {
            fValidator.setFeature(ID_IDREF_CHECKING, true);
        } catch (SAXException e) {
            Assert.fail("Error setting feature.");
        }
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        checkDefault();
    }
    private void checkDefault() {
        assertError(DUPLICATE_ID);
        assertError(NO_ID_BINDING);
        assertValidity(ItemPSVI.VALIDITY_INVALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementName("A", fRootNode.getElementDeclaration().getName());
        assertTypeName("X", fRootNode.getTypeDefinition().getName());
        PSVIElementNSImpl child = super.getChild(1);
        assertValidity(ItemPSVI.VALIDITY_VALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("A", child.getElementDeclaration().getName());
        assertTypeName("idType", child.getTypeDefinition().getName());
        child = super.getChild(2);
        assertValidity(ItemPSVI.VALIDITY_INVALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("A", child.getElementDeclaration().getName());
        assertTypeName("idType", child.getTypeDefinition().getName());
        child = super.getChild(3);
        assertValidity(ItemPSVI.VALIDITY_VALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("A", child.getElementDeclaration().getName());
        assertTypeName("idrefType", child.getTypeDefinition().getName());
    }
    private void checkValidResult() {
        assertNoError(DUPLICATE_ID);
        assertNoError(NO_ID_BINDING);
        assertValidity(ItemPSVI.VALIDITY_VALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementName("A", fRootNode.getElementDeclaration().getName());
        assertTypeName("X", fRootNode.getTypeDefinition().getName());
        PSVIElementNSImpl child = super.getChild(1);
        assertValidity(ItemPSVI.VALIDITY_VALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("A", child.getElementDeclaration().getName());
        assertTypeName("idType", child.getTypeDefinition().getName());
        child = super.getChild(2);
        assertValidity(ItemPSVI.VALIDITY_VALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("A", child.getElementDeclaration().getName());
        assertTypeName("idType", child.getTypeDefinition().getName());
        child = super.getChild(3);
        assertValidity(ItemPSVI.VALIDITY_VALID, child.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, child
                .getValidationAttempted());
        assertElementName("A", child.getElementDeclaration().getName());
        assertTypeName("idrefType", child.getTypeDefinition().getName());
    }
}
