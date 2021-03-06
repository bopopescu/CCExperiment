package org.apache.batik.dom.svg;
import org.apache.batik.parser.NumberListHandler;
import org.apache.batik.parser.NumberListParser;
import org.apache.batik.parser.ParseException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGNumber;
import org.w3c.dom.svg.SVGNumberList;
public abstract class AbstractSVGNumberList
        extends AbstractSVGList
        implements SVGNumberList {
    public static final String SVG_NUMBER_LIST_SEPARATOR
        = " ";
    protected String getItemSeparator() {
        return SVG_NUMBER_LIST_SEPARATOR;
    }
    protected abstract SVGException createSVGException(short type,
                                                       String key,
                                                       Object[] args);
    protected abstract Element getElement();
    protected AbstractSVGNumberList() {
    }
    public SVGNumber initialize(SVGNumber newItem)
        throws DOMException, SVGException {
        return (SVGNumber)initializeImpl(newItem);
    }
    public SVGNumber getItem(int index) throws DOMException {
        return (SVGNumber)getItemImpl(index);
    }
    public SVGNumber insertItemBefore(SVGNumber newItem, int index)
        throws DOMException, SVGException {
        return (SVGNumber)insertItemBeforeImpl(newItem,index);
    }
    public SVGNumber replaceItem(SVGNumber newItem, int index)
        throws DOMException, SVGException {
        return (SVGNumber)replaceItemImpl(newItem,index);
    }
    public SVGNumber removeItem(int index) throws DOMException {
        return (SVGNumber)removeItemImpl(index);
    }
    public SVGNumber appendItem(SVGNumber newItem)
        throws DOMException, SVGException {
        return (SVGNumber)appendItemImpl(newItem);
    }
    protected SVGItem createSVGItem(Object newItem) {
        SVGNumber l = (SVGNumber)newItem;
        return new SVGNumberItem(l.getValue());
    }
    protected void doParse(String value, ListHandler handler)
        throws ParseException{
        NumberListParser NumberListParser = new NumberListParser();
        NumberListBuilder builder = new NumberListBuilder(handler);
        NumberListParser.setNumberListHandler(builder);
        NumberListParser.parse(value);
    }
    protected void checkItemType(Object newItem) throws SVGException {
        if (!(newItem instanceof SVGNumber)) {
            createSVGException(SVGException.SVG_WRONG_TYPE_ERR,
                               "expected SVGNumber",
                               null);
        }
    }
    protected class SVGNumberItem extends AbstractSVGNumber implements SVGItem {
        protected AbstractSVGList parentList;
        public SVGNumberItem(float value) {
            this.value = value;
        }
        public String getValueAsString() {
            return Float.toString(value);
        }
        public void setParent(AbstractSVGList list) {
            parentList = list;
        }
        public AbstractSVGList getParent() {
            return parentList;
        }
        protected void reset() {
            if (parentList != null) {
                parentList.itemChanged();
            }
        }
    }
    protected class NumberListBuilder implements NumberListHandler {
        protected ListHandler listHandler;
        protected float currentValue;
        public NumberListBuilder(ListHandler listHandler) {
            this.listHandler = listHandler;
        }
        public void startNumberList() throws ParseException{
            listHandler.startList();
        }
        public void startNumber() throws ParseException {
            currentValue = 0.0f;
        }
        public void numberValue(float v) throws ParseException {
            currentValue = v;
        }
        public void endNumber() throws ParseException {
            listHandler.item(new SVGNumberItem(currentValue));
        }
        public void endNumberList() throws ParseException {
            listHandler.endList();
        }
    }
}
