package org.apache.solr.highlight;
import org.apache.lucene.search.vectorhighlight.FragListBuilder;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrInfoMBean;
import org.apache.solr.util.plugin.NamedListInitializedPlugin;
public interface SolrFragListBuilder extends SolrInfoMBean, NamedListInitializedPlugin {
  public void init( NamedList args);
  public FragListBuilder getFragListBuilder( SolrParams params );
}
