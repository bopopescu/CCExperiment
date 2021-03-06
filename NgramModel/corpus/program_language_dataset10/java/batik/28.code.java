package org.apache.batik.anim.timing;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
public abstract class TimeContainer extends TimedElement {
    protected List children = new LinkedList();
    public void addChild(TimedElement e) {
        if (e == this) {
            throw new IllegalArgumentException("recursive datastructure not allowed here!");
        }
        children.add(e);
        e.parent = this;
        setRoot(e, root);
        root.fireElementAdded(e);
        root.currentIntervalWillUpdate();
    }
    protected void setRoot(TimedElement e, TimedDocumentRoot root) {
        e.root = root;
        if (e instanceof TimeContainer) {
            TimeContainer c = (TimeContainer) e;
            Iterator it = c.children.iterator();
            while (it.hasNext()) {
                TimedElement te = (TimedElement)it.next();
                setRoot(te, root);
            }
        }
    }
    public void removeChild(TimedElement e) {
        children.remove(e);
        e.parent = null;
        setRoot(e, null);
        root.fireElementRemoved(e);
        root.currentIntervalWillUpdate();
    }
    public TimedElement[] getChildren() {
        return (TimedElement[]) children.toArray(new TimedElement[0]);
    }
    protected float sampleAt(float parentSimpleTime, boolean hyperlinking) {
        super.sampleAt(parentSimpleTime, hyperlinking);
        return sampleChildren(parentSimpleTime, hyperlinking);
    }
    protected float sampleChildren(float parentSimpleTime,
                                   boolean hyperlinking) {
        float mint = Float.POSITIVE_INFINITY;
        Iterator i = children.iterator();
        while (i.hasNext()) {
            TimedElement e = (TimedElement) i.next();
            float t = e.sampleAt(parentSimpleTime, hyperlinking);
            if (t < mint) {
                mint = t;
            }
        }
        return mint;
    }
    protected void reset(boolean clearCurrentBegin) {
        super.reset(clearCurrentBegin);
        Iterator i = children.iterator();
        while (i.hasNext()) {
            TimedElement e = (TimedElement) i.next();
            e.reset(clearCurrentBegin);
        }
    }
    protected boolean isConstantAnimation() {
        return false;
    }
    public abstract float getDefaultBegin(TimedElement child);
}
