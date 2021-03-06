package org.apache.batik.extension.svg;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.dom.DomExtension;
import org.apache.batik.dom.ExtensibleDOMImplementation;
import org.apache.batik.dom.svg.SVGDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class BatikDomExtension
    implements DomExtension, BatikExtConstants {
    public float getPriority() { return 1.0f; }
    public String getAuthor() {
        return "Thomas DeWeese";
    }
    public String getContactAddress() {
        return "deweese@apache.org";
    }
    public String getURL() {
        return "http://xml.apache.org/batik";
    }
    public String getDescription() {
        return "Example extension to standard SVG shape tags";
    }
    public void registerTags(ExtensibleDOMImplementation di) {
        di.registerCustomElementFactory
            (BATIK_EXT_NAMESPACE_URI,
             BATIK_EXT_REGULAR_POLYGON_TAG,
             new BatikRegularPolygonElementFactory());
        di.registerCustomElementFactory
            (BATIK_EXT_NAMESPACE_URI,
             BATIK_EXT_STAR_TAG,
             new BatikStarElementFactory());
        di.registerCustomElementFactory
            (BATIK_EXT_NAMESPACE_URI,
             BATIK_EXT_HISTOGRAM_NORMALIZATION_TAG,
             new BatikHistogramNormalizationElementFactory());
        di.registerCustomElementFactory
            (BATIK_EXT_NAMESPACE_URI,
             BATIK_EXT_COLOR_SWITCH_TAG,
             new ColorSwitchElementFactory());
        di.registerCustomElementFactory
            (BATIK_12_NAMESPACE_URI,
             BATIK_EXT_FLOW_TEXT_TAG,
             new FlowTextElementFactory());
        di.registerCustomElementFactory
            (BATIK_12_NAMESPACE_URI,
             BATIK_EXT_FLOW_DIV_TAG,
             new FlowDivElementFactory());
        di.registerCustomElementFactory
            (BATIK_12_NAMESPACE_URI,
             BATIK_EXT_FLOW_PARA_TAG,
             new FlowParaElementFactory());
        di.registerCustomElementFactory
            (BATIK_12_NAMESPACE_URI,
             BATIK_EXT_FLOW_REGION_BREAK_TAG,
             new FlowRegionBreakElementFactory());
        di.registerCustomElementFactory
            (BATIK_12_NAMESPACE_URI,
             BATIK_EXT_FLOW_REGION_TAG,
             new FlowRegionElementFactory());
        di.registerCustomElementFactory
            (BATIK_12_NAMESPACE_URI,
             BATIK_EXT_FLOW_LINE_TAG,
             new FlowLineElementFactory());
        di.registerCustomElementFactory
            (BATIK_12_NAMESPACE_URI,
             BATIK_EXT_FLOW_SPAN_TAG,
             new FlowSpanElementFactory());
    }
    protected static class BatikRegularPolygonElementFactory
        implements ExtensibleDOMImplementation.ElementFactory {
        public BatikRegularPolygonElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new BatikRegularPolygonElement
                (prefix, (AbstractDocument)doc);
        }
    }
    protected static class BatikStarElementFactory
        implements ExtensibleDOMImplementation.ElementFactory {
        public BatikStarElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new BatikStarElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class BatikHistogramNormalizationElementFactory
        implements ExtensibleDOMImplementation.ElementFactory {
        public BatikHistogramNormalizationElementFactory() {}
        public Element create(String prefix, Document doc) {
            return new BatikHistogramNormalizationElement
                (prefix, (AbstractDocument)doc);
        }
    }
    protected static class ColorSwitchElementFactory
        implements ExtensibleDOMImplementation.ElementFactory {
        public ColorSwitchElementFactory() {
        }
        public Element create(String prefix, Document doc) {
            return new ColorSwitchElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FlowTextElementFactory
        implements SVGDOMImplementation.ElementFactory {
        public FlowTextElementFactory() {
        }
        public Element create(String prefix, Document doc) {
            return new FlowTextElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FlowDivElementFactory
        implements SVGDOMImplementation.ElementFactory {
        public FlowDivElementFactory() {
        }
        public Element create(String prefix, Document doc) {
            return new FlowDivElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FlowParaElementFactory
        implements SVGDOMImplementation.ElementFactory {
        public FlowParaElementFactory() {
        }
        public Element create(String prefix, Document doc) {
            return new FlowParaElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FlowRegionBreakElementFactory
        implements SVGDOMImplementation.ElementFactory {
        public FlowRegionBreakElementFactory() {
        }
        public Element create(String prefix, Document doc) {
            return new FlowRegionBreakElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FlowRegionElementFactory
        implements SVGDOMImplementation.ElementFactory {
        public FlowRegionElementFactory() {
        }
        public Element create(String prefix, Document doc) {
            return new FlowRegionElement(prefix, (AbstractDocument)doc);
        }
     }
    protected static class FlowLineElementFactory
        implements SVGDOMImplementation.ElementFactory {
        public FlowLineElementFactory() {
        }
        public Element create(String prefix, Document doc) {
            return new FlowLineElement(prefix, (AbstractDocument)doc);
        }
    }
    protected static class FlowSpanElementFactory
        implements SVGDOMImplementation.ElementFactory {
        public FlowSpanElementFactory() {
        }
        public Element create(String prefix, Document doc) {
            return new FlowSpanElement(prefix, (AbstractDocument)doc);
        }
    }
}
