package org.apache.batik.bridge;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg.ICCColor;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.ext.awt.MultipleGradientPaint;
import org.apache.batik.ext.awt.image.renderable.ClipRable;
import org.apache.batik.ext.awt.image.renderable.Filter;
import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.filter.Mask;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Element;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
public abstract class CSSUtilities
    implements CSSConstants, ErrorConstants, XMLConstants {
    protected CSSUtilities() {}
    public static CSSEngine getCSSEngine(Element e) {
        return ((SVGOMDocument)e.getOwnerDocument()).getCSSEngine();
    }
    public static Value getComputedStyle(Element e, int property) {
        CSSEngine engine = getCSSEngine(e);
        if (engine == null) return null;
        return engine.getComputedStyle((CSSStylableElement)e,
                                       null, property);
    }
    public static int convertPointerEvents(Element e) {
        Value v = getComputedStyle(e, SVGCSSEngine.POINTER_EVENTS_INDEX);
        String s = v.getStringValue();
        switch(s.charAt(0)) {
        case 'v':
            if (s.length() == 7) {
                return GraphicsNode.VISIBLE;
            } else {
                switch(s.charAt(7)) {
                case 'p':
                    return GraphicsNode.VISIBLE_PAINTED;
                case 'f':
                    return GraphicsNode.VISIBLE_FILL;
                case 's':
                    return GraphicsNode.VISIBLE_STROKE;
                default:
                    throw new IllegalStateException("unexpected event, must be one of (p,f,s) is:" + s.charAt(7) );
                }
            }
        case 'p':
            return GraphicsNode.PAINTED;
        case 'f':
            return GraphicsNode.FILL;
        case 's':
            return GraphicsNode.STROKE;
        case 'a':
            return GraphicsNode.ALL;
        case 'n':
            return GraphicsNode.NONE;
        default:
            throw new IllegalStateException("unexpected event, must be one of (v,p,f,s,a,n) is:" + s.charAt(0) );
        }
    }
    public static Rectangle2D convertEnableBackground(Element e ) {
        Value v = getComputedStyle(e, SVGCSSEngine.ENABLE_BACKGROUND_INDEX);
        if (v.getCssValueType() != CSSValue.CSS_VALUE_LIST) {
            return null; 
        }
        ListValue lv = (ListValue)v;
        int length = lv.getLength();
        switch (length) {
        case 1:
            return CompositeGraphicsNode.VIEWPORT; 
        case 5: 
            float x = lv.item(1).getFloatValue();
            float y = lv.item(2).getFloatValue();
            float w = lv.item(3).getFloatValue();
            float h = lv.item(4).getFloatValue();
            return new Rectangle2D.Float(x, y, w, h);
        default:
            throw new IllegalStateException("Unexpected length:" + length ); 
        }
    }
    public static boolean convertColorInterpolationFilters(Element e) {
        Value v = getComputedStyle(e,
                             SVGCSSEngine.COLOR_INTERPOLATION_FILTERS_INDEX);
        return CSS_LINEARRGB_VALUE == v.getStringValue();
    }
    public static MultipleGradientPaint.ColorSpaceEnum
        convertColorInterpolation(Element e) {
        Value v = getComputedStyle(e, SVGCSSEngine.COLOR_INTERPOLATION_INDEX);
        return (CSS_LINEARRGB_VALUE == v.getStringValue())
            ? MultipleGradientPaint.LINEAR_RGB
            : MultipleGradientPaint.SRGB;
    }
    public static boolean isAutoCursor(Element e) {
        Value cursorValue =
            CSSUtilities.getComputedStyle(e,
                                          SVGCSSEngine.CURSOR_INDEX);
        boolean isAuto = false;
        if (cursorValue != null){
            if(
               cursorValue.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
               &&
               cursorValue.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
               &&
               cursorValue.getStringValue().charAt(0) == 'a'
               ) {
                isAuto = true;
            } else if (
                       cursorValue.getCssValueType() == CSSValue.CSS_VALUE_LIST
                       &&
                       cursorValue.getLength() == 1) {
                Value lValue = cursorValue.item(0);
                if (lValue != null
                    &&
                    lValue.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE
                    &&
                    lValue.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT
                    &&
                    lValue.getStringValue().charAt(0) == 'a') {
                    isAuto = true;
                }
            }
        }
        return isAuto;
    }
    public static Cursor
        convertCursor(Element e, BridgeContext ctx) {
        return ctx.getCursorManager().convertCursor(e);
    }
    public static RenderingHints convertShapeRendering(Element e,
                                                       RenderingHints hints) {
        Value  v = getComputedStyle(e, SVGCSSEngine.SHAPE_RENDERING_INDEX);
        String s = v.getStringValue();
        int    len = s.length();
        if ((len == 4) && (s.charAt(0) == 'a')) 
            return hints;
        if (len < 10) return hints;  
        if (hints == null)
            hints = new RenderingHints(null);
        switch(s.charAt(0)) {
        case 'o': 
            hints.put(RenderingHints.KEY_RENDERING,
                      RenderingHints.VALUE_RENDER_SPEED);
            hints.put(RenderingHints.KEY_ANTIALIASING,
                      RenderingHints.VALUE_ANTIALIAS_OFF);
            break;
        case 'c': 
            hints.put(RenderingHints.KEY_RENDERING,
                      RenderingHints.VALUE_RENDER_DEFAULT);
            hints.put(RenderingHints.KEY_ANTIALIASING,
                      RenderingHints.VALUE_ANTIALIAS_OFF);
            break;
        case 'g': 
            hints.put(RenderingHints.KEY_RENDERING,
                      RenderingHints.VALUE_RENDER_QUALITY);
            hints.put(RenderingHints.KEY_ANTIALIASING,
                      RenderingHints.VALUE_ANTIALIAS_ON);
            hints.put(RenderingHints.KEY_STROKE_CONTROL,
                      RenderingHints.VALUE_STROKE_PURE);
            break;
        }
        return hints;
    }
    public static RenderingHints convertTextRendering(Element e,
                                                      RenderingHints hints) {
        Value v = getComputedStyle(e, SVGCSSEngine.TEXT_RENDERING_INDEX);
        String s = v.getStringValue();
        int    len = s.length();
        if ((len == 4) && (s.charAt(0) == 'a')) 
            return hints;
        if (len < 13) return hints;  
        if (hints == null)
            hints = new RenderingHints(null);
        switch(s.charAt(8)) {
        case 's': 
            hints.put(RenderingHints.KEY_RENDERING,
                      RenderingHints.VALUE_RENDER_SPEED);
            hints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                      RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            hints.put(RenderingHints.KEY_ANTIALIASING,
                      RenderingHints.VALUE_ANTIALIAS_OFF);
            break;
        case 'l': 
            hints.put(RenderingHints.KEY_RENDERING,
                      RenderingHints.VALUE_RENDER_QUALITY);
            hints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                      RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            hints.put(RenderingHints.KEY_ANTIALIASING,
                      RenderingHints.VALUE_ANTIALIAS_ON);
            break;
        case 'c': 
            hints.put(RenderingHints.KEY_RENDERING,
                      RenderingHints.VALUE_RENDER_QUALITY);
            hints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
                      RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            hints.put(RenderingHints.KEY_ANTIALIASING,
                      RenderingHints.VALUE_ANTIALIAS_ON);
            hints.put(RenderingHints.KEY_FRACTIONALMETRICS,
                      RenderingHints.VALUE_FRACTIONALMETRICS_ON);
            hints.put(RenderingHints.KEY_STROKE_CONTROL,
                      RenderingHints.VALUE_STROKE_PURE);
            break;
        }
        return hints;
    }
    public static RenderingHints convertImageRendering(Element e,
                                                       RenderingHints hints) {
        Value v = getComputedStyle(e, SVGCSSEngine.IMAGE_RENDERING_INDEX);
        String s = v.getStringValue();
        int    len = s.length();
        if ((len == 4) && (s.charAt(0) == 'a')) 
            return hints;
        if (len < 13) return hints;  
        if (hints == null)
            hints = new RenderingHints(null);
        switch(s.charAt(8)) {
        case 's': 
            hints.put(RenderingHints.KEY_RENDERING,
                      RenderingHints.VALUE_RENDER_SPEED);
            hints.put(RenderingHints.KEY_INTERPOLATION,
                      RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            break;
        case 'q': 
            hints.put(RenderingHints.KEY_RENDERING,
                      RenderingHints.VALUE_RENDER_QUALITY);
            hints.put(RenderingHints.KEY_INTERPOLATION,
                      RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            break;
        }
        return hints;
    }
    public static RenderingHints convertColorRendering(Element e,
                                                       RenderingHints hints) {
        Value v = getComputedStyle(e, SVGCSSEngine.COLOR_RENDERING_INDEX);
        String s = v.getStringValue();
        int    len = s.length();
        if ((len == 4) && (s.charAt(0) == 'a')) 
            return hints;
        if (len < 13) return hints;  
        if (hints == null)
            hints = new RenderingHints(null);
        switch(s.charAt(8)) {
        case 's': 
            hints.put(RenderingHints.KEY_COLOR_RENDERING,
                      RenderingHints.VALUE_COLOR_RENDER_SPEED);
            hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION,
                      RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
            break;
        case 'q': 
            hints.put(RenderingHints.KEY_COLOR_RENDERING,
                      RenderingHints.VALUE_COLOR_RENDER_QUALITY);
            hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION,
                      RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
            break;
        }
        return hints;
    }
    public static boolean convertDisplay(Element e) {
        if (!(e instanceof CSSStylableElement)) {
            return true;
        }
        Value v = getComputedStyle(e, SVGCSSEngine.DISPLAY_INDEX);
        return v.getStringValue().charAt(0) != 'n';
    }
    public static boolean convertVisibility(Element e) {
        Value v = getComputedStyle(e, SVGCSSEngine.VISIBILITY_INDEX);
        return v.getStringValue().charAt(0) == 'v';
    }
    public static final Composite TRANSPARENT =
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0);
    public static Composite convertOpacity(Element e) {
        Value v = getComputedStyle(e, SVGCSSEngine.OPACITY_INDEX);
        float f = v.getFloatValue();
        if (f <= 0f) {
            return TRANSPARENT;
        } else if (f >= 1.0f) {
            return AlphaComposite.SrcOver;
        } else {
            return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f);
        }
    }
    public static boolean convertOverflow(Element e) {
        Value v = getComputedStyle(e, SVGCSSEngine.OVERFLOW_INDEX);
        String s = v.getStringValue();
        return (s.charAt(0) == 'h') || (s.charAt(0) == 's');
    }
    public static float[] convertClip(Element e) {
        Value v = getComputedStyle(e, SVGCSSEngine.CLIP_INDEX);
        int primitiveType = v.getPrimitiveType();
        switch ( primitiveType ) {
        case CSSPrimitiveValue.CSS_RECT:
            float [] off = new float[4];
            off[0] = v.getTop().getFloatValue();
            off[1] = v.getRight().getFloatValue();
            off[2] = v.getBottom().getFloatValue();
            off[3] = v.getLeft().getFloatValue();
            return off;
        case CSSPrimitiveValue.CSS_IDENT:
            return null; 
        default:
            throw new IllegalStateException("Unexpected primitiveType:" + primitiveType );
        }
    }
    public static Filter convertFilter(Element filteredElement,
                                       GraphicsNode filteredNode,
                                       BridgeContext ctx) {
        Value v = getComputedStyle(filteredElement, SVGCSSEngine.FILTER_INDEX);
        int primitiveType = v.getPrimitiveType();
        switch ( primitiveType ) {
        case CSSPrimitiveValue.CSS_IDENT:
            return null; 
        case CSSPrimitiveValue.CSS_URI:
            String uri = v.getStringValue();
            Element filter = ctx.getReferencedElement(filteredElement, uri);
            Bridge bridge = ctx.getBridge(filter);
            if (bridge == null || !(bridge instanceof FilterBridge)) {
                throw new BridgeException(ctx, filteredElement,
                                          ERR_CSS_URI_BAD_TARGET,
                                          new Object[] {uri});
            }
            return ((FilterBridge)bridge).createFilter(ctx,
                                                       filter,
                                                       filteredElement,
                                                       filteredNode);
        default:
            throw new IllegalStateException("Unexpected primitive type:" + primitiveType ); 
        }
    }
    public static ClipRable convertClipPath(Element clippedElement,
                                            GraphicsNode clippedNode,
                                            BridgeContext ctx) {
        Value v = getComputedStyle(clippedElement,
                                   SVGCSSEngine.CLIP_PATH_INDEX);
        int primitiveType = v.getPrimitiveType();
        switch ( primitiveType ) {
        case CSSPrimitiveValue.CSS_IDENT:
            return null; 
        case CSSPrimitiveValue.CSS_URI:
            String uri = v.getStringValue();
            Element cp = ctx.getReferencedElement(clippedElement, uri);
            Bridge bridge = ctx.getBridge(cp);
            if (bridge == null || !(bridge instanceof ClipBridge)) {
                throw new BridgeException(ctx, clippedElement,
                                          ERR_CSS_URI_BAD_TARGET,
                                          new Object[] {uri});
            }
            return ((ClipBridge)bridge).createClip(ctx,
                                                   cp,
                                                   clippedElement,
                                                   clippedNode);
        default:
            throw new IllegalStateException("Unexpected primitive type:" + primitiveType ); 
        }
    }
    public static int convertClipRule(Element e) {
        Value v = getComputedStyle(e, SVGCSSEngine.CLIP_RULE_INDEX);
        return (v.getStringValue().charAt(0) == 'n')
            ? GeneralPath.WIND_NON_ZERO
            : GeneralPath.WIND_EVEN_ODD;
    }
    public static Mask convertMask(Element maskedElement,
                                   GraphicsNode maskedNode,
                                   BridgeContext ctx) {
        Value v = getComputedStyle(maskedElement, SVGCSSEngine.MASK_INDEX);
        int primitiveType = v.getPrimitiveType();
        switch ( primitiveType ) {
        case CSSPrimitiveValue.CSS_IDENT:
            return null; 
        case CSSPrimitiveValue.CSS_URI:
            String uri = v.getStringValue();
            Element m = ctx.getReferencedElement(maskedElement, uri);
            Bridge bridge = ctx.getBridge(m);
            if (bridge == null || !(bridge instanceof MaskBridge)) {
                throw new BridgeException(ctx, maskedElement,
                                          ERR_CSS_URI_BAD_TARGET,
                                          new Object[] {uri});
            }
            return ((MaskBridge)bridge).createMask(ctx,
                                                   m,
                                                   maskedElement,
                                                   maskedNode);
        default:
            throw new IllegalStateException("Unexpected primitive type:" + primitiveType ); 
        }
    }
    public static int convertFillRule(Element e) {
        Value v = getComputedStyle(e, SVGCSSEngine.FILL_RULE_INDEX);
        return (v.getStringValue().charAt(0) == 'n')
            ? GeneralPath.WIND_NON_ZERO
            : GeneralPath.WIND_EVEN_ODD;
    }
    public static Color convertLightingColor(Element e, BridgeContext ctx) {
        Value v = getComputedStyle(e, SVGCSSEngine.LIGHTING_COLOR_INDEX);
        if (v.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
            return PaintServer.convertColor(v, 1);
        } else {
            return PaintServer.convertRGBICCColor
                (e, v.item(0), (ICCColor)v.item(1), 1, ctx);
        }
    }
    public static Color convertFloodColor(Element e, BridgeContext ctx) {
        Value v = getComputedStyle(e, SVGCSSEngine.FLOOD_COLOR_INDEX);
        Value o = getComputedStyle(e, SVGCSSEngine.FLOOD_OPACITY_INDEX);
        float f = PaintServer.convertOpacity(o);
        if (v.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
            return PaintServer.convertColor(v, f);
        } else {
            return PaintServer.convertRGBICCColor
                (e, v.item(0), (ICCColor)v.item(1), f, ctx);
        }
    }
    public static Color convertStopColor(Element e,
                                         float opacity,
                                         BridgeContext ctx) {
        Value v = getComputedStyle(e, SVGCSSEngine.STOP_COLOR_INDEX);
        Value o = getComputedStyle(e, SVGCSSEngine.STOP_OPACITY_INDEX);
        opacity *= PaintServer.convertOpacity(o);
        if (v.getCssValueType() == CSSValue.CSS_PRIMITIVE_VALUE) {
            return PaintServer.convertColor(v, opacity);
        } else {
            return PaintServer.convertRGBICCColor
                (e, v.item(0), (ICCColor)v.item(1), opacity, ctx);
        }
    }
    public static void computeStyleAndURIs(Element refElement,
                                           Element localRefElement,
                                           String  uri) {
        int idx = uri.indexOf('#');
        if (idx != -1)
            uri = uri.substring(0,idx);
        if (uri.length() != 0)
            localRefElement.setAttributeNS(XML_NAMESPACE_URI,
                                           "base",
                                           uri);
        CSSEngine engine    = CSSUtilities.getCSSEngine(localRefElement);
        CSSEngine refEngine = CSSUtilities.getCSSEngine(refElement);
        engine.importCascadedStyleMaps(refElement, refEngine, localRefElement);
    }
    protected static int rule(CSSValue v) {
        return (((CSSPrimitiveValue)v).getStringValue().charAt(0) == 'n')
            ? GeneralPath.WIND_NON_ZERO
            : GeneralPath.WIND_EVEN_ODD;
    }
}
