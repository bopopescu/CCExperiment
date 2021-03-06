package org.apache.batik.dom.util;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
public class XLinkSupport implements XMLConstants {
    public static String getXLinkType(Element elt) {
        return elt.getAttributeNS(XLINK_NAMESPACE_URI, "type");
    }
    public static void setXLinkType(Element elt, String str) {
        if (!"simple".equals(str)   &&
            !"extended".equals(str) &&
            !"locator".equals(str)  &&
            !"arc".equals(str)) {
            throw new DOMException(DOMException.SYNTAX_ERR,
                                   "xlink:type='" + str + "'");
        }
        elt.setAttributeNS(XLINK_NAMESPACE_URI, "type", str);
    }
    public static String getXLinkRole(Element elt) {
        return elt.getAttributeNS(XLINK_NAMESPACE_URI, "role");
    }
    public static void setXLinkRole(Element elt, String str) {
        elt.setAttributeNS(XLINK_NAMESPACE_URI, "role", str);
    }
    public static String getXLinkArcRole(Element elt) {
        return elt.getAttributeNS(XLINK_NAMESPACE_URI, "arcrole");
    }
    public static void setXLinkArcRole(Element elt, String str) {
        elt.setAttributeNS(XLINK_NAMESPACE_URI, "arcrole", str);
    }
    public static String getXLinkTitle(Element elt) {
        return elt.getAttributeNS(XLINK_NAMESPACE_URI, "title");
    }
    public static void setXLinkTitle(Element elt, String str) {
        elt.setAttributeNS(XLINK_NAMESPACE_URI, "title", str);
    }
    public static String getXLinkShow(Element elt) {
        return elt.getAttributeNS(XLINK_NAMESPACE_URI, "show");
    }
    public static void setXLinkShow(Element elt, String str) {
        if (!"new".equals(str)   &&
            !"replace".equals(str)  &&
            !"embed".equals(str)) {
            throw new DOMException(DOMException.SYNTAX_ERR,
                                   "xlink:show='" + str + "'");
        }
        elt.setAttributeNS(XLINK_NAMESPACE_URI, "show", str);
    }
    public static String getXLinkActuate(Element elt) {
        return elt.getAttributeNS(XLINK_NAMESPACE_URI, "actuate");
    }
    public static void setXLinkActuate(Element elt, String str) {
        if (!"onReplace".equals(str) && !"onLoad".equals(str)) {
            throw new DOMException(DOMException.SYNTAX_ERR,
                                   "xlink:actuate='" + str + "'");
        }
        elt.setAttributeNS(XLINK_NAMESPACE_URI, "actuate", str);
    }
    public static String getXLinkHref(Element elt) {
        return elt.getAttributeNS(XLINK_NAMESPACE_URI, XLINK_HREF_ATTRIBUTE);
    }
    public static void setXLinkHref(Element elt, String str) {
        elt.setAttributeNS(XLINK_NAMESPACE_URI, XLINK_HREF_ATTRIBUTE, str);
    }
}
