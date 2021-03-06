package org.apache.lucene.search.vectorhighlight;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
public interface FragmentsBuilder {
  public String createFragment( IndexReader reader, int docId, String fieldName,
      FieldFragList fieldFragList ) throws IOException;
  public String[] createFragments( IndexReader reader, int docId, String fieldName,
      FieldFragList fieldFragList, int maxNumFragments ) throws IOException;
}
