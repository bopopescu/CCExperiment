package org.apache.solr.request;
import java.io.Writer;
import org.apache.solr.response.SolrQueryResponse;
public abstract class TextResponseWriter extends org.apache.solr.response.TextResponseWriter 
{
  public TextResponseWriter(Writer writer, SolrQueryRequest req, SolrQueryResponse rsp) {
    super(writer, req, rsp);
  }
}
