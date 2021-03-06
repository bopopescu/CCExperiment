package org.apache.solr.common.params;
public interface UpdateParams 
{
  public static String WAIT_FLUSH = "waitFlush";
  public static String WAIT_SEARCHER = "waitSearcher";
  public static String OVERWRITE = "overwrite";
  public static String COMMIT = "commit";
  public static String OPTIMIZE = "optimize";
  public static String ROLLBACK = "rollback";
  public static final String UPDATE_PROCESSOR = "update.processor";
  public static final String MAX_OPTIMIZE_SEGMENTS = "maxSegments";
  public static final String EXPUNGE_DELETES = "expungeDeletes";
}
