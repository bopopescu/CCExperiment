package org.apache.solr.search.function;
import org.apache.lucene.search.Searcher;
import java.io.IOException;
import java.util.Map;
 public abstract class SingleFunction extends ValueSource {
  protected final ValueSource source;
  public SingleFunction(ValueSource source) {
    this.source = source;
  }
  protected abstract String name();
  public String description() {
    return name() + '(' + source.description() + ')';
  }
  public int hashCode() {
    return source.hashCode() + name().hashCode();
  }
  public boolean equals(Object o) {
    if (this.getClass() != o.getClass()) return false;
    SingleFunction other = (SingleFunction)o;
    return this.name().equals(other.name())
         && this.source.equals(other.source);
  }
  @Override
  public void createWeight(Map context, Searcher searcher) throws IOException {
    source.createWeight(context, searcher);
  }
}