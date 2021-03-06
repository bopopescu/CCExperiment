package org.apache.solr.search;
import java.io.IOException;
public interface CacheRegenerator {
  public boolean regenerateItem(SolrIndexSearcher newSearcher, SolrCache newCache, SolrCache oldCache, Object oldKey, Object oldVal) throws IOException;
}
