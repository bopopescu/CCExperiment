package org.apache.lucene.ant;
import org.apache.lucene.document.Field;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.tidy.Tidy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
public class HtmlDocument {
    private Element rawDoc;
    public HtmlDocument(File file) throws IOException {
        Tidy tidy = new Tidy();
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        org.w3c.dom.Document root = null;
        InputStream is = new FileInputStream(file);
        try {
          root =  tidy.parseDOM(is, null);
        } finally {
          is.close();
        }
        rawDoc = root.getDocumentElement();
    }
    public HtmlDocument(InputStream is) {
        Tidy tidy = new Tidy();
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        org.w3c.dom.Document root = tidy.parseDOM(is, null);
        rawDoc = root.getDocumentElement();
    }
    public HtmlDocument(File file, String tidyConfigFile) throws IOException {
        Tidy tidy = new Tidy();
        tidy.setConfigurationFromFile(tidyConfigFile);
        tidy.setQuiet(true);
        tidy.setShowWarnings(false);
        org.w3c.dom.Document root =
                tidy.parseDOM(new FileInputStream(file), null);
        rawDoc = root.getDocumentElement();
    }
    public static org.apache.lucene.document.Document
        Document(File file, String tidyConfigFile) throws IOException {
        HtmlDocument htmlDoc = new HtmlDocument(file, tidyConfigFile);
        org.apache.lucene.document.Document luceneDoc = new org.apache.lucene.document.Document();
        luceneDoc.add(new Field("title", htmlDoc.getTitle(), Field.Store.YES, Field.Index.ANALYZED));
        luceneDoc.add(new Field("contents", htmlDoc.getBody(), Field.Store.YES, Field.Index.ANALYZED));
        String contents = null;
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
        luceneDoc.add(new Field("rawcontents", contents, Field.Store.YES, Field.Index.NO));
        return luceneDoc;
    }
    public static org.apache.lucene.document.Document
            getDocument(InputStream is) {
        HtmlDocument htmlDoc = new HtmlDocument(is);
        org.apache.lucene.document.Document luceneDoc =
                new org.apache.lucene.document.Document();
        luceneDoc.add(new Field("title", htmlDoc.getTitle(), Field.Store.YES, Field.Index.ANALYZED));
        luceneDoc.add(new Field("contents", htmlDoc.getBody(), Field.Store.YES, Field.Index.ANALYZED));
        return luceneDoc;
    }
    public static org.apache.lucene.document.Document
            Document(File file) throws IOException {
        HtmlDocument htmlDoc = new HtmlDocument(file);
        org.apache.lucene.document.Document luceneDoc =
                new org.apache.lucene.document.Document();
        luceneDoc.add(new Field("title", htmlDoc.getTitle(), Field.Store.YES, Field.Index.ANALYZED));
        luceneDoc.add(new Field("contents", htmlDoc.getBody(), Field.Store.YES, Field.Index.ANALYZED));
        String contents = null;
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
        luceneDoc.add(new Field("rawcontents", contents, Field.Store.YES, Field.Index.NO));
        return luceneDoc;
    }
    public static void main(String args[]) throws Exception {
        HtmlDocument doc =
                new HtmlDocument(new FileInputStream(new File(args[0])));
        System.out.println("Title = " + doc.getTitle());
        System.out.println("Body  = " + doc.getBody());
    }
    public String getTitle() {
        if (rawDoc == null) {
            return null;
        }
        String title = "";
        NodeList nl = rawDoc.getElementsByTagName("title");
        if (nl.getLength() > 0) {
            Element titleElement = ((Element) nl.item(0));
            Text text = (Text) titleElement.getFirstChild();
            if (text != null) {
                title = text.getData();
            }
        }
        return title;
    }
    public String getBody() {
        if (rawDoc == null) {
            return null;
        }
        String body = "";
        NodeList nl = rawDoc.getElementsByTagName("body");
        if (nl.getLength() > 0) {
            body = getBodyText(nl.item(0));
        }
        return body;
    }
    private String getBodyText(Node node) {
        NodeList nl = node.getChildNodes();
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < nl.getLength(); i++) {
            Node child = nl.item(i);
            switch (child.getNodeType()) {
                case Node.ELEMENT_NODE:
                    buffer.append(getBodyText(child));
                    buffer.append(" ");
                    break;
                case Node.TEXT_NODE:
                    buffer.append(((Text) child).getData());
                    break;
            }
        }
        return buffer.toString();
    }
}
