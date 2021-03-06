package org.apache.batik.dom.events;
import java.util.HashSet;
import java.util.Iterator;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.events.MouseEvent;
import org.w3c.dom.views.AbstractView;
public class DOMMouseEvent extends DOMUIEvent implements MouseEvent {
    private int screenX;
    private int screenY;
    private int clientX;
    private int clientY;
    private short button;
    private EventTarget relatedTarget;
    protected HashSet modifierKeys = new HashSet();
    public int getScreenX() {
        return screenX;
    }
    public int getScreenY() {
        return screenY;
    }
    public int getClientX() {
        return clientX;
    }
    public int getClientY() {
        return clientY;
    }
    public boolean getCtrlKey() {
        return modifierKeys.contains(DOMKeyboardEvent.KEY_CONTROL);
    }
    public boolean getShiftKey() {
        return modifierKeys.contains(DOMKeyboardEvent.KEY_SHIFT);
    }
    public boolean getAltKey() {
        return modifierKeys.contains(DOMKeyboardEvent.KEY_ALT);
    }
    public boolean getMetaKey() {
        return modifierKeys.contains(DOMKeyboardEvent.KEY_META);
    }
    public short getButton() {
        return button;
    }
    public EventTarget getRelatedTarget() {
        return relatedTarget;
    }
    public boolean getModifierState(String keyIdentifierArg) {
        return modifierKeys.contains(keyIdentifierArg);
    }
    public String getModifiersString() {
        if (modifierKeys.isEmpty()) {
            return "";
        }
        StringBuffer sb = new StringBuffer(modifierKeys.size() * 8);
        Iterator i = modifierKeys.iterator();
        sb.append((String) i.next());
        while (i.hasNext()) {
             sb.append(' ');
             sb.append((String) i.next());
        }
        return sb.toString();
    }
    public void initMouseEvent(String typeArg,
                               boolean canBubbleArg,
                               boolean cancelableArg,
                               AbstractView viewArg,
                               int detailArg,
                               int screenXArg,
                               int screenYArg,
                               int clientXArg,
                               int clientYArg,
                               boolean ctrlKeyArg,
                               boolean altKeyArg,
                               boolean shiftKeyArg,
                               boolean metaKeyArg,
                               short buttonArg,
                               EventTarget relatedTargetArg) {
        initUIEvent(typeArg, canBubbleArg, cancelableArg,
                    viewArg, detailArg);
        this.screenX = screenXArg;
        this.screenY = screenYArg;
        this.clientX = clientXArg;
        this.clientY = clientYArg;
        if (ctrlKeyArg) {
            modifierKeys.add(DOMKeyboardEvent.KEY_CONTROL);
        }
        if (altKeyArg) {
            modifierKeys.add(DOMKeyboardEvent.KEY_ALT);
        }
        if (shiftKeyArg) {
            modifierKeys.add(DOMKeyboardEvent.KEY_SHIFT);
        }
        if (metaKeyArg) {
            modifierKeys.add(DOMKeyboardEvent.KEY_META);
        }
        this.button = buttonArg;
        this.relatedTarget = relatedTargetArg;
    }
    public void initMouseEventNS(String namespaceURIArg,
                                 String typeArg,
                                 boolean canBubbleArg,
                                 boolean cancelableArg,
                                 AbstractView viewArg,
                                 int detailArg,
                                 int screenXArg,
                                 int screenYArg,
                                 int clientXArg,
                                 int clientYArg,
                                 short buttonArg,
                                 EventTarget relatedTargetArg,
                                 String modifiersList) {
        initUIEventNS(namespaceURIArg,
                      typeArg,
                      canBubbleArg,
                      cancelableArg,
                      viewArg,
                      detailArg);
        screenX = screenXArg;
        screenY = screenYArg;
        clientX = clientXArg;
        clientY = clientYArg;
        button = buttonArg;
        relatedTarget = relatedTargetArg;
        modifierKeys.clear();
        String[] modifiers = split(modifiersList);
        for (int i = 0; i < modifiers.length; i++) {
            modifierKeys.add(modifiers[i]);
        }
    }
}
