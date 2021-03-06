package org.apache.batik.anim.values;
import org.apache.batik.dom.anim.AnimationTarget;
import org.apache.batik.util.SVGConstants;
import org.w3c.dom.svg.SVGPreserveAspectRatio;
public class AnimatablePreserveAspectRatioValue extends AnimatableValue {
    protected static final String[] ALIGN_VALUES = {
        null,
        SVGConstants.SVG_NONE_VALUE,
        SVGConstants.SVG_XMINYMIN_VALUE,
        SVGConstants.SVG_XMIDYMIN_VALUE,
        SVGConstants.SVG_XMAXYMIN_VALUE,
        SVGConstants.SVG_XMINYMID_VALUE,
        SVGConstants.SVG_XMIDYMID_VALUE,
        SVGConstants.SVG_XMAXYMID_VALUE,
        SVGConstants.SVG_XMINYMAX_VALUE,
        SVGConstants.SVG_XMIDYMAX_VALUE,
        SVGConstants.SVG_XMAXYMAX_VALUE
    };
    protected static final String[] MEET_OR_SLICE_VALUES = {
        null,
        SVGConstants.SVG_MEET_VALUE,
        SVGConstants.SVG_SLICE_VALUE
    };
    protected short align;
    protected short meetOrSlice;
    protected AnimatablePreserveAspectRatioValue(AnimationTarget target) {
        super(target);
    }
    public AnimatablePreserveAspectRatioValue(AnimationTarget target,
                                              short align, short meetOrSlice) {
        super(target);
        this.align = align;
        this.meetOrSlice = meetOrSlice;
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to, float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        AnimatablePreserveAspectRatioValue res;
        if (result == null) {
            res = new AnimatablePreserveAspectRatioValue(target);
        } else {
            res = (AnimatablePreserveAspectRatioValue) result;
        }
        short newAlign, newMeetOrSlice;
        if (to != null && interpolation >= 0.5) {
            AnimatablePreserveAspectRatioValue toValue =
                (AnimatablePreserveAspectRatioValue) to;
            newAlign = toValue.align;
            newMeetOrSlice = toValue.meetOrSlice;
        } else {
            newAlign = align;
            newMeetOrSlice = meetOrSlice;
        }
        if (res.align != newAlign || res.meetOrSlice != newMeetOrSlice) {
            res.align = align;
            res.meetOrSlice = meetOrSlice;
            res.hasChanged = true;
        }
        return res;
    }
    public short getAlign() {
        return align;
    }
    public short getMeetOrSlice() {
        return meetOrSlice;
    }
    public boolean canPace() {
        return false;
    }
    public float distanceTo(AnimatableValue other) {
        return 0f;
    }
    public AnimatableValue getZeroValue() {
        return new AnimatablePreserveAspectRatioValue
            (target, SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_NONE,
             SVGPreserveAspectRatio.SVG_MEETORSLICE_MEET);
    }
    public String toStringRep() {
        if (align < 1 || align > 10) {
            return null;
        }
        String value = ALIGN_VALUES[align];
        if (align == SVGPreserveAspectRatio.SVG_PRESERVEASPECTRATIO_NONE) {
            return value;
        }
        if (meetOrSlice < 1 || meetOrSlice > 2) {
            return null;
        }
        return value + ' ' + MEET_OR_SLICE_VALUES[meetOrSlice];
    }
}
