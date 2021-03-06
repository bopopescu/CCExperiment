package org.apache.solr.handler.admin;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import junit.framework.TestCase;
import org.apache.solr.common.util.SimpleOrderedMap;
public class SystemInfoHandlerTest extends TestCase {
  public void testMagickGetter() {
    OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
    SimpleOrderedMap<Object> info = new SimpleOrderedMap<Object>();
    info.add( "name", os.getName() );
    info.add( "version", os.getVersion() );
    info.add( "arch", os.getArch() );
    SimpleOrderedMap<Object> info2 = new SimpleOrderedMap<Object>();
    SystemInfoHandler.addGetterIfAvaliable( os, "name", info2 );
    SystemInfoHandler.addGetterIfAvaliable( os, "version", info2 );
    SystemInfoHandler.addGetterIfAvaliable( os, "arch", info2 );
    assertEquals( info.toString(), info2.toString() );
  }
}
