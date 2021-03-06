package org.apache.xerces.dom;
import java.util.ArrayList;
import java.util.Vector;
import org.w3c.dom.DOMStringList;
public class DOMStringListImpl implements DOMStringList {
    private final ArrayList fStrings;
    public DOMStringListImpl() {
        fStrings = new ArrayList();    
    }
    public DOMStringListImpl(ArrayList params) {
        fStrings = params;    
    }
    public DOMStringListImpl(Vector params) {
        fStrings = new ArrayList(params);
    }
	public String item(int index) {
	    final int length = getLength();
	    if (index >= 0 && index < length) {
	        return (String) fStrings.get(index);
	    }
	    return null;
	}
	public int getLength() {
		return fStrings.size();
	}
	public boolean contains(String param) {
		return fStrings.contains(param);
	}
    public void add(String param) {
        fStrings.add(param);
    }
}
