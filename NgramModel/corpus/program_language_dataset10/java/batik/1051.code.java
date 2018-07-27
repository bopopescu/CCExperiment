package org.apache.batik.gvt.text;
public class TextHit {
    private int charIndex;
    private boolean leadingEdge;
    public TextHit(int charIndex, boolean leadingEdge) {
        this.charIndex = charIndex;
        this.leadingEdge = leadingEdge;
    }
    public int getCharIndex() {
        return charIndex;
    }
    public boolean isLeadingEdge() {
        return leadingEdge;
    }
}
