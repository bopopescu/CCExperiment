package org.apache.solr.client.solrj.embedded;
import java.io.File;
import org.apache.solr.client.solrj.MultiCoreExampleTestBase;
import org.apache.solr.client.solrj.SolrServer;
public class MultiCoreEmbeddedTest extends MultiCoreExampleTestBase {
  @Override public void setUp() throws Exception 
  {
    super.setUp();
    File home = new File( getSolrHome() );
    File f = new File( home, "solr.xml" );
    cores.load( getSolrHome(), f );
  }
  @Override
  protected SolrServer getSolrCore0()
  {
    return new EmbeddedSolrServer( cores, "core0" );
  }
  @Override
  protected SolrServer getSolrCore1()
  {
    return new EmbeddedSolrServer( cores, "core1" );
  }
  @Override
  protected SolrServer getSolrCore(String name)
  {
    return new EmbeddedSolrServer( cores, name );
  }
  @Override
  protected SolrServer getSolrAdmin()
  {
    return new EmbeddedSolrServer( cores, "core0" );
  } 
}
