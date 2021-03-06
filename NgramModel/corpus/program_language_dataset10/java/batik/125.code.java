package org.apache.batik.bridge;
import org.apache.batik.css.engine.CSSEngineEvent;
import org.apache.batik.dom.svg.AnimatedLiveAttributeValue;
import org.w3c.dom.events.MutationEvent;
public interface BridgeUpdateHandler {
    void handleDOMAttrModifiedEvent(MutationEvent evt);
    void handleDOMNodeInsertedEvent(MutationEvent evt);
    void handleDOMNodeRemovedEvent(MutationEvent evt);
    void handleDOMCharacterDataModified(MutationEvent evt);
    void handleCSSEngineEvent(CSSEngineEvent evt);
    void handleAnimatedAttributeChanged(AnimatedLiveAttributeValue alav);
    void handleOtherAnimationChanged(String type);
    void dispose();
}
