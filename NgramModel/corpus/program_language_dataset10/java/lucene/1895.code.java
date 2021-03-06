package org.apache.lucene.index;
import java.io.IOException;
public class TestMultiReader extends TestDirectoryReader {
  public TestMultiReader(String s) {
    super(s);
  }
  @Override
  protected IndexReader openReader() throws IOException {
    IndexReader reader;
    sis.read(dir);
    SegmentReader reader1 = SegmentReader.get(false, sis.info(0), IndexReader.DEFAULT_TERMS_INDEX_DIVISOR);
    SegmentReader reader2 = SegmentReader.get(false, sis.info(1), IndexReader.DEFAULT_TERMS_INDEX_DIVISOR);
    readers[0] = reader1;
    readers[1] = reader2;
    assertTrue(reader1 != null);
    assertTrue(reader2 != null);
    reader = new MultiReader(readers);
    assertTrue(dir != null);
    assertTrue(sis != null);
    return reader;
  }
}
