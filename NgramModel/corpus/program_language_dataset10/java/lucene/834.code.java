package org.apache.lucene.ant;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
public class TextDocument {
    private String contents;
    public TextDocument(File file) throws IOException {
        BufferedReader br =
                new BufferedReader(new FileReader(file));
        StringWriter sw = new StringWriter();
        String line = br.readLine();
        while (line != null) {
            sw.write(line);
            line = br.readLine();
        }
        br.close();
        contents = sw.toString();
        sw.close();
    }
    public static Document Document(File f) throws IOException {
        TextDocument textDoc = new TextDocument(f);
        Document doc = new Document();
        doc.add(new Field("title", f.getName(), Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("contents", textDoc.getContents(), Field.Store.YES, Field.Index.ANALYZED));
        doc.add(new Field("rawcontents", textDoc.getContents(), Field.Store.YES, Field.Index.NO));
        return doc;
    }
    public String getContents() {
        return contents;
    }
}
