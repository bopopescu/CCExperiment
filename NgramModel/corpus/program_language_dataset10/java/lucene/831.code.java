package org.apache.lucene.ant;
import org.apache.lucene.document.Document;
import java.io.File;
public class FileExtensionDocumentHandler
        implements DocumentHandler {
    public Document getDocument(File file)
            throws DocumentHandlerException {
        Document doc = null;
        String name = file.getName();
        try {
            if (name.endsWith(".txt")) {
                doc = TextDocument.Document(file);
            }
            if (name.endsWith(".html")) {
                doc = HtmlDocument.Document(file);
            }
        } catch (java.io.IOException e) {
            throw new DocumentHandlerException(e);
        }
        return doc;
    }
}
