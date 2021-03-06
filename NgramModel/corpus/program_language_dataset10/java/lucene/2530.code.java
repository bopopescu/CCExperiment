package org.apache.solr.search.function;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import java.io.IOException;
import java.util.Map;
public class LongFieldSource extends FieldCacheSource {
  protected FieldCache.LongParser parser;
  public LongFieldSource(String field) {
    this(field, null);
  }
  public LongFieldSource(String field, FieldCache.LongParser parser) {
    super(field);
    this.parser = parser;
  }
  public String description() {
    return "long(" + field + ')';
  }
  public long externalToLong(String extVal) {
    return Long.parseLong(extVal);
  }
  public DocValues getValues(Map context, IndexReader reader) throws IOException {
    final long[] arr = (parser == null) ?
            ((FieldCache) cache).getLongs(reader, field) :
            ((FieldCache) cache).getLongs(reader, field, parser);
    return new DocValues() {
      public float floatVal(int doc) {
        return (float) arr[doc];
      }
      public int intVal(int doc) {
        return (int) arr[doc];
      }
      public long longVal(int doc) {
        return (long) arr[doc];
      }
      public double doubleVal(int doc) {
        return arr[doc];
      }
      public String strVal(int doc) {
        return Long.toString(arr[doc]);
      }
      public String toString(int doc) {
        return description() + '=' + longVal(doc);
      }
      @Override
      public ValueSourceScorer getRangeScorer(IndexReader reader, String lowerVal, String upperVal, boolean includeLower, boolean includeUpper) {
        long lower,upper;
        if (lowerVal==null) {
          lower = Long.MIN_VALUE;
        } else {
          lower = externalToLong(lowerVal);
          if (!includeLower && lower < Long.MAX_VALUE) lower++;
        }
         if (upperVal==null) {
          upper = Long.MAX_VALUE;
        } else {
          upper = externalToLong(upperVal);
          if (!includeUpper && upper > Long.MIN_VALUE) upper--;
        }
        final long ll = lower;
        final long uu = upper;
        return new ValueSourceScorer(reader, this) {
          @Override
          public boolean matchesValue(int doc) {
            long val = arr[doc];
            return val >= ll && val <= uu;
          }
        };
      }
    };
  }
  public boolean equals(Object o) {
    if (o.getClass() != this.getClass()) return false;
    LongFieldSource other = (LongFieldSource) o;
    return super.equals(other)
            && this.parser == null ? other.parser == null :
            this.parser.getClass() == other.parser.getClass();
  }
  public int hashCode() {
    int h = parser == null ? this.getClass().hashCode() : parser.getClass().hashCode();
    h += super.hashCode();
    return h;
  }
}
