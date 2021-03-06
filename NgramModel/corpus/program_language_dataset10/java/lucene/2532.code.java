package org.apache.solr.search.function;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Searcher;
import java.util.Map;
import java.util.Arrays;
import java.io.IOException;
public abstract class MultiFloatFunction extends ValueSource {
  protected final ValueSource[] sources;
  public MultiFloatFunction(ValueSource[] sources) {
    this.sources = sources;
  }
  abstract protected String name();
  abstract protected float func(int doc, DocValues[] valsArr);
  public String description() {
    StringBuilder sb = new StringBuilder();
    sb.append(name()).append('(');
    boolean firstTime=true;
    for (ValueSource source : sources) {
      if (firstTime) {
        firstTime=false;
      } else {
        sb.append(',');
      }
      sb.append(source);
    }
    sb.append(')');
    return sb.toString();
  }
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    final DocValues[] valsArr = new DocValues[sources.length];
    for (int i=0; i<sources.length; i++) {
      valsArr[i] = sources[i].getValues(context, reader);
    }
    return new DocValues() {
      public float floatVal(int doc) {
        return func(doc, valsArr);
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
        StringBuilder sb = new StringBuilder();
        sb.append(name()).append('(');
        boolean firstTime=true;
        for (DocValues vals : valsArr) {
          if (firstTime) {
            firstTime=false;
          } else {
            sb.append(',');
          }
          sb.append(vals.toString(doc));
        }
        sb.append(')');
        return sb.toString();
      }
    };
  }
  @Override
  public void createWeight(Map context, Searcher searcher) throws IOException {
    for (ValueSource source : sources)
      source.createWeight(context, searcher);
  }
  public int hashCode() {
    return Arrays.hashCode(sources) + name().hashCode();
  }
  public boolean equals(Object o) {
    if (this.getClass() != o.getClass()) return false;
    MultiFloatFunction other = (MultiFloatFunction)o;
    return this.name().equals(other.name())
            && Arrays.equals(this.sources, other.sources);
  }
}
