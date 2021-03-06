package org.apache.batik.css.dom;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg.ICCColor;
import org.apache.batik.util.CSSConstants;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.w3c.dom.css.CSSValue;
import org.w3c.dom.svg.SVGPaint;
public class CSSOMSVGPaint extends CSSOMSVGColor implements SVGPaint {
    public CSSOMSVGPaint(ValueProvider vp) {
        super(vp);
    }
    public void setModificationHandler(ModificationHandler h) {
        if (!(h instanceof PaintModificationHandler)) {
            throw new IllegalArgumentException();
        }
        super.setModificationHandler(h);
    }
    public short getColorType() {
        throw new DOMException(DOMException.INVALID_ACCESS_ERR, "");
    }
    public short getPaintType() {
        Value value = valueProvider.getValue();
        switch (value.getCssValueType()) {
        case CSSValue.CSS_PRIMITIVE_VALUE:
            switch (value.getPrimitiveType()) {
            case CSSPrimitiveValue.CSS_IDENT: {
                String str = value.getStringValue();
                if (str.equalsIgnoreCase(CSSConstants.CSS_NONE_VALUE)) {
                    return SVG_PAINTTYPE_NONE;
                } else if (str.equalsIgnoreCase
                           (CSSConstants.CSS_CURRENTCOLOR_VALUE)) {
                    return SVG_PAINTTYPE_CURRENTCOLOR;
                }
                return SVG_PAINTTYPE_RGBCOLOR;
            }
            case CSSPrimitiveValue.CSS_RGBCOLOR:
                return SVG_PAINTTYPE_RGBCOLOR;
            case CSSPrimitiveValue.CSS_URI:
                return SVG_PAINTTYPE_URI;
            }
            break;
        case CSSValue.CSS_VALUE_LIST:
            Value v0 = value.item(0);
            Value v1 = value.item(1);
            switch (v0.getPrimitiveType()) {
            case CSSPrimitiveValue.CSS_IDENT:
                return SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR;
            case CSSPrimitiveValue.CSS_URI:
                if (v1.getCssValueType() == CSSValue.CSS_VALUE_LIST)
                    return SVG_PAINTTYPE_URI_RGBCOLOR_ICCCOLOR;
                switch (v1.getPrimitiveType()) {
                case CSSPrimitiveValue.CSS_IDENT: {
                    String str = v1.getStringValue();
                    if (str.equalsIgnoreCase(CSSConstants.CSS_NONE_VALUE)) {
                        return SVG_PAINTTYPE_URI_NONE;
                    } else if (str.equalsIgnoreCase
                               (CSSConstants.CSS_CURRENTCOLOR_VALUE)) {
                        return SVG_PAINTTYPE_URI_CURRENTCOLOR;
                    }
                    return SVG_PAINTTYPE_URI_RGBCOLOR;
                }
                case CSSPrimitiveValue.CSS_RGBCOLOR:
                    return SVG_PAINTTYPE_URI_RGBCOLOR;
                }
            case CSSPrimitiveValue.CSS_RGBCOLOR:
                return SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR;
            }
        }
        return SVG_PAINTTYPE_UNKNOWN;
    }
    public String getUri() {
        switch (getPaintType()) {
        case SVG_PAINTTYPE_URI:
            return valueProvider.getValue().getStringValue();
        case SVG_PAINTTYPE_URI_NONE:
        case SVG_PAINTTYPE_URI_CURRENTCOLOR:
        case SVG_PAINTTYPE_URI_RGBCOLOR:
        case SVG_PAINTTYPE_URI_RGBCOLOR_ICCCOLOR:
            return valueProvider.getValue().item(0).getStringValue();
        }
        throw new InternalError();
    }
    public void setUri(String uri) {
        if (handler == null) {
            throw new DOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
        } else {
            ((PaintModificationHandler)handler).uriChanged(uri);
        }
    }
    public void setPaint(short paintType, String uri,
                         String rgbColor, String iccColor) {
        if (handler == null) {
            throw new DOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
        } else {
            ((PaintModificationHandler)handler).paintChanged
                (paintType, uri, rgbColor, iccColor);
        }
    }
    public interface PaintModificationHandler extends ModificationHandler {
        void uriChanged(String uri);
        void paintChanged(short type, String uri, String rgb, String icc);
    }
    public abstract class AbstractModificationHandler
        implements PaintModificationHandler {
        protected abstract Value getValue();
        public void redTextChanged(String text) throws DOMException {
            switch (getPaintType()) {
            case SVG_PAINTTYPE_RGBCOLOR:
                text = "rgb(" +
                    text + ", " +
                    getValue().getGreen().getCssText() + ", " +
                    getValue().getBlue().getCssText() + ')';
                break;
            case SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR:
                text = "rgb(" +
                    text + ", " +
                    getValue().item(0).getGreen().getCssText() + ", " +
                    getValue().item(0).getBlue().getCssText() + ") " +
                    getValue().item(1).getCssText();
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR:
                text = getValue().item(0) +
                    " rgb(" +
                    text + ", " +
                    getValue().item(1).getGreen().getCssText() + ", " +
                    getValue().item(1).getBlue().getCssText() + ')';
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR_ICCCOLOR:
                text = getValue().item(0) +
                    " rgb(" +
                    text + ", " +
                    getValue().item(1).getGreen().getCssText() + ", " +
                    getValue().item(1).getBlue().getCssText() + ") " +
                    getValue().item(2).getCssText();
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
            textChanged(text);
        }
        public void redFloatValueChanged(short unit, float value)
            throws DOMException {
            String text;
            switch (getPaintType()) {
            case SVG_PAINTTYPE_RGBCOLOR:
                text = "rgb(" +
                    FloatValue.getCssText(unit, value) + ", " +
                    getValue().getGreen().getCssText() + ", " +
                    getValue().getBlue().getCssText() + ')';
                break;
            case SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR:
                text = "rgb(" +
                    FloatValue.getCssText(unit, value) + ", " +
                    getValue().item(0).getGreen().getCssText() + ", " +
                    getValue().item(0).getBlue().getCssText() + ") " +
                    getValue().item(1).getCssText();
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR:
                text = getValue().item(0) +
                    " rgb(" +
                    FloatValue.getCssText(unit, value) + ", " +
                    getValue().item(1).getGreen().getCssText() + ", " +
                    getValue().item(1).getBlue().getCssText() + ')';
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR_ICCCOLOR:
                text = getValue().item(0) +
                    " rgb(" +
                    FloatValue.getCssText(unit, value) + ", " +
                    getValue().item(1).getGreen().getCssText() + ", " +
                    getValue().item(1).getBlue().getCssText() + ") " +
                    getValue().item(2).getCssText();
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
            textChanged(text);
        }
        public void greenTextChanged(String text) throws DOMException {
            switch (getPaintType()) {
            case SVG_PAINTTYPE_RGBCOLOR:
                text = "rgb(" +
                    getValue().getRed().getCssText() + ", " +
                    text + ", " +
                    getValue().getBlue().getCssText() + ')';
                break;
            case SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR:
                text = "rgb(" +
                    getValue().item(0).getRed().getCssText() + ", " +
                    text + ", " +
                    getValue().item(0).getBlue().getCssText() + ") " +
                    getValue().item(1).getCssText();
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR:
                text = getValue().item(0) +
                    " rgb(" +
                    getValue().item(1).getRed().getCssText() + ", " +
                    text + ", " +
                    getValue().item(1).getBlue().getCssText() + ')';
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR_ICCCOLOR:
                text = getValue().item(0) +
                    " rgb(" +
                    getValue().item(1).getRed().getCssText() + ", " +
                    text + ", " +
                    getValue().item(1).getBlue().getCssText() + ") " +
                    getValue().item(2).getCssText();
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
            textChanged(text);
        }
        public void greenFloatValueChanged(short unit, float value)
            throws DOMException {
            String text;
            switch (getPaintType()) {
            case SVG_PAINTTYPE_RGBCOLOR:
                text = "rgb(" +
                    getValue().getRed().getCssText() + ", " +
                    FloatValue.getCssText(unit, value) + ", " +
                    getValue().getBlue().getCssText() + ')';
                break;
            case SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR:
                text = "rgb(" +
                    getValue().item(0).getRed().getCssText() + ", " +
                    FloatValue.getCssText(unit, value) + ", " +
                    getValue().item(0).getBlue().getCssText() + ") " +
                    getValue().item(1).getCssText();
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR:
                text = getValue().item(0) +
                    " rgb(" +
                    getValue().item(1).getRed().getCssText() + ", " +
                    FloatValue.getCssText(unit, value) + ", " +
                    getValue().item(1).getBlue().getCssText() + ')';
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR_ICCCOLOR:
                text = getValue().item(0) +
                    " rgb(" +
                    getValue().item(1).getRed().getCssText() + ", " +
                    FloatValue.getCssText(unit, value) + ", " +
                    getValue().item(1).getBlue().getCssText() + ") " +
                    getValue().item(2).getCssText();
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
            textChanged(text);
        }
        public void blueTextChanged(String text) throws DOMException {
            switch (getPaintType()) {
            case SVG_PAINTTYPE_RGBCOLOR:
                text = "rgb(" +
                    getValue().getRed().getCssText() + ", " +
                    getValue().getGreen().getCssText() + ", " +
                    text + ')';
                break;
            case SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR:
                text = "rgb(" +
                    getValue().item(0).getRed().getCssText() + ", " +
                    getValue().item(0).getGreen().getCssText() + ", " +
                    text + ") " +
                    getValue().item(1).getCssText();
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR:
                text = getValue().item(0) +
                    " rgb(" +
                    getValue().item(1).getRed().getCssText() + ", " +
                    getValue().item(1).getGreen().getCssText() + ", " +
                    text + ")";
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR_ICCCOLOR:
                text = getValue().item(0) +
                    " rgb(" +
                    getValue().item(1).getRed().getCssText() + ", " +
                    getValue().item(1).getGreen().getCssText() + ", " +
                    text + ") " +
                    getValue().item(2).getCssText();
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
            textChanged(text);
        }
        public void blueFloatValueChanged(short unit, float value)
            throws DOMException {
            String text;
            switch (getPaintType()) {
            case SVG_PAINTTYPE_RGBCOLOR:
                text = "rgb(" +
                    getValue().getRed().getCssText() + ", " +
                    getValue().getGreen().getCssText() + ", " +
                    FloatValue.getCssText(unit, value) + ')';
                break;
            case SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR:
                text = "rgb(" +
                    getValue().item(0).getRed().getCssText() + ", " +
                    getValue().item(0).getGreen().getCssText() + ", " +
                    FloatValue.getCssText(unit, value) + ") " +
                    getValue().item(1).getCssText();
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR:
                text = getValue().item(0) +
                    " rgb(" +
                    getValue().item(1).getRed().getCssText() + ", " +
                    getValue().item(1).getGreen().getCssText() + ", " +
                    FloatValue.getCssText(unit, value) + ')';
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR_ICCCOLOR:
                text = getValue().item(0) +
                    " rgb(" +
                    getValue().item(1).getRed().getCssText() + ", " +
                    getValue().item(1).getGreen().getCssText() + ", " +
                    FloatValue.getCssText(unit, value) + ") " +
                    getValue().item(2).getCssText();
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
            textChanged(text);
        }
        public void rgbColorChanged(String text) throws DOMException {
            switch (getPaintType()) {
            case SVG_PAINTTYPE_RGBCOLOR:
                break;
            case SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR:
                text += getValue().item(1).getCssText();
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR:
                text = getValue().item(0).getCssText() + ' ' + text;
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR_ICCCOLOR:
                text = getValue().item(0).getCssText() + ' ' + text + ' ' +
                    getValue().item(2).getCssText();
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
            textChanged(text);
        }
        public void rgbColorICCColorChanged(String rgb, String icc)
            throws DOMException {
            switch (getPaintType()) {
            case SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR:
                textChanged(rgb + ' ' + icc);
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR_ICCCOLOR:
                textChanged(getValue().item(0).getCssText() + ' ' +
                            rgb + ' ' + icc);
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
        }
        public void colorChanged(short type, String rgb, String icc)
            throws DOMException {
            switch (type) {
            case SVG_PAINTTYPE_CURRENTCOLOR:
                textChanged("currentcolor");
                break;
            case SVG_PAINTTYPE_RGBCOLOR:
                textChanged(rgb);
                break;
            case SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR:
                textChanged(rgb + ' ' + icc);
                break;
            default:
                throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "");
            }
        }
        public void colorProfileChanged(String cp) throws DOMException {
            switch (getPaintType()) {
            case SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR:
                StringBuffer sb =
                    new StringBuffer(getValue().item(0).getCssText());
                sb.append(" icc-color(");
                sb.append(cp);
                ICCColor iccc = (ICCColor)getValue().item(1);
                for (int i = 0; i < iccc.getLength(); i++) {
                    sb.append( ',' );
                    sb.append(iccc.getColor(i));
                }
                sb.append( ')' );
                textChanged(sb.toString());
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR_ICCCOLOR:
                sb = new StringBuffer(getValue().item(0).getCssText());
                sb.append( ' ' );
                sb.append(getValue().item(1).getCssText());
                sb.append(" icc-color(");
                sb.append(cp);
                iccc = (ICCColor)getValue().item(1);
                for (int i = 0; i < iccc.getLength(); i++) {
                    sb.append( ',' );
                    sb.append(iccc.getColor(i));
                }
                sb.append( ')' );
                textChanged(sb.toString());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
        }
        public void colorsCleared() throws DOMException {
            switch (getPaintType()) {
            case SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR:
                StringBuffer sb =
                    new StringBuffer(getValue().item(0).getCssText());
                sb.append(" icc-color(");
                ICCColor iccc = (ICCColor)getValue().item(1);
                sb.append(iccc.getColorProfile());
                sb.append( ')' );
                textChanged(sb.toString());
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR_ICCCOLOR:
                sb = new StringBuffer(getValue().item(0).getCssText());
                sb.append( ' ' );
                sb.append(getValue().item(1).getCssText());
                sb.append(" icc-color(");
                iccc = (ICCColor)getValue().item(1);
                sb.append(iccc.getColorProfile());
                sb.append( ')' );
                textChanged(sb.toString());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
        }
        public void colorsInitialized(float f) throws DOMException {
            switch (getPaintType()) {
            case SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR:
                StringBuffer sb =
                    new StringBuffer(getValue().item(0).getCssText());
                sb.append(" icc-color(");
                ICCColor iccc = (ICCColor)getValue().item(1);
                sb.append(iccc.getColorProfile());
                sb.append( ',' );
                sb.append(f);
                sb.append( ')' );
                textChanged(sb.toString());
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR_ICCCOLOR:
                sb = new StringBuffer(getValue().item(0).getCssText());
                sb.append( ' ' );
                sb.append(getValue().item(1).getCssText());
                sb.append(" icc-color(");
                iccc = (ICCColor)getValue().item(1);
                sb.append(iccc.getColorProfile());
                sb.append( ',' );
                sb.append(f);
                sb.append( ')' );
                textChanged(sb.toString());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
        }
        public void colorInsertedBefore(float f, int idx) throws DOMException {
            switch (getPaintType()) {
            case SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR:
                StringBuffer sb =
                    new StringBuffer(getValue().item(0).getCssText());
                sb.append(" icc-color(");
                ICCColor iccc = (ICCColor)getValue().item(1);
                sb.append(iccc.getColorProfile());
                for (int i = 0; i < idx; i++) {
                    sb.append( ',' );
                    sb.append(iccc.getColor(i));
                }
                sb.append( ',' );
                sb.append(f);
                for (int i = idx; i < iccc.getLength(); i++) {
                    sb.append( ',' );
                    sb.append(iccc.getColor(i));
                }
                sb.append( ')' );
                textChanged(sb.toString());
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR_ICCCOLOR:
                sb = new StringBuffer(getValue().item(0).getCssText());
                sb.append( ' ' );
                sb.append(getValue().item(1).getCssText());
                sb.append(" icc-color(");
                iccc = (ICCColor)getValue().item(1);
                sb.append(iccc.getColorProfile());
                for (int i = 0; i < idx; i++) {
                    sb.append( ',' );
                    sb.append(iccc.getColor(i));
                }
                sb.append( ',' );
                sb.append(f);
                for (int i = idx; i < iccc.getLength(); i++) {
                    sb.append( ',' );
                    sb.append(iccc.getColor(i));
                }
                sb.append( ')' );
                textChanged(sb.toString());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
        }
        public void colorReplaced(float f, int idx) throws DOMException {
            switch (getPaintType()) {
            case SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR:
                StringBuffer sb =
                    new StringBuffer(getValue().item(0).getCssText());
                sb.append(" icc-color(");
                ICCColor iccc = (ICCColor)getValue().item(1);
                sb.append(iccc.getColorProfile());
                for (int i = 0; i < idx; i++) {
                    sb.append( ',' );
                    sb.append(iccc.getColor(i));
                }
                sb.append( ',' );
                sb.append(f);
                for (int i = idx + 1; i < iccc.getLength(); i++) {
                    sb.append( ',' );
                    sb.append(iccc.getColor(i));
                }
                sb.append( ')' );
                textChanged(sb.toString());
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR_ICCCOLOR:
                sb = new StringBuffer(getValue().item(0).getCssText());
                sb.append( ' ' );
                sb.append(getValue().item(1).getCssText());
                sb.append(" icc-color(");
                iccc = (ICCColor)getValue().item(1);
                sb.append(iccc.getColorProfile());
                for (int i = 0; i < idx; i++) {
                    sb.append( ',' );
                    sb.append(iccc.getColor(i));
                }
                sb.append( ',' );
                sb.append(f);
                for (int i = idx + 1; i < iccc.getLength(); i++) {
                    sb.append( ',' );
                    sb.append(iccc.getColor(i));
                }
                sb.append( ')' );
                textChanged(sb.toString());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
        }
        public void colorRemoved(int idx) throws DOMException {
            switch (getPaintType()) {
            case SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR:
                StringBuffer sb =
                    new StringBuffer(getValue().item(0).getCssText());
                sb.append(" icc-color(");
                ICCColor iccc = (ICCColor)getValue().item(1);
                sb.append(iccc.getColorProfile());
                for (int i = 0; i < idx; i++) {
                    sb.append( ',' );
                    sb.append(iccc.getColor(i));
                }
                for (int i = idx + 1; i < iccc.getLength(); i++) {
                    sb.append( ',' );
                    sb.append(iccc.getColor(i));
                }
                sb.append( ')' );
                textChanged(sb.toString());
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR_ICCCOLOR:
                sb = new StringBuffer(getValue().item(0).getCssText());
                sb.append( ' ' );
                sb.append(getValue().item(1).getCssText());
                sb.append(" icc-color(");
                iccc = (ICCColor)getValue().item(1);
                sb.append(iccc.getColorProfile());
                for (int i = 0; i < idx; i++) {
                    sb.append( ',' );
                    sb.append(iccc.getColor(i));
                }
                for (int i = idx + 1; i < iccc.getLength(); i++) {
                    sb.append( ',' );
                    sb.append(iccc.getColor(i));
                }
                sb.append( ')' );
                textChanged(sb.toString());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
        }
        public void colorAppend(float f) throws DOMException {
            switch (getPaintType()) {
            case SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR:
                StringBuffer sb =
                    new StringBuffer(getValue().item(0).getCssText());
                sb.append(" icc-color(");
                ICCColor iccc = (ICCColor)getValue().item(1);
                sb.append(iccc.getColorProfile());
                for (int i = 0; i < iccc.getLength(); i++) {
                    sb.append( ',' );
                    sb.append(iccc.getColor(i));
                }
                sb.append( ',' );
                sb.append(f);
                sb.append( ')' );
                textChanged(sb.toString());
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR_ICCCOLOR:
                sb = new StringBuffer(getValue().item(0).getCssText());
                sb.append( ' ' );
                sb.append(getValue().item(1).getCssText());
                sb.append(" icc-color(");
                iccc = (ICCColor)getValue().item(1);
                sb.append(iccc.getColorProfile());
                for (int i = 0; i < iccc.getLength(); i++) {
                    sb.append( ',' );
                    sb.append(iccc.getColor(i));
                }
                sb.append( ',' );
                sb.append(f);
                sb.append( ')' );
                textChanged(sb.toString());
                break;
            default:
                throw new DOMException
                    (DOMException.NO_MODIFICATION_ALLOWED_ERR, "");
            }
        }
        public void uriChanged(String uri) {
            textChanged("url(" + uri + ") none");
        }
        public void paintChanged(short type, String uri,
                                 String rgb, String icc) {
            switch (type) {
            case SVG_PAINTTYPE_NONE:
                textChanged("none");
                break;
            case SVG_PAINTTYPE_CURRENTCOLOR:
                textChanged("currentcolor");
                break;
            case SVG_PAINTTYPE_RGBCOLOR:
                textChanged(rgb);
                break;
            case SVG_PAINTTYPE_RGBCOLOR_ICCCOLOR:
                textChanged(rgb + ' ' + icc);
                break;
            case SVG_PAINTTYPE_URI:
                textChanged("url(" + uri + ')' );
                break;
            case SVG_PAINTTYPE_URI_NONE:
                textChanged("url(" + uri + ") none");
                break;
            case SVG_PAINTTYPE_URI_CURRENTCOLOR:
                textChanged("url(" + uri + ") currentcolor");
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR:
                textChanged("url(" + uri + ") " + rgb);
                break;
            case SVG_PAINTTYPE_URI_RGBCOLOR_ICCCOLOR:
                textChanged("url(" + uri + ") " + rgb + ' ' + icc);
            }
        }
    }
}
