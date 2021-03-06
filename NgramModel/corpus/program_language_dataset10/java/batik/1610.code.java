package org.apache.batik.test.svg;
import org.apache.batik.transcoder.image.ImageTranscoder;
import org.apache.batik.transcoder.image.PNGTranscoder;
public class SVGMediaRenderingAccuracyTest 
    extends ParametrizedRenderingAccuracyTest {
    public ImageTranscoder getTestImageTranscoder(){
        ImageTranscoder t = super.getTestImageTranscoder();
        t.addTranscodingHint(PNGTranscoder.KEY_MEDIA, parameter);
        return t;
    }
}
