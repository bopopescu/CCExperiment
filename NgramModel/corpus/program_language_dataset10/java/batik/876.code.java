package org.apache.batik.ext.awt.image.rendered;
import org.apache.batik.util.DoublyLinkedList;
public class LRUCache {
        public interface LRUObj {
        void    lruSet(LRUNode nde);
        LRUNode lruGet();
        void    lruRemove();
        }
        public class LRUNode extends DoublyLinkedList.Node {
                private   LRUObj  obj  = null;
                public    LRUObj  getObj ()               { return obj; }
                protected void    setObj (LRUObj  newObj) {
                        if (obj != null) obj.lruRemove();
                        obj = newObj;
                        if (obj != null) obj.lruSet(this);
                }
        }
        private DoublyLinkedList free    = null;
        private DoublyLinkedList used    = null;
        private int     maxSize = 0;
        public LRUCache(int size) {
                if (size <= 0) size=1;
                maxSize = size;
                free = new DoublyLinkedList();
                used = new DoublyLinkedList();
                while (size > 0) {
                        free.add(new LRUNode());
                        size--;
                }
        }
        public int getUsed() {
                return used.getSize();
        }
        public synchronized void setSize(int newSz) {
                if (maxSize < newSz) {  
                        for (int i=maxSize; i<newSz; i++)
                                free.add(new LRUNode());
                } else if (maxSize > newSz) {
                        for (int i=used.getSize(); i>newSz; i--) {
                                LRUNode nde = (LRUNode)used.getTail();
                                used.remove(nde);
                                nde.setObj(null);
                        }
                }
                maxSize = newSz;
        }
        public synchronized void flush() {
                while (used.getSize() > 0) {
                        LRUNode nde = (LRUNode)used.pop();
                        nde.setObj(null);
                        free.add(nde);
                }
        }
        public synchronized void remove(LRUObj obj) {
                LRUNode nde = obj.lruGet();
                if (nde == null) return;
                used.remove(nde);
                nde.setObj(null);
                free.add(nde);
        }
        public synchronized void touch(LRUObj obj) {
                LRUNode nde = obj.lruGet();
                if (nde == null) return;
                used.touch(nde);
        }
        public synchronized void add(LRUObj obj) {
                LRUNode nde = obj.lruGet();
                if (nde != null) {
                        used.touch(nde);
                        return;
                }
                if (free.getSize() > 0) {
                        nde = (LRUNode)free.pop();
                        nde.setObj(obj);
                        used.add(nde);
                } else {
                        nde = (LRUNode)used.getTail();
                        nde.setObj(obj);
                        used.touch(nde);
                }
        }
        protected synchronized void print() {
                System.out.println("In Use: " + used.getSize() +
                                                   " Free: " + free.getSize());
                LRUNode nde = (LRUNode)used.getHead();
                if (nde == null) return;
                do {
                        System.out.println(nde.getObj());
                        nde = (LRUNode)nde.getNext();
                } while (nde != used.getHead());
        }
}
