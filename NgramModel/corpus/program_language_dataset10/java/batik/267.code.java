package org.apache.batik.css.dom;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.StyleDeclaration;
import org.apache.batik.css.engine.StyleDeclarationProvider;
import org.apache.batik.css.engine.value.Value;
public abstract class CSSOMStoredStyleDeclaration
    extends CSSOMSVGStyleDeclaration
    implements CSSOMStyleDeclaration.ValueProvider,
               CSSOMStyleDeclaration.ModificationHandler,
               StyleDeclarationProvider {
    protected StyleDeclaration declaration;
    public CSSOMStoredStyleDeclaration(CSSEngine eng) {
        super(null, null, eng);
        valueProvider = this;
        setModificationHandler(this);
    }
    public StyleDeclaration getStyleDeclaration() {
        return declaration;
    }
    public void setStyleDeclaration(StyleDeclaration sd) {
        declaration = sd;
    }
    public Value getValue(String name) {
        int idx = cssEngine.getPropertyIndex(name);
        for (int i = 0; i < declaration.size(); i++) {
            if (idx == declaration.getIndex(i)) {
                return declaration.getValue(i);
            }
        }
        return null;
    }
    public boolean isImportant(String name) {
        int idx = cssEngine.getPropertyIndex(name);
        for (int i = 0; i < declaration.size(); i++) {
            if (idx == declaration.getIndex(i)) {
                return declaration.getPriority(i);
            }
        }
        return false;
    }
    public String getText() {
        return declaration.toString(cssEngine);
    }
    public int getLength() {
        return declaration.size();
    }
    public String item(int idx) {
        return cssEngine.getPropertyName(declaration.getIndex(idx));
    }
}
