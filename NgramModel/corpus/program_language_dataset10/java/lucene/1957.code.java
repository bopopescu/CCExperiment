package org.apache.lucene.search;
import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.English;
import java.io.IOException;
public class TestMultiThreadTermVectors extends LuceneTestCase {
  private RAMDirectory directory = new RAMDirectory();
  public int numDocs = 100;
  public int numThreads = 3;
  public TestMultiThreadTermVectors(String s) {
    super(s);
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(TEST_VERSION_CURRENT, new SimpleAnalyzer(TEST_VERSION_CURRENT)));
    for (int i = 0; i < numDocs; i++) {
      Document doc = new Document();
      Fieldable fld = new Field("field", English.intToEnglish(i), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.YES);
      doc.add(fld);
      writer.addDocument(doc);
    }
    writer.close();
  }
  public void test() throws Exception {
    IndexReader reader = null;
    try {
      reader = IndexReader.open(directory, true);
      for(int i = 1; i <= numThreads; i++)
        testTermPositionVectors(reader, i);
    }
    catch (IOException ioe) {
      fail(ioe.getMessage());
    }
    finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ioe) {
          ioe.printStackTrace();
        }
      }
    }
  }
  public void testTermPositionVectors(final IndexReader reader, int threadCount) throws Exception {
    MultiThreadTermVectorsReader[] mtr = new MultiThreadTermVectorsReader[threadCount];
    for (int i = 0; i < threadCount; i++) {
      mtr[i] = new MultiThreadTermVectorsReader();
      mtr[i].init(reader);
    }
    int threadsAlive = mtr.length;
    while (threadsAlive > 0) {
        Thread.sleep(10);
        threadsAlive = mtr.length;
        for (int i = 0; i < mtr.length; i++) {
          if (mtr[i].isAlive() == true) {
            break;
          }
          threadsAlive--; 
        }
    }
    long totalTime = 0L;
    for (int i = 0; i < mtr.length; i++) {
      totalTime += mtr[i].timeElapsed;
      mtr[i] = null;
    }
  }
}
class MultiThreadTermVectorsReader implements Runnable {
  private IndexReader reader = null;
  private Thread t = null;
  private final int runsToDo = 100;
  long timeElapsed = 0;
  public void init(IndexReader reader) {
    this.reader = reader;
    timeElapsed = 0;
    t=new Thread(this);
    t.start();
  }
  public boolean isAlive() {
    if (t == null) return false;
    return t.isAlive();
  }
  public void run() {
      try {
        for (int i = 0; i < runsToDo; i++)
          testTermVectors();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      return;
  }
  private void testTermVectors() throws Exception {
    int numDocs = reader.numDocs();
    long start = 0L;
    for (int docId = 0; docId < numDocs; docId++) {
      start = System.currentTimeMillis();
      TermFreqVector [] vectors = reader.getTermFreqVectors(docId);
      timeElapsed += System.currentTimeMillis()-start;
      verifyVectors(vectors, docId);
      start = System.currentTimeMillis();
      TermFreqVector vector = reader.getTermFreqVector(docId, "field");
      timeElapsed += System.currentTimeMillis()-start;
      vectors = new TermFreqVector[1];
      vectors[0] = vector;
      verifyVectors(vectors, docId);
    }
  }
  private void verifyVectors(TermFreqVector[] vectors, int num) {
    StringBuilder temp = new StringBuilder();
    String[] terms = null;
    for (int i = 0; i < vectors.length; i++) {
      terms = vectors[i].getTerms();
      for (int z = 0; z < terms.length; z++) {
        temp.append(terms[z]);
      }
    }
    if (!English.intToEnglish(num).trim().equals(temp.toString().trim()))
        System.out.println("wrong term result");
  }
}
