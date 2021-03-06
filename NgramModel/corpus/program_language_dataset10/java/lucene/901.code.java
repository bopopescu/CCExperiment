package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
public class ResetSystemEraseTask extends ResetSystemSoftTask {
  public ResetSystemEraseTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public int doLogic() throws Exception {
    getRunData().reinit(true);
    return 0;
  }
}
