package org.apache.batik.bridge;
public class SVGTitleElementBridge extends SVGDescriptiveElementBridge {
    public SVGTitleElementBridge() {}
    public String getLocalName() {
        return SVG_TITLE_TAG;
    }
    public Bridge getInstance() { return new SVGTitleElementBridge(); }
}
