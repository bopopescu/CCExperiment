package org.apache.solr.handler;
import java.io.File;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.response.SolrQueryResponse;
public class PingRequestHandler extends RequestHandlerBase 
{
  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception 
  {
    SolrParams params = req.getParams();
    SolrParams required = params.required();
    SolrCore core = req.getCore();
    String healthcheck = core.getSolrConfig().get("admin/healthcheck/text()", null );
    if( healthcheck != null && !new File(healthcheck).exists() ) {
      throw new SolrException(SolrException.ErrorCode.SERVICE_UNAVAILABLE, "Service disabled", true);
    }
    String qt = required.get( CommonParams.QT );
    SolrRequestHandler handler = core.getRequestHandler( qt );
    if( handler == null ) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, 
          "Unknown RequestHandler: "+qt );
    }
    Throwable ex = null;
    try {
      SolrQueryResponse pingrsp = new SolrQueryResponse();
      core.execute(handler, req, pingrsp );
      ex = pingrsp.getException();
    }
    catch( Throwable th ) {
      ex = th;
    }
    if( ex != null ) {
      throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, 
          "Ping query caused exception: "+ex.getMessage(), ex );
    }
    rsp.add( "status", "OK" );
  }
  @Override
  public String getVersion() {
    return "$Revision: 898152 $";
  }
  @Override
  public String getDescription() {
    return "Reports application health to a load-balancer";
  }
  @Override
  public String getSourceId() {
    return "$Id: PingRequestHandler.java 898152 2010-01-12 02:19:56Z ryan $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/PingRequestHandler.java $";
  }
}
