package org.apache.lucene.search;
import java.io.IOException;
import java.util.BitSet;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.TimeLimitingCollector.TimeExceededException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util.ThreadInterruptedException;
public class TestTimeLimitingCollector extends LuceneTestCase {
  private static final int SLOW_DOWN = 3;
  private static final long TIME_ALLOWED = 17 * SLOW_DOWN; 
  private static final double MULTI_THREAD_SLACK = 7;      
  private static final int N_DOCS = 3000;
  private static final int N_THREADS = 50;
  private Searcher searcher;
  private final String FIELD_NAME = "body";
  private Query query;
  public TestTimeLimitingCollector(String name) {
    super(name);
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    final String docText[] = {
        "docThatNeverMatchesSoWeCanRequireLastDocCollectedToBeGreaterThanZero",
        "one blah three",
        "one foo three multiOne",
        "one foobar three multiThree",
        "blueberry pancakes",
        "blueberry pie",
        "blueberry strudel",
        "blueberry pizza",
    };
    Directory directory = new RAMDirectory();
    IndexWriter iw = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new WhitespaceAnalyzer(TEST_VERSION_CURRENT)));
    for (int i=0; i<N_DOCS; i++) {
      add(docText[i%docText.length], iw);
    }
    iw.close();
    searcher = new IndexSearcher(directory, true);
    String qtxt = "one";
    for (int i = 1; i < docText.length; i++) {
      qtxt += ' ' + docText[i]; 
    }
    QueryParser queryParser = new QueryParser(TEST_VERSION_CURRENT, FIELD_NAME, new WhitespaceAnalyzer(TEST_VERSION_CURRENT));
    query = queryParser.parse(qtxt);
    searcher.search(query, null, 1000);
  }
  @Override
  protected void tearDown() throws Exception {
    searcher.close();
    super.tearDown();
  }
  private void add(String value, IndexWriter iw) throws IOException {
    Document d = new Document();
    d.add(new Field(FIELD_NAME, value, Field.Store.NO, Field.Index.ANALYZED));
    iw.addDocument(d);
  }
  private void search(Collector collector) throws Exception {
    searcher.search(query, collector);
  }
  public void testSearch() {
    doTestSearch();
  }
  private void doTestSearch() {
    int totalResults = 0;
    int totalTLCResults = 0;
    try {
      MyHitCollector myHc = new MyHitCollector();
      search(myHc);
      totalResults = myHc.hitCount();
      myHc = new MyHitCollector();
      long oneHour = 3600000;
      Collector tlCollector = createTimedCollector(myHc, oneHour, false);
      search(tlCollector);
      totalTLCResults = myHc.hitCount();
    } catch (Exception e) {
      e.printStackTrace();
      assertTrue("Unexpected exception: "+e, false); 
    }
    assertEquals( "Wrong number of results!", totalResults, totalTLCResults );
  }
  private Collector createTimedCollector(MyHitCollector hc, long timeAllowed, boolean greedy) {
    TimeLimitingCollector res = new TimeLimitingCollector(hc, timeAllowed);
    res.setGreedy(greedy); 
    return res;
  }
  public void testTimeoutGreedy() {
    doTestTimeout(false, true);
  }
  public void testTimeoutNotGreedy() {
    doTestTimeout(false, false);
  }
  private void doTestTimeout(boolean multiThreaded, boolean greedy) {
    MyHitCollector myHc = new MyHitCollector();
    myHc.setSlowDown(SLOW_DOWN);
    Collector tlCollector = createTimedCollector(myHc, TIME_ALLOWED, greedy);
    TimeExceededException timoutException = null;
    try {
      search(tlCollector);
    } catch (TimeExceededException x) {
      timoutException = x;
    } catch (Exception e) {
      assertTrue("Unexpected exception: "+e, false); 
    }
    assertNotNull( "Timeout expected!", timoutException );
    int exceptionDoc = timoutException.getLastDocCollected();
    int lastCollected = myHc.getLastDocCollected(); 
    assertTrue( "doc collected at timeout must be > 0!", exceptionDoc > 0 );
    if (greedy) {
      assertTrue("greedy="+greedy+" exceptionDoc="+exceptionDoc+" != lastCollected="+lastCollected, exceptionDoc==lastCollected);
      assertTrue("greedy, but no hits found!", myHc.hitCount() > 0 );
    } else {
      assertTrue("greedy="+greedy+" exceptionDoc="+exceptionDoc+" not > lastCollected="+lastCollected, exceptionDoc>lastCollected);
    }
    assertEquals( timoutException.getTimeAllowed(), TIME_ALLOWED);
    assertTrue ( "elapsed="+timoutException.getTimeElapsed()+" <= (allowed-resolution)="+(TIME_ALLOWED-TimeLimitingCollector.getResolution()),
        timoutException.getTimeElapsed() > TIME_ALLOWED-TimeLimitingCollector.getResolution());
    if (timoutException.getTimeElapsed() > maxTime(multiThreaded)) {
      System.out.println("Informative: timeout exceeded (no action required: most probably just " +
        " because the test machine is slower than usual):  " +
        "lastDoc="+exceptionDoc+
        " ,&& allowed="+timoutException.getTimeAllowed() +
        " ,&& elapsed="+timoutException.getTimeElapsed() +
        " >= " + maxTimeStr(multiThreaded));
    }
  }
  private long maxTime(boolean multiThreaded) {
    long res = 2 * TimeLimitingCollector.getResolution() + TIME_ALLOWED + SLOW_DOWN; 
    if (multiThreaded) {
      res *= MULTI_THREAD_SLACK; 
    }
    return res;
  }
  private String maxTimeStr(boolean multiThreaded) {
    String s =
      "( " +
      "2*resolution +  TIME_ALLOWED + SLOW_DOWN = " +
      "2*" + TimeLimitingCollector.getResolution() + " + " + TIME_ALLOWED + " + " + SLOW_DOWN +
      ")";
    if (multiThreaded) {
      s = MULTI_THREAD_SLACK + " * "+s;  
    }
    return maxTime(multiThreaded) + " = " + s;
  }
  public void testModifyResolution() {
    try {
      long resolution = 20 * TimeLimitingCollector.DEFAULT_RESOLUTION; 
      TimeLimitingCollector.setResolution(resolution);
      assertEquals(resolution, TimeLimitingCollector.getResolution());
      doTestTimeout(false,true);
      resolution = 5;
      TimeLimitingCollector.setResolution(resolution);
      assertEquals(resolution, TimeLimitingCollector.getResolution());
      doTestTimeout(false,true);
      resolution = TimeLimitingCollector.DEFAULT_RESOLUTION;
      TimeLimitingCollector.setResolution(resolution);
      assertEquals(resolution, TimeLimitingCollector.getResolution());
      doTestTimeout(false,true);
    } finally {
      TimeLimitingCollector.setResolution(TimeLimitingCollector.DEFAULT_RESOLUTION);
    }
  }
  public void testSearchMultiThreaded() throws Exception {
    doTestMultiThreads(false);
  }
  public void testTimeoutMultiThreaded() throws Exception {
    doTestMultiThreads(true);
  }
  private void doTestMultiThreads(final boolean withTimeout) throws Exception {
    Thread [] threadArray = new Thread[N_THREADS];
    final BitSet success = new BitSet(N_THREADS);
    for( int i = 0; i < threadArray.length; ++i ) {
      final int num = i;
      threadArray[num] = new Thread() {
          @Override
          public void run() {
            if (withTimeout) {
              doTestTimeout(true,true);
            } else {
              doTestSearch();
            }
            synchronized(success) {
              success.set(num);
            }
          }
      };
    }
    for( int i = 0; i < threadArray.length; ++i ) {
      threadArray[i].start();
    }
    for( int i = 0; i < threadArray.length; ++i ) {
      threadArray[i].join();
    }
    assertEquals("some threads failed!", N_THREADS,success.cardinality());
  }
  private class MyHitCollector extends Collector {
    private final BitSet bits = new BitSet();
    private int slowdown = 0;
    private int lastDocCollected = -1;
    private int docBase = 0;
    public void setSlowDown( int milliseconds ) {
      slowdown = milliseconds;
    }
    public int hitCount() {
      return bits.cardinality();
    }
    public int getLastDocCollected() {
      return lastDocCollected;
    }
    @Override
    public void setScorer(Scorer scorer) throws IOException {
    }
    @Override
    public void collect(final int doc) throws IOException {
      int docId = doc + docBase;
      if( slowdown > 0 ) {
        try {
          Thread.sleep(slowdown);
        } catch (InterruptedException ie) {
          throw new ThreadInterruptedException(ie);
        }
      }
      assert docId >= 0: " base=" + docBase + " doc=" + doc;
      bits.set( docId );
      lastDocCollected = docId;
    }
    @Override
    public void setNextReader(IndexReader reader, int base) {
      docBase = base;
    }
    @Override
    public boolean acceptsDocsOutOfOrder() {
      return false;
    }
  }
}
