package org.apache.solr.response;
import java.io.Writer;
import java.io.IOException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
public class PHPResponseWriter implements QueryResponseWriter {
  static String CONTENT_TYPE_PHP_UTF8="text/x-php;charset=UTF-8";
  public void init(NamedList n) {
  }
 public void write(Writer writer, SolrQueryRequest req, SolrQueryResponse rsp) throws IOException {
    PHPWriter w = new PHPWriter(writer, req, rsp);
    try {
      w.writeResponse();
    } finally {
      w.close();
    }
  }
  public String getContentType(SolrQueryRequest request, SolrQueryResponse response) {
    return CONTENT_TYPE_TEXT_UTF8;
  }
}
class PHPWriter extends JSONWriter {
  public PHPWriter(Writer writer, SolrQueryRequest req, SolrQueryResponse rsp) {
    super(writer, req, rsp);
  }
  @Override
  public void writeNamedList(String name, NamedList val) throws IOException {
    writeNamedListAsMapMangled(name,val);
  }
  @Override
  public void writeMapOpener(int size) throws IOException {
    writer.write("array(");
  }
  @Override
  public void writeMapCloser() throws IOException {
    writer.write(')');
  }
  @Override
  public void writeArrayOpener(int size) throws IOException {
    writer.write("array(");
  }
  @Override
  public void writeArrayCloser() throws IOException {
    writer.write(')');
  }
  @Override
  public void writeNull(String name) throws IOException {
    writer.write("null");
  }
  @Override
  protected void writeKey(String fname, boolean needsEscaping) throws IOException {
    writeStr(null, fname, needsEscaping);
    writer.write('=');
    writer.write('>');
  }
  @Override
  public void writeStr(String name, String val, boolean needsEscaping) throws IOException {
    if (needsEscaping) {
      writer.write('\'');
      for (int i=0; i<val.length(); i++) {
        char ch = val.charAt(i);
        switch (ch) {
          case '\'':
          case '\\': writer.write('\\'); writer.write(ch); break;
          default:
            writer.write(ch);
        }
      }
      writer.write('\'');
    } else {
      writer.write('\'');
      writer.write(val);
      writer.write('\'');
    }
  }
}
