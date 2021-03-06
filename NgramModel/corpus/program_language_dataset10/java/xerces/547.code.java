package org.apache.xerces.jaxp.validation;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
final class XMLSchema extends AbstractXMLSchema {
    private final XMLGrammarPool fGrammarPool;
    private final boolean fFullyComposed;
    public XMLSchema(XMLGrammarPool grammarPool) {
        this(grammarPool, true);
    }
    public XMLSchema(XMLGrammarPool grammarPool, boolean fullyComposed) {
        fGrammarPool = grammarPool;
        fFullyComposed = fullyComposed;
    }
    public XMLGrammarPool getGrammarPool() {
        return fGrammarPool;
    }
    public boolean isFullyComposed() {
        return fFullyComposed;
    }
} 
