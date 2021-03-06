package org.apache.batik.anim.values;
import org.apache.batik.dom.anim.AnimationTarget;
public class AnimatableNumberOrPercentageValue extends AnimatableNumberValue {
    protected boolean isPercentage;
    protected AnimatableNumberOrPercentageValue(AnimationTarget target) {
        super(target);
    }
    public AnimatableNumberOrPercentageValue(AnimationTarget target, float n) {
        super(target, n);
    }
    public AnimatableNumberOrPercentageValue(AnimationTarget target, float n,
                                             boolean isPercentage) {
        super(target, n);
        this.isPercentage = isPercentage;
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to,
                                       float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        AnimatableNumberOrPercentageValue res;
        if (result == null) {
            res = new AnimatableNumberOrPercentageValue(target);
        } else {
            res = (AnimatableNumberOrPercentageValue) result;
        }
        float newValue;
        boolean newIsPercentage;
        AnimatableNumberOrPercentageValue toValue
            = (AnimatableNumberOrPercentageValue) to;
        AnimatableNumberOrPercentageValue accValue
            = (AnimatableNumberOrPercentageValue) accumulation;
        if (to != null) {
            if (toValue.isPercentage == isPercentage) {
                newValue = value + interpolation * (toValue.value - value);
                newIsPercentage = isPercentage;
            } else {
                if (interpolation >= 0.5) {
                    newValue = toValue.value;
                    newIsPercentage = toValue.isPercentage;
                } else {
                    newValue = value;
                    newIsPercentage = isPercentage;
                }
            }
        } else {
            newValue = value;
            newIsPercentage = isPercentage;
        }
        if (accumulation != null && accValue.isPercentage == newIsPercentage) {
            newValue += multiplier * accValue.value;
        }
        if (res.value != newValue
                || res.isPercentage != newIsPercentage) {
            res.value = newValue;
            res.isPercentage = newIsPercentage;
            res.hasChanged = true;
        }
        return res;
    }
    public boolean isPercentage() {
        return isPercentage;
    }
    public boolean canPace() {
        return false;
    }
    public float distanceTo(AnimatableValue other) {
        return 0f;
    }
    public AnimatableValue getZeroValue() {
        return new AnimatableNumberOrPercentageValue(target, 0, isPercentage);
    }
    public String getCssText() {
        StringBuffer sb = new StringBuffer();
        sb.append(formatNumber(value));
        if (isPercentage) {
            sb.append('%');
        }
        return sb.toString();
    }
}
