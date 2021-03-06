package org.apache.batik.gvt.flow;
public class MarginInfo {
    public static final int ALIGN_START  = 0;
    public static final int ALIGN_MIDDLE = 1;
    public static final int ALIGN_END    = 2;
    public static final int ALIGN_FULL   = 3;
    protected float   top;
    protected float   right;
    protected float   bottom;
    protected float   left;
    protected float   indent;
    protected int     alignment;
    protected float   lineHeight;
    protected boolean fontSizeRelative;
    protected boolean flowRegionBreak;
    public MarginInfo(float top, float right, float bottom, float left,
                      float indent, int alignment, float lineHeight,
                      boolean fontSizeRelative, boolean flowRegionBreak) {
        this.top    = top;
        this.right  = right;
        this.bottom = bottom;
        this.left   = left;
        this.indent = indent;
        this.alignment = alignment;
        this.lineHeight = lineHeight;
        this.fontSizeRelative = fontSizeRelative;
        this.flowRegionBreak = flowRegionBreak;
    }
    public MarginInfo(float margin, int alignment) {
        setMargin(margin);
        this.indent = 0;
        this.alignment = alignment;
        this.flowRegionBreak = false;
    }
    public void setMargin(float margin) {
        this.top    = margin;
        this.right  = margin;
        this.bottom = margin;
        this.left   = margin;
    }
    public float   getTopMargin()       { return top; }
    public float   getRightMargin()     { return right; }
    public float   getBottomMargin()    { return bottom; }
    public float   getLeftMargin()      { return left; }
    public float   getIndent()          { return indent; }
    public int     getTextAlignment()   { return alignment; }
    public float   getLineHeight()      { return lineHeight; }
    public boolean isFontSizeRelative() { return fontSizeRelative; }
    public boolean isFlowRegionBreak()  { return flowRegionBreak; }
}
