package org.apache.batik.util.gui.xmleditor;
public class XMLToken {
    private int context;
    private int startOffset;
    private int endOffset;
    public XMLToken(int context, int startOffset, int endOffset) {
        this.context = context;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
    }
    public int getContext() {
        return context;
    }
    public int getStartOffset() {
        return startOffset;
    }
    public int getEndOffset() {
        return endOffset;
    }
}
