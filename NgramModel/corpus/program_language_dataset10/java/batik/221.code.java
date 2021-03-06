package org.apache.batik.bridge;
import org.apache.batik.anim.AbstractAnimation;
import org.apache.batik.dom.anim.AnimationTarget;
import org.apache.batik.anim.SetAnimation;
import org.apache.batik.anim.values.AnimatableValue;
public class SVGSetElementBridge extends SVGAnimationElementBridge {
    public String getLocalName() {
        return SVG_SET_TAG;
    }
    public Bridge getInstance() {
        return new SVGSetElementBridge();
    }
    protected AbstractAnimation createAnimation(AnimationTarget target) {
        AnimatableValue to = parseAnimatableValue(SVG_TO_ATTRIBUTE);
        return new SetAnimation(timedElement, this, to);
    }
    protected boolean canAnimateType(int type) {
        return true;
    }
    protected boolean isConstantAnimation() {
        return true;
    }
}
