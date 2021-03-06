package org.apache.batik.script;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.batik.dom.svg.SVGOMDocument;
import org.apache.batik.util.Service;
import org.w3c.dom.Document;
public class InterpreterPool {
    public static final String BIND_NAME_DOCUMENT = "document";
    protected static Map defaultFactories = new HashMap(7);
    protected Map factories = new HashMap(7);
    static {
        Iterator iter = Service.providers(InterpreterFactory.class);
        while (iter.hasNext()) {
            InterpreterFactory factory = null;
            factory = (InterpreterFactory)iter.next();
            String[] mimeTypes = factory.getMimeTypes();
            for (int i = 0; i < mimeTypes.length; i++) {
                defaultFactories.put(mimeTypes[i], factory);
            }
        }
    }
    public InterpreterPool() {
        factories.putAll(defaultFactories);
    }
    public Interpreter createInterpreter(Document document, 
                                         String language) {
        return createInterpreter(document, language, null);
    }
    public Interpreter createInterpreter(Document document, 
                                         String language,
                                         ImportInfo imports) {
        InterpreterFactory factory;
        factory = (InterpreterFactory)factories.get(language);
        if (factory == null) return null;
        if (imports == null)
            imports = ImportInfo.getImports();
        Interpreter interpreter = null;
        SVGOMDocument svgDoc = (SVGOMDocument) document;
        URL url = null;
        try {
            url = new URL(svgDoc.getDocumentURI());
        } catch (MalformedURLException e) {
        }
        interpreter = factory.createInterpreter(url, svgDoc.isSVG12(),
                                                imports);
        if (interpreter == null) return null;
        if (document != null)
            interpreter.bindObject(BIND_NAME_DOCUMENT, document);
        return interpreter;
    }
    public void putInterpreterFactory(String language, 
                                      InterpreterFactory factory) {
        factories.put(language, factory);
    }
    public void removeInterpreterFactory(String language) {
        factories.remove(language);
    }
}
