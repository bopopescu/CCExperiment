package org.apache.batik.swing.svg;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.InterruptedBridgeException;
import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.EventDispatcher;
import org.apache.batik.util.EventDispatcher.Dispatcher;
import org.apache.batik.util.HaltingThread;
import org.w3c.dom.svg.SVGDocument;
public class SVGLoadEventDispatcher extends HaltingThread {
    protected SVGDocument svgDocument;
    protected GraphicsNode root;
    protected BridgeContext bridgeContext;
    protected UpdateManager updateManager;
    protected List listeners = Collections.synchronizedList(new LinkedList());
    protected Exception exception;
    public SVGLoadEventDispatcher(GraphicsNode gn,
                                  SVGDocument doc,
                                  BridgeContext bc,
                                  UpdateManager um) {
        svgDocument = doc;
        root = gn;
        bridgeContext = bc;
        updateManager = um;
    }
    public void run() {
        SVGLoadEventDispatcherEvent ev;
        ev = new SVGLoadEventDispatcherEvent(this, root);
        try {
            fireEvent(startedDispatcher, ev);
            if (isHalted()) {
                fireEvent(cancelledDispatcher, ev);
                return;
            }
            updateManager.dispatchSVGLoadEvent();
            if (isHalted()) {
                fireEvent(cancelledDispatcher, ev);
                return;
            }
            fireEvent(completedDispatcher, ev);
        } catch (InterruptedException e) {
            fireEvent(cancelledDispatcher, ev);
        } catch (InterruptedBridgeException e) {
            fireEvent(cancelledDispatcher, ev);
        } catch (Exception e) {
            exception = e;
            fireEvent(failedDispatcher, ev);
        } catch (ThreadDeath td) {
            exception = new Exception(td.getMessage());
            fireEvent(failedDispatcher, ev);
            throw td;
        } catch (Throwable t) {
            t.printStackTrace();
            exception = new Exception(t.getMessage());
            fireEvent(failedDispatcher, ev);
        }
    }
    public UpdateManager getUpdateManager() {
        return updateManager;
    }
    public Exception getException() {
        return exception;
    }
    public void addSVGLoadEventDispatcherListener
        (SVGLoadEventDispatcherListener l) {
        listeners.add(l);
    }
    public void removeSVGLoadEventDispatcherListener
        (SVGLoadEventDispatcherListener l) {
        listeners.remove(l);
    }
    public void fireEvent(Dispatcher dispatcher, Object event) {
        EventDispatcher.fireEvent(dispatcher, listeners, event, true);
    }
    static Dispatcher startedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((SVGLoadEventDispatcherListener)listener).
                    svgLoadEventDispatchStarted
                    ((SVGLoadEventDispatcherEvent)event);
            }
        };
    static Dispatcher completedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((SVGLoadEventDispatcherListener)listener).
                    svgLoadEventDispatchCompleted
                    ((SVGLoadEventDispatcherEvent)event);
            }
        };
    static Dispatcher cancelledDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((SVGLoadEventDispatcherListener)listener).
                    svgLoadEventDispatchCancelled
                    ((SVGLoadEventDispatcherEvent)event);
            }
        };
    static Dispatcher failedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((SVGLoadEventDispatcherListener)listener).
                    svgLoadEventDispatchFailed
                    ((SVGLoadEventDispatcherEvent)event);
            }
        };
}
