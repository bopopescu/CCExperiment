package org.apache.solr.search.function;
import org.apache.lucene.search.FieldCache;
public abstract class FieldCacheSource extends ValueSource {
  protected String field;
  protected FieldCache cache = FieldCache.DEFAULT;
  public FieldCacheSource(String field) {
    this.field=field;
  }
  public FieldCache getFieldCache() {
    return cache;
  }
  public String description() {
    return field;
  }
  public boolean equals(Object o) {
    if (!(o instanceof FieldCacheSource)) return false;
    FieldCacheSource other = (FieldCacheSource)o;
    return this.field.equals(other.field)
           && this.cache == other.cache;
  }
  public int hashCode() {
    return cache.hashCode() + field.hashCode();
  };
}
