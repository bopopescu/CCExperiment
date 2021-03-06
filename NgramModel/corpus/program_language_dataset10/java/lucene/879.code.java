package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.util.ArrayUtil;
public class NearRealtimeReaderTask extends PerfTask {
  long pauseMSec = 3000L;
  int reopenCount;
  int[] reopenTimes = new int[1];
  public NearRealtimeReaderTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public int doLogic() throws Exception {
    final PerfRunData runData = getRunData();
    IndexWriter w = runData.getIndexWriter();
    if (w == null) {
      throw new RuntimeException("please open the writer before invoking NearRealtimeReader");
    }
    if (runData.getIndexReader() != null) {
      throw new RuntimeException("please close the existing reader before invoking NearRealtimeReader");
    }
    long t = System.currentTimeMillis();
    IndexReader r = w.getReader();
    runData.setIndexReader(r);
    r.decRef();
    reopenCount = 0;
    while(!stopNow) {
      long waitForMsec = (pauseMSec - (System.currentTimeMillis() - t));
      if (waitForMsec > 0) {
        Thread.sleep(waitForMsec);
      }
      t = System.currentTimeMillis();
      final IndexReader newReader = r.reopen();
      if (r != newReader) {
        final int delay = (int) (System.currentTimeMillis()-t);
        if (reopenTimes.length == reopenCount) {
          reopenTimes = ArrayUtil.grow(reopenTimes, 1+reopenCount);
        }
        reopenTimes[reopenCount++] = delay;
        runData.setIndexReader(newReader);
        newReader.decRef();
        r = newReader;
      }
    }
    stopNow = false;
    return reopenCount;
  }
  @Override
  public void setParams(String params) {
    super.setParams(params);
    pauseMSec = (long) (1000.0*Float.parseFloat(params));
  }
  @Override
  public void close() {
    System.out.println("NRT reopen times:");
    for(int i=0;i<reopenCount;i++) {
      System.out.print(" " + reopenTimes[i]);
    }
    System.out.println();
  }
  @Override
  public boolean supportsParams() {
    return true;
  }
}
