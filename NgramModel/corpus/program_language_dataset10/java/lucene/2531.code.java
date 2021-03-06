package org.apache.solr.search.function;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Searcher;
import java.io.IOException;
import java.util.Map;
public class MaxFloatFunction extends ValueSource {
  protected final ValueSource source;
  protected final float fval;
  public MaxFloatFunction(ValueSource source, float fval) {
    this.source = source;
    this.fval = fval;
  }
  public String description() {
    return "max(" + source.description() + "," + fval + ")";
  }
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    final DocValues vals =  source.getValues(context, reader);
    return new DocValues() {
      public float floatVal(int doc) {
	float v = vals.floatVal(doc);
        return v < fval ? fval : v;
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
	return "max(" + vals.toString(doc) + "," + fval + ")";
      }
    };
  }
  @Override
  public void createWeight(Map context, Searcher searcher) throws IOException {
    source.createWeight(context, searcher);
  }
  public int hashCode() {
    int h = Float.floatToIntBits(fval);
    h = (h >>> 2) | (h << 30);
    return h + source.hashCode();
  }
  public boolean equals(Object o) {
    if (MaxFloatFunction.class != o.getClass()) return false;
    MaxFloatFunction other = (MaxFloatFunction)o;
    return  this.fval == other.fval
         && this.source.equals(other.source);
  }
}
