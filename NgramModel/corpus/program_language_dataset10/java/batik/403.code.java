package org.apache.batik.css.engine.value.svg12;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueFactory;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.util.SVG12CSSConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
public class MarginShorthandManager
    extends AbstractValueFactory
    implements ShorthandManager {
    public MarginShorthandManager() { }
    public String getPropertyName() {
        return SVG12CSSConstants.CSS_MARGIN_PROPERTY;
    }
    public boolean isAnimatableProperty() {
        return true;
    }
    public boolean isAdditiveProperty() {
        return false;
    }
    public void setValues(CSSEngine eng,
                          ShorthandManager.PropertyHandler ph,
                          LexicalUnit lu,
                          boolean imp)
        throws DOMException {
        if (lu.getLexicalUnitType() == LexicalUnit.SAC_INHERIT)
            return;
        LexicalUnit []lus  = new LexicalUnit[4];
        int cnt=0;
        while (lu != null) {
            if (cnt == 4)
                throw createInvalidLexicalUnitDOMException
                    (lu.getLexicalUnitType());
            lus[cnt++] = lu;
            lu = lu.getNextLexicalUnit();
        }
        switch (cnt) {
        case 1: lus[3] = lus[2] = lus[1] = lus[0]; break;
        case 2: lus[2] = lus[0];  lus[3] = lus[1]; break;
        case 3: lus[3] = lus[1]; break;
        default:
        }
        ph.property(SVG12CSSConstants.CSS_MARGIN_TOP_PROPERTY,    lus[0], imp);
        ph.property(SVG12CSSConstants.CSS_MARGIN_RIGHT_PROPERTY,  lus[1], imp);
        ph.property(SVG12CSSConstants.CSS_MARGIN_BOTTOM_PROPERTY, lus[2], imp);
        ph.property(SVG12CSSConstants.CSS_MARGIN_LEFT_PROPERTY,   lus[3], imp);
    }
}
