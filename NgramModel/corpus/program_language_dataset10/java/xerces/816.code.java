package schema.config;
import junit.framework.Assert;
import org.apache.xerces.dom.PSVIElementNSImpl;
import org.apache.xerces.xs.ItemPSVI;
import org.xml.sax.SAXException;
public class UnparsedEntityCheckingTest extends BaseTest {
    public static final String UNDECLARED_ENTITY = "UndeclaredEntity";
    public static void main(String[] args) {
        junit.textui.TestRunner.run(UnparsedEntityCheckingTest.class);
    }
    protected String getXMLDocument() {
        return "unparsedEntity.xml";
    }
    protected String getSchemaFile() {
        return "base.xsd";
    }
    protected String[] getRelevantErrorIDs() {
        return new String[] { UNDECLARED_ENTITY };
    }
    public UnparsedEntityCheckingTest(String name) {
        super(name);
    }
    public void testDefaultValid() {
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        checkDefault();
    }
    public void testSetFalseValid() {
        try {
            fValidator.setFeature(UNPARSED_ENTITY_CHECKING, false);
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
    public void testSetTrueValid() {
        try {
            fValidator.setFeature(UNPARSED_ENTITY_CHECKING, true);
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
    public void testDefaultInvalid() {
        ((PSVIElementNSImpl) fRootNode).setAttributeNS(null,
                "unparsedEntityAttr", "invalid");
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        checkInvalid();
    }
    public void testSetFalseInvalid() {
        ((PSVIElementNSImpl) fRootNode).setAttributeNS(null,
                "unparsedEntityAttr", "invalid");
        try {
            fValidator.setFeature(UNPARSED_ENTITY_CHECKING, false);
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
    public void testSetTrueInvalid() {
        ((PSVIElementNSImpl) fRootNode).setAttributeNS(null,
                "unparsedEntityAttr", "invalid");
        try {
            fValidator.setFeature(UNPARSED_ENTITY_CHECKING, true);
        } catch (SAXException e) {
            Assert.fail("Error setting feature.");
        }
        try {
            validateDocument();
        } catch (Exception e) {
            Assert.fail("Validation failed: " + e.getMessage());
        }
        checkInvalid();
    }
    private void checkDefault() {
        assertNoError(UNDECLARED_ENTITY);
        assertValidity(ItemPSVI.VALIDITY_VALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementName("A", fRootNode.getElementDeclaration().getName());
        assertTypeName("X", fRootNode.getTypeDefinition().getName());
    }
    private void checkInvalid() {
        assertError(UNDECLARED_ENTITY);
        assertValidity(ItemPSVI.VALIDITY_INVALID, fRootNode.getValidity());
        assertValidationAttempted(ItemPSVI.VALIDATION_FULL, fRootNode
                .getValidationAttempted());
        assertElementName("A", fRootNode.getElementDeclaration().getName());
        assertTypeName("X", fRootNode.getTypeDefinition().getName());
    }
}
