package org.apache.lucene.benchmark.byTask.tasks;
import org.apache.lucene.benchmark.byTask.PerfRunData;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.SetBasedFieldSelector;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.io.IOException;
public class SearchTravRetLoadFieldSelectorTask extends SearchTravTask {
  protected FieldSelector fieldSelector;
  public SearchTravRetLoadFieldSelectorTask(PerfRunData runData) {
    super(runData);
  }
  @Override
  public boolean withRetrieve() {
    return true;
  }
  @Override
  protected Document retrieveDoc(IndexReader ir, int id) throws IOException {
    return ir.document(id, fieldSelector);
  }
  @Override
  public void setParams(String params) {
    this.params = params; 
    Set<String> fieldsToLoad = new HashSet<String>();
    for (StringTokenizer tokenizer = new StringTokenizer(params, ","); tokenizer.hasMoreTokens();) {
      String s = tokenizer.nextToken();
      fieldsToLoad.add(s);
    }
    fieldSelector = new SetBasedFieldSelector(fieldsToLoad, Collections.<String> emptySet());
  }
  @Override
  public boolean supportsParams() {
    return true;
  }
}
