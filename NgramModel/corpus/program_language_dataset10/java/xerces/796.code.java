package schema.annotations;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroupDefinition;
import org.apache.xerces.xs.XSObjectList;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
public class XSModelGroupDefinitionAnnotationsTest extends TestCase {
    private XSLoader fSchemaLoader;
    private DOMConfiguration fConfig;
    protected void setUp() {
        try {
            System.setProperty(DOMImplementationRegistry.PROPERTY,
                    "org.apache.xerces.dom.DOMXSImplementationSourceImpl");
            DOMImplementationRegistry registry = DOMImplementationRegistry
                    .newInstance();
            XSImplementation impl = (XSImplementation) registry
                    .getDOMImplementation("XS-Loader");
            fSchemaLoader = impl.createXSLoader(null);
            fConfig = fSchemaLoader.getConfig();
            fConfig.setParameter("validate", Boolean.TRUE);
        } catch (Exception e) {
            fail("Expecting a NullPointerException");
            System.err.println("SETUP FAILED: XSModelGroupDefinitionTest");
        }
    }
    protected void tearDown() {
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.FALSE);
    }
    public void testGroup1Annotation() {
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.FALSE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSModelGroupDefinitionTest01.xsd"));
        XSModelGroupDefinition group = model.getModelGroupDefinition("group1",
                "XSModelGroupDefn");
        XSAnnotation annotation = group.getAnnotation();
        assertNull("TEST1_NO_ANNOTATION", annotation);
        XSObjectList annotations = group.getAnnotations();
        assertEquals("TEST1_NO_ANNOTATIONS", 0, annotations.getLength());
    }
    public void testGroup2Annotation() {
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.FALSE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSModelGroupDefinitionTest01.xsd"));
        XSModelGroupDefinition group = model.getModelGroupDefinition("group2",
                "XSModelGroupDefn");
        XSAnnotation annotation = group.getAnnotation();
        assertNull("TEST2_NO_ANNOTATION", annotation);
        XSObjectList annotations = group.getAnnotations();
        assertEquals("TEST2_NO_ANNOTATIONS", 0, annotations.getLength());
    }
    public void testGroup2SynthAnnotation() {
        String expected = trim("<annotation sn:att=\"ANNOT1\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSModelGroupDefn\" xmlns:sn=\"SyntheticAnnotation\" >"
                + "<documentation>SYNTHETIC_ANNOTATION</documentation>"
                + "</annotation>");
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.TRUE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSModelGroupDefinitionTest01.xsd"));
        XSModelGroupDefinition group = model.getModelGroupDefinition("group2",
                "XSModelGroupDefn");
        XSAnnotation annotation = group.getAnnotation();
        assertEquals("TEST3_ANNOTATION", expected, trim(annotation
                .getAnnotationString()));
        XSObjectList annotations = group.getAnnotations();
        assertEquals(
                "TEST3_ANNOTATIONS",
                expected,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
    }
    public void testGroup3Annotation() {
        String expected = trim("<annotation id=\"ANNOT1\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" xmlns:sv=\"XSModelGroupDefn\" "
                + "xmlns:sn=\"SyntheticAnnotation\" >" + "</annotation>");
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.FALSE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSModelGroupDefinitionTest01.xsd"));
        XSModelGroupDefinition group = model.getModelGroupDefinition("group3",
                "XSModelGroupDefn");
        XSAnnotation annotation = group.getAnnotation();
        assertEquals("TEST3_ANNOTATION", expected, trim(annotation
                .getAnnotationString()));
        XSObjectList annotations = group.getAnnotations();
        assertEquals(
                "TEST3_ANNOTATIONS",
                expected,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
    }
    public void testGroup4Annotation() {
        String expected = trim("<annotation sn:att=\"ANNOT2\"  id=\"ANNOT2\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" xmlns:sv=\"XSModelGroupDefn\" "
                + "xmlns:sn=\"SyntheticAnnotation\" >" + "</annotation>");
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.TRUE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSModelGroupDefinitionTest01.xsd"));
        XSModelGroupDefinition group = model.getModelGroupDefinition("group4",
                "XSModelGroupDefn");
        XSAnnotation annotation = group.getAnnotation();
        assertEquals("TEST4_NO_SYNTH_ANNOTATION", expected, trim(annotation
                .getAnnotationString()));
        XSObjectList annotations = group.getAnnotations();
        assertEquals(
                "TEST4_NO_SYNTH_ANNOTATIONS",
                expected,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
    }
    public static void main(String args[]) {
        junit.textui.TestRunner
                .run(XSModelGroupDefinitionAnnotationsTest.class);
    }
}
