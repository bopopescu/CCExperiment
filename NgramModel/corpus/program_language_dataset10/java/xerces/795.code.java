package schema.annotations;
import org.apache.xerces.xs.XSAnnotation;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSModelGroupDefinition;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
public class XSModelGroupAnnotationsTest extends TestCase {
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
                        Boolean.TRUE);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSModelGroupTest01.xsd"));
        XSModelGroupDefinition mgd = model.getModelGroupDefinition("group1",
                "XSModelGroup");
        XSModelGroup group = mgd.getModelGroup();
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
                .loadURI(getResourceURL("XSModelGroupTest01.xsd"));
        XSModelGroupDefinition mgd = model.getModelGroupDefinition("group2",
                "XSModelGroup");
        XSModelGroup group = mgd.getModelGroup();
        XSAnnotation annotation = group.getAnnotation();
        assertNull("TEST2_NO_ANNOTATION", annotation);
        XSObjectList annotations = group.getAnnotations();
        assertEquals("TEST2_NO_ANNOTATIONS", 0, annotations.getLength());
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        Boolean.TRUE);
        model = fSchemaLoader
                .loadURI(getResourceURL("XSModelGroupTest01.xsd"));
        mgd = model.getModelGroupDefinition("group2", "XSModelGroup");
        group = mgd.getModelGroup();
        annotation = group.getAnnotation();
        assertNotNull("TEST2_SYNTH_ANNOTATION", annotation);
        annotations = group.getAnnotations();
        assertEquals("TEST2_SYNTH_ANNOTATIONS", 1, annotations.getLength());
    }
    private void group3AnnotationTest(Boolean synth) {
        String expected = trim("<annotation id=\"ANNOT1\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" xmlns:sv=\"XSModelGroup\" "
                + "xmlns:sn=\"SyntheticAnnotation\" >" + "</annotation>");
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        synth);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSModelGroupTest01.xsd"));
        XSModelGroupDefinition mgd = model.getModelGroupDefinition("group3",
                "XSModelGroup");
        XSModelGroup groupChoice = mgd.getModelGroup();
        XSParticle choiceparticle = (XSParticle) groupChoice.getParticles()
                .item(0);
        XSModelGroup group = (XSModelGroup) choiceparticle.getTerm();
        XSAnnotation annotation = group.getAnnotation();
        assertEquals("TEST3_NO_ANNOTATION_" + synth, expected, trim(annotation
                .getAnnotationString()));
        XSObjectList annotations = group.getAnnotations();
        assertEquals("TEST3_NO_ANNOTATIONS_" + synth, 1, annotations
                .getLength());
        assertEquals(
                "TEST3_NO_ANNOTATIONS_" + synth,
                expected,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
    }
    public void testGroup3Annotation() {
        group3AnnotationTest(Boolean.FALSE);
        group3AnnotationTest(Boolean.TRUE);
    }
    private void group4AnnotationTest(Boolean synth) {
        String expected = trim("<annotation sn:att=\"SA1\"  id=\"ANNOT2\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSModelGroup\" xmlns:sn=\"SyntheticAnnotation\" >"
                + "</annotation>");
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        synth);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSModelGroupTest01.xsd"));
        XSModelGroupDefinition mgd = model.getModelGroupDefinition("group4",
                "XSModelGroup");
        XSModelGroup group = mgd.getModelGroup();
        XSAnnotation annotation = group.getAnnotation();
        assertEquals("TEST4_NO_ANNOTATION_" + synth, expected, trim(annotation
                .getAnnotationString()));
        XSObjectList annotations = group.getAnnotations();
        assertEquals("TEST4_NO_ANNOTATIONS_" + synth, 1, annotations
                .getLength());
        assertEquals(
                "TEST4_NO_ANNOTATIONS_" + synth,
                expected,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
    }
    public void testGroup4Annotation() {
        group4AnnotationTest(Boolean.FALSE);
        group4AnnotationTest(Boolean.TRUE);
    }
    private void group5AnnotationTest(Boolean synth) {
        String expected1 = trim("<annotation sn:att=\"SA2\"  id=\"ANNOT3\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSModelGroup\" xmlns:sn=\"SyntheticAnnotation\" >"
                + "</annotation>");
        String expected2 = trim("<annotation sn:att=\"SA2\"  id=\"ANNOT4\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSModelGroup\" xmlns:sn=\"SyntheticAnnotation\" >"
                + "</annotation>");
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        synth);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSModelGroupTest01.xsd"));
        XSModelGroupDefinition mgd = model.getModelGroupDefinition("group5",
                "XSModelGroup");
        XSModelGroup group = mgd.getModelGroup();
        XSAnnotation annotation = group.getAnnotation();
        assertEquals("TEST5.1_NO_ANNOTATION_" + synth, expected1,
                trim(annotation.getAnnotationString()));
        XSObjectList annotations = group.getAnnotations();
        assertEquals("TEST5.1_NO_ANNOTATIONS_" + synth, 1, annotations
                .getLength());
        assertEquals(
                "TEST5.1_NO_ANNOTATIONS_" + synth,
                expected1,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
        group = (XSModelGroup) ((XSParticle) group.getParticles().item(0))
                .getTerm();
        annotation = group.getAnnotation();
        assertEquals("TEST5.2_NO_ANNOTATION_" + synth, expected2,
                trim(annotation.getAnnotationString()));
        annotations = group.getAnnotations();
        assertEquals("TEST5.2_NO_ANNOTATIONS_" + synth, 1, annotations
                .getLength());
        assertEquals(
                "TEST5.2_NO_ANNOTATIONS_" + synth,
                expected2,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
    }
    public void testGroup5Annotation() {
        group5AnnotationTest(Boolean.FALSE);
        group5AnnotationTest(Boolean.TRUE);
    }
    private void group6AnnotationTest(Boolean synth) {
        String expected1 = trim("<annotation sn:att=\"SA2\"  id=\"ANNOT5\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSModelGroup\" xmlns:sn=\"SyntheticAnnotation\" >"
                + "</annotation>");
        String expected2 = trim("<annotation sn:att=\"SA3\"  id=\"ANNOT6\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSModelGroup\" xmlns:sn=\"SyntheticAnnotation\" >"
                + "</annotation>");
        String expected3 = trim("<annotation id=\"ANNOT7\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSModelGroup\" xmlns:sn=\"SyntheticAnnotation\" >"
                + "</annotation>");
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        synth);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSModelGroupTest01.xsd"));
        XSModelGroupDefinition mgd = model.getModelGroupDefinition("group6",
                "XSModelGroup");
        XSModelGroup group = mgd.getModelGroup();
        XSAnnotation annotation = group.getAnnotation();
        assertEquals("TEST6.1_NO_ANNOTATION_" + synth, expected1,
                trim(annotation.getAnnotationString()));
        XSObjectList annotations = group.getAnnotations();
        assertEquals("TEST6.1_NO_ANNOTATIONS_" + synth, 1, annotations
                .getLength());
        assertEquals(
                "TEST6.1_NO_ANNOTATIONS_" + synth,
                expected1,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
        group = (XSModelGroup) ((XSParticle) group.getParticles().item(0))
                .getTerm();
        annotation = group.getAnnotation();
        assertEquals("TEST6.2_NO_ANNOTATION_" + synth, expected2,
                trim(annotation.getAnnotationString()));
        annotations = group.getAnnotations();
        assertEquals("TEST6.2_NO_ANNOTATIONS_" + synth, 1, annotations
                .getLength());
        assertEquals(
                "TEST6.2_NO_ANNOTATIONS_" + synth,
                expected2,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
        group = (XSModelGroup) ((XSParticle) group.getParticles().item(0))
                .getTerm();
        annotation = group.getAnnotation();
        assertEquals("TEST6.3_NO_ANNOTATION_" + synth, expected3,
                trim(annotation.getAnnotationString()));
        annotations = group.getAnnotations();
        assertEquals("TEST6.3_NO_ANNOTATIONS_" + synth, 1, annotations
                .getLength());
        assertEquals(
                "TEST6.3_NO_ANNOTATIONS_" + synth,
                expected3,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
    }
    public void testGroup6Annotation() {
        group6AnnotationTest(Boolean.FALSE);
        group6AnnotationTest(Boolean.TRUE);
    }
    private void group7AnnotationTest(Boolean synth) {
        String expected1 = trim("<annotation sn:att=\"SA1\"  id=\"ANNOT8\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSModelGroup\" xmlns:sn=\"SyntheticAnnotation\" >"
                + "</annotation>");
        String expected2 = trim("<annotation sn:att=\"SA1\"  "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSModelGroup\" xmlns:sn=\"SyntheticAnnotation\" >"
                + "<documentation>SYNTHETIC_ANNOTATION</documentation></annotation>");
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        synth);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSModelGroupTest01.xsd"));
        XSModelGroupDefinition mgd = model.getModelGroupDefinition("group7",
                "XSModelGroup");
        XSModelGroup group = mgd.getModelGroup();
        XSAnnotation annotation = group.getAnnotation();
        assertEquals("TEST7.1_ANNOTATION_" + synth, expected1,
                trim(annotation.getAnnotationString()));
        XSObjectList annotations = group.getAnnotations();
        assertEquals("TEST7.1_ANNOTATIONS_" + synth, 1, annotations
                .getLength());
        assertEquals(
                "TEST7.1_ANNOTATIONS_" + synth,
                expected1,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
        if (synth == Boolean.TRUE) {
            group = (XSModelGroup) ((XSParticle) group.getParticles().item(0))
                    .getTerm();
            annotation = group.getAnnotation();
            assertEquals("TEST7.2_ANNOTATION_" + synth, expected2,
                    trim(annotation.getAnnotationString()));
            annotations = group.getAnnotations();
            assertEquals("TEST7.2_ANNOTATIONS_" + synth, 1, annotations
                    .getLength());
            assertEquals("TEST7.2_ANNOTATIONS_" + synth, expected2,
                    trim(((XSAnnotation) annotations.item(0))
                            .getAnnotationString()));
        }
    }
    public void testGroup7Annotation() {
        group7AnnotationTest(Boolean.FALSE);
        group7AnnotationTest(Boolean.TRUE);
    }
    private void group8AnnotationTest(Boolean synth) {
        String expected = trim("<annotation sn:att=\"SA1\"  id=\"ANNOT2\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSModelGroup\" xmlns:sn=\"SyntheticAnnotation\" >"
                + "</annotation>");
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        synth);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSModelGroupTest01.xsd"));
        XSComplexTypeDefinition ct = (XSComplexTypeDefinition) model
                .getTypeDefinition("CT1", "XSModelGroup");
        XSParticle particle = ct.getParticle();
        XSModelGroup group = (XSModelGroup) particle.getTerm();
        XSAnnotation annotation = group.getAnnotation();
        assertEquals("TEST8_NO_ANNOTATION_" + synth, expected, trim(annotation
                .getAnnotationString()));
        XSObjectList annotations = group.getAnnotations();
        assertEquals("TEST8_NO_ANNOTATIONS_" + synth, 1, annotations
                .getLength());
        assertEquals(
                "TEST8_NO_ANNOTATIONS_" + synth,
                expected,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
    }
    public void testGroup8Annotation() {
        group8AnnotationTest(Boolean.FALSE);
        group8AnnotationTest(Boolean.TRUE);
    }
    private void group9AnnotationTest(Boolean synth) {
        String expected = trim("<annotation sn:att=\"SA2\"  id=\"ANNOT12\" "
                + "xmlns=\"http://www.w3.org/2001/XMLSchema\" "
                + "xmlns:sv=\"XSModelGroup\" xmlns:sn=\"SyntheticAnnotation\" >"
                + "</annotation>");
        fConfig
                .setParameter(
                        "http://apache.org/xml/features/generate-synthetic-annotations",
                        synth);
        XSModel model = fSchemaLoader
                .loadURI(getResourceURL("XSModelGroupTest01.xsd"));
        XSComplexTypeDefinition ct = (XSComplexTypeDefinition) model
                .getTypeDefinition("CT3", "XSModelGroup");
        XSParticle particle = ct.getParticle();
        XSModelGroup group = (XSModelGroup) particle.getTerm();
        XSAnnotation annotation = group.getAnnotation();
        assertEquals("TEST9_NO_ANNOTATION_" + synth, expected, trim(annotation
                .getAnnotationString()));
        XSObjectList annotations = group.getAnnotations();
        assertEquals("TEST9_NO_ANNOTATIONS_" + synth, 1, annotations
                .getLength());
        assertEquals(
                "TEST9_NO_ANNOTATIONS_" + synth,
                expected,
                trim(((XSAnnotation) annotations.item(0)).getAnnotationString()));
    }
    public void testGroup9Annotation() {
        group9AnnotationTest(Boolean.FALSE);
        group9AnnotationTest(Boolean.TRUE);
    }
    public static void main(String args[]) {
        junit.textui.TestRunner.run(XSModelGroupAnnotationsTest.class);
    }
}
