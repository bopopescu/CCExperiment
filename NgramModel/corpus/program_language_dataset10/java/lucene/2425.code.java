package org.apache.solr.response;
import org.apache.lucene.document.Document;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.FastWriter;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.DocList;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
public abstract class TextResponseWriter {
  protected final FastWriter writer;
  protected final IndexSchema schema;
  protected final SolrQueryRequest req;
  protected final SolrQueryResponse rsp;
  protected Set<String> returnFields;
  protected int level;
  protected boolean doIndent;
  public TextResponseWriter(Writer writer, SolrQueryRequest req, SolrQueryResponse rsp) {
    this.writer = FastWriter.wrap(writer);
    this.schema = req.getSchema();
    this.req = req;
    this.rsp = rsp;
    String indent = req.getParams().get("indent");
    if (indent != null && !"".equals(indent) && !"off".equals(indent)) {
      doIndent=true;
    }
    returnFields = rsp.getReturnFields();
  }
  public void close() throws IOException {
    writer.flushBuffer();
  }
  public Writer getWriter() { return writer; }
  private static final String[] indentArr = new String[] {
    "\n",
    "\n ",
    "\n  ",
    "\n\t",
    "\n\t ",
    "\n\t  ",  
    "\n\t\t",
    "\n\t\t "};
  public void indent() throws IOException {
     if (doIndent) indent(level);
  }
  public void indent(int lev) throws IOException {
    int arrsz = indentArr.length-1;
    String istr = indentArr[lev & (indentArr.length-1)];
    writer.write(istr);
  }
  public void setLevel(int level) { this.level = level; }
  public int level() { return level; }
  public int incLevel() { return ++level; }
  public int decLevel() { return --level; }
  public void setIndent(boolean doIndent) {
    this.doIndent = doIndent;
  }
  public abstract void writeNamedList(String name, NamedList val) throws IOException;
  public void writeVal(String name, Object val) throws IOException {
    if (val==null) {
      writeNull(name);
    } else if (val instanceof String) {
      writeStr(name, val.toString(), true);
    } else if (val instanceof Integer) {
      writeInt(name, val.toString());
    } else if (val instanceof Boolean) {
      writeBool(name, val.toString());
    } else if (val instanceof Long) {
      writeLong(name, val.toString());
    } else if (val instanceof Date) {
      writeDate(name,(Date)val);
    } else if (val instanceof Float) {
      writeFloat(name, ((Float)val).floatValue());
    } else if (val instanceof Double) {
      writeDouble(name, ((Double)val).doubleValue());
    } else if (val instanceof Document) {
      writeDoc(name, (Document)val, returnFields, 0.0f, false);
    } else if (val instanceof SolrDocument) {
      writeSolrDocument(name, (SolrDocument)val, returnFields, null);
    } else if (val instanceof DocList) {
      writeDocList(name, (DocList)val, returnFields,null);
    } else if (val instanceof SolrDocumentList) {
      writeSolrDocumentList(name, (SolrDocumentList)val, returnFields, null);
    } else if (val instanceof Map) {
      writeMap(name, (Map)val, false, true);
    } else if (val instanceof NamedList) {
      writeNamedList(name, (NamedList)val);
    } else if (val instanceof Iterable) {
      writeArray(name,((Iterable)val).iterator());
    } else if (val instanceof Object[]) {
      writeArray(name,(Object[])val);
    } else if (val instanceof Iterator) {
      writeArray(name,(Iterator)val);
    } else {
      writeStr(name, val.getClass().getName() + ':' + val.toString(), true);
    }
  }
  public abstract void writeDoc(String name, Document doc, Set<String> returnFields, float score, boolean includeScore) throws IOException;
  public abstract void writeSolrDocument(String name, SolrDocument doc, Set<String> returnFields, Map pseudoFields) throws IOException;  
  public abstract void writeDocList(String name, DocList ids, Set<String> fields, Map otherFields) throws IOException;
  public abstract void writeSolrDocumentList(String name, SolrDocumentList docs, Set<String> fields, Map otherFields) throws IOException;  
  public abstract void writeStr(String name, String val, boolean needsEscaping) throws IOException;
  public abstract void writeMap(String name, Map val, boolean excludeOuter, boolean isFirstVal) throws IOException;
  public abstract void writeArray(String name, Object[] val) throws IOException;
  public abstract void writeArray(String name, Iterator val) throws IOException;
  public abstract void writeNull(String name) throws IOException;
  public abstract void writeInt(String name, String val) throws IOException;
  public void writeInt(String name, int val) throws IOException {
    writeInt(name,Integer.toString(val));
  }
  public abstract void writeLong(String name, String val) throws IOException;
  public  void writeLong(String name, long val) throws IOException {
    writeLong(name,Long.toString(val));
  }
  public abstract void writeBool(String name, String val) throws IOException;
  public void writeBool(String name, boolean val) throws IOException {
    writeBool(name,Boolean.toString(val));
  }
  public abstract void writeFloat(String name, String val) throws IOException;
  public void writeFloat(String name, float val) throws IOException {
    writeFloat(name,Float.toString(val));
  }
  public abstract void writeDouble(String name, String val) throws IOException;
  public void writeDouble(String name, double val) throws IOException {
    writeDouble(name,Double.toString(val));
  }
  public abstract void writeDate(String name, Date val) throws IOException;
  public abstract void writeDate(String name, String val) throws IOException;
  public abstract void writeShort(String name, String val) throws IOException;
  public void writeShort(String name, short val) throws IOException{
    writeShort(name, Short.toString(val));
  }
  public abstract void writeByte(String name, String s) throws IOException;
  public void writeByte(String name, byte val) throws IOException{
    writeByte(name, Byte.toString(val));
  }
}
