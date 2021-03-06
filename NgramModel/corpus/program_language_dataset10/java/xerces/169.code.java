package org.apache.wml.dom;
import org.apache.wml.WMLNoopElement;
public class WMLNoopElementImpl extends WMLElementImpl implements WMLNoopElement {
    private static final long serialVersionUID = -1581314434256075931L;
    public WMLNoopElementImpl (WMLDocumentImpl owner, String tagName) {
        super( owner, tagName);
    }
    public void setClassName(String newValue) {
        setAttribute("class", newValue);
    }
    public String getClassName() {
        return getAttribute("class");
    }
    public void setId(String newValue) {
        setAttribute("id", newValue);
    }
    public String getId() {
        return getAttribute("id");
    }
}
