package org.apache.batik.swing.svg;
import java.io.InterruptedIOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.batik.bridge.DocumentLoader;
import org.apache.batik.util.EventDispatcher;
import org.apache.batik.util.EventDispatcher.Dispatcher;
import org.apache.batik.util.HaltingThread;
import org.w3c.dom.svg.SVGDocument;
public class SVGDocumentLoader extends HaltingThread {
    protected String url;
    protected DocumentLoader loader;
    protected Exception exception;
    protected List listeners = Collections.synchronizedList(new LinkedList());
    public SVGDocumentLoader(String u, DocumentLoader l) {
        url = u;
        loader = l;
    }
    public void run() {
        SVGDocumentLoaderEvent evt;
        evt = new SVGDocumentLoaderEvent(this, null);
        try {
            fireEvent(startedDispatcher, evt);
            if (isHalted()) {
                fireEvent(cancelledDispatcher, evt);
                return;
            }
            SVGDocument svgDocument = (SVGDocument)loader.loadDocument(url);
            if (isHalted()) {
                fireEvent(cancelledDispatcher, evt);
                return;
            }
            evt = new SVGDocumentLoaderEvent(this, svgDocument);
            fireEvent(completedDispatcher, evt);
        } catch (InterruptedIOException e) {
            fireEvent(cancelledDispatcher, evt);
        } catch (Exception e) {
            exception = e;
            fireEvent(failedDispatcher, evt);
        } catch (ThreadDeath td) {
            exception = new Exception(td.getMessage());
            fireEvent(failedDispatcher, evt);
            throw td;
        } catch (Throwable t) {
            t.printStackTrace();
            exception = new Exception(t.getMessage());
            fireEvent(failedDispatcher, evt);
        }
    }
    public Exception getException() {
        return exception;
    }
    public void addSVGDocumentLoaderListener(SVGDocumentLoaderListener l) {
        listeners.add(l);
    }
    public void removeSVGDocumentLoaderListener(SVGDocumentLoaderListener l) {
        listeners.remove(l);
    }
    public void fireEvent(Dispatcher dispatcher, Object event) {
        EventDispatcher.fireEvent(dispatcher, listeners, event, true);
    }
    static Dispatcher startedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((SVGDocumentLoaderListener)listener).documentLoadingStarted
                    ((SVGDocumentLoaderEvent)event);
            }
        };
            static Dispatcher completedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((SVGDocumentLoaderListener)listener).documentLoadingCompleted
                 ((SVGDocumentLoaderEvent)event);
            }
        };
    static Dispatcher cancelledDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((SVGDocumentLoaderListener)listener).documentLoadingCancelled
                 ((SVGDocumentLoaderEvent)event);
            }
        };
    static Dispatcher failedDispatcher = new Dispatcher() {
            public void dispatch(Object listener,
                                 Object event) {
                ((SVGDocumentLoaderListener)listener).documentLoadingFailed
                 ((SVGDocumentLoaderEvent)event);
            }
        };
}
