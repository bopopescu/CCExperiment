package org.apache.lucene.benchmark.byTask.tasks;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.index.IndexReader;
public class FlushReaderTask extends PerfTask {
  String userData = null;
  public FlushReaderTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public boolean supportsParams() {
    return true;
  }
  @Override
  public void setParams(String params) {
    super.setParams(params);
    userData = params;
  }
  @Override
  public int doLogic() throws IOException {
    IndexReader reader = getRunData().getIndexReader();
    if (userData != null) {
      Map<String,String> map = new HashMap<String,String>();
      map.put(OpenReaderTask.USER_DATA, userData);
      reader.flush(map);
    } else {
      reader.flush();
    }
    reader.decRef();
    return 1;
  }
}
