package org.apache.solr.search.function;
import org.apache.lucene.index.IndexReader;
import java.io.IOException;
import java.util.Map;
 public abstract class SimpleFloatFunction extends SingleFunction {
  public SimpleFloatFunction(ValueSource source) {
    super(source);
  }
  protected abstract float func(int doc, DocValues vals);
  @Override
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    final DocValues vals =  source.getValues(context, reader);
    return new DocValues() {
      public float floatVal(int doc) {
	return func(doc, vals);
      }
      public int intVal(int doc) {
        return (int)floatVal(doc);
      }
      public long longVal(int doc) {
        return (long)floatVal(doc);
      }
      public double doubleVal(int doc) {
        return (double)floatVal(doc);
      }
      public String strVal(int doc) {
        return Float.toString(floatVal(doc));
      }
      public String toString(int doc) {
	return name() + '(' + vals.toString(doc) + ')';
      }
    };
  }
}
