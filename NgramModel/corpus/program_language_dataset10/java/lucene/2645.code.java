package org.apache.solr.client.solrj.request;
import java.io.IOException;
import java.util.Collection;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.ContentStream;
public class DirectXmlRequest extends SolrRequest
{
  final String xml;
  private SolrParams params;
  public DirectXmlRequest( String path, String body )
  {
    super( METHOD.POST, path );
    xml = body;
  }
  @Override
  public Collection<ContentStream> getContentStreams() {
    return ClientUtils.toContentStreams( xml, ClientUtils.TEXT_XML );
  }
  @Override
  public SolrParams getParams() {
    return params;
  }
  public void setParams(SolrParams params) {
    this.params = params;
  }
  @Override
  public UpdateResponse process( SolrServer server ) throws SolrServerException, IOException
  {
    long startTime = System.currentTimeMillis();
    UpdateResponse res = new UpdateResponse();
    res.setResponse( server.request( this ) );
    res.setElapsedTime( System.currentTimeMillis()-startTime );
    return res;
  }
}
