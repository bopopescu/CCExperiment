package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.benchmark.byTask.feeds.DocMaker;
import org.apache.lucene.document.Document;
public class AddDocTask extends PerfTask {
  public AddDocTask(PerfRunData runData) {
    super(runData);
  }
  private int docSize = 0;
  private Document doc = null;
  @Override
  public void setup() throws Exception {
    super.setup();
    DocMaker docMaker = getRunData().getDocMaker();
    if (docSize > 0) {
      doc = docMaker.makeDocument(docSize);
    } else {
      doc = docMaker.makeDocument();
    }
  }
  @Override
  public void tearDown() throws Exception {
    doc = null;
    super.tearDown();
  }
  @Override
  protected String getLogMessage(int recsCount) {
    return "added " + recsCount + " docs";
  }
  @Override
  public int doLogic() throws Exception {
    getRunData().getIndexWriter().addDocument(doc);
    return 1;
  }
  @Override
  public void setParams(String params) {
    super.setParams(params);
    docSize = (int) Float.parseFloat(params); 
  }
  @Override
  public boolean supportsParams() {
    return true;
  }
}
