package org.apache.batik.gvt.event;
import java.util.EventListener;
public interface GraphicsNodeFocusListener extends EventListener {
    void focusGained(GraphicsNodeFocusEvent evt);
    void focusLost(GraphicsNodeFocusEvent evt);
}
