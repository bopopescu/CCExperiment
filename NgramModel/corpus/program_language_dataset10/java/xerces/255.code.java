package org.apache.xerces.dom.events;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MouseEvent;
import org.w3c.dom.views.AbstractView;
public class MouseEventImpl 
    extends UIEventImpl 
    implements MouseEvent {
    private int fScreenX;
    private int fScreenY;
    private int fClientX;
    private int fClientY;
    private boolean fCtrlKey;
    private boolean fAltKey;
    private boolean fShiftKey;
    private boolean fMetaKey;
    private short fButton;
    private EventTarget fRelatedTarget;
    public int getScreenX() {
        return fScreenX;
    }
    public int getScreenY() {
        return fScreenY;
    }
    public int getClientX() {
        return fClientX;
    }
    public int getClientY() {
        return fClientY;
    }
    public boolean getCtrlKey() {
        return fCtrlKey;
    }
    public boolean getAltKey() {
        return fAltKey;
    }
    public boolean getShiftKey() {
        return fShiftKey;
    }
    public boolean getMetaKey() {
        return fMetaKey;
    }
    public short getButton() {
        return fButton;
    }
    public EventTarget getRelatedTarget() {
        return fRelatedTarget;
    }
    public void initMouseEvent(String typeArg, boolean canBubbleArg, boolean cancelableArg, AbstractView viewArg, 
            int detailArg, int screenXArg, int screenYArg, int clientXArg, int clientYArg, 
            boolean ctrlKeyArg, boolean altKeyArg, boolean shiftKeyArg, boolean metaKeyArg, 
            short buttonArg, EventTarget relatedTargetArg) {
        fScreenX = screenXArg;
        fScreenY = screenYArg;
        fClientX = clientXArg;
        fClientY = clientYArg;
        fCtrlKey = ctrlKeyArg;
        fAltKey = altKeyArg;
        fShiftKey = shiftKeyArg;
        fMetaKey = metaKeyArg;
        fButton = buttonArg;
        fRelatedTarget = relatedTargetArg;
        super.initUIEvent(typeArg, canBubbleArg, cancelableArg, viewArg, detailArg);
    }
}
