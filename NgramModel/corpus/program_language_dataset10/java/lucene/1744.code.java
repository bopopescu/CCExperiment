package org.apache.lucene.search.spans;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.ArrayList;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.ToStringUtils;
public class SpanFirstQuery extends SpanQuery implements Cloneable {
  private SpanQuery match;
  private int end;
  public SpanFirstQuery(SpanQuery match, int end) {
    this.match = match;
    this.end = end;
  }
  public SpanQuery getMatch() { return match; }
  public int getEnd() { return end; }
  @Override
  public String getField() { return match.getField(); }
  @Override
  public String toString(String field) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("spanFirst(");
    buffer.append(match.toString(field));
    buffer.append(", ");
    buffer.append(end);
    buffer.append(")");
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }
  @Override
  public Object clone() {
    SpanFirstQuery spanFirstQuery = new SpanFirstQuery((SpanQuery) match.clone(), end);
    spanFirstQuery.setBoost(getBoost());
    return spanFirstQuery;
  }
  @Override
  public void extractTerms(Set<Term> terms) {
	    match.extractTerms(terms);
  }
  @Override
  public Spans getSpans(final IndexReader reader) throws IOException {
    return new Spans() {
        private Spans spans = match.getSpans(reader);
        @Override
        public boolean next() throws IOException {
          while (spans.next()) {                  
            if (end() <= end)
              return true;
          }
          return false;
        }
        @Override
        public boolean skipTo(int target) throws IOException {
          if (!spans.skipTo(target))
            return false;
          return spans.end() <= end || next();
        }
        @Override
        public int doc() { return spans.doc(); }
        @Override
        public int start() { return spans.start(); }
        @Override
        public int end() { return spans.end(); }
      @Override
      public Collection<byte[]> getPayload() throws IOException {
        ArrayList<byte[]> result = null;
        if (spans.isPayloadAvailable()) {
          result = new ArrayList<byte[]>(spans.getPayload());
        }
        return result;
      }
      @Override
      public boolean isPayloadAvailable() {
        return spans.isPayloadAvailable();
      }
      @Override
      public String toString() {
          return "spans(" + SpanFirstQuery.this.toString() + ")";
        }
      };
  }
  @Override
  public Query rewrite(IndexReader reader) throws IOException {
    SpanFirstQuery clone = null;
    SpanQuery rewritten = (SpanQuery) match.rewrite(reader);
    if (rewritten != match) {
      clone = (SpanFirstQuery) this.clone();
      clone.match = rewritten;
    }
    if (clone != null) {
      return clone;                        
    } else {
      return this;                         
    }
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SpanFirstQuery)) return false;
    SpanFirstQuery other = (SpanFirstQuery)o;
    return this.end == other.end
         && this.match.equals(other.match)
         && this.getBoost() == other.getBoost();
  }
  @Override
  public int hashCode() {
    int h = match.hashCode();
    h ^= (h << 8) | (h >>> 25);  
    h ^= Float.floatToRawIntBits(getBoost()) ^ end;
    return h;
  }
}
