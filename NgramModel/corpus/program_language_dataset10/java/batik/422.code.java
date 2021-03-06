package org.apache.batik.css.parser;
import org.w3c.css.sac.Selector;
import org.w3c.css.sac.SimpleSelector;
public class DefaultDirectAdjacentSelector extends AbstractSiblingSelector {
    public DefaultDirectAdjacentSelector(short type,
                                         Selector parent,
                                         SimpleSelector simple) {
        super(type, parent, simple);
    }
    public short getSelectorType() {
        return SAC_DIRECT_ADJACENT_SELECTOR;
    }
    public String toString() {
        return getSelector() + " + " + getSiblingSelector();
    }
}
