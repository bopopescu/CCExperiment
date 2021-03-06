package org.apache.solr.update;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Scorer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Vector;
import java.io.IOException;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.FieldType;
import org.apache.solr.common.SolrException;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.apache.solr.core.*;
public abstract class UpdateHandler implements SolrInfoMBean {
  protected final static Logger log = LoggerFactory.getLogger(UpdateHandler.class);
  protected final SolrCore core;
  protected final IndexSchema schema;
  protected final SchemaField idField;
  protected final FieldType idFieldType;
  protected final Term idTerm; 
  protected Vector<SolrEventListener> commitCallbacks = new Vector<SolrEventListener>();
  protected Vector<SolrEventListener> optimizeCallbacks = new Vector<SolrEventListener>();
  private void parseEventListeners() {
    for (PluginInfo pluginInfo : core.getSolrConfig().getPluginInfos(SolrEventListener.class.getName())) {
      String event = pluginInfo.attributes.get("event");
      SolrEventListener listener = core.createInitInstance(pluginInfo,SolrEventListener.class,"Event Listener",null);
      if ("postCommit".equals(event)) {
        commitCallbacks.add(core.createInitInstance(pluginInfo,SolrEventListener.class,"Event Listener",null));
        log.info("added SolrEventListener for postCommit: " + listener);
      } else if ("postOptimize".equals(event)) {
        optimizeCallbacks.add(listener);
        log.info("added SolrEventListener for postOptimize: " + listener);
      }
    }
  }
  protected void callPostCommitCallbacks() {
    for (SolrEventListener listener : commitCallbacks) {
      listener.postCommit();
    }
  }
  protected void callPostOptimizeCallbacks() {
    for (SolrEventListener listener : optimizeCallbacks) {
      listener.postCommit();
    }
  }
  public UpdateHandler(SolrCore core)  {
    this.core=core;
    schema = core.getSchema();
    idField = schema.getUniqueKeyField();
    idFieldType = idField!=null ? idField.getType() : null;
    idTerm = idField!=null ? new Term(idField.getName(),"") : null;
    parseEventListeners();
  }
  protected SolrIndexWriter createMainIndexWriter(String name, boolean removeAllExisting) throws IOException {
    return new SolrIndexWriter(name,core.getNewIndexDir(), core.getDirectoryFactory(), removeAllExisting, schema, core.getSolrConfig().mainIndexConfig, core.getDeletionPolicy());
  }
  protected final Term idTerm(String readableId) {
    return new Term(idField.getName(), idFieldType.toInternal(readableId));
  }
  protected final String getIndexedId(Document doc) {
    if (idField == null)
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"Operation requires schema to have a unique key field");
    Fieldable[] id = doc.getFieldables( idField.getName() );
    if (id == null || id.length < 1)
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"Document is missing mandatory uniqueKey field: " + idField.getName());
    if( id.length > 1 )
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST,"Document contains multiple values for uniqueKey field: " + idField.getName());
    return idFieldType.storedToIndexed( id[0] );
  }
  protected final String getIndexedIdOptional(Document doc) {
    if (idField == null) return null;
    Field f = doc.getField(idField.getName());
    if (f == null) return null;
    return idFieldType.storedToIndexed(f);
  }
  public abstract int addDoc(AddUpdateCommand cmd) throws IOException;
  public abstract void delete(DeleteUpdateCommand cmd) throws IOException;
  public abstract void deleteByQuery(DeleteUpdateCommand cmd) throws IOException;
  public abstract int mergeIndexes(MergeIndexesCommand cmd) throws IOException;
  public abstract void commit(CommitUpdateCommand cmd) throws IOException;
  public abstract void rollback(RollbackUpdateCommand cmd) throws IOException;
  public abstract void close() throws IOException;
  static class DeleteHitCollector extends Collector {
    public int deleted=0;
    public final SolrIndexSearcher searcher;
    private int docBase;
    public DeleteHitCollector(SolrIndexSearcher searcher) {
      this.searcher = searcher;
    }
    @Override
    public void collect(int doc) {
      try {
        searcher.getReader().deleteDocument(doc + docBase);
        deleted++;
      } catch (IOException e) {
        throw new SolrException( SolrException.ErrorCode.SERVER_ERROR,"Error deleting doc# "+doc,e,false);
      }
    }
    @Override
    public boolean acceptsDocsOutOfOrder() {
      return false;
    }
    @Override
    public void setNextReader(IndexReader arg0, int docBase) throws IOException {
      this.docBase = docBase;
    }
    @Override
    public void setScorer(Scorer scorer) throws IOException {
    }
  }
  public void registerCommitCallback( SolrEventListener listener )
  {
    commitCallbacks.add( listener );
  }
  public void registerOptimizeCallback( SolrEventListener listener )
  {
    optimizeCallbacks.add( listener );
  }
}
