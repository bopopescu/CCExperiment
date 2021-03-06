package org.apache.lucene.misc;
import java.io.IOException;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.FieldNormModifier;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
public class TestLengthNormModifier extends LuceneTestCase {
    public TestLengthNormModifier(String name) {
	super(name);
    }
    public static int NUM_DOCS = 5;
    public Directory store = new RAMDirectory();
    public static Similarity s = new DefaultSimilarity() {
	    @Override
	    public float lengthNorm(String fieldName, int numTokens) {
		return numTokens;
	    }
	};
    @Override
    protected void setUp() throws Exception {
      super.setUp();
	IndexWriter writer = new IndexWriter(store, new IndexWriterConfig(
        TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)));
	for (int i = 0; i < NUM_DOCS; i++) {
	    Document d = new Document();
	    d.add(new Field("field", "word",
			    Field.Store.YES, Field.Index.ANALYZED));
	    d.add(new Field("nonorm", "word",
			    Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
	    for (int j = 1; j <= i; j++) {
		d.add(new Field("field", "crap",
				Field.Store.YES, Field.Index.ANALYZED));
		d.add(new Field("nonorm", "more words",
				Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS));
	    }
	    writer.addDocument(d);
	}
	writer.close();
    }
    public void testMissingField() {
	FieldNormModifier fnm = new FieldNormModifier(store, s);
	try {
	    fnm.reSetNorms("nobodyherebutuschickens");
	} catch (Exception e) {
	    assertNull("caught something", e);
	}
    }
    public void testFieldWithNoNorm() throws Exception {
	IndexReader r = IndexReader.open(store, false);
	byte[] norms = r.norms("nonorm");
	assertTrue("Whoops we have norms?", !r.hasNorms("nonorm"));
	assertNull(norms);
	r.close();
	FieldNormModifier fnm = new FieldNormModifier(store, s);
	try {
	    fnm.reSetNorms("nonorm");
	} catch (Exception e) {
	    assertNull("caught something", e);
	}
	r = IndexReader.open(store, false);
	norms = r.norms("nonorm");
	assertTrue("Whoops we have norms?", !r.hasNorms("nonorm"));
  assertNull(norms);
	r.close();
    }
    public void testGoodCases() throws Exception {
	IndexSearcher searcher;
	final float[] scores = new float[NUM_DOCS];
	float lastScore = 0.0f;
  searcher = new IndexSearcher(store, false);
  searcher.search(new TermQuery(new Term("field", "word")), new Collector() {
    private int docBase = 0;
    private Scorer scorer;
    @Override
    public final void collect(int doc) throws IOException {
      scores[doc + docBase] = scorer.score();
    }
    @Override
    public void setNextReader(IndexReader reader, int docBase) {
      this.docBase = docBase;
    }
    @Override
    public void setScorer(Scorer scorer) throws IOException {
      this.scorer = scorer;
    }
    @Override
    public boolean acceptsDocsOutOfOrder() {
      return true;
    }
  });
  searcher.close();
	lastScore = Float.MAX_VALUE;
	for (int i = 0; i < NUM_DOCS; i++) {
	    String msg = "i=" + i + ", "+scores[i]+" <= "+lastScore;
	    assertTrue(msg, scores[i] <= lastScore);
	    lastScore = scores[i];
	}
	Similarity s = new DefaultSimilarity() {
		@Override
		public float lengthNorm(String fieldName, int numTokens) {
		    return numTokens;
		}
	    };
	FieldNormModifier fnm = new FieldNormModifier(store, s);
	fnm.reSetNorms("field");
	searcher = new IndexSearcher(store, false);
	searcher.search(new TermQuery(new Term("field", "word")), new Collector() {
      private int docBase = 0;
      private Scorer scorer;
      @Override
      public final void collect(int doc) throws IOException {
        scores[doc + docBase] = scorer.score();
      }
      @Override
      public void setNextReader(IndexReader reader, int docBase) {
        this.docBase = docBase;
      }
      @Override
      public void setScorer(Scorer scorer) throws IOException {
        this.scorer = scorer;
      }
      @Override
      public boolean acceptsDocsOutOfOrder() {
        return true;
      }
    });
    searcher.close();
	lastScore = 0.0f;
	for (int i = 0; i < NUM_DOCS; i++) {
	    String msg = "i=" + i + ", "+scores[i]+" >= "+lastScore;
	    assertTrue(msg, scores[i] >= lastScore);
	    lastScore = scores[i];
	}
    }
}
