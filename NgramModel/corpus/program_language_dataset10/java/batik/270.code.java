package org.apache.batik.css.dom;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.SVGCSSEngine;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg.SVGColorManager;
import org.apache.batik.css.engine.value.svg.SVGPaintManager;
import org.w3c.dom.css.CSSValue;
public class CSSOMSVGComputedStyle extends CSSOMComputedStyle {
    public CSSOMSVGComputedStyle(CSSEngine e,
                                 CSSStylableElement elt,
                                 String pseudoElt) {
        super(e, elt, pseudoElt);
    }
    protected CSSValue createCSSValue(int idx) {
        if (idx > SVGCSSEngine.FINAL_INDEX) {
            if (cssEngine.getValueManagers()[idx] instanceof SVGPaintManager) {
                return new ComputedCSSPaintValue(idx);
            }
            if (cssEngine.getValueManagers()[idx] instanceof SVGColorManager) {
                return new ComputedCSSColorValue(idx);
            }
        } else {
            switch (idx) {
            case SVGCSSEngine.FILL_INDEX:
            case SVGCSSEngine.STROKE_INDEX:
                return new ComputedCSSPaintValue(idx);
            case SVGCSSEngine.FLOOD_COLOR_INDEX:
            case SVGCSSEngine.LIGHTING_COLOR_INDEX:
            case SVGCSSEngine.STOP_COLOR_INDEX:
                return new ComputedCSSColorValue(idx);
            }
        }
        return super.createCSSValue(idx);
    }
    protected class ComputedCSSColorValue
        extends CSSOMSVGColor
        implements CSSOMSVGColor.ValueProvider {
        protected int index;
        public ComputedCSSColorValue(int idx) {
            super(null);
            valueProvider = this;
            index = idx;
        }
        public Value getValue() {
            return cssEngine.getComputedStyle(element, pseudoElement, index);
        }
    }
    public class ComputedCSSPaintValue
        extends CSSOMSVGPaint
        implements CSSOMSVGPaint.ValueProvider {
        protected int index;
        public ComputedCSSPaintValue(int idx) {
            super(null);
            valueProvider = this;
            index = idx;
        }
        public Value getValue() {
            return cssEngine.getComputedStyle(element, pseudoElement, index);
        }
    }
}
