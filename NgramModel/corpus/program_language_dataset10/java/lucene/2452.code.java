package org.apache.solr.schema;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.SortField;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import java.util.Map;
import java.io.IOException;
public final class SchemaField extends FieldProperties {
  final String name;
  final FieldType type;
  final int properties;
  final String defaultValue;
  boolean required = false;  
  public SchemaField(String name, FieldType type) {
    this(name, type, type.properties, null);
  }
  public SchemaField(SchemaField prototype, String name) {
    this(name, prototype.type, prototype.properties, prototype.defaultValue );
  }
  public SchemaField(String name, FieldType type, int properties, String defaultValue ) {
    this.name = name;
    this.type = type;
    this.properties = properties;
    this.defaultValue = defaultValue;
    required = (properties & REQUIRED) !=0;
  }
  public String getName() { return name; }
  public FieldType getType() { return type; }
  int getProperties() { return properties; }
  public boolean indexed() { return (properties & INDEXED)!=0; }
  public boolean stored() { return (properties & STORED)!=0; }
  public boolean storeTermVector() { return (properties & STORE_TERMVECTORS)!=0; }
  public boolean storeTermPositions() { return (properties & STORE_TERMPOSITIONS)!=0; }
  public boolean storeTermOffsets() { return (properties & STORE_TERMOFFSETS)!=0; }
  public boolean omitNorms() { return (properties & OMIT_NORMS)!=0; }
  public boolean omitTf() { return (properties & OMIT_TF_POSITIONS)!=0; }
  public boolean multiValued() { return (properties & MULTIVALUED)!=0; }
  public boolean sortMissingFirst() { return (properties & SORT_MISSING_FIRST)!=0; }
  public boolean sortMissingLast() { return (properties & SORT_MISSING_LAST)!=0; }
  public boolean isRequired() { return required; } 
  boolean isTokenized() { return (properties & TOKENIZED)!=0; }
  boolean isBinary() { return (properties & BINARY)!=0; }
  public Field createField(String val, float boost) {
    return type.createField(this,val,boost);
  }
  public Fieldable[] createFields(String val, float boost) {
    return type.createFields(this,val,boost);
  }
  public boolean isPolyField(){
    return type.isPolyField();
  }
  @Override
  public String toString() {
    return name + "{type="+type.getTypeName()
      + ((defaultValue==null)?"":(",default="+defaultValue))
      + ",properties=" + propertiesToString(properties)
      + ( required ? ", required=true" : "" )
      + "}";
  }
  public void write(XMLWriter writer, String name, Fieldable val) throws IOException {
    type.write(writer,name,val);
  }
  public void write(TextResponseWriter writer, String name, Fieldable val) throws IOException {
    type.write(writer,name,val);
  }
  public SortField getSortField(boolean top) {
    return type.getSortField(this, top);
  }
  static SchemaField create(String name, FieldType ft, Map<String,String> props) {
    String defaultValue = null;
    if( props.containsKey( "default" ) ) {
    	defaultValue = (String)props.get( "default" );
    }
    return new SchemaField(name, ft, calcProps(name, ft, props), defaultValue );
  }
  static SchemaField create(String name, FieldType ft, int props, String defValue){
    return new SchemaField(name, ft, props, defValue);
  }
  static int calcProps(String name, FieldType ft, Map<String, String> props) {
    int trueProps = parseProperties(props,true);
    int falseProps = parseProperties(props,false);
    int p = ft.properties;
    if (on(falseProps,STORED)) {
      int pp = STORED | BINARY;
      if (on(pp,trueProps)) {
        throw new RuntimeException("SchemaField: " + name + " conflicting stored field options:" + props);
      }
      p &= ~pp;
    }
    if (on(falseProps,INDEXED)) {
      int pp = (INDEXED | OMIT_NORMS | OMIT_TF_POSITIONS
              | STORE_TERMVECTORS | STORE_TERMPOSITIONS | STORE_TERMOFFSETS
              | SORT_MISSING_FIRST | SORT_MISSING_LAST);
      if (on(pp,trueProps)) {
        throw new RuntimeException("SchemaField: " + name + " conflicting indexed field options:" + props);
      }
      p &= ~pp;
    }
    if (on(falseProps,STORE_TERMVECTORS)) {
      int pp = (STORE_TERMVECTORS | STORE_TERMPOSITIONS | STORE_TERMOFFSETS);
      if (on(pp,trueProps)) {
        throw new RuntimeException("SchemaField: " + name + " conflicting termvector field options:" + props);
      }
      p &= ~pp;
    }
    if (on(trueProps,SORT_MISSING_FIRST)) {
      p &= ~SORT_MISSING_LAST;
    }
    if (on(trueProps,SORT_MISSING_LAST)) {
      p &= ~SORT_MISSING_FIRST;
    }
    p &= ~falseProps;
    p |= trueProps;
    return p;
  }
  public String getDefaultValue() {
    return defaultValue;
  }
  @Override
  public int hashCode() {
    return name.hashCode();
  }
  @Override
  public boolean equals(Object obj) {
    return(obj instanceof SchemaField) && name.equals(((SchemaField)obj).name);
  }
}
