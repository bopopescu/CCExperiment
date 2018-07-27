package org.apache.batik.bridge;
public class SVGDescElementBridge extends SVGDescriptiveElementBridge {
    public SVGDescElementBridge() {}
    public String getLocalName() {
        return SVG_DESC_TAG;
    }
    public Bridge getInstance() { return new SVGDescElementBridge(); }
}
