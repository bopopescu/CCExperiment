package org.apache.batik.gvt.event;
import java.awt.event.InputEvent;
import org.apache.batik.gvt.GraphicsNode;
public abstract class GraphicsNodeInputEvent extends GraphicsNodeEvent {
    public static final int SHIFT_MASK = InputEvent.SHIFT_MASK;
    public static final int CTRL_MASK = InputEvent.CTRL_MASK;
    public static final int META_MASK = InputEvent.META_MASK;
    public static final int ALT_MASK = InputEvent.ALT_MASK;
    public static final int ALT_GRAPH_MASK = InputEvent.ALT_GRAPH_MASK;
    public static final int BUTTON1_MASK = 1 << 10; 
    public static final int BUTTON2_MASK = 1 << 11; 
    public static final int BUTTON3_MASK = 1 << 12; 
    public static final int CAPS_LOCK_MASK = 0x01;
    public static final int NUM_LOCK_MASK = 0x02;
    public static final int SCROLL_LOCK_MASK = 0x04;
    public static final int KANA_LOCK_MASK = 0x08;
    long when;
    int modifiers;
    int lockState;
    protected GraphicsNodeInputEvent(GraphicsNode source, int id,
                                     long when, int modifiers, int lockState) {
        super(source, id);
        this.when = when;
        this.modifiers = modifiers;
        this.lockState = lockState;
    }
    protected GraphicsNodeInputEvent(GraphicsNode source,
                                     InputEvent evt,
                                     int lockState) {
        super(source, evt.getID());
        this.when = evt.getWhen();
        this.modifiers = evt.getModifiers();
        this.lockState = lockState;
    }
    public boolean isShiftDown() {
        return (modifiers & SHIFT_MASK) != 0;
    }
    public boolean isControlDown() {
        return (modifiers & CTRL_MASK) != 0;
    }
    public boolean isMetaDown() {
        return AWTEventDispatcher.isMetaDown(modifiers);
    }
    public boolean isAltDown() {
        return (modifiers & ALT_MASK) != 0;
    }
    public boolean isAltGraphDown() {
        return (modifiers & ALT_GRAPH_MASK) != 0;
    }
    public long getWhen() {
        return when;
    }
    public int getModifiers() {
        return modifiers;
    }
    public int getLockState() {
        return lockState;
    }
}
