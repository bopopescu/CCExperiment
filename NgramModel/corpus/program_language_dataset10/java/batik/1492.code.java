package org.apache.batik.dom;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
public class EventTargetAddEventListenerNSTest extends DOM3Test {
    static class Listener1 implements EventListener {
        int count = 0;
        public void handleEvent(Event e) {
            count++;
        }
        int getCount() {
            int c = count;
            count = 0;
            return c;
        }
    }
    static class Listener2 implements EventListener {
        int count = 0;
        public void handleEvent(Event e) {
            count++;
            e.stopPropagation();
        }
        int getCount() {
            int c = count;
            count = 0;
            return c;
        }
    }
    public boolean runImplBasic() throws Exception {
        Listener1 l1 = new Listener1();
        Listener2 l2 = new Listener2();
        Document doc = newDoc();
        Element e = doc.createElementNS(null, "test");
        AbstractNode et = (AbstractNode) e;
        doc.appendChild(e);
        et.addEventListenerNS(XML_EVENTS_NAMESPACE_URI, "DOMAttrModified", l1, false, null);
        et.addEventListenerNS(null, "DOMAttrModified", l1, false, null);
        e.setAttributeNS(null, "test", "abc");
        boolean pass = l1.getCount() == 2;
        et.addEventListenerNS(XML_EVENTS_NAMESPACE_URI, "DOMAttrModified", l2, false, "g1");
        et.addEventListenerNS(null, "DOMAttrModified", l2, false, "g1");
        e.setAttributeNS(null, "test", "def");
        pass = pass && l2.getCount() == 2;
        return pass;
    }
}
