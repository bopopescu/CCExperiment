package org.apache.xerces.xs.datatypes;
import java.util.List;
public interface ObjectList extends List {
    public int getLength();
    public boolean contains(Object item);
    public Object item(int index);
}
