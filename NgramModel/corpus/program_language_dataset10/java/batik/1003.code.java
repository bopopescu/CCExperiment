package org.apache.batik.gvt.flow;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.gvt.TextPainter;
public class FlowTextNode extends TextNode{
    public FlowTextNode() {
        textPainter = FlowTextPainter.getInstance();
    }
    public void setTextPainter(TextPainter textPainter) {
        if (textPainter == null)
            this.textPainter = FlowTextPainter.getInstance();
        else
            this.textPainter = textPainter;
    }
}
