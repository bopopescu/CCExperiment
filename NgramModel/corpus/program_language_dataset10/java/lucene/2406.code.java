package org.apache.solr.request;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrInfoMBean;
import org.apache.solr.response.SolrQueryResponse;
public interface SolrRequestHandler extends SolrInfoMBean {
  public void init(NamedList args);
  public void handleRequest(SolrQueryRequest req, SolrQueryResponse rsp);
}
