package org.apache.batik.gvt.event;
import java.util.EventListener;
public interface GraphicsNodeChangeListener extends EventListener {
    void changeStarted(GraphicsNodeChangeEvent gnce);
    void changeCompleted(GraphicsNodeChangeEvent gnce);
}
