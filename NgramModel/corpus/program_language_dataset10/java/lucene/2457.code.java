package org.apache.solr.schema;
import org.apache.lucene.search.SortField;
import org.apache.solr.search.function.ValueSource;
import org.apache.solr.search.function.FieldCacheSource;
import org.apache.solr.search.function.DocValues;
import org.apache.solr.search.function.StringIndexDocValues;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexReader;
import org.apache.solr.util.NumberUtils;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import java.util.Map;
import java.io.IOException;
public class SortableIntField extends FieldType {
  protected void init(IndexSchema schema, Map<String,String> args) {
  }
  public SortField getSortField(SchemaField field,boolean reverse) {
    return getStringSort(field,reverse);
  }
  public ValueSource getValueSource(SchemaField field) {
    return new SortableIntFieldSource(field.name);
  }
  public String toInternal(String val) {
    return NumberUtils.int2sortableStr(val);
  }
  public String toExternal(Fieldable f) {
    return indexedToReadable(f.stringValue());
  }
  public String indexedToReadable(String indexedForm) {
    return NumberUtils.SortableStr2int(indexedForm);
  }
  @Override
  public Integer toObject(Fieldable f) {
    return NumberUtils.SortableStr2int(f.stringValue(), 0, 3);    
  }
  public void write(XMLWriter xmlWriter, String name, Fieldable f) throws IOException {
    String sval = f.stringValue();
    xmlWriter.writeInt(name, NumberUtils.SortableStr2int(sval,0,sval.length()));
  }
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException {
    String sval = f.stringValue();
    writer.writeInt(name, NumberUtils.SortableStr2int(sval,0,sval.length()));
  }
}
class SortableIntFieldSource extends FieldCacheSource {
  protected int defVal;
  public SortableIntFieldSource(String field) {
    this(field, 0);
  }
  public SortableIntFieldSource(String field, int defVal) {
    super(field);
    this.defVal = defVal;
  }
  public String description() {
    return "sint(" + field + ')';
  }
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    final int def = defVal;
    return new StringIndexDocValues(this, reader, field) {
      protected String toTerm(String readableValue) {
        return NumberUtils.int2sortableStr(readableValue);
      }
      public float floatVal(int doc) {
        return (float)intVal(doc);
      }
      public int intVal(int doc) {
        int ord=order[doc];
        return ord==0 ? def  : NumberUtils.SortableStr2int(lookup[ord],0,3);
      }
      public long longVal(int doc) {
        return (long)intVal(doc);
      }
      public double doubleVal(int doc) {
        return (double)intVal(doc);
      }
      public String strVal(int doc) {
        return Integer.toString(intVal(doc));
      }
      public String toString(int doc) {
        return description() + '=' + intVal(doc);
      }
    };
  }
  public boolean equals(Object o) {
    return o instanceof SortableIntFieldSource
            && super.equals(o)
            && defVal == ((SortableIntFieldSource)o).defVal;
  }
  private static int hcode = SortableIntFieldSource.class.hashCode();
  public int hashCode() {
    return hcode + super.hashCode() + defVal;
  };
}
