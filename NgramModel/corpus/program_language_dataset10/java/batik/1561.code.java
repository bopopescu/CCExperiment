package org.apache.batik.svggen;
import java.io.File;
import java.net.URL;
import org.apache.batik.test.Test;
import org.apache.batik.test.DefaultTestSuite;
import org.apache.batik.test.svg.SVGRenderingAccuracyTest;
import org.apache.batik.test.util.ImageCompareTest;
public class SVGGeneratorTests extends DefaultTestSuite {
    public static final String GENERATOR_REFERENCE_BASE
        = "test-references/org/apache/batik/svggen/";
    public static final String RENDERING_DIR
        = "rendering";
    public static final String ACCEPTED_VARIATION_DIR
        = "accepted-variation";
    public static final String[] VARIATION_PLATFORMS = 
        org.apache.batik.test.svg.PreconfiguredRenderingTest.DEFAULT_VARIATION_PLATFORMS;
    public static final String CANDIDATE_VARIATION_DIR
        = "candidate-variation";
    public static final String CANDIDATE_REF_DIR
        = "candidate-ref";
    public static final String RENDERING_CANDIDATE_REF_DIR
        = "candidate-reference";
    public static final String PNG_EXTENSION
        = ".png";
    public static final String SVG_EXTENSION
        = ".svg";
    public static final String PLAIN_GENERATION_PREFIX = "";
    public static final String CUSTOM_CONTEXT_GENERATION_PREFIX = "Context";
    public SVGGeneratorTests(){
    }
    public void setId(String id){
        super.setId(id);
        String clName = getPackageName() + "." + id;
        Class cl = null;
        try{
            cl = Class.forName(clName);
        }catch(ClassNotFoundException e){
            throw new IllegalArgumentException(clName);
        }
        Object o = null;
        try {
            o = cl.newInstance();
        }catch(Exception e){
            throw new IllegalArgumentException(clName);
        }
        if(!(o instanceof Painter)){
            throw new IllegalArgumentException(clName);
        }
        Painter painter = (Painter)o;
        addTest(makeSVGAccuracyTest(painter, id));
        addTest(makeGeneratorContext(painter, id));
        addTest(makeSVGRenderingAccuracyTest(painter, id, PLAIN_GENERATION_PREFIX));
        addTest(makeSVGRenderingAccuracyTest(painter, id, CUSTOM_CONTEXT_GENERATION_PREFIX));
        addTest(makeImageCompareTest(painter, id, PLAIN_GENERATION_PREFIX,
                                     CUSTOM_CONTEXT_GENERATION_PREFIX));
    }
    public String getName(){
        return "SVGGeneratorTest - " + getId();
    }
    protected String getPackageName(){
        return "org.apache.batik.svggen";
    }
    private Test makeImageCompareTest(Painter painter,
                                      String id,
                                      String prefixA,
                                      String prefixB){
        String cl = getNonQualifiedClassName(painter);
        String clA = prefixA + cl;
        String clB = prefixB + cl;
        String testReferenceA = GENERATOR_REFERENCE_BASE + RENDERING_DIR + "/" + clA + PNG_EXTENSION;
        String testReferenceB = GENERATOR_REFERENCE_BASE + RENDERING_DIR + "/" + clB + PNG_EXTENSION;
        ImageCompareTest t = new ImageCompareTest(testReferenceA, testReferenceB);
        t.setName(id + "-RenderingComparison");
        t.setId(id + ".renderingComparison");
        return t;
    }
    private Test makeSVGRenderingAccuracyTest(Painter painter, String id, String prefix){
        String cl = prefix + getNonQualifiedClassName(painter);
        String testSource = GENERATOR_REFERENCE_BASE + cl + SVG_EXTENSION;
        String testReference = GENERATOR_REFERENCE_BASE + RENDERING_DIR + "/" + cl + PNG_EXTENSION;
        String[] variationURLs = new String[VARIATION_PLATFORMS.length + 1];
        variationURLs[0] = GENERATOR_REFERENCE_BASE + RENDERING_DIR + "/" + ACCEPTED_VARIATION_DIR + "/" + cl + PNG_EXTENSION;
        for (int i = 0; i < VARIATION_PLATFORMS.length; i++) {
            variationURLs[i + 1] = GENERATOR_REFERENCE_BASE + RENDERING_DIR + "/" + ACCEPTED_VARIATION_DIR + "/" + cl + '_' + VARIATION_PLATFORMS[i] + PNG_EXTENSION;
        }
        String saveVariation = GENERATOR_REFERENCE_BASE + RENDERING_DIR + "/" + CANDIDATE_VARIATION_DIR + "/" + cl + PNG_EXTENSION;
        String candidateReference = GENERATOR_REFERENCE_BASE + RENDERING_DIR + "/" + RENDERING_CANDIDATE_REF_DIR + "/" + cl + PNG_EXTENSION;
        SVGRenderingAccuracyTest test = new SVGRenderingAccuracyTest(testSource, testReference);
        for (int i = 0; i < variationURLs.length; i++) {
            test.addVariationURL(variationURLs[i]);
        }
        test.setSaveVariation(new File(saveVariation));
        test.setCandidateReference(new File(candidateReference));
        test.setName(id + "-" + prefix + "RenderingCheck");
        test.setId(id + "." + prefix + "renderingCheck");
        return test;
    }
    private Test makeGeneratorContext(Painter painter, String id){
        String cl = CUSTOM_CONTEXT_GENERATION_PREFIX + getNonQualifiedClassName(painter);
        GeneratorContext test
            = new GeneratorContext(painter, makeURL(painter, CUSTOM_CONTEXT_GENERATION_PREFIX));
        test.setSaveSVG(new File(GENERATOR_REFERENCE_BASE + CANDIDATE_REF_DIR + "/" + cl + SVG_EXTENSION));
        test.setName(id + "-ConfiguredContextGeneration");
        test.setId(id + ".configuredContextGeneration");
        return test;
    }
    private Test makeSVGAccuracyTest(Painter painter, String id){
        String cl = PLAIN_GENERATION_PREFIX + getNonQualifiedClassName(painter);
        SVGAccuracyTest test
            = new SVGAccuracyTest(painter, makeURL(painter, PLAIN_GENERATION_PREFIX));
        test.setSaveSVG(new File(GENERATOR_REFERENCE_BASE + CANDIDATE_REF_DIR + "/" + cl + SVG_EXTENSION));
        test.setName(id + "-DefaultContextGeneration");
        test.setId(id + ".defaultContextGeneration");
        return test;
    }
    private String getNonQualifiedClassName(Painter painter){
        String cl = painter.getClass().getName();
        int n = cl.lastIndexOf('.');
        return cl.substring(n+1);
    }
    private URL makeURL(Painter painter, String prefix){
        String urlString = "file:" + GENERATOR_REFERENCE_BASE
            + prefix + getNonQualifiedClassName(painter) + SVG_EXTENSION;
        URL url = null;
        try{
            url = new URL(urlString);
        }catch(Exception e){
            throw new Error( e.getMessage() ); 
        }
        return url;
    }
}
