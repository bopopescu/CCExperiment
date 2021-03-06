package org.apache.solr.handler.admin;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.Date;
import org.apache.commons.io.IOUtils;
import org.apache.lucene.LucenePackage;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.common.util.XML;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.RequestHandlerBase;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.IndexSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class SystemInfoHandler extends RequestHandlerBase 
{
  private static Logger log = LoggerFactory.getLogger(SystemInfoHandler.class);
  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception
  {
    rsp.add( "core", getCoreInfo( req.getCore() ) );
    rsp.add( "lucene", getLuceneInfo() );
    rsp.add( "jvm", getJvmInfo() );
    rsp.add( "system", getSystemInfo() );
    rsp.setHttpCaching(false);
  }
  private static SimpleOrderedMap<Object> getCoreInfo( SolrCore core ) throws Exception 
  {
    SimpleOrderedMap<Object> info = new SimpleOrderedMap<Object>();
    IndexSchema schema = core.getSchema();
    info.add( "schema", schema != null ? schema.getSchemaName():"no schema!" );
    InetAddress addr = InetAddress.getLocalHost();
    info.add( "host", addr.getCanonicalHostName() );
    info.add( "now", new Date() );
    info.add( "start", new Date(core.getStartTime()) );
    SimpleOrderedMap<Object> dirs = new SimpleOrderedMap<Object>();
    dirs.add( "instance", new File( core.getResourceLoader().getInstanceDir() ).getAbsolutePath() );
    dirs.add( "data", new File( core.getDataDir() ).getAbsolutePath() );
    dirs.add( "index", new File( core.getIndexDir() ).getAbsolutePath() );
    info.add( "directory", dirs );
    return info;
  }
  public static SimpleOrderedMap<Object> getSystemInfo() throws Exception 
  {
    SimpleOrderedMap<Object> info = new SimpleOrderedMap<Object>();
    OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    info.add( "name", os.getName() );
    info.add( "version", os.getVersion() );
    info.add( "arch", os.getArch() );
    addGetterIfAvaliable( os, "systemLoadAverage", info );
    addGetterIfAvaliable( os, "openFileDescriptorCount", info );
    addGetterIfAvaliable( os, "maxFileDescriptorCount", info );
    addGetterIfAvaliable( os, "committedVirtualMemorySize", info );
    addGetterIfAvaliable( os, "totalPhysicalMemorySize", info );
    addGetterIfAvaliable( os, "totalSwapSpaceSize", info );
    addGetterIfAvaliable( os, "processCpuTime", info );
    try { 
      if( !os.getName().toLowerCase().startsWith( "windows" ) ) {
        info.add( "uname",  execute( "uname -a" ) );
        info.add( "ulimit", execute( "ulimit -n" ) );
        info.add( "uptime", execute( "uptime" ) );
      }
    }
    catch( Throwable ex ) {} 
    return info;
  }
  static void addGetterIfAvaliable( Object obj, String getter, NamedList<Object> info )
  {
    try {
      String n = Character.toUpperCase( getter.charAt(0) ) + getter.substring( 1 );
      Method m = obj.getClass().getMethod( "get" + n );
      Object v = m.invoke( obj, (Object[])null );
      if( v != null ) {
        info.add( getter, v );
      }
    }
    catch( Exception ex ) {} 
  }
  private static String execute( String cmd )
  {
    DataInputStream in = null;
    BufferedReader reader = null;
    try {
      Process process = Runtime.getRuntime().exec(cmd);
      in = new DataInputStream( process.getInputStream() );
      return IOUtils.toString( in );
    }
    catch( Exception ex ) {
      return "(error executing: " + cmd + ")";
    }
    finally {
      IOUtils.closeQuietly( reader );
      IOUtils.closeQuietly( in );
    }
  }
  public static SimpleOrderedMap<Object> getJvmInfo()
  {
    SimpleOrderedMap<Object> jvm = new SimpleOrderedMap<Object>();
    jvm.add( "version", System.getProperty("java.vm.version") );
    jvm.add( "name", System.getProperty("java.vm.name") );
    Runtime runtime = Runtime.getRuntime();
    jvm.add( "processors", runtime.availableProcessors() );
    long used = runtime.totalMemory() - runtime.freeMemory();
    DecimalFormat df = new DecimalFormat("#.#");
    double percentUsed = ((double)(used)/(double)runtime.maxMemory())*100;
    SimpleOrderedMap<Object> mem = new SimpleOrderedMap<Object>();
    mem.add("free", humanReadableUnits(runtime.freeMemory(), df));
    mem.add("total", humanReadableUnits(runtime.totalMemory(), df));
    mem.add("max", humanReadableUnits(runtime.maxMemory(), df));
    mem.add("used", humanReadableUnits(used, df) + " (%" + df.format(percentUsed) + ")");
    jvm.add("memory", mem);
    SimpleOrderedMap<Object> jmx = new SimpleOrderedMap<Object>();
    try{
      RuntimeMXBean mx = ManagementFactory.getRuntimeMXBean();
      jmx.add( "bootclasspath", mx.getBootClassPath());
      jmx.add( "classpath", mx.getClassPath() );
      jmx.add( "commandLineArgs", mx.getInputArguments());
      jmx.add( "startTime", new Date(mx.getStartTime()));
      jmx.add( "upTimeMS",  mx.getUptime() );
    }
    catch (Exception e) {
      log.warn("Error getting JMX properties", e);
    }
    jvm.add( "jmx", jmx );
    return jvm;
  }
  private static SimpleOrderedMap<Object> getLuceneInfo() throws Exception 
  {
    SimpleOrderedMap<Object> info = new SimpleOrderedMap<Object>();
    String solrImplVersion = "";
    String solrSpecVersion = "";
    String luceneImplVersion = "";
    String luceneSpecVersion = "";
    Package p = SolrCore.class.getPackage();
    StringWriter tmp = new StringWriter();
    solrImplVersion = p.getImplementationVersion();
    if (null != solrImplVersion) {
      XML.escapeCharData(solrImplVersion, tmp);
      solrImplVersion = tmp.toString();
    }
    tmp = new StringWriter();
    solrSpecVersion = p.getSpecificationVersion() ;
    if (null != solrSpecVersion) {
      XML.escapeCharData(solrSpecVersion, tmp);
      solrSpecVersion = tmp.toString();
    }
    p = LucenePackage.class.getPackage();
    tmp = new StringWriter();
    luceneImplVersion = p.getImplementationVersion();
    if (null != luceneImplVersion) {
      XML.escapeCharData(luceneImplVersion, tmp);
      luceneImplVersion = tmp.toString();
    }
    tmp = new StringWriter();
    luceneSpecVersion = p.getSpecificationVersion() ;
    if (null != luceneSpecVersion) {
      XML.escapeCharData(luceneSpecVersion, tmp);
      luceneSpecVersion = tmp.toString();
    }
    info.add( "solr-spec-version",   solrSpecVersion   );
    info.add( "solr-impl-version",   solrImplVersion   );
    info.add( "lucene-spec-version", luceneSpecVersion );
    info.add( "lucene-impl-version", luceneImplVersion );
    return info;
  }
  @Override
  public String getDescription() {
    return "Get System Info";
  }
  @Override
  public String getVersion() {
    return "$Revision: 898152 $";
  }
  @Override
  public String getSourceId() {
    return "$Id: SystemInfoHandler.java 898152 2010-01-12 02:19:56Z ryan $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/admin/SystemInfoHandler.java $";
  }
  private static final long ONE_KB = 1024;
  private static final long ONE_MB = ONE_KB * ONE_KB;
  private static final long ONE_GB = ONE_KB * ONE_MB;
  private static String humanReadableUnits(long bytes, DecimalFormat df) {
    String newSizeAndUnits;
    if (bytes / ONE_GB > 0) {
      newSizeAndUnits = String.valueOf(df.format((float)bytes / ONE_GB)) + " GB";
    } else if (bytes / ONE_MB > 0) {
      newSizeAndUnits = String.valueOf(df.format((float)bytes / ONE_MB)) + " MB";
    } else if (bytes / ONE_KB > 0) {
      newSizeAndUnits = String.valueOf(df.format((float)bytes / ONE_KB)) + " KB";
    } else {
      newSizeAndUnits = String.valueOf(bytes) + " bytes";
    }
    return newSizeAndUnits;
  }
}
