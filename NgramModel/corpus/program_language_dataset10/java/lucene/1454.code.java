package org.apache.lucene.analysis;
import java.io.IOException;
import java.io.Closeable;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.Attribute;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.util.AttributeSource;
public abstract class TokenStream extends AttributeSource implements Closeable {
  protected TokenStream() {
    super();
  }
  protected TokenStream(AttributeSource input) {
    super(input);
  }
  protected TokenStream(AttributeFactory factory) {
    super(factory);
  }
  public abstract boolean incrementToken() throws IOException;
  public void end() throws IOException {
  }
  public void reset() throws IOException {}
  public void close() throws IOException {}
}
