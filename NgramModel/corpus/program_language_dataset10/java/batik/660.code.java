package org.apache.batik.dom.svg;
import org.apache.batik.dom.anim.AnimationTarget;
public class TraitInformation {
    public static final short PERCENTAGE_FONT_SIZE       = AnimationTarget.PERCENTAGE_FONT_SIZE;
    public static final short PERCENTAGE_VIEWPORT_WIDTH  = AnimationTarget.PERCENTAGE_VIEWPORT_WIDTH;
    public static final short PERCENTAGE_VIEWPORT_HEIGHT = AnimationTarget.PERCENTAGE_VIEWPORT_HEIGHT;
    public static final short PERCENTAGE_VIEWPORT_SIZE   = AnimationTarget.PERCENTAGE_VIEWPORT_SIZE;
    protected boolean isAnimatable;
    protected int type;
    protected short percentageInterpretation;
    public TraitInformation(boolean isAnimatable, 
                            int type, short percentageInterpretation) {
        this.isAnimatable = isAnimatable;
        this.type = type;
        this.percentageInterpretation = percentageInterpretation;
    }
    public TraitInformation(boolean isAnimatable, 
                            int type) {
        this.isAnimatable = isAnimatable;
        this.type = type;
        this.percentageInterpretation = -1;
    }
    public boolean isAnimatable() {
        return isAnimatable;
    }
    public int getType() {
        return type;
    }
    public short getPercentageInterpretation() {
        return percentageInterpretation;
    }
}
