package org.apache.batik.dom.svg;
import org.apache.batik.dom.anim.AnimationTargetListener;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.css.dom.CSSOMSVGColor;
import org.apache.batik.css.dom.CSSOMSVGPaint;
import org.apache.batik.css.dom.CSSOMStoredStyleDeclaration;
import org.apache.batik.css.dom.CSSOMValue;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.css.engine.StyleDeclarationProvider;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg.SVGColorManager;
import org.apache.batik.css.engine.value.svg.SVGPaintManager;
import org.apache.batik.dom.AbstractDocument;
import org.apache.batik.util.DoublyIndexedTable;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGTypes;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.svg.SVGAnimatedString;
public abstract class SVGStylableElement
    extends SVGOMElement
    implements CSSStylableElement {
    protected static DoublyIndexedTable xmlTraitInformation;
    static {
        DoublyIndexedTable t =
            new DoublyIndexedTable(SVGOMElement.xmlTraitInformation);
        t.put(null, SVG_CLASS_ATTRIBUTE,
                new TraitInformation(true, SVGTypes.TYPE_CDATA));
        xmlTraitInformation = t;
    }
    protected StyleMap computedStyleMap;
    protected OverrideStyleDeclaration overrideStyleDeclaration;
    protected SVGOMAnimatedString className;
    protected StyleDeclaration style;
    protected SVGStylableElement() {
    }
    protected SVGStylableElement(String prefix, AbstractDocument owner) {
        super(prefix, owner);
        initializeLiveAttributes();
    }
    protected void initializeAllLiveAttributes() {
        super.initializeAllLiveAttributes();
        initializeLiveAttributes();
    }
    private void initializeLiveAttributes() {
        className = createLiveAnimatedString(null, SVG_CLASS_ATTRIBUTE);
    }
    public CSSStyleDeclaration getOverrideStyle() {
        if (overrideStyleDeclaration == null) {
            CSSEngine eng = ((SVGOMDocument) getOwnerDocument()).getCSSEngine();
            overrideStyleDeclaration = new OverrideStyleDeclaration(eng);
        }
        return overrideStyleDeclaration;
    }
    public StyleMap getComputedStyleMap(String pseudoElement) {
        return computedStyleMap;
    }
    public void setComputedStyleMap(String pseudoElement, StyleMap sm) {
        computedStyleMap = sm;
    }
    public String getXMLId() {
        return getAttributeNS(null, "id");
    }
    public String getCSSClass() {
        return getAttributeNS(null, "class");
    }
    public ParsedURL getCSSBase() {
        if (getXblBoundElement() != null) {
            return null;
        }
        String bu = getBaseURI();
        return bu == null ? null : new ParsedURL(bu);
    }
    public boolean isPseudoInstanceOf(String pseudoClass) {
        if (pseudoClass.equals("first-child")) {
            Node n = getPreviousSibling();
            while (n != null && n.getNodeType() != ELEMENT_NODE) {
                n = n.getPreviousSibling();
            }
            return n == null;
        }
        return false;
    }
    public StyleDeclarationProvider getOverrideStyleDeclarationProvider() {
        return (StyleDeclarationProvider) getOverrideStyle();
    }
    public void updatePropertyValue(String pn, AnimatableValue val) {
        CSSStyleDeclaration over = getOverrideStyle();
        if (val == null) {
            over.removeProperty(pn);
        } else {
            over.setProperty(pn, val.getCssText(), "");
        }
    }
    public boolean useLinearRGBColorInterpolation() {
        CSSEngine eng = ((SVGOMDocument) getOwnerDocument()).getCSSEngine();
        Value v = eng.getComputedStyle(this, null,
                                       SVGCSSEngine.COLOR_INTERPOLATION_INDEX);
        return v.getStringValue().charAt(0) == 'l';
    }
    public void addTargetListener(String ns, String an, boolean isCSS,
                                  AnimationTargetListener l) {
        if (isCSS) {
            if (svgContext != null) {
                SVGAnimationTargetContext actx =
                    (SVGAnimationTargetContext) svgContext;
                actx.addTargetListener(an, l);
            }
        } else {
            super.addTargetListener(ns, an, isCSS, l);
        }
    }
    public void removeTargetListener(String ns, String an, boolean isCSS,
                                     AnimationTargetListener l) {
        if (isCSS) {
            if (svgContext != null) {
                SVGAnimationTargetContext actx =
                    (SVGAnimationTargetContext) svgContext;
                actx.removeTargetListener(an, l);
            }
        } else {
            super.removeTargetListener(ns, an, isCSS, l);
        }
    }
    public CSSStyleDeclaration getStyle() {
        if (style == null) {
            CSSEngine eng = ((SVGOMDocument)getOwnerDocument()).getCSSEngine();
            style = new StyleDeclaration(eng);
            putLiveAttributeValue(null, SVG_STYLE_ATTRIBUTE, style);
        }
        return style;
    }
    public CSSValue getPresentationAttribute(String name) {
        CSSValue result = (CSSValue)getLiveAttributeValue(null, name);
        if (result != null)
            return result;
        CSSEngine eng = ((SVGOMDocument)getOwnerDocument()).getCSSEngine();
        int idx = eng.getPropertyIndex(name);
        if (idx == -1)
            return null;
        if (idx > SVGCSSEngine.FINAL_INDEX) {
            if (eng.getValueManagers()[idx] instanceof SVGPaintManager) {
                result = new PresentationAttributePaintValue(eng, name);
            }
            if (eng.getValueManagers()[idx] instanceof SVGColorManager) {
                result = new PresentationAttributeColorValue(eng, name);
            }
        } else {
            switch (idx) {
            case SVGCSSEngine.FILL_INDEX:
            case SVGCSSEngine.STROKE_INDEX:
                result = new PresentationAttributePaintValue(eng, name);
                break;
            case SVGCSSEngine.FLOOD_COLOR_INDEX:
            case SVGCSSEngine.LIGHTING_COLOR_INDEX:
            case SVGCSSEngine.STOP_COLOR_INDEX:
                result = new PresentationAttributeColorValue(eng, name);
                break;
            default:
                result = new PresentationAttributeValue(eng, name);
            }
        }
        putLiveAttributeValue(null, name, (LiveAttributeValue)result);
        if (getAttributeNS(null, name).length() == 0) {
            return null;
        }
        return result;
    }
    public SVGAnimatedString getClassName() {
        return className;
    }
    protected DoublyIndexedTable getTraitInformationTable() {
        return xmlTraitInformation;
    }
    public class PresentationAttributeValue
        extends CSSOMValue
        implements LiveAttributeValue,
                   CSSOMValue.ValueProvider {
        protected CSSEngine cssEngine;
        protected String property;
        protected Value value;
        protected boolean mutate;
        public PresentationAttributeValue(CSSEngine eng, String prop) {
            super(null);
            valueProvider = this;
            setModificationHandler(new AbstractModificationHandler() {
                    protected Value getValue() {
                        return PresentationAttributeValue.this.getValue();
                    }
                    public void textChanged(String text) throws DOMException {
                        value = cssEngine.parsePropertyValue
                            (SVGStylableElement.this, property, text);
                        mutate = true;
                        setAttributeNS(null, property, text);
                        mutate = false;
                    }
                });
            cssEngine = eng;
            property = prop;
            Attr attr = getAttributeNodeNS(null, prop);
            if (attr != null) {
                value = cssEngine.parsePropertyValue
                    (SVGStylableElement.this, prop, attr.getValue());
            }
        }
        public Value getValue() {
            if (value == null) {
                throw new DOMException(DOMException.INVALID_STATE_ERR, "");
            }
            return value;
        }
        public void attrAdded(Attr node, String newv) {
            if (!mutate) {
                value = cssEngine.parsePropertyValue
                    (SVGStylableElement.this, property, newv);
            }
        }
        public void attrModified(Attr node, String oldv, String newv) {
            if (!mutate) {
                value = cssEngine.parsePropertyValue
                    (SVGStylableElement.this, property, newv);
            }
        }
        public void attrRemoved(Attr node, String oldv) {
            if (!mutate) {
                value = null;
            }
        }
    }
    public class PresentationAttributeColorValue
        extends CSSOMSVGColor
        implements LiveAttributeValue,
                   CSSOMSVGColor.ValueProvider {
        protected CSSEngine cssEngine;
        protected String property;
        protected Value value;
        protected boolean mutate;
        public PresentationAttributeColorValue(CSSEngine eng, String prop) {
            super(null);
            valueProvider = this;
            setModificationHandler(new AbstractModificationHandler() {
                    protected Value getValue() {
                        return PresentationAttributeColorValue.this.getValue();
                    }
                    public void textChanged(String text) throws DOMException {
                        value = cssEngine.parsePropertyValue
                            (SVGStylableElement.this, property, text);
                        mutate = true;
                        setAttributeNS(null, property, text);
                        mutate = false;
                    }
                });
            cssEngine = eng;
            property = prop;
            Attr attr = getAttributeNodeNS(null, prop);
            if (attr != null) {
                value = cssEngine.parsePropertyValue
                    (SVGStylableElement.this, prop, attr.getValue());
            }
        }
        public Value getValue() {
            if (value == null) {
                throw new DOMException(DOMException.INVALID_STATE_ERR, "");
            }
            return value;
        }
        public void attrAdded(Attr node, String newv) {
            if (!mutate) {
                value = cssEngine.parsePropertyValue
                    (SVGStylableElement.this, property, newv);
            }
        }
        public void attrModified(Attr node, String oldv, String newv) {
            if (!mutate) {
                value = cssEngine.parsePropertyValue
                    (SVGStylableElement.this, property, newv);
            }
        }
        public void attrRemoved(Attr node, String oldv) {
            if (!mutate) {
                value = null;
            }
        }
    }
    public class PresentationAttributePaintValue
        extends CSSOMSVGPaint
        implements LiveAttributeValue,
                   CSSOMSVGPaint.ValueProvider {
        protected CSSEngine cssEngine;
        protected String property;
        protected Value value;
        protected boolean mutate;
        public PresentationAttributePaintValue(CSSEngine eng, String prop) {
            super(null);
            valueProvider = this;
            setModificationHandler(new AbstractModificationHandler() {
                    protected Value getValue() {
                        return PresentationAttributePaintValue.this.getValue();
                    }
                    public void textChanged(String text) throws DOMException {
                        value = cssEngine.parsePropertyValue
                            (SVGStylableElement.this, property, text);
                        mutate = true;
                        setAttributeNS(null, property, text);
                        mutate = false;
                    }
                });
            cssEngine = eng;
            property = prop;
            Attr attr = getAttributeNodeNS(null, prop);
            if (attr != null) {
                value = cssEngine.parsePropertyValue
                    (SVGStylableElement.this, prop, attr.getValue());
            }
        }
        public Value getValue() {
            if (value == null) {
                throw new DOMException(DOMException.INVALID_STATE_ERR, "");
            }
            return value;
        }
        public void attrAdded(Attr node, String newv) {
            if (!mutate) {
                value = cssEngine.parsePropertyValue
                    (SVGStylableElement.this, property, newv);
            }
        }
        public void attrModified(Attr node, String oldv, String newv) {
            if (!mutate) {
                value = cssEngine.parsePropertyValue
                    (SVGStylableElement.this, property, newv);
            }
        }
        public void attrRemoved(Attr node, String oldv) {
            if (!mutate) {
                value = null;
            }
        }
    }
    public class StyleDeclaration
        extends CSSOMStoredStyleDeclaration
        implements LiveAttributeValue,
                   CSSEngine.MainPropertyReceiver {
        protected boolean mutate;
        public StyleDeclaration(CSSEngine eng) {
            super(eng);
            declaration = cssEngine.parseStyleDeclaration
                (SVGStylableElement.this,
                 getAttributeNS(null, SVG_STYLE_ATTRIBUTE));
        }
        public void attrAdded(Attr node, String newv) {
            if (!mutate) {
                declaration = cssEngine.parseStyleDeclaration
                    (SVGStylableElement.this, newv);
            }
        }
        public void attrModified(Attr node, String oldv, String newv) {
            if (!mutate) {
                declaration = cssEngine.parseStyleDeclaration
                    (SVGStylableElement.this, newv);
            }
        }
        public void attrRemoved(Attr node, String oldv) {
            if (!mutate) {
                declaration =
                    new org.apache.batik.css.engine.StyleDeclaration();
            }
        }
        public void textChanged(String text) throws DOMException {
            declaration = cssEngine.parseStyleDeclaration
                (SVGStylableElement.this, text);
            mutate = true;
            setAttributeNS(null, SVG_STYLE_ATTRIBUTE, text);
            mutate = false;
        }
        public void propertyRemoved(String name) throws DOMException {
            int idx = cssEngine.getPropertyIndex(name);
            for (int i = 0; i < declaration.size(); i++) {
                if (idx == declaration.getIndex(i)) {
                    declaration.remove(i);
                    mutate = true;
                    setAttributeNS(null, SVG_STYLE_ATTRIBUTE,
                                   declaration.toString(cssEngine));
                    mutate = false;
                    return;
                }
            }
        }
        public void propertyChanged(String name, String value, String prio)
            throws DOMException {
            boolean important = prio != null && prio.length() > 0;
            cssEngine.setMainProperties(SVGStylableElement.this,
                                        this, name, value, important);
            mutate = true;
            setAttributeNS(null, SVG_STYLE_ATTRIBUTE,
                           declaration.toString(cssEngine));
            mutate = false;
        }
        public void setMainProperty(String name, Value v, boolean important) {
            int idx = cssEngine.getPropertyIndex(name);
            if (idx == -1)
                return;   
            int i;
            for (i = 0; i < declaration.size(); i++) {
                if (idx == declaration.getIndex(i))
                    break;
            }
            if (i < declaration.size())
                declaration.put(i, v, idx, important);
            else
                declaration.append(v, idx, important);
        }
    }
    protected class OverrideStyleDeclaration
        extends CSSOMStoredStyleDeclaration {
        protected OverrideStyleDeclaration(CSSEngine eng) {
            super(eng);
            declaration = new org.apache.batik.css.engine.StyleDeclaration();
        }
        public void textChanged(String text) throws DOMException {
            ((SVGOMDocument) ownerDocument).overrideStyleTextChanged
                (SVGStylableElement.this, text);
        }
        public void propertyRemoved(String name) throws DOMException {
            ((SVGOMDocument) ownerDocument).overrideStylePropertyRemoved
                (SVGStylableElement.this, name);
        }
        public void propertyChanged(String name, String value, String prio)
                throws DOMException {
            ((SVGOMDocument) ownerDocument).overrideStylePropertyChanged
                (SVGStylableElement.this, name, value, prio);
        }
    }
}
