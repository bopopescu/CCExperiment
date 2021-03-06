package org.apache.lucene.search.function;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import java.io.IOException;
final class JustCompileSearchFunction {
  private static final String UNSUPPORTED_MSG = "unsupported: used for back-compat testing only !";
  static final class JustCompileDocValues extends DocValues {
    @Override
    public float floatVal(int doc) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public String toString(int doc) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileFieldCacheSource extends FieldCacheSource {
    public JustCompileFieldCacheSource(String field) {
      super(field);
    }
    @Override
    public boolean cachedFieldSourceEquals(FieldCacheSource other) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public int cachedFieldSourceHashCode() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public DocValues getCachedFieldValues(FieldCache cache, String field,
                                          IndexReader reader) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
  static final class JustCompileValueSource extends ValueSource {
    @Override
    public String description() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public boolean equals(Object o) {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public DocValues getValues(IndexReader reader) throws IOException {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
    @Override
    public int hashCode() {
      throw new UnsupportedOperationException(UNSUPPORTED_MSG);
    }
  }
}
