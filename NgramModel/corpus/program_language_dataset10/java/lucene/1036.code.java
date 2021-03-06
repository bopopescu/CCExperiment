package org.apache.lucene.store.instantiated;
import junit.framework.TestCase;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
public class TestRealTime extends TestCase {
  public void test() throws Exception {
    InstantiatedIndex index = new InstantiatedIndex();
    InstantiatedIndexReader reader = new InstantiatedIndexReader(index);
    IndexSearcher searcher = new IndexSearcher(reader);
    InstantiatedIndexWriter writer = new InstantiatedIndexWriter(index);
    Document doc;
    Collector collector;
    doc = new Document();
    doc.add(new Field("f", "a", Field.Store.NO, Field.Index.NOT_ANALYZED));
    writer.addDocument(doc);
    writer.commit();
    collector = new Collector();
    searcher.search(new TermQuery(new Term("f", "a")), collector);
    assertEquals(1, collector.hits);
    doc = new Document();
    doc.add(new Field("f", "a", Field.Store.NO, Field.Index.NOT_ANALYZED));
    writer.addDocument(doc);
    writer.commit();
    collector = new Collector();
    searcher.search(new TermQuery(new Term("f", "a")), collector);
    assertEquals(2, collector.hits);
  }
  public static class Collector extends org.apache.lucene.search.Collector {
    private int hits = 0;
    @Override
    public void setScorer(Scorer scorer) {}
    @Override
    public void setNextReader(IndexReader reader, int docBase) {}
    @Override
    public boolean acceptsDocsOutOfOrder() { return true; }
    @Override
    public void collect(int doc) {
      hits++;
    }
  }
}
