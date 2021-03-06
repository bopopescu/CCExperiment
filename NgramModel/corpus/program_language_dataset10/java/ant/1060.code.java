package org.apache.tools.ant.taskdefs.optional;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;
public class XmlValidateTest extends BuildFileTest {
    private final static String TASKDEFS_DIR =
        "src/etc/testcases/taskdefs/optional/";
    public XmlValidateTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject(TASKDEFS_DIR + "xmlvalidate.xml");
    }
    public void tearDown() {}
    public void testValidate() throws Exception {
        executeTarget("testValidate");
    }
    public void testDeepValidate() throws Exception {
        executeTarget("testDeepValidate");
    }
    public void testXmlCatalog() {
        executeTarget("xmlcatalog");
    }
    public void testXmlCatalogViaRefid() {
        executeTarget("xmlcatalogViaRefid");
    }
    public void testXmlCatalogFiles() {
        executeTarget("xmlcatalogfiles-override");
    }
    public void testXmlCatalogPath() {
        executeTarget("xmlcatalogpath-override");
    }
    public void testXmlCatalogNested() {
        executeTarget("xmlcatalognested");
    }
    public void testXmlSchemaGood() throws BuildException {
        try {
            executeTarget("testSchemaGood");
        } catch (BuildException e) {
            if (e
                .getMessage()
                .endsWith(" doesn't recognize feature http://apache.org/xml/features/validation/schema")
                || e.getMessage().endsWith(
                    " doesn't support feature http://apache.org/xml/features/validation/schema")) {
                System.err.println(" skipped, parser doesn't support schema");
            } else {
                throw e;
            }
        }
    }
    public void testXmlSchemaBad() {
        try {
            executeTarget("testSchemaBad");
            fail("Should throw BuildException because 'Bad Schema Validation'");
            expectBuildExceptionContaining(
                "testSchemaBad",
                "Bad Schema Validation",
                "not a valid XML document");
        } catch (BuildException e) {
            if (e
                .getMessage()
                .endsWith(" doesn't recognize feature http://apache.org/xml/features/validation/schema")
                || e.getMessage().endsWith(
                    " doesn't support feature http://apache.org/xml/features/validation/schema")) {
                System.err.println(" skipped, parser doesn't support schema");
            } else {
                assertTrue(
                    e.getMessage().indexOf("not a valid XML document") > -1);
            }
        }
    }
    public void testIso2022Jp() {
        executeTarget("testIso2022Jp");
    }
    public void testUtf8() {
        expectBuildException("testUtf8", "invalid characters in file");
    }
    public void testPropertySchemaForValidXML() {
        executeTarget("testProperty.validXML");
    }
    public void testPropertySchemaForInvalidXML() {
        expectBuildException(
            "testProperty.invalidXML",
            "XML file does not satisfy schema.");
    }
}
