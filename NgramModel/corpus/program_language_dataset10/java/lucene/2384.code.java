package org.apache.solr.request;
import org.apache.solr.common.params.SolrParams;
@Deprecated
public class AppendedSolrParams extends org.apache.solr.common.params.AppendedSolrParams {
  public AppendedSolrParams(SolrParams main, SolrParams extra) {
    super(main, extra);
  }
}
