package org.apache.solr.core;
import java.net.URL;
import org.apache.solr.common.util.NamedList;
public interface SolrInfoMBean {
  public enum Category { CORE, QUERYHANDLER, UPDATEHANDLER, CACHE, HIGHLIGHTING, OTHER };
  public String getName();
  public String getVersion();
  public String getDescription();
  public Category getCategory();
  public String getSourceId();
  public String getSource();
  public URL[] getDocs();
  public NamedList getStatistics();
}
