package org.apache.batik.dom.xbl;
import org.w3c.dom.events.Event;
public interface ShadowTreeEvent extends Event {
    XBLShadowTreeElement getXblShadowTree();
    void initShadowTreeEvent(String typeArg,
                             boolean canBubbleArg,
                             boolean cancelableArg,
                             XBLShadowTreeElement xblShadowTreeArg);
    void initShadowTreeEventNS(String namespaceURIArg,
                               String typeArg,
                               boolean canBubbleArg,
                               boolean cancelableArg,
                               XBLShadowTreeElement xblShadowTreeArg);
}
