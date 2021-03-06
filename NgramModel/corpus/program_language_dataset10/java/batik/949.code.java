package org.apache.batik.gvt;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.apache.batik.util.HaltingThread;
public class CompositeGraphicsNode extends AbstractGraphicsNode
    implements List {
    public static final Rectangle2D VIEWPORT  = new Rectangle();
    public static final Rectangle2D NULL_RECT = new Rectangle();
    protected GraphicsNode [] children;
    protected volatile int count;
    protected volatile int modCount;
    protected Rectangle2D backgroundEnableRgn = null;
    private volatile Rectangle2D geometryBounds;
    private volatile Rectangle2D primitiveBounds;
    private volatile Rectangle2D sensitiveBounds;
    private Shape outline;
    public CompositeGraphicsNode() {}
    public List getChildren() {
        return this;
    }
    public void setBackgroundEnable(Rectangle2D bgRgn) {
        backgroundEnableRgn = bgRgn;
    }
    public Rectangle2D getBackgroundEnable() {
        return backgroundEnableRgn;
    }
    public void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }
    public void primitivePaint(Graphics2D g2d) {
        if (count == 0) {
            return;
        }
        Thread currentThread = Thread.currentThread();
        for (int i=0; i < count; ++i) {
            if (HaltingThread.hasBeenHalted( currentThread ))
                return;
            GraphicsNode node = children[i];
            if (node == null) {
                continue;
            }
            node.paint(g2d);
        }
    }
    protected void invalidateGeometryCache() {
        super.invalidateGeometryCache();
        geometryBounds = null;
        primitiveBounds = null;
        sensitiveBounds = null;
        outline = null;
    }
    public Rectangle2D getPrimitiveBounds() {
        if (primitiveBounds != null) {
            if (primitiveBounds == NULL_RECT) return null;
            return primitiveBounds;
        }
        Thread currentThread = Thread.currentThread();
        int i=0;
        Rectangle2D bounds = null;
        while ((bounds == null) && i < count) {
            bounds = children[i++].getTransformedBounds(IDENTITY);
            if (((i & 0x0F) == 0) && HaltingThread.hasBeenHalted( currentThread ))
                break; 
        }
        if (HaltingThread.hasBeenHalted( currentThread )) {
            invalidateGeometryCache();
            return null;
        }
        if (bounds == null) {
            primitiveBounds = NULL_RECT;
            return null;
        }
        primitiveBounds = bounds;
        while (i < count) {
            Rectangle2D ctb = children[i++].getTransformedBounds(IDENTITY);
            if (ctb != null) {
                if (primitiveBounds == null) {
                    return null;
                } else {
                    primitiveBounds.add(ctb);
                }
            }
            if (((i & 0x0F) == 0) && HaltingThread.hasBeenHalted( currentThread ))
                break; 
        }
        if (HaltingThread.hasBeenHalted( currentThread )) {
            invalidateGeometryCache();
        }
        return primitiveBounds;
    }
    public static Rectangle2D  getTransformedBBox(Rectangle2D r2d, AffineTransform t) {
        if ((t  == null) || (r2d == null)) return r2d;
        double x  = r2d.getX();
        double w  = r2d.getWidth();
        double y  = r2d.getY();
        double h  = r2d.getHeight();
        double sx = t.getScaleX();
        double sy = t.getScaleY();
        if (sx < 0) {
            x = -(x + w);
            sx = -sx;
        }
        if (sy < 0) {
            y = -(y + h);
            sy = -sy;
        }
        return new Rectangle2D.Float
            ((float)(x*sx+t.getTranslateX()),
             (float)(y*sy+t.getTranslateY()),
             (float)(w*sx), (float)(h*sy));
    }
    public Rectangle2D getTransformedPrimitiveBounds(AffineTransform txf) {
        AffineTransform t = txf;
        if (transform != null) {
            t = new AffineTransform(txf);
            t.concatenate(transform);
        }
        if ((t == null) || ((t.getShearX() == 0) && (t.getShearY() == 0))) {
            return getTransformedBBox(getPrimitiveBounds(), t);
        }
        int i = 0;
        Rectangle2D tpb = null;
        while (tpb == null && i < count) {
            tpb = children[i++].getTransformedBounds(t);
        }
        while (i < count) {
            Rectangle2D ctb = children[i++].getTransformedBounds(t);
            if(ctb != null){
                tpb.add(ctb);
            }
        }
        return tpb;
    }
    public Rectangle2D getGeometryBounds() {
        if (geometryBounds == null) {
            int i=0;
            while(geometryBounds == null && i < count){
                geometryBounds =
                children[i++].getTransformedGeometryBounds (IDENTITY);
            }
            while (i<count) {
                Rectangle2D cgb = children[i++].getTransformedGeometryBounds(IDENTITY);
                if (cgb != null) {
                    if (geometryBounds == null) {
                        return getGeometryBounds();
                    } else {
                        geometryBounds.add(cgb);
                    }
                }
            }
        }
        return geometryBounds;
    }
    public Rectangle2D getTransformedGeometryBounds(AffineTransform txf) {
        AffineTransform t = txf;
        if (transform != null) {
            t = new AffineTransform(txf);
            t.concatenate(transform);
        }
        if ((t == null) || ((t.getShearX() == 0) && (t.getShearY() == 0))) {
            return getTransformedBBox(getGeometryBounds(), t);
        }
        Rectangle2D gb = null;
        int i=0;
        while (gb == null && i < count) {
            gb = children[i++].getTransformedGeometryBounds(t);
        }
        Rectangle2D cgb = null;
        while (i < count) {
            cgb = children[i++].getTransformedGeometryBounds(t);
            if (cgb != null) {
                gb.add(cgb);
            }
        }
        return gb;
    }
    public Rectangle2D getSensitiveBounds() {
        if (sensitiveBounds != null)
            return sensitiveBounds;
        int i=0;
        while(sensitiveBounds == null && i < count){
            sensitiveBounds =
                children[i++].getTransformedSensitiveBounds(IDENTITY);
        }
        while (i<count) {
            Rectangle2D cgb = children[i++].getTransformedSensitiveBounds(IDENTITY);
            if (cgb != null) {
                if (sensitiveBounds == null)
                    return getSensitiveBounds();
                sensitiveBounds.add(cgb);
            }
        }
        return sensitiveBounds;
    }
    public Rectangle2D getTransformedSensitiveBounds(AffineTransform txf) {
        AffineTransform t = txf;
        if (transform != null) {
            t = new AffineTransform(txf);
            t.concatenate(transform);
        }
        if ((t == null) || ((t.getShearX() == 0) && (t.getShearY() == 0))) {
            return getTransformedBBox(getSensitiveBounds(), t);
        }
        Rectangle2D sb = null;
        int i=0;
        while (sb == null && i < count) {
            sb = children[i++].getTransformedSensitiveBounds(t);
        }
        while (i < count) {
            Rectangle2D csb = children[i++].getTransformedSensitiveBounds(t);
            if (csb != null) {
                sb.add(csb);
            }
        }
        return sb;
    }
    public boolean contains(Point2D p) {
        Rectangle2D bounds = getSensitiveBounds();
        if (count > 0 && bounds != null && bounds.contains(p)) {
            Point2D pt = null;
            Point2D cp = null; 
            for (int i=0; i < count; ++i) {
                AffineTransform t = children[i].getInverseTransform();
                if(t != null){
                    pt = t.transform(p, pt);
                    cp = pt;
                } else {
                    cp = p;
                }
                if (children[i].contains(cp)) {
                    return true;
                }
            }
        }
        return false;
    }
    public GraphicsNode nodeHitAt(Point2D p) {
        Rectangle2D bounds = getSensitiveBounds();
        if (count > 0 && bounds != null && bounds.contains(p)) {
            Point2D pt = null;
            Point2D cp = null; 
            for (int i=count-1; i >= 0; --i) {
                AffineTransform t = children[i].getInverseTransform();
                if(t != null){
                    pt = t.transform(p, pt);
                    cp = pt;
                } else {
                    cp = p;
                }
                GraphicsNode node = children[i].nodeHitAt(cp);
                if (node != null) {
                    return node;
                }
            }
        }
        return null;
    }
    public Shape getOutline() {
        if (outline != null)
            return outline;
        outline = new GeneralPath();
        for (int i = 0; i < count; i++) {
            Shape childOutline = children[i].getOutline();
            if (childOutline != null) {
                AffineTransform tr = children[i].getTransform();
                if (tr != null) {
                    ((GeneralPath)outline).append(tr.createTransformedShape(childOutline), false);
                } else {
                    ((GeneralPath)outline).append(childOutline, false);
                }
            }
        }
        return outline;
    }
    protected void setRoot(RootGraphicsNode newRoot) {
        super.setRoot(newRoot);
        for (int i=0; i < count; ++i) {
            GraphicsNode node = children[i];
            ((AbstractGraphicsNode)node).setRoot(newRoot);
        }
    }
    public int size() {
        return count;
    }
    public boolean isEmpty() {
        return (count == 0);
    }
    public boolean contains(Object node) {
        return (indexOf(node) >= 0);
    }
    public Iterator iterator() {
        return new Itr();
    }
    public Object [] toArray() {
        GraphicsNode [] result = new GraphicsNode[count];
        System.arraycopy( children, 0, result, 0, count );
        return result;
    }
    public Object[] toArray(Object [] a) {
        if (a.length < count) {
            a = new GraphicsNode[count];
        }
        System.arraycopy(children, 0, a, 0, count);
        if (a.length > count) {
            a[count] = null;
        }
        return a;
    }
    public Object get(int index) {
        checkRange(index);
        return children[index];
    }
    public Object set(int index, Object o) {
        if (!(o instanceof GraphicsNode)) {
            throw new IllegalArgumentException(o+" is not a GraphicsNode");
        }
        checkRange(index);
        GraphicsNode node = (GraphicsNode) o;
        {
            fireGraphicsNodeChangeStarted(node);
        }
        if (node.getParent() != null) {
            node.getParent().getChildren().remove(node);
        }
        GraphicsNode oldNode = children[index];
        children[index] = node;
        ((AbstractGraphicsNode) node).setParent(this);
        ((AbstractGraphicsNode) oldNode).setParent(null);
        ((AbstractGraphicsNode) node).setRoot(this.getRoot());
        ((AbstractGraphicsNode) oldNode).setRoot(null);
        invalidateGeometryCache();
        fireGraphicsNodeChangeCompleted();
        return oldNode;
     }
    public boolean add(Object o) {
        if (!(o instanceof GraphicsNode)) {
            throw new IllegalArgumentException(o+" is not a GraphicsNode");
        }
        GraphicsNode node = (GraphicsNode) o;
        {
            fireGraphicsNodeChangeStarted(node);
        }
        if (node.getParent() != null) {
            node.getParent().getChildren().remove(node);
        }
        ensureCapacity(count + 1);  
        children[count++] = node;
        ((AbstractGraphicsNode) node).setParent(this);
        ((AbstractGraphicsNode) node).setRoot(this.getRoot());
        invalidateGeometryCache();
        fireGraphicsNodeChangeCompleted();
        return true;
    }
    public void add(int index, Object o) {
        if (!(o instanceof GraphicsNode)) {
            throw new IllegalArgumentException(o+" is not a GraphicsNode");
        }
        if (index > count || index < 0) {
            throw new IndexOutOfBoundsException(
                "Index: "+index+", Size: "+count);
        }
        GraphicsNode node = (GraphicsNode) o;
        {
            fireGraphicsNodeChangeStarted(node);
        }
        if (node.getParent() != null) {
            node.getParent().getChildren().remove(node);
        }
        ensureCapacity(count+1);  
        System.arraycopy(children, index, children, index+1, count-index);
        children[index] = node;
        count++;
        ((AbstractGraphicsNode) node).setParent(this);
        ((AbstractGraphicsNode) node).setRoot(this.getRoot());
        invalidateGeometryCache();
        fireGraphicsNodeChangeCompleted();
    }
    public boolean addAll(Collection c) {
        throw new UnsupportedOperationException();
    }
    public boolean addAll(int index, Collection c) {
        throw new UnsupportedOperationException();
    }
    public boolean remove(Object o) {
        if (!(o instanceof GraphicsNode)) {
            throw new IllegalArgumentException(o+" is not a GraphicsNode");
        }
        GraphicsNode node = (GraphicsNode) o;
        if (node.getParent() != this) {
            return false;
        }
        int index = 0;
        for (; node != children[index]; index++);     
        remove(index);
        return true;
    }
    public Object remove(int index) {
        checkRange(index);
        GraphicsNode oldNode = children[index];
        {
            fireGraphicsNodeChangeStarted(oldNode);
        }
        modCount++;
        int numMoved = count - index - 1;
        if (numMoved > 0) {
            System.arraycopy(children, index+1, children, index, numMoved);
        }
        children[--count] = null; 
        if (count == 0) {
            children = null;
        }
        ((AbstractGraphicsNode) oldNode).setParent(null);
        ((AbstractGraphicsNode) oldNode).setRoot(null);
        invalidateGeometryCache();
        fireGraphicsNodeChangeCompleted();
        return oldNode;
    }
    public boolean removeAll(Collection c) {
        throw new UnsupportedOperationException();
    }
    public boolean retainAll(Collection c) {
        throw new UnsupportedOperationException();
    }
    public void clear() {
        throw new UnsupportedOperationException();
    }
    public boolean containsAll(Collection c) {
        Iterator i = c.iterator();
        while (i.hasNext()) {
            if (!contains(i.next())) {
                    return false;
            }
        }
        return true;
    }
    public int indexOf(Object node) {
        if (node == null || !(node instanceof GraphicsNode)) {
            return -1;
        }
        if (((GraphicsNode) node).getParent() == this) {
            int iCount = count;                  
            GraphicsNode[] workList = children;  
            for (int i = 0; i < iCount; i++) {
                if (node == workList[ i ]) {
                    return i;
                }
            }
        }
        return -1;
    }
    public int lastIndexOf(Object node) {
        if (node == null || !(node instanceof GraphicsNode)) {
            return -1;
        }
        if (((GraphicsNode) node).getParent() == this) {
            for (int i = count-1; i >= 0; i--) {
                if (node == children[i]) {
                    return i;
                }
            }
        }
        return -1;
    }
    public ListIterator listIterator() {
        return listIterator(0);
    }
    public ListIterator listIterator(int index) {
        if (index < 0 || index > count) {
            throw new IndexOutOfBoundsException("Index: "+index);
        }
        return new ListItr(index);
    }
    public List subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }
    private void checkRange(int index) {
        if (index >= count || index < 0) {
            throw new IndexOutOfBoundsException(
                "Index: "+index+", Size: "+count);
        }
    }
    public void ensureCapacity(int minCapacity) {
        if (children == null) {
            children = new GraphicsNode[4];
        }
        modCount++;
        int oldCapacity = children.length;
        if (minCapacity > oldCapacity) {
            GraphicsNode [] oldData = children;
            int newCapacity = oldCapacity + oldCapacity/2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            children = new GraphicsNode[newCapacity];
            System.arraycopy(oldData, 0, children, 0, count);
        }
    }
    private class Itr implements Iterator {
        int cursor = 0;
        int lastRet = -1;
        int expectedModCount = modCount;
        public boolean hasNext() {
            return cursor != count;
        }
        public Object next() {
            try {
                Object next = get(cursor);
                checkForComodification();
                lastRet = cursor++;
                return next;
            } catch(IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }
        public void remove() {
            if (lastRet == -1) {
                throw new IllegalStateException();
            }
            checkForComodification();
            try {
                CompositeGraphicsNode.this.remove(lastRet);
                if (lastRet < cursor) {
                    cursor--;
                }
                lastRet = -1;
                expectedModCount = modCount;
            } catch(IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }
        final void checkForComodification() {
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
    }
    private class ListItr extends Itr implements ListIterator {
        ListItr(int index) {
            cursor = index;
        }
        public boolean hasPrevious() {
            return cursor != 0;
        }
        public Object previous() {
            try {
                Object previous = get(--cursor);
                checkForComodification();
                lastRet = cursor;
                return previous;
            } catch(IndexOutOfBoundsException e) {
                checkForComodification();
                throw new NoSuchElementException();
            }
        }
        public int nextIndex() {
            return cursor;
        }
        public int previousIndex() {
            return cursor-1;
        }
        public void set(Object o) {
            if (lastRet == -1) {
                throw new IllegalStateException();
            }
            checkForComodification();
            try {
                CompositeGraphicsNode.this.set(lastRet, o);
                expectedModCount = modCount;
            } catch(IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }
        public void add(Object o) {
            checkForComodification();
            try {
                CompositeGraphicsNode.this.add(cursor++, o);
                lastRet = -1;
                expectedModCount = modCount;
            } catch(IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }
    }
}
