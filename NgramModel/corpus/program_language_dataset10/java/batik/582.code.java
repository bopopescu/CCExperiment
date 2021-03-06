package org.apache.batik.dom.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Node;
import org.w3c.dom.svg.SVGAnimatedNumber;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGFEGaussianBlurElement;
public class SVGOMFEGaussianBlurElement
    extends    SVGOMFilterPrimitiveStandardAttributes
    implements SVGFEGaussianBlurElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMFilterPrimitiveStandardAttributes.xmlTraitInformation);
        t.put(null, SVG_IN_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_CDATA));
        t.put(null, SVG_STD_DEVIATION_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_NUMBER_OPTIONAL_NUMBER));
        xmlTraitInformation = t;
    }
    protected SVGOMAnimatedString in;
    protected SVGOMFEGaussianBlurElement() {
    }
    public SVGOMFEGaussianBlurElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        in = createLiveAnimatedString(null, SVG_IN_ATTRIBUTE);
    }
    public String getLocalName() {
        return SVG_FE_GAUSSIAN_BLUR_TAG;
    }
    public SVGAnimatedString getIn1() {
        return in;
    }
    public SVGAnimatedNumber getStdDeviationX() {
        throw new UnsupportedOperationException
            ("SVGFEGaussianBlurElement.getStdDeviationX is not implemented"); 
    }
    public SVGAnimatedNumber getStdDeviationY() {
        throw new UnsupportedOperationException
            ("SVGFEGaussianBlurElement.getStdDeviationY is not implemented"); 
    }
    public void setStdDeviation(float devX, float devY) {
        setAttributeNS(null, SVG_STD_DEVIATION_ATTRIBUTE,
                       Float.toString(devX) + " " + Float.toString(devY));
    }
    protected Node newNode() {
        return new SVGOMFEGaussianBlurElement();
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
}
