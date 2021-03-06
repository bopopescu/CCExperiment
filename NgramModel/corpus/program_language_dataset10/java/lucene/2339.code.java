package org.apache.solr.handler;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.*;
import java.net.MalformedURLException;
import java.net.URL;
public class StandardRequestHandler extends SearchHandler 
{
  @Override
  public String getVersion() {
    return "$Revision: 631357 $";
  }
  @Override
  public String getDescription() {
    return "The standard Solr request handler";
  }
  @Override
  public String getSourceId() {
    return "$Id: StandardRequestHandler.java 631357 2008-02-26 19:47:07Z yonik $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/StandardRequestHandler.java $";
  }
  @Override
  public URL[] getDocs() {
    try {
      return new URL[] { new URL("http://wiki.apache.org/solr/StandardRequestHandler") };
    }
    catch( MalformedURLException ex ) { return null; }
  }
}
