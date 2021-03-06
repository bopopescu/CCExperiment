package org.apache.solr.handler.clustering;
public interface ClusteringParams {
  public static final String CLUSTERING_PREFIX = "clustering.";
  public static final String ENGINE_NAME = CLUSTERING_PREFIX + "engine";
  public static final String USE_SEARCH_RESULTS = CLUSTERING_PREFIX + "results";
  public static final String USE_COLLECTION = CLUSTERING_PREFIX + "collection";
  public static final String USE_DOC_SET = CLUSTERING_PREFIX + "docs.useDocSet";
}
