package org.apache.batik.anim;
import org.apache.batik.anim.timing.TimedElement;
import org.apache.batik.anim.values.AnimatableValue;
import org.apache.batik.anim.values.AnimatableTransformListValue;
import org.apache.batik.dom.anim.AnimatableElement;
import org.w3c.dom.svg.SVGTransform;
public class TransformAnimation extends SimpleAnimation {
    protected short type;
    protected float[] keyTimes2;
    protected float[] keyTimes3;
    public TransformAnimation(TimedElement timedElement,
                              AnimatableElement animatableElement,
                              int calcMode,
                              float[] keyTimes,
                              float[] keySplines,
                              boolean additive,
                              boolean cumulative,
                              AnimatableValue[] values,
                              AnimatableValue from,
                              AnimatableValue to,
                              AnimatableValue by,
                              short type) {
        super(timedElement, animatableElement,
              calcMode == CALC_MODE_PACED ? CALC_MODE_LINEAR : calcMode,
              calcMode == CALC_MODE_PACED ? null : keyTimes,
              keySplines, additive, cumulative, values, from, to, by);
        this.calcMode = calcMode;
        this.type = type;
        if (calcMode != CALC_MODE_PACED) {
            return;
        }
        int count = this.values.length;
        float[] cumulativeDistances1;
        float[] cumulativeDistances2 = null;
        float[] cumulativeDistances3 = null;
        switch (type) {
            case SVGTransform.SVG_TRANSFORM_ROTATE:
                cumulativeDistances3 = new float[count];
                cumulativeDistances3[0] = 0f;
            case SVGTransform.SVG_TRANSFORM_SCALE:
            case SVGTransform.SVG_TRANSFORM_TRANSLATE:
                cumulativeDistances2 = new float[count];
                cumulativeDistances2[0] = 0f;
            default:
                cumulativeDistances1 = new float[count];
                cumulativeDistances1[0] = 0f;
        }
        for (int i = 1; i < this.values.length; i++) {
            switch (type) {
                case SVGTransform.SVG_TRANSFORM_ROTATE:
                    cumulativeDistances3[i] =
                        cumulativeDistances3[i - 1]
                            + ((AnimatableTransformListValue)
                                this.values[i - 1]).distanceTo3(this.values[i]);
                case SVGTransform.SVG_TRANSFORM_SCALE:
                case SVGTransform.SVG_TRANSFORM_TRANSLATE:
                    cumulativeDistances2[i] =
                        cumulativeDistances2[i - 1]
                            + ((AnimatableTransformListValue)
                                this.values[i - 1]).distanceTo2(this.values[i]);
                default:
                    cumulativeDistances1[i] =
                        cumulativeDistances1[i - 1]
                            + ((AnimatableTransformListValue)
                                this.values[i - 1]).distanceTo1(this.values[i]);
            }
        }
        switch (type) {
            case SVGTransform.SVG_TRANSFORM_ROTATE:
                float totalLength = cumulativeDistances3[count - 1];
                keyTimes3 = new float[count];
                keyTimes3[0] = 0f;
                for (int i = 1; i < count - 1; i++) {
                    keyTimes3[i] = cumulativeDistances3[i] / totalLength;
                }
                keyTimes3[count - 1] = 1f;
            case SVGTransform.SVG_TRANSFORM_SCALE:
            case SVGTransform.SVG_TRANSFORM_TRANSLATE:
                totalLength = cumulativeDistances2[count - 1];
                keyTimes2 = new float[count];
                keyTimes2[0] = 0f;
                for (int i = 1; i < count - 1; i++) {
                    keyTimes2[i] = cumulativeDistances2[i] / totalLength;
                }
                keyTimes2[count - 1] = 1f;
            default:
                totalLength = cumulativeDistances1[count - 1];
                this.keyTimes = new float[count];
                this.keyTimes[0] = 0f;
                for (int i = 1; i < count - 1; i++) {
                    this.keyTimes[i] = cumulativeDistances1[i] / totalLength;
                }
                this.keyTimes[count - 1] = 1f;
        }
    }
    protected void sampledAtUnitTime(float unitTime, int repeatIteration) {
        if (calcMode != CALC_MODE_PACED
                || type == SVGTransform.SVG_TRANSFORM_SKEWX
                || type == SVGTransform.SVG_TRANSFORM_SKEWY) {
            super.sampledAtUnitTime(unitTime, repeatIteration);
            return;
        }
        AnimatableTransformListValue
            value1, value2, value3 = null, nextValue1, nextValue2,
            nextValue3 = null, accumulation;
        float interpolation1 = 0f, interpolation2 = 0f, interpolation3 = 0f;
        if (unitTime != 1) {
            switch (type) {
                case SVGTransform.SVG_TRANSFORM_ROTATE:
                    int keyTimeIndex = 0;
                    while (keyTimeIndex < keyTimes3.length - 1
                            && unitTime >= keyTimes3[keyTimeIndex + 1]) {
                        keyTimeIndex++;
                    }
                    value3 = (AnimatableTransformListValue)
                        this.values[keyTimeIndex];
                    nextValue3 = (AnimatableTransformListValue)
                        this.values[keyTimeIndex + 1];
                    interpolation3 = (unitTime - keyTimes3[keyTimeIndex])
                        / (keyTimes3[keyTimeIndex + 1] -
                                keyTimes3[keyTimeIndex]);
                default:
                    keyTimeIndex = 0;
                    while (keyTimeIndex < keyTimes2.length - 1
                            && unitTime >= keyTimes2[keyTimeIndex + 1]) {
                        keyTimeIndex++;
                    }
                    value2 = (AnimatableTransformListValue)
                        this.values[keyTimeIndex];
                    nextValue2 = (AnimatableTransformListValue)
                        this.values[keyTimeIndex + 1];
                    interpolation2 = (unitTime - keyTimes2[keyTimeIndex])
                        / (keyTimes2[keyTimeIndex + 1] -
                                keyTimes2[keyTimeIndex]);
                    keyTimeIndex = 0;
                    while (keyTimeIndex < keyTimes.length - 1
                            && unitTime >= keyTimes[keyTimeIndex + 1]) {
                        keyTimeIndex++;
                    }
                    value1 = (AnimatableTransformListValue)
                        this.values[keyTimeIndex];
                    nextValue1 = (AnimatableTransformListValue)
                        this.values[keyTimeIndex + 1];
                    interpolation1 = (unitTime - keyTimes[keyTimeIndex])
                        / (keyTimes[keyTimeIndex + 1] -
                                keyTimes[keyTimeIndex]);
            }
        } else {
            value1 = value2 = value3 = (AnimatableTransformListValue)
                this.values[this.values.length - 1];
            nextValue1 = nextValue2 = nextValue3 = null;
            interpolation1 = interpolation2 = interpolation3 = 1f;
        }
        if (cumulative) {
            accumulation = (AnimatableTransformListValue)
                this.values[this.values.length - 1];
        } else {
            accumulation = null;
        }
        switch (type) {
            case SVGTransform.SVG_TRANSFORM_ROTATE:
                this.value = AnimatableTransformListValue.interpolate
                    ((AnimatableTransformListValue) this.value, value1, value2,
                     value3, nextValue1, nextValue2, nextValue3, interpolation1,
                     interpolation2, interpolation3, accumulation,
                     repeatIteration);
                break;
            default:
                this.value = AnimatableTransformListValue.interpolate
                    ((AnimatableTransformListValue) this.value, value1, value2,
                     nextValue1, nextValue2, interpolation1, interpolation2,
                     accumulation, repeatIteration);
                break;
        }
        if (this.value.hasChanged()) {
            markDirty();
        }
    }
}
