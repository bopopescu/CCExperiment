package org.apache.batik.css.engine.value.svg;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
public class PointerEventsManager extends IdentifierManager {
    protected static final StringMap values = new StringMap();
    static {
        values.put(CSSConstants.CSS_ALL_VALUE,
                   SVGValueConstants.ALL_VALUE);
        values.put(CSSConstants.CSS_FILL_VALUE,
                   SVGValueConstants.FILL_VALUE);
        values.put(CSSConstants.CSS_FILLSTROKE_VALUE,
                   SVGValueConstants.FILLSTROKE_VALUE);
        values.put(CSSConstants.CSS_NONE_VALUE,
                   SVGValueConstants.NONE_VALUE);
        values.put(CSSConstants.CSS_PAINTED_VALUE,
                   SVGValueConstants.PAINTED_VALUE);
        values.put(CSSConstants.CSS_STROKE_VALUE,
                   SVGValueConstants.STROKE_VALUE);
        values.put(CSSConstants.CSS_VISIBLE_VALUE,
                   SVGValueConstants.VISIBLE_VALUE);
        values.put(CSSConstants.CSS_VISIBLEFILL_VALUE,
                   SVGValueConstants.VISIBLEFILL_VALUE);
        values.put(CSSConstants.CSS_VISIBLEFILLSTROKE_VALUE,
                   SVGValueConstants.VISIBLEFILLSTROKE_VALUE);
        values.put(CSSConstants.CSS_VISIBLEPAINTED_VALUE,
                   SVGValueConstants.VISIBLEPAINTED_VALUE);
        values.put(CSSConstants.CSS_VISIBLESTROKE_VALUE,
                   SVGValueConstants.VISIBLESTROKE_VALUE);
    }
    public boolean isInheritedProperty() {
        return true;
    }
    public boolean isAnimatableProperty() {
        return true;
    }
    public boolean isAdditiveProperty() {
        return false;
    }
    public int getPropertyType() {
        return SVGTypes.TYPE_IDENT;
    }
    public String getPropertyName() {
        return CSSConstants.CSS_POINTER_EVENTS_PROPERTY;
    }
    public Value getDefaultValue() {
        return SVGValueConstants.VISIBLEPAINTED_VALUE;
    }
    public StringMap getIdentifiers() {
        return values;
    }
}
