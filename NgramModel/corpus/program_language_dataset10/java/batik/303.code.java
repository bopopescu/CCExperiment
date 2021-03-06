package org.apache.batik.css.engine.sac;
import java.util.Set;
import org.w3c.css.sac.ElementSelector;
public abstract class AbstractElementSelector
    implements ElementSelector,
               ExtendedSelector {
    protected String namespaceURI;
    protected String localName;
    protected AbstractElementSelector(String uri, String name) {
        namespaceURI = uri;
        localName    = name;
    }
    public boolean equals(Object obj) {
        if (obj == null || (obj.getClass() != getClass())) {
            return false;
        }
        AbstractElementSelector s = (AbstractElementSelector)obj;
        return (s.namespaceURI.equals(namespaceURI) &&
                s.localName.equals(localName));
    }
    public String getNamespaceURI() {
        return namespaceURI;
    }
    public String getLocalName() {
        return localName;
    }
    public void fillAttributeSet(Set attrSet) {
    }
}
