package org.apache.batik.anim.values;
import org.apache.batik.dom.anim.AnimationTarget;
import org.w3c.dom.svg.SVGLength;
public class AnimatableLengthOrIdentValue extends AnimatableLengthValue {
    protected boolean isIdent;
    protected String ident;
    protected AnimatableLengthOrIdentValue(AnimationTarget target) {
        super(target);
    }
    public AnimatableLengthOrIdentValue(AnimationTarget target, short type,
                                        float v, short pcInterp) {
        super(target, type, v, pcInterp);
    }
    public AnimatableLengthOrIdentValue(AnimationTarget target, String ident) {
        super(target);
        this.ident = ident;
        this.isIdent = true;
    }
    public boolean isIdent() {
        return isIdent;
    }
    public String getIdent() {
        return ident;
    }
    public boolean canPace() {
        return false;
    }
    public float distanceTo(AnimatableValue other) {
        return 0f;
    }
    public AnimatableValue getZeroValue() {
        return new AnimatableLengthOrIdentValue
            (target, SVGLength.SVG_LENGTHTYPE_NUMBER, 0f,
             percentageInterpretation);
    }
    public String getCssText() {
        if (isIdent) {
            return ident;
        }
        return super.getCssText();
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to, float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        AnimatableLengthOrIdentValue res;
        if (result == null) {
            res = new AnimatableLengthOrIdentValue(target);
        } else {
            res = (AnimatableLengthOrIdentValue) result;
        }
        if (to == null) {
            if (isIdent) {
                res.hasChanged = !res.isIdent || !res.ident.equals(ident);
                res.ident = ident;
                res.isIdent = true;
            } else {
                short oldLengthType = res.lengthType;
                float oldLengthValue = res.lengthValue;
                short oldPercentageInterpretation = res.percentageInterpretation;
                super.interpolate(res, to, interpolation, accumulation,
                                  multiplier);
                if (res.lengthType != oldLengthType
                        || res.lengthValue != oldLengthValue
                        || res.percentageInterpretation
                            != oldPercentageInterpretation) {
                    res.hasChanged = true;
                }
            }
        } else {
            AnimatableLengthOrIdentValue toValue
                = (AnimatableLengthOrIdentValue) to;
            if (isIdent || toValue.isIdent) {
                if (interpolation >= 0.5) {
                    if (res.isIdent != toValue.isIdent
                            || res.lengthType != toValue.lengthType
                            || res.lengthValue != toValue.lengthValue
                            || res.isIdent && toValue.isIdent
                                && !toValue.ident.equals(ident)) {
                        res.isIdent = toValue.isIdent;
                        res.ident = toValue.ident;
                        res.lengthType = toValue.lengthType;
                        res.lengthValue = toValue.lengthValue;
                        res.hasChanged = true;
                    }
                } else {
                    if (res.isIdent != isIdent
                            || res.lengthType != lengthType
                            || res.lengthValue != lengthValue
                            || res.isIdent && isIdent
                                && !res.ident.equals(ident)) {
                        res.isIdent = isIdent;
                        res.ident = ident;
                        res.ident = ident;
                        res.lengthType = lengthType;
                        res.hasChanged = true;
                    }
                }
            } else {
                super.interpolate(res, to, interpolation, accumulation,
                                  multiplier);
            }
        }
        return res;
    }
}
