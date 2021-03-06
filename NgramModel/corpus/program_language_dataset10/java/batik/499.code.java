package org.apache.batik.dom.svg;
public abstract class AbstractSVGItem implements SVGItem {
    protected AbstractSVGList parent;
    protected String itemStringValue;
    protected abstract String getStringValue();
    protected AbstractSVGItem() {
    }
    public void setParent(AbstractSVGList list) {
        parent = list;
    }
    public AbstractSVGList getParent() {
        return parent;
    }
    protected void resetAttribute() {
        if (parent != null) {
            itemStringValue = null;
            parent.itemChanged();
        }
    }
    public String getValueAsString() {
        if (itemStringValue == null) {
            itemStringValue = getStringValue();
        }
        return itemStringValue;
    }
}
