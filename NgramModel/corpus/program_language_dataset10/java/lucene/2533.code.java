package org.apache.solr.search.function;
import org.apache.solr.search.function.ValueSource;
public abstract class MultiValueSource extends ValueSource {
  public abstract int dimension();
}
