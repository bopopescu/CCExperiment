package org.apache.batik.css.engine.value.css2;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.CSSConstants;
import org.apache.batik.util.SVGTypes;
public class FontVariantManager extends IdentifierManager {
    protected static final StringMap values = new StringMap();
    static {
        values.put(CSSConstants.CSS_NORMAL_VALUE,
                   ValueConstants.NORMAL_VALUE);
        values.put(CSSConstants.CSS_SMALL_CAPS_VALUE,
                   ValueConstants.SMALL_CAPS_VALUE);
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
        return CSSConstants.CSS_FONT_VARIANT_PROPERTY;
    }
    public Value getDefaultValue() {
        return ValueConstants.NORMAL_VALUE;
    }
    public StringMap getIdentifiers() {
        return values;
    }
}
