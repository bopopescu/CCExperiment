package org.apache.solr.update;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.solr.core.SolrCore;
import org.apache.solr.util.AbstractSolrTestCase;
import java.io.File;
import java.io.FileFilter;
public class DirectUpdateHandlerOptimizeTest extends AbstractSolrTestCase {
  public String getSchemaFile() {
    return "schema12.xml";
  }
  public String getSolrConfigFile() {
    return "solrconfig-duh-optimize.xml";
  }
  public void testOptimize() throws Exception {
    SolrCore core = h.getCore();
    UpdateHandler updater = core.getUpdateHandler();
    AddUpdateCommand cmd = new AddUpdateCommand();
    cmd.overwriteCommitted = true;
    cmd.overwritePending = true;
    cmd.allowDups = false;
    for (int i = 0; i < 99; i++) {
      cmd.doc = new Document();
      cmd.doc.add(new Field("id", "id_" + i, Field.Store.YES, Field.Index.NOT_ANALYZED));
      cmd.doc.add(new Field("subject", "subject_" + i, Field.Store.NO, Field.Index.ANALYZED));
      updater.addDoc(cmd);
    }
    CommitUpdateCommand cmtCmd = new CommitUpdateCommand(false);
    updater.commit(cmtCmd);
    updater.commit(cmtCmd);  
    String indexDir = core.getIndexDir();
    assertNumSegments(indexDir, 50);
    cmtCmd = new CommitUpdateCommand(true);
    cmtCmd.maxOptimizeSegments = 25;
    updater.commit(cmtCmd);
    updater.commit(cmtCmd);
    assertNumSegments(indexDir, 25);
    cmtCmd.maxOptimizeSegments = -1;
    try {
      updater.commit(cmtCmd);
      assertTrue(false);
    } catch (IllegalArgumentException e) {
    }
    cmtCmd.maxOptimizeSegments = 1;
    updater.commit(cmtCmd);
    updater.commit(cmtCmd);
    assertNumSegments(indexDir, 1);
  }
  private void assertNumSegments(String indexDir, int numSegs) {
    File file = new File(indexDir);
    File[] segs = file.listFiles(new FileFilter() {
      public boolean accept(File file) {
        return file.getName().endsWith("cfs");
      }
    });
  }
}
