package org.apache.batik.dom.events;
import org.w3c.dom.events.TextEvent;
import org.w3c.dom.views.AbstractView;
public class DOMTextEvent extends DOMUIEvent implements TextEvent {
    protected String data;
    public String getData() {
        return data;
    }
    public void initTextEvent(String typeArg, 
                              boolean canBubbleArg, 
                              boolean cancelableArg, 
                              AbstractView viewArg, 
                              String dataArg) {
        initUIEvent(typeArg, canBubbleArg, cancelableArg, viewArg, 0);
        data = dataArg;
    }
    public void initTextEventNS(String namespaceURIArg,
                                String typeArg, 
                                boolean canBubbleArg, 
                                boolean cancelableArg, 
                                AbstractView viewArg, 
                                String dataArg) {
        initUIEventNS(namespaceURIArg,
                      typeArg,
                      canBubbleArg,
                      cancelableArg,
                      viewArg,
                      0);
        data = dataArg;
    }
}
