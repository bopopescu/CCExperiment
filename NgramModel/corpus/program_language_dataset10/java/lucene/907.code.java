package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
public class SearchTravRetTask extends SearchTravTask {
  public SearchTravRetTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public boolean withRetrieve() {
    return true;
  }
}
