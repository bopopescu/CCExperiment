package org.apache.batik.transcoder.image;
import java.util.Map;
import java.util.HashMap;
import org.apache.batik.transcoder.TranscoderInput;
public class MaxDimensionTest extends AbstractImageTranscoderTest {
    protected String inputURI;
    protected String refImageURI;
    protected Float maxWidth = new Float(Float.NaN);
    protected Float maxHeight = new Float(Float.NaN);
    protected Float width = new Float(Float.NaN);
    protected Float height = new Float(Float.NaN);
    public MaxDimensionTest(String inputURI, String refImageURI, Float maxWidth, Float maxHeight) {
        this.inputURI = inputURI;
        this.refImageURI = refImageURI;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }
    public MaxDimensionTest(String inputURI, String refImageURI, Float maxWidth, Float maxHeight, Float width, Float height) {
        this.inputURI = inputURI;
        this.refImageURI = refImageURI;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
        this.width = width;
        this.height = height;
    }
    protected TranscoderInput createTranscoderInput() {
        return new TranscoderInput(resolveURL(inputURI).toString());
    }
    protected Map createTranscodingHints() {
        Map hints = new HashMap(7);
        if (!width.isNaN() && width.floatValue() > 0) {
            hints.put(ImageTranscoder.KEY_WIDTH, width);
        }
        if (!height.isNaN() && height.floatValue() > 0) {
            hints.put(ImageTranscoder.KEY_HEIGHT, height);
        }
        if (maxWidth.floatValue() > 0) {
            hints.put(ImageTranscoder.KEY_MAX_WIDTH, maxWidth);
        }
        if (maxHeight.floatValue() > 0) {
            hints.put(ImageTranscoder.KEY_MAX_HEIGHT, maxHeight);
        }
        return hints;
    }
    protected byte [] getReferenceImageData() {
        return createBufferedImageData(resolveURL(refImageURI));
    }
}
