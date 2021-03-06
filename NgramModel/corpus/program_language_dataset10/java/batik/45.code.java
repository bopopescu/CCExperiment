package org.apache.batik.anim.values;
import org.apache.batik.dom.anim.AnimationTarget;
public class AnimatableMotionPointValue extends AnimatableValue {
    protected float x;
    protected float y;
    protected float angle;
    protected AnimatableMotionPointValue(AnimationTarget target) {
        super(target);
    }
    public AnimatableMotionPointValue(AnimationTarget target, float x, float y,
                                     float angle) {
        super(target);
        this.x = x;
        this.y = y;
        this.angle = angle;
    }
    public AnimatableValue interpolate(AnimatableValue result,
                                       AnimatableValue to,
                                       float interpolation,
                                       AnimatableValue accumulation,
                                       int multiplier) {
        AnimatableMotionPointValue res;
        if (result == null) {
            res = new AnimatableMotionPointValue(target);
        } else {
            res = (AnimatableMotionPointValue) result;
        }
        float newX = x, newY = y, newAngle = angle;
        int angleCount = 1;
        if (to != null) {
            AnimatableMotionPointValue toValue =
                (AnimatableMotionPointValue) to;
            newX += interpolation * (toValue.x - x);
            newY += interpolation * (toValue.y - y);
            newAngle += toValue.angle;
            angleCount++;
        }
        if (accumulation != null && multiplier != 0) {
            AnimatableMotionPointValue accValue =
                (AnimatableMotionPointValue) accumulation;
            newX += multiplier * accValue.x;
            newY += multiplier * accValue.y;
            newAngle += accValue.angle;
            angleCount++;
        }
        newAngle /= angleCount;
        if (res.x != newX || res.y != newY || res.angle != newAngle) {
            res.x = newX;
            res.y = newY;
            res.angle = newAngle;
            res.hasChanged = true;
        }
        return res;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public float getAngle() {
        return angle;
    }
    public boolean canPace() {
        return true;
    }
    public float distanceTo(AnimatableValue other) {
        AnimatableMotionPointValue o = (AnimatableMotionPointValue) other;
        float dx = x - o.x;
        float dy = y - o.y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
    public AnimatableValue getZeroValue() {
        return new AnimatableMotionPointValue(target, 0f, 0f, 0f);
    }
    public String toStringRep() {
        StringBuffer sb = new StringBuffer();
        sb.append(formatNumber(x));
        sb.append(',');
        sb.append(formatNumber(y));
        sb.append(',');
        sb.append(formatNumber(angle));
        sb.append("rad");
        return sb.toString();
    }
}
