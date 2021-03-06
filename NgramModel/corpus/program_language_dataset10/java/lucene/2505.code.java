package org.apache.solr.search;
import java.net.URL;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.core.SolrInfoMBean;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.FieldCache.CacheEntry;
import org.apache.lucene.util.FieldCacheSanityChecker;
import org.apache.lucene.util.FieldCacheSanityChecker.Insanity;
public class SolrFieldCacheMBean implements SolrInfoMBean {
  protected FieldCacheSanityChecker checker = new FieldCacheSanityChecker();
  public String getName() { return this.getClass().getName(); }
  public String getVersion() { return SolrCore.version; }
  public String getDescription() {
    return "Provides introspection of the Lucene FieldCache, "
      +    "this is **NOT** a cache that is managed by Solr.";
  }
  public Category getCategory() { return Category.CACHE; } 
  public String getSourceId() { 
    return "$Id: SolrFieldCacheMBean.java 826788 2009-10-19 19:44:41Z hossman $"; 
  }
  public String getSource() { 
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/search/SolrFieldCacheMBean.java $";
  }
  public URL[] getDocs() {
    return null;
  }
  public NamedList getStatistics() {
    NamedList stats = new SimpleOrderedMap();
    CacheEntry[] entries = FieldCache.DEFAULT.getCacheEntries();
    stats.add("entries_count", entries.length);
    for (int i = 0; i < entries.length; i++) {
      CacheEntry e = entries[i];
      stats.add("entry#" + i, e.toString());
    }
    Insanity[] insanity = checker.checkSanity(entries);
    stats.add("insanity_count", insanity.length);
    for (int i = 0; i < insanity.length; i++) {
      for (CacheEntry e : insanity[i].getCacheEntries()) {
        if (null == e.getEstimatedSize()) e.estimateSize();
      }
      stats.add("insanity#" + i, insanity[i].toString());
    }
    return stats;
  }
}
