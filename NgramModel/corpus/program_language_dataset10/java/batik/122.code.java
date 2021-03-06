package org.apache.batik.bridge;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.ref.SoftReference;
import java.text.AttributedCharacterIterator;
import java.util.List;
import org.apache.batik.dom.events.DOMKeyEvent;
import org.apache.batik.dom.events.DOMMouseEvent;
import org.apache.batik.dom.events.NodeEventTarget;
import org.apache.batik.dom.util.DOMUtilities;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.TextNode;
import org.apache.batik.gvt.event.EventDispatcher;
import org.apache.batik.gvt.event.GraphicsNodeKeyEvent;
import org.apache.batik.gvt.event.GraphicsNodeKeyListener;
import org.apache.batik.gvt.event.GraphicsNodeMouseEvent;
import org.apache.batik.gvt.event.GraphicsNodeMouseListener;
import org.apache.batik.gvt.renderer.StrokingTextPainter;
import org.apache.batik.gvt.text.GVTAttributedCharacterIterator;
import org.apache.batik.gvt.text.TextHit;
import org.apache.batik.gvt.text.TextSpanLayout;
import org.apache.batik.util.SVGConstants;
import org.apache.batik.util.XMLConstants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;
public abstract class BridgeEventSupport implements SVGConstants {
    public static final
        AttributedCharacterIterator.Attribute TEXT_COMPOUND_ID =
        GVTAttributedCharacterIterator.TextAttribute.TEXT_COMPOUND_ID;
    protected BridgeEventSupport() {}
    public static void addGVTListener(BridgeContext ctx, Document doc) {
        UserAgent ua = ctx.getUserAgent();
        if (ua != null) {
            EventDispatcher dispatcher = ua.getEventDispatcher();
            if (dispatcher != null) {
                final Listener listener = new Listener(ctx, ua);
                dispatcher.addGraphicsNodeMouseListener(listener);
                dispatcher.addGraphicsNodeKeyListener(listener);
                EventListener l = new GVTUnloadListener(dispatcher, listener);
                NodeEventTarget target = (NodeEventTarget)doc;
                target.addEventListenerNS
                    (XMLConstants.XML_EVENTS_NAMESPACE_URI,
                     "SVGUnload",
                     l, false, null);
                storeEventListenerNS
                    (ctx, target,
                     XMLConstants.XML_EVENTS_NAMESPACE_URI,
                     "SVGUnload",
                     l, false);
            }
        }
    }
    protected static void storeEventListener(BridgeContext ctx,
                                             EventTarget e,
                                             String t,
                                             EventListener l,
                                             boolean c) {
        ctx.storeEventListener(e, t, l, c);
    }
    protected static void storeEventListenerNS(BridgeContext ctx,
                                               EventTarget e,
                                               String n,
                                               String t,
                                               EventListener l,
                                               boolean c) {
        ctx.storeEventListenerNS(e, n, t, l, c);
    }
    protected static class GVTUnloadListener implements EventListener {
        protected EventDispatcher dispatcher;
        protected Listener listener;
        public GVTUnloadListener(EventDispatcher dispatcher,
                                 Listener listener) {
            this.dispatcher = dispatcher;
            this.listener = listener;
        }
        public void handleEvent(Event evt) {
            dispatcher.removeGraphicsNodeMouseListener(listener);
            dispatcher.removeGraphicsNodeKeyListener(listener);
            NodeEventTarget et = (NodeEventTarget) evt.getTarget();
            et.removeEventListenerNS
                (XMLConstants.XML_EVENTS_NAMESPACE_URI, "SVGUnload",
                 this, false);
        }
    }
    protected static class Listener implements GraphicsNodeMouseListener,
                                               GraphicsNodeKeyListener {
        protected BridgeContext context;
        protected UserAgent ua;
        protected Element lastTargetElement;
        protected boolean isDown;
        public Listener(BridgeContext ctx, UserAgent u) {
            context = ctx;
            ua = u;
        }
        public void keyPressed(GraphicsNodeKeyEvent evt) {
            if (!isDown) {
                isDown = true;
                dispatchKeyEvent("keydown", evt);
            }
            if (evt.getKeyChar() == KeyEvent.CHAR_UNDEFINED) {
                dispatchKeyEvent("keypress", evt);
            }
        }
        public void keyReleased(GraphicsNodeKeyEvent evt) {
            dispatchKeyEvent("keyup", evt);
            isDown = false;
        }
        public void keyTyped(GraphicsNodeKeyEvent evt) {
            dispatchKeyEvent("keypress", evt);
        }
        protected void dispatchKeyEvent(String eventType,
                                        GraphicsNodeKeyEvent evt) {
            FocusManager fmgr = context.getFocusManager();
            if (fmgr == null) return;
            Element targetElement = (Element)fmgr.getCurrentEventTarget();
            if (targetElement == null) {
                targetElement = context.getDocument().getDocumentElement();
            }
            DocumentEvent d = (DocumentEvent)targetElement.getOwnerDocument();
            DOMKeyEvent keyEvt = (DOMKeyEvent)d.createEvent("KeyEvents");
            keyEvt.initKeyEvent(eventType,
                                true,
                                true,
                                evt.isControlDown(),
                                evt.isAltDown(),
                                evt.isShiftDown(),
                                evt.isMetaDown(),
                                mapKeyCode(evt.getKeyCode()),
                                evt.getKeyChar(),
                                null);
            try {
                ((EventTarget)targetElement).dispatchEvent(keyEvt);
            } catch (RuntimeException e) {
                ua.displayError(e);
            }
        }
        protected final int mapKeyCode(int keyCode) {
            switch (keyCode) {
                case KeyEvent.VK_ENTER:
                    return DOMKeyEvent.DOM_VK_ENTER;
            case KeyEvent.VK_KANA_LOCK:
                return DOMKeyEvent.DOM_VK_UNDEFINED;
            case KeyEvent.VK_INPUT_METHOD_ON_OFF:
                return DOMKeyEvent.DOM_VK_UNDEFINED;
            default:
                return keyCode;
            }
        }
        public void mouseClicked(GraphicsNodeMouseEvent evt) {
            dispatchMouseEvent("click", evt, true);
        }
        public void mousePressed(GraphicsNodeMouseEvent evt) {
            dispatchMouseEvent("mousedown", evt, true);
        }
        public void mouseReleased(GraphicsNodeMouseEvent evt) {
            dispatchMouseEvent("mouseup", evt, true);
        }
        public void mouseEntered(GraphicsNodeMouseEvent evt) {
            Point clientXY = evt.getClientPoint();
            GraphicsNode node = evt.getGraphicsNode();
            Element targetElement = getEventTarget(node, evt.getPoint2D());
            Element relatedElement = getRelatedElement(evt);
            dispatchMouseEvent("mouseover",
                               targetElement,
                               relatedElement,
                               clientXY,
                               evt,
                               true);
        }
        public void mouseExited(GraphicsNodeMouseEvent evt) {
            Point clientXY = evt.getClientPoint();
            GraphicsNode node = evt.getRelatedNode();
            Element targetElement = getEventTarget(node, evt.getPoint2D());
            if (lastTargetElement != null) {
                dispatchMouseEvent("mouseout",
                                   lastTargetElement, 
                                   targetElement,     
                                   clientXY,
                                   evt,
                                   true);
                lastTargetElement = null;
            }
        }
        public void mouseDragged(GraphicsNodeMouseEvent evt) {
            dispatchMouseEvent("mousemove", evt, false);
        }
        public void mouseMoved(GraphicsNodeMouseEvent evt) {
            Point clientXY = evt.getClientPoint();
            GraphicsNode node = evt.getGraphicsNode();
            Element targetElement = getEventTarget(node, evt.getPoint2D());
            Element holdLTE = lastTargetElement;
            if (holdLTE != targetElement) {
                if (holdLTE != null) {
                    dispatchMouseEvent("mouseout",
                                       holdLTE, 
                                       targetElement,     
                                       clientXY,
                                       evt,
                                       true);
                }
                if (targetElement != null) {
                    dispatchMouseEvent("mouseover",
                                       targetElement,     
                                       holdLTE, 
                                       clientXY,
                                       evt,
                                       true);
                }
            }
            dispatchMouseEvent("mousemove",
                               targetElement,     
                               null,              
                               clientXY,
                               evt,
                               false);
        }
        protected void dispatchMouseEvent(String eventType,
                                          GraphicsNodeMouseEvent evt,
                                          boolean cancelable) {
            Point clientXY = evt.getClientPoint();
            GraphicsNode node = evt.getGraphicsNode();
            Element targetElement = getEventTarget(node, evt.getPoint2D());
            Element relatedElement = getRelatedElement(evt);
            dispatchMouseEvent(eventType,
                               targetElement,
                               relatedElement,
                               clientXY,
                               evt,
                               cancelable);
        }
        protected void dispatchMouseEvent(String eventType,
                                          Element targetElement,
                                          Element relatedElement,
                                          Point clientXY,
                                          GraphicsNodeMouseEvent evt,
                                          boolean cancelable) {
            if (targetElement == null) {
                return;
            }
            Point screenXY = evt.getScreenPoint();
            DocumentEvent d = (DocumentEvent)targetElement.getOwnerDocument();
            DOMMouseEvent mouseEvt
                = (DOMMouseEvent)d.createEvent("MouseEvents");
            String modifiers
                = DOMUtilities.getModifiersList(evt.getLockState(),
                                                evt.getModifiers());
            mouseEvt.initMouseEventNS(XMLConstants.XML_EVENTS_NAMESPACE_URI,
                                      eventType,
                                      true,
                                      cancelable,
                                      null,
                                      evt.getClickCount(),
                                      screenXY.x,
                                      screenXY.y,
                                      clientXY.x,
                                      clientXY.y,
                                      (short) (evt.getButton() - 1),
                                      (EventTarget)relatedElement,
                                      modifiers);
            try {
                ((EventTarget)targetElement).dispatchEvent(mouseEvt);
            } catch (RuntimeException e) {
                ua.displayError(e);
            } finally {
                lastTargetElement = targetElement;
            }
        }
        protected Element getRelatedElement(GraphicsNodeMouseEvent evt) {
            GraphicsNode relatedNode = evt.getRelatedNode();
            Element relatedElement = null;
            if (relatedNode != null) {
                relatedElement = context.getElement(relatedNode);
            }
            return relatedElement;
        }
        protected Element getEventTarget(GraphicsNode node, Point2D pt) {
            Element target = context.getElement(node);
            if (target != null && node instanceof TextNode) {
                TextNode textNode = (TextNode)node;
                List list = textNode.getTextRuns();
                if (list != null){
                    float x = (float)pt.getX();
                    float y = (float)pt.getY();
                    for (int i = 0 ; i < list.size(); i++) {
                        StrokingTextPainter.TextRun run =
                            (StrokingTextPainter.TextRun)list.get(i);
                        AttributedCharacterIterator aci = run.getACI();
                        TextSpanLayout layout = run.getLayout();
                        TextHit textHit = layout.hitTestChar(x, y);
                        Rectangle2D bounds = layout.getBounds2D();
                        if ((textHit != null) &&
                            (bounds != null) && bounds.contains(x, y)) {
                            SoftReference sr;
                            sr =(SoftReference)aci.getAttribute
                                (TEXT_COMPOUND_ID);
                            Object delimiter = sr.get();
                            if (delimiter instanceof Element) {
                                return (Element)delimiter;
                            }
                        }
                    }
                }
            }
            return target;
        }
    }
}
