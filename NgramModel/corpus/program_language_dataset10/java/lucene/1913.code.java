package org.apache.lucene.index;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MockRAMDirectory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.util._TestUtil;
import org.apache.lucene.util.English;
import org.apache.lucene.util.LuceneTestCase;
import java.io.IOException;
import java.io.File;
public class TestThreadedOptimize extends LuceneTestCase {
  private static final Analyzer ANALYZER = new SimpleAnalyzer(TEST_VERSION_CURRENT);
  private final static int NUM_THREADS = 3;
  private final static int NUM_ITER = 1;
  private final static int NUM_ITER2 = 1;
  private boolean failed;
  private void setFailed() {
    failed = true;
  }
  public void runTest(Directory directory, MergeScheduler merger) throws Exception {
    IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(
        TEST_VERSION_CURRENT, ANALYZER)
        .setOpenMode(OpenMode.CREATE).setMaxBufferedDocs(2).setMergeScheduler(
            merger));
    for(int iter=0;iter<NUM_ITER;iter++) {
      final int iterFinal = iter;
      ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(1000);
      for(int i=0;i<200;i++) {
        Document d = new Document();
        d.add(new Field("id", Integer.toString(i), Field.Store.YES, Field.Index.NOT_ANALYZED));
        d.add(new Field("contents", English.intToEnglish(i), Field.Store.NO, Field.Index.ANALYZED));
        writer.addDocument(d);
      }
      ((LogMergePolicy) writer.getMergePolicy()).setMergeFactor(4);
      Thread[] threads = new Thread[NUM_THREADS];
      for(int i=0;i<NUM_THREADS;i++) {
        final int iFinal = i;
        final IndexWriter writerFinal = writer;
        threads[i] = new Thread() {
          @Override
          public void run() {
            try {
              for(int j=0;j<NUM_ITER2;j++) {
                writerFinal.optimize(false);
                for(int k=0;k<17*(1+iFinal);k++) {
                  Document d = new Document();
                  d.add(new Field("id", iterFinal + "_" + iFinal + "_" + j + "_" + k, Field.Store.YES, Field.Index.NOT_ANALYZED));
                  d.add(new Field("contents", English.intToEnglish(iFinal+k), Field.Store.NO, Field.Index.ANALYZED));
                  writerFinal.addDocument(d);
                }
                for(int k=0;k<9*(1+iFinal);k++)
                  writerFinal.deleteDocuments(new Term("id", iterFinal + "_" + iFinal + "_" + j + "_" + k));
                writerFinal.optimize();
              }
            } catch (Throwable t) {
              setFailed();
              System.out.println(Thread.currentThread().getName() + ": hit exception");
              t.printStackTrace(System.out);
            }
          }
        };
      }
      for(int i=0;i<NUM_THREADS;i++)
        threads[i].start();
      for(int i=0;i<NUM_THREADS;i++)
        threads[i].join();
      assertTrue(!failed);
      final int expectedDocCount = (int) ((1+iter)*(200+8*NUM_ITER2*(NUM_THREADS/2.0)*(1+NUM_THREADS)));
      assertEquals(expectedDocCount, writer.maxDoc());
      writer.close();
      writer = new IndexWriter(directory, new IndexWriterConfig(
          TEST_VERSION_CURRENT, ANALYZER).setOpenMode(
          OpenMode.APPEND).setMaxBufferedDocs(2));
      IndexReader reader = IndexReader.open(directory, true);
      assertTrue(reader.isOptimized());
      assertEquals(expectedDocCount, reader.numDocs());
      reader.close();
    }
    writer.close();
  }
  public void testThreadedOptimize() throws Exception {
    Directory directory = new MockRAMDirectory();
    runTest(directory, new SerialMergeScheduler());
    runTest(directory, new ConcurrentMergeScheduler());
    directory.close();
    File dirName = new File(TEMP_DIR, "luceneTestThreadedOptimize");
    directory = FSDirectory.open(dirName);
    runTest(directory, new SerialMergeScheduler());
    runTest(directory, new ConcurrentMergeScheduler());
    directory.close();
    _TestUtil.rmDir(dirName);
  }
}
