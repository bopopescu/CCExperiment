package org.apache.solr.handler;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.search.DisMaxQParserPlugin;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.util.SolrPluginUtils;
import java.net.MalformedURLException;
import java.net.URL;
@Deprecated
public class DisMaxRequestHandler extends StandardRequestHandler  
{
  @Override
  public void init(NamedList args) {
    super.init( args );
    NamedList def = null;
    Object o = args.get("defaults");
    if (o != null && o instanceof NamedList) {
      def = (NamedList)o;
    } else {
      def = args;
    }
    if (def.get(QueryParsing.DEFTYPE) == null) {
      def = def.clone();
      def.add(QueryParsing.DEFTYPE, DisMaxQParserPlugin.NAME);
      defaults = SolrParams.toSolrParams( def );
    }
  }
	@Override
	public String getDescription() {
	    return "DisjunctionMax Request Handler: Does relevancy based queries "
	       + "across a variety of fields using configured boosts";
	}
	@Override
	public String getVersion() {
	    return "$Revision: 630746 $";
	}
	@Override
	public String getSourceId() {
	  return "$Id: DisMaxRequestHandler.java 630746 2008-02-25 07:02:09Z hossman $";
	}
	@Override
	public String getSource() {
	  return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/DisMaxRequestHandler.java $";
	}
  @Override
  public URL[] getDocs() {
    try {
    return new URL[] { new URL("http://wiki.apache.org/solr/DisMaxRequestHandler") };
    }
    catch( MalformedURLException ex ) { return null; }
  }
}
