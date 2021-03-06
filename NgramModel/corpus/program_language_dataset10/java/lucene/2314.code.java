package org.apache.solr.core;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.plugin.NamedListInitializedPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public interface SolrEventListener extends NamedListInitializedPlugin{
  static final Logger log = LoggerFactory.getLogger(SolrCore.class);
  public void postCommit();
  public void newSearcher(SolrIndexSearcher newSearcher, SolrIndexSearcher currentSearcher);
}
