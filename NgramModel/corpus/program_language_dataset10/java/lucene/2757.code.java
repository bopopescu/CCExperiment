package org.apache.solr.client.solrj.embedded;
import static junit.framework.Assert.assertEquals;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.request.QueryRequest;
import org.apache.solr.client.solrj.request.UpdateRequest;
import org.apache.solr.client.solrj.request.UpdateRequest.ACTION;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.util.AbstractSolrTestCase;
import org.junit.After;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class TestSolrProperties {
  protected static Logger log = LoggerFactory.getLogger(TestSolrProperties.class);
  protected CoreContainer cores = null;
  public String getSolrHome() {
    return "solr/shared";
  }
  public String getSolrXml() {
    return "solr.xml";
  }
  @Before
  public void setUp() throws Exception {
    System.setProperty("solr.solr.home", getSolrHome());
    log.info("pwd: " + (new File(".")).getAbsolutePath());
    File home = new File(getSolrHome());
    File f = new File(home, "solr.xml");
    cores = new CoreContainer(getSolrHome(), f);
  }
  @After
  public void tearDown() throws Exception {
    if (cores != null)
      cores.shutdown();
    File dataDir = new File(getSolrHome() + "/data");
    String skip = System.getProperty("solr.test.leavedatadir");
    if (null != skip && 0 != skip.trim().length()) {
      log.info("NOTE: per solr.test.leavedatadir, dataDir will not be removed: " + dataDir.getAbsolutePath());
    } else {
      if (!AbstractSolrTestCase.recurseDelete(dataDir)) {
        log.warn("!!!! WARNING: best effort to remove " + dataDir.getAbsolutePath() + " FAILED !!!!!");
      }
    }
    File persistedFile = new File(getSolrHome() + File.separator + "solr-persist.xml");
    persistedFile.delete();
  }
  protected SolrServer getSolrCore0() {
    return new EmbeddedSolrServer(cores, "core0");
  }
  protected SolrServer getSolrCore1() {
    return new EmbeddedSolrServer(cores, "core1");
  }
  protected SolrServer getSolrAdmin() {
    return new EmbeddedSolrServer(cores, "core0");
  }
  protected SolrServer getSolrCore(String name) {
    return new EmbeddedSolrServer(cores, name);
  }
  @Test
  public void testProperties() throws Exception {
    UpdateRequest up = new UpdateRequest();
    up.setAction(ACTION.COMMIT, true, true);
    up.deleteByQuery("*:*");
    up.process(getSolrCore0());
    up.process(getSolrCore1());
    up.clear();
    SolrInputDocument doc = new SolrInputDocument();
    doc.setField("id", "AAA");
    doc.setField("core0", "yup stopfra stopfrb stopena stopenb");
    up.add(doc);
    up.process(getSolrCore0());
    try {
      up.process(getSolrCore1());
      fail("Can't add core0 field to core1!");
    }
    catch (Exception ex) {
    }
    doc.setField("id", "BBB");
    doc.setField("core1", "yup stopfra stopfrb stopena stopenb");
    doc.removeField("core0");
    up.add(doc);
    up.process(getSolrCore1());
    try {
      up.process(getSolrCore0());
      fail("Can't add core1 field to core0!");
    }
    catch (Exception ex) {
    }
    SolrQuery q = new SolrQuery();
    QueryRequest r = new QueryRequest(q);
    q.setQuery("id:AAA");
    assertEquals(1, r.process(getSolrCore0()).getResults().size());
    assertEquals(0, r.process(getSolrCore1()).getResults().size());
    assertEquals(1, getSolrCore0().query(new SolrQuery("id:AAA")).getResults().size());
    assertEquals(0, getSolrCore0().query(new SolrQuery("id:BBB")).getResults().size());
    assertEquals(0, getSolrCore1().query(new SolrQuery("id:AAA")).getResults().size());
    assertEquals(1, getSolrCore1().query(new SolrQuery("id:BBB")).getResults().size());
    String name = "core0";
    SolrServer coreadmin = getSolrAdmin();
    CoreAdminResponse mcr = CoreAdminRequest.getStatus(name, coreadmin);
    long before = mcr.getStartTime(name).getTime();
    CoreAdminRequest.reloadCore(name, coreadmin);
    mcr = CoreAdminRequest.getStatus(name, coreadmin);
    long after = mcr.getStartTime(name).getTime();
    assertTrue("should have more recent time: " + after + "," + before, after > before);
    mcr = CoreAdminRequest.persist("solr-persist.xml", coreadmin);
  }
}
