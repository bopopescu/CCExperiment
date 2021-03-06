package org.apache.solr.update.processor;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.util.plugin.PluginInfoInitialized;
import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrCore;
import java.util.List;
public final class UpdateRequestProcessorChain implements PluginInfoInitialized
{
  private UpdateRequestProcessorFactory[] chain;
  private final SolrCore solrCore;
  public UpdateRequestProcessorChain(SolrCore solrCore) {
    this.solrCore = solrCore;
  }
  public void init(PluginInfo info) {
    List<UpdateRequestProcessorFactory> list = solrCore.initPlugins(info.getChildren("processor"),UpdateRequestProcessorFactory.class,null);
    if(list.isEmpty()){
      throw new RuntimeException( "updateRequestProcessorChain require at least one processor");
    }
    chain = list.toArray(new UpdateRequestProcessorFactory[list.size()]); 
  }
  public UpdateRequestProcessorChain( UpdateRequestProcessorFactory[] chain , SolrCore solrCore) {
    this.chain = chain;
    this.solrCore =  solrCore;
  }
  public UpdateRequestProcessor createProcessor(SolrQueryRequest req, SolrQueryResponse rsp) 
  {
    UpdateRequestProcessor processor = null;
    UpdateRequestProcessor last = null;
    for (int i = chain.length-1; i>=0; i--) {
      processor = chain[i].getInstance(req, rsp, last);
      last = processor == null ? last : processor;
    }
    return last;
  }
  public UpdateRequestProcessorFactory[] getFactories() {
    return chain;
  }
}
