package org.apache.solr.update;
import org.apache.lucene.index.*;
import org.apache.lucene.store.*;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.DirectoryFactory;
import org.apache.solr.core.StandardDirectoryFactory;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.util.SolrPluginUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.util.Date;
public class SolrIndexWriter extends IndexWriter {
  private static Logger log = LoggerFactory.getLogger(SolrIndexWriter.class);
  String name;
  IndexSchema schema;
  private PrintStream infoStream;
  private void init(String name, IndexSchema schema, SolrIndexConfig config) throws IOException {
    log.debug("Opened Writer " + name);
    this.name = name;
    this.schema = schema;
    setSimilarity(schema.getSimilarity());
    if (config != null) {
      if (config.maxBufferedDocs != -1) {
        setMaxBufferedDocs(config.maxBufferedDocs);
      }
      if (config.ramBufferSizeMB != -1) {
        setRAMBufferSizeMB(config.ramBufferSizeMB);
      }
      if (config.termIndexInterval != -1) {
        setTermIndexInterval(config.termIndexInterval);
      }
      if (config.maxMergeDocs != -1) setMaxMergeDocs(config.maxMergeDocs);
      if (config.maxFieldLength != -1) setMaxFieldLength(config.maxFieldLength);
      String className = config.mergePolicyInfo == null ? SolrIndexConfig.DEFAULT_MERGE_POLICY_CLASSNAME: config.mergePolicyInfo.className;
      MergePolicy  policy = null;
      try {
        policy = (MergePolicy) schema.getResourceLoader().newInstance(className, null, new Class[]{IndexWriter.class}, new Object[] { this });
      } catch (Exception e) {
        policy = (MergePolicy) schema.getResourceLoader().newInstance(className);
      }
      if(config.mergePolicyInfo != null) SolrPluginUtils.invokeSetters(policy,config.mergePolicyInfo.initArgs);
      setMergePolicy(policy);
      if (getMergePolicy() instanceof LogMergePolicy) {
        setUseCompoundFile(config.useCompoundFile);
        if (config.mergeFactor != -1) { setMergeFactor(config.mergeFactor); }
      } else  {
        log.warn("Use of compound file format or mergefactor cannot be configured if merge policy is not an instance " +
                "of LogMergePolicy. The configured policy's defaults will be used.");
      }
      className = config.mergeSchedulerInfo == null ? SolrIndexConfig.DEFAULT_MERGE_SCHEDULER_CLASSNAME: config.mergeSchedulerInfo.className;
      MergeScheduler scheduler = (MergeScheduler) schema.getResourceLoader().newInstance(className);
      if(config.mergeSchedulerInfo != null) SolrPluginUtils.invokeSetters(scheduler,config.mergeSchedulerInfo.initArgs);
      setMergeScheduler(scheduler);
      String infoStreamFile = config.infoStreamFile;
      if (infoStreamFile != null) {
        File f = new File(infoStreamFile);
        File parent = f.getParentFile();
        if (parent != null) parent.mkdirs();
        FileOutputStream fos = new FileOutputStream(f, true);
        infoStream = new TimeLoggingPrintStream(fos, true);
        setInfoStream(infoStream);
      }
    }
  }
  public static Directory getDirectory(String path, DirectoryFactory directoryFactory, SolrIndexConfig config) throws IOException {
    Directory d = directoryFactory.open(path);
    String rawLockType = (null == config) ? null : config.lockType;
    if (null == rawLockType) {
      log.warn("No lockType configured for " + path + " assuming 'simple'");
      rawLockType = "simple";
    }
    final String lockType = rawLockType.toLowerCase().trim();
    if ("simple".equals(lockType)) {
      d.setLockFactory(new SimpleFSLockFactory(path));
    } else if ("native".equals(lockType)) {
      d.setLockFactory(new NativeFSLockFactory(path));
    } else if ("single".equals(lockType)) {
      if (!(d.getLockFactory() instanceof SingleInstanceLockFactory))
        d.setLockFactory(new SingleInstanceLockFactory());
    } else if ("none".equals(lockType)) {
      log.error("CONFIGURATION WARNING: locks are disabled on " + path);      
      d.setLockFactory(new NoLockFactory());
    } else {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR,
              "Unrecognized lockType: " + rawLockType);
    }
    return d;
  }
  private static DirectoryFactory LEGACY_DIR_FACTORY 
    = new StandardDirectoryFactory();
  static {
    LEGACY_DIR_FACTORY.init(new NamedList());
  }
  public static Directory getDirectory(String path, SolrIndexConfig config) throws IOException {
    log.warn("SolrIndexWriter is using LEGACY_DIR_FACTORY which means deprecated code is likely in use and SolrIndexWriter is ignoring any custom DirectoryFactory.");
    return getDirectory(path, LEGACY_DIR_FACTORY, config);
  }
  public SolrIndexWriter(String name, String path, DirectoryFactory dirFactory, boolean create, IndexSchema schema) throws IOException {
    super(getDirectory(path, dirFactory, null), schema.getAnalyzer(), create, MaxFieldLength.LIMITED);
    init(name, schema, null);
  }
  @Deprecated
  public SolrIndexWriter(String name, String path, DirectoryFactory dirFactory, boolean create, IndexSchema schema, SolrIndexConfig config) throws IOException {
    super(getDirectory(path, dirFactory, null), schema.getAnalyzer(), create, MaxFieldLength.LIMITED);
    init(name, schema, config);
  }
  public SolrIndexWriter(String name, String path, boolean create, IndexSchema schema) throws IOException {
    super(getDirectory(path, null), schema.getAnalyzer(), create, MaxFieldLength.LIMITED);
    init(name, schema, null);
  }
  public SolrIndexWriter(String name, String path, boolean create, IndexSchema schema, SolrIndexConfig config) throws IOException {
    super(getDirectory(path, config), schema.getAnalyzer(), create, MaxFieldLength.LIMITED);
    init(name, schema, config);
  }
  public SolrIndexWriter(String name, String path, DirectoryFactory dirFactory, boolean create, IndexSchema schema, SolrIndexConfig config, IndexDeletionPolicy delPolicy) throws IOException {
    super(getDirectory(path, dirFactory, config), schema.getAnalyzer(), create, delPolicy, new MaxFieldLength(IndexWriter.DEFAULT_MAX_FIELD_LENGTH));
    init(name, schema, config);
  }
  private volatile boolean isClosed = false;
  public void close() throws IOException {
    log.debug("Closing Writer " + name);
    try {
      super.close();
      if(infoStream != null) {
        infoStream.close();
      }
    } finally {
      isClosed = true;
    }
  }
  @Override
  public void rollback() throws IOException {
    try {
      super.rollback();
    } finally {
      isClosed = true;
    }
  }
  @Override
  protected void finalize() throws Throwable {
    try {
      if(!isClosed){
        log.error("SolrIndexWriter was not closed prior to finalize(), indicates a bug -- POSSIBLE RESOURCE LEAK!!!");
        close();
      }
    } finally { 
      super.finalize();
    }
  }
  class TimeLoggingPrintStream extends PrintStream {
    private DateFormat dateFormat;
    public TimeLoggingPrintStream(OutputStream underlyingOutputStream,
        boolean autoFlush) {
      super(underlyingOutputStream, autoFlush);
      this.dateFormat = DateFormat.getDateTimeInstance();
    }
    public void println(String x) {
      print(dateFormat.format(new Date()) + " ");
      super.println(x);
    }
  }
}
