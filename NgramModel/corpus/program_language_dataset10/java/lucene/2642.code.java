package org.apache.solr.client.solrj.request;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.UpdateParams;
import java.io.IOException;
public abstract class AbstractUpdateRequest extends SolrRequest {
  protected ModifiableSolrParams params;
  protected int commitWithin = -1;
  public enum ACTION {
    COMMIT,
    OPTIMIZE
  }
  public AbstractUpdateRequest(METHOD m, String path) {
    super(m, path);
  }
  public AbstractUpdateRequest setAction(ACTION action, boolean waitFlush, boolean waitSearcher ) {
    return setAction(action, waitFlush, waitSearcher, 1);
  }
  public AbstractUpdateRequest setAction(ACTION action, boolean waitFlush, boolean waitSearcher, int maxSegments ) {
    if (params == null)
      params = new ModifiableSolrParams();
    if( action == ACTION.OPTIMIZE ) {
      params.set( UpdateParams.OPTIMIZE, "true" );
      params.set(UpdateParams.MAX_OPTIMIZE_SEGMENTS, maxSegments);
    }
    else if( action == ACTION.COMMIT ) {
      params.set( UpdateParams.COMMIT, "true" );
    }
    params.set( UpdateParams.WAIT_FLUSH, String.valueOf(waitFlush));
    params.set( UpdateParams.WAIT_SEARCHER, String.valueOf(waitSearcher));
    return this;
  }
  public AbstractUpdateRequest setAction(ACTION action, boolean waitFlush, boolean waitSearcher, int maxSegments , boolean expungeDeletes) {
    setAction(action, waitFlush, waitSearcher,maxSegments) ;
    params.set(UpdateParams.EXPUNGE_DELETES, String.valueOf(expungeDeletes));
    return this;
  }
  public AbstractUpdateRequest rollback() {
    if (params == null)
      params = new ModifiableSolrParams();
    params.set( UpdateParams.ROLLBACK, "true" );
    return this;
  }
  public void setParam(String param, String value) {
    if (params == null)
      params = new ModifiableSolrParams();
    params.set(param, value);
  }
  public void setParams(ModifiableSolrParams params) {
    this.params = params;
  }
  @Override
  public ModifiableSolrParams getParams() {
    return params;
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
  public boolean isWaitFlush() {
    return params != null && params.getBool(UpdateParams.WAIT_FLUSH, false);
  }
  public boolean isWaitSearcher() {
    return params != null && params.getBool(UpdateParams.WAIT_SEARCHER, false);
  }
  public ACTION getAction() {
    if (params==null) return null;
    if (params.getBool(UpdateParams.COMMIT, false)) return ACTION.COMMIT;
    if (params.getBool(UpdateParams.OPTIMIZE, false)) return ACTION.OPTIMIZE;
    return null;
  }
  public void setWaitFlush(boolean waitFlush) {
    setParam( UpdateParams.WAIT_FLUSH, waitFlush+"" );
  }
  public void setWaitSearcher(boolean waitSearcher) {
    setParam( UpdateParams.WAIT_SEARCHER, waitSearcher+"" );
  }
  public int getCommitWithin() {
    return commitWithin;
  }
  public void setCommitWithin(int commitWithin) {
    this.commitWithin = commitWithin;
  }
}
