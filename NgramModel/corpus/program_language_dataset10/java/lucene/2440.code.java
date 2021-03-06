package org.apache.solr.schema;
import org.apache.lucene.search.SortField;
import org.apache.lucene.document.Fieldable;
import org.apache.solr.search.function.ValueSource;
import org.apache.solr.search.function.FloatFieldSource;
import org.apache.solr.search.function.FileFloatSource;
import org.apache.solr.search.QParser;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import org.apache.solr.common.SolrException;
import java.util.Map;
import java.io.IOException;
public class ExternalFileField extends FieldType {
  private FieldType ftype;
  private String keyFieldName;
  private IndexSchema schema;
  private float defVal;
  protected void init(IndexSchema schema, Map<String,String> args) {
    restrictProps(SORT_MISSING_FIRST | SORT_MISSING_LAST);
    String ftypeS = getArg("valType", args);
    if (ftypeS!=null) {
      ftype = schema.getFieldTypes().get(ftypeS);
      if (ftype==null || !(ftype instanceof FloatField)) {
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Only float (FloatField) is currently supported as external field type.  got " + ftypeS);
      }
    }   
    keyFieldName = args.remove("keyField");
    String defValS = args.remove("defVal");
    defVal = defValS==null ? 0 : Float.parseFloat(defValS);
    this.schema = schema;
  }
  public void write(XMLWriter xmlWriter, String name, Fieldable f) throws IOException {
    throw new UnsupportedOperationException();
  }
  public void write(TextResponseWriter writer, String name, Fieldable f) throws IOException {
    throw new UnsupportedOperationException();
  }
  public SortField getSortField(SchemaField field,boolean reverse) {
    throw new UnsupportedOperationException();
  }
  public ValueSource getValueSource(SchemaField field, QParser parser) {
    SchemaField keyField = keyFieldName==null ? schema.getUniqueKeyField() : schema.getField(keyFieldName);
    return new FileFloatSource(field, keyField, defVal, parser);
  }
}
