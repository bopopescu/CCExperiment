package org.apache.batik.test.svg;
public class SamplesRenderingTest extends PreconfiguredRenderingTest {
    public static final String SVG_URL_PREFIX
        = "";
    public static final String REF_IMAGE_PREFIX
        = "test-references/";
    public static final String REF_IMAGE_SUFFIX
        = "";
    public static final String VARIATION_PREFIX
        = "test-references/";
    public static final String VARIATION_SUFFIX
        = "accepted-variation/";
    public static final String SAVE_VARIATION_PREFIX
        = "test-references/";
    public static final String SAVE_VARIATION_SUFFIX
        = "candidate-variation/";
    public static final String SAVE_CANDIDATE_REFERENCE_PREFIX
        = "test-references/";
    public static final String SAVE_CANDIDATE_REFERENCE_SUFFIX
        = "candidate-reference/";
    public SamplesRenderingTest(){
        setValidating( Boolean.TRUE );
    }
    protected String getSVGURLPrefix(){
        return SVG_URL_PREFIX;
    }
    protected String getRefImagePrefix(){
        return REF_IMAGE_PREFIX;
    }
    protected String getRefImageSuffix(){
        return REF_IMAGE_SUFFIX;
    }
    protected String getVariationPrefix(){
        return VARIATION_PREFIX;
    }
    protected String getVariationSuffix(){
        return VARIATION_SUFFIX;
    }
    protected String[] getVariationPlatforms() {
        return DEFAULT_VARIATION_PLATFORMS;
    }
    protected String getSaveVariationPrefix(){
        return SAVE_VARIATION_PREFIX;
    }
    protected String getSaveVariationSuffix(){
        return SAVE_VARIATION_SUFFIX;
    }
    protected String getCandidateReferencePrefix(){
        return SAVE_CANDIDATE_REFERENCE_PREFIX;
    }
    protected String getCandidateReferenceSuffix(){
        return SAVE_CANDIDATE_REFERENCE_SUFFIX;
    }
}
