package org.apache.solr.handler.component;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.ShardParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.StrUtils;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.*;
import org.apache.solr.util.SolrPluginUtils;
import java.io.IOException;
import java.net.URL;
import java.util.*;
public class QueryComponent extends SearchComponent
{
  public static final String COMPONENT_NAME = "query";
  @Override
  public void prepare(ResponseBuilder rb) throws IOException
  {
    SolrQueryRequest req = rb.req;
    SolrParams params = req.getParams();
    if (!params.getBool(COMPONENT_NAME, true)) {
      return;
    }
    SolrQueryResponse rsp = rb.rsp;
    String fl = params.get(CommonParams.FL);
    int fieldFlags = 0;
    if (fl != null) {
      fieldFlags |= SolrPluginUtils.setReturnFields(fl, rsp);
    }
    rb.setFieldFlags( fieldFlags );
    String defType = params.get(QueryParsing.DEFTYPE);
    defType = defType==null ? QParserPlugin.DEFAULT_QTYPE : defType;
    if (rb.getQueryString() == null) {
      rb.setQueryString( params.get( CommonParams.Q ) );
    }
    try {
      QParser parser = QParser.getParser(rb.getQueryString(), defType, req);
      rb.setQuery( parser.getQuery() );
      rb.setSortSpec( parser.getSort(true) );
      rb.setQparser(parser);
      String[] fqs = req.getParams().getParams(CommonParams.FQ);
      if (fqs!=null && fqs.length!=0) {
        List<Query> filters = rb.getFilters();
        if (filters==null) {
          filters = new ArrayList<Query>();
          rb.setFilters( filters );
        }
        for (String fq : fqs) {
          if (fq != null && fq.trim().length()!=0) {
            QParser fqp = QParser.getParser(fq, null, req);
            filters.add(fqp.getQuery());
          }
        }
      }
    } catch (ParseException e) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
    }
    String shards = params.get(ShardParams.SHARDS);
    if (shards != null) {
      List<String> lst = StrUtils.splitSmart(shards, ",", true);
      rb.shards = lst.toArray(new String[lst.size()]);
    }
    String shards_rows = params.get(ShardParams.SHARDS_ROWS);
    if(shards_rows != null) {
      rb.shards_rows = Integer.parseInt(shards_rows);
    }
    String shards_start = params.get(ShardParams.SHARDS_START);
    if(shards_start != null) {
      rb.shards_start = Integer.parseInt(shards_start);
    }
  }
  @Override
  public void process(ResponseBuilder rb) throws IOException
  {
    SolrQueryRequest req = rb.req;
    SolrQueryResponse rsp = rb.rsp;
    SolrParams params = req.getParams();
    if (!params.getBool(COMPONENT_NAME, true)) {
      return;
    }
    SolrIndexSearcher searcher = req.getSearcher();
    if (rb.getQueryCommand().getOffset() < 0) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "'start' parameter cannot be negative");
    }
    long timeAllowed = (long)params.getInt( CommonParams.TIME_ALLOWED, -1 );
    String ids = params.get(ShardParams.IDS);
    if (ids != null) {
      SchemaField idField = req.getSchema().getUniqueKeyField();
      List<String> idArr = StrUtils.splitSmart(ids, ",", true);
      int[] luceneIds = new int[idArr.size()];
      int docs = 0;
      for (int i=0; i<idArr.size(); i++) {
        int id = req.getSearcher().getFirstMatch(
                new Term(idField.getName(), idField.getType().toInternal(idArr.get(i))));
        if (id >= 0)
          luceneIds[docs++] = id;
      }
      DocListAndSet res = new DocListAndSet();
      res.docList = new DocSlice(0, docs, luceneIds, null, docs, 0);
      if (rb.isNeedDocSet()) {
        List<Query> queries = new ArrayList<Query>();
        queries.add(rb.getQuery());
        List<Query> filters = rb.getFilters();
        if (filters != null) queries.addAll(filters);
        res.docSet = searcher.getDocSet(queries);
      }
      rb.setResults(res);
      rsp.add("response",rb.getResults().docList);
      return;
    }
    SolrIndexSearcher.QueryCommand cmd = rb.getQueryCommand();
    cmd.setTimeAllowed(timeAllowed);
    SolrIndexSearcher.QueryResult result = new SolrIndexSearcher.QueryResult();
    searcher.search(result,cmd);
    rb.setResult( result );
    rsp.add("response",rb.getResults().docList);
    rsp.getToLog().add("hits", rb.getResults().docList.matches());
    doFieldSortValues(rb, searcher);
    doPrefetch(rb);
  }
  protected void doFieldSortValues(ResponseBuilder rb, SolrIndexSearcher searcher) throws IOException
  {
    SolrQueryRequest req = rb.req;
    SolrQueryResponse rsp = rb.rsp;
    boolean fsv = req.getParams().getBool(ResponseBuilder.FIELD_SORT_VALUES,false);
    if(fsv){
      Sort sort = rb.getSortSpec().getSort();
      SortField[] sortFields = sort==null ? new SortField[]{SortField.FIELD_SCORE} : sort.getSort();
      NamedList sortVals = new NamedList(); 
      Field field = new Field("dummy", "", Field.Store.YES, Field.Index.NO); 
      SolrIndexReader reader = searcher.getReader();
      SolrIndexReader[] readers = reader.getLeafReaders();
      SolrIndexReader subReader = reader;
      if (readers.length==1) {
        subReader = readers[0];
        readers=null;
      }
      int[] offsets = reader.getLeafOffsets();
      for (SortField sortField: sortFields) {
        int type = sortField.getType();
        if (type==SortField.SCORE || type==SortField.DOC) continue;
        FieldComparator comparator = null;
        FieldComparator comparators[] = (readers==null) ? null : new FieldComparator[readers.length];
        String fieldname = sortField.getField();
        FieldType ft = fieldname==null ? null : req.getSchema().getFieldTypeNoEx(fieldname);
        DocList docList = rb.getResults().docList;
        ArrayList<Object> vals = new ArrayList<Object>(docList.size());
        DocIterator it = rb.getResults().docList.iterator();
        int offset = 0;
        int idx = 0;
        while(it.hasNext()) {
          int doc = it.nextDoc();
          if (readers != null) {
            idx = SolrIndexReader.readerIndex(doc, offsets);
            subReader = readers[idx];
            offset = offsets[idx];
            comparator = comparators[idx];
          }
          if (comparator == null) {
            comparator = sortField.getComparator(1,0);
            comparator.setNextReader(subReader, offset);
            if (comparators != null)
              comparators[idx] = comparator;
          }
          doc -= offset;  
          comparator.copy(0, doc);
          Object val = comparator.value(0);
          if (val instanceof String) {
            field.setValue((String)val);
            val = ft.toObject(field);
          }
          vals.add(val);
        }
        sortVals.add(fieldname, vals);
      }
      rsp.add("sort_values", sortVals);
    }
  }
  protected void doPrefetch(ResponseBuilder rb) throws IOException
  {
    SolrQueryRequest req = rb.req;
    SolrQueryResponse rsp = rb.rsp;
    if (!req.getParams().getBool(ShardParams.IS_SHARD,false) && rb.getResults().docList != null && rb.getResults().docList.size()<=50) {
      SolrPluginUtils.optimizePreFetchDocs(rb.getResults().docList, rb.getQuery(), req, rsp);
    }
  }
  @Override  
  public int distributedProcess(ResponseBuilder rb) throws IOException {
    if (rb.stage < ResponseBuilder.STAGE_PARSE_QUERY)
      return ResponseBuilder.STAGE_PARSE_QUERY;
    if (rb.stage == ResponseBuilder.STAGE_PARSE_QUERY) {
      createDistributedIdf(rb);
      return ResponseBuilder.STAGE_EXECUTE_QUERY;
    }
    if (rb.stage < ResponseBuilder.STAGE_EXECUTE_QUERY) return ResponseBuilder.STAGE_EXECUTE_QUERY;
    if (rb.stage == ResponseBuilder.STAGE_EXECUTE_QUERY) {
      createMainQuery(rb);
      return ResponseBuilder.STAGE_GET_FIELDS;
    }
    if (rb.stage < ResponseBuilder.STAGE_GET_FIELDS) return ResponseBuilder.STAGE_GET_FIELDS;
    if (rb.stage == ResponseBuilder.STAGE_GET_FIELDS) {
      createRetrieveDocs(rb);
      return ResponseBuilder.STAGE_DONE;
    }
    return ResponseBuilder.STAGE_DONE;
  }
  @Override
  public void handleResponses(ResponseBuilder rb, ShardRequest sreq) {
    if ((sreq.purpose & ShardRequest.PURPOSE_GET_TOP_IDS) != 0) {
      mergeIds(rb, sreq);
    }
    if ((sreq.purpose & ShardRequest.PURPOSE_GET_FIELDS) != 0) {
      returnFields(rb, sreq);
      return;
    }
  }
  @Override
  public void finishStage(ResponseBuilder rb) {
    if (rb.stage == ResponseBuilder.STAGE_GET_FIELDS) {
      for (Iterator<SolrDocument> iter = rb._responseDocs.iterator(); iter.hasNext();) {
        if (iter.next() == null) {
          iter.remove();
          rb._responseDocs.setNumFound(rb._responseDocs.getNumFound()-1);
        }        
      }
      rb.rsp.add("response", rb._responseDocs);
    }
  }
  private void createDistributedIdf(ResponseBuilder rb) {
  }
  private void createMainQuery(ResponseBuilder rb) {
    ShardRequest sreq = new ShardRequest();
    sreq.purpose = ShardRequest.PURPOSE_GET_TOP_IDS;
    sreq.params = new ModifiableSolrParams(rb.req.getParams());
    sreq.params.remove(ShardParams.SHARDS);
    if(rb.shards_start > -1) {
      sreq.params.set(CommonParams.START,rb.shards_start);
    } else {
      sreq.params.set(CommonParams.START, "0");
    }
    if(rb.shards_rows > -1) {
      sreq.params.set(CommonParams.ROWS,rb.shards_rows);
    } else {
      sreq.params.set(CommonParams.ROWS, rb.getSortSpec().getOffset() + rb.getSortSpec().getCount());
    }
    sreq.params.set(ResponseBuilder.FIELD_SORT_VALUES,"true");
    if ( (rb.getFieldFlags() & SolrIndexSearcher.GET_SCORES)!=0 || rb.getSortSpec().includesScore()) {
      sreq.params.set(CommonParams.FL, rb.req.getSchema().getUniqueKeyField().getName() + ",score");
    } else {
      sreq.params.set(CommonParams.FL, rb.req.getSchema().getUniqueKeyField().getName());      
    }
    rb.addRequest(this, sreq);
  }
  private void mergeIds(ResponseBuilder rb, ShardRequest sreq) {
      SortSpec ss = rb.getSortSpec();
      Sort sort = ss.getSort();
      SortField[] sortFields = null;
      if(sort != null) sortFields = sort.getSort();
      else {
        sortFields = new SortField[]{SortField.FIELD_SCORE};
      }
      SchemaField uniqueKeyField = rb.req.getSchema().getUniqueKeyField();
      HashMap<Object,String> uniqueDoc = new HashMap<Object,String>();    
      ShardFieldSortedHitQueue queue;
      queue = new ShardFieldSortedHitQueue(sortFields, ss.getOffset() + ss.getCount());
      long numFound = 0;
      Float maxScore=null;
      for (ShardResponse srsp : sreq.responses) {
        SolrDocumentList docs = (SolrDocumentList)srsp.getSolrResponse().getResponse().get("response");
        if (docs.getMaxScore() != null) {
          maxScore = maxScore==null ? docs.getMaxScore() : Math.max(maxScore, docs.getMaxScore());
        }
        numFound += docs.getNumFound();
        NamedList sortFieldValues = (NamedList)(srsp.getSolrResponse().getResponse().get("sort_values"));
        for (int i=0; i<docs.size(); i++) {
          SolrDocument doc = docs.get(i);
          Object id = doc.getFieldValue(uniqueKeyField.getName());
          String prevShard = uniqueDoc.put(id, srsp.getShard());
          if (prevShard != null) {
            numFound--;
            continue;
          }
          ShardDoc shardDoc = new ShardDoc();
          shardDoc.id = id;
          shardDoc.shard = srsp.getShard();
          shardDoc.orderInShard = i;
          Object scoreObj = doc.getFieldValue("score");
          if (scoreObj != null) {
            if (scoreObj instanceof String) {
              shardDoc.score = Float.parseFloat((String)scoreObj);
            } else {
              shardDoc.score = (Float)scoreObj;
            }
          }
          shardDoc.sortFieldValues = sortFieldValues;
          queue.insertWithOverflow(shardDoc);
        } 
      } 
      int resultSize = queue.size() - ss.getOffset();
      resultSize = Math.max(0, resultSize);  
      Map<Object,ShardDoc> resultIds = new HashMap<Object,ShardDoc>();
      for (int i=resultSize-1; i>=0; i--) {
        ShardDoc shardDoc = (ShardDoc)queue.pop();
        shardDoc.positionInResponse = i;
        resultIds.put(shardDoc.id.toString(), shardDoc);
      }
      SolrDocumentList responseDocs = new SolrDocumentList();
      if (maxScore!=null) responseDocs.setMaxScore(maxScore);
      responseDocs.setNumFound(numFound);
      responseDocs.setStart(ss.getOffset());
      for (int i=0; i<resultSize; i++) responseDocs.add(null);
      rb.resultIds = resultIds;
      rb._responseDocs = responseDocs;
  }
  private void createRetrieveDocs(ResponseBuilder rb) {
    HashMap<String, Collection<ShardDoc>> shardMap = new HashMap<String,Collection<ShardDoc>>();
    for (ShardDoc sdoc : rb.resultIds.values()) {
      Collection<ShardDoc> shardDocs = shardMap.get(sdoc.shard);
      if (shardDocs == null) {
        shardDocs = new ArrayList<ShardDoc>();
        shardMap.put(sdoc.shard, shardDocs);
      }
      shardDocs.add(sdoc);
    }
    SchemaField uniqueField = rb.req.getSchema().getUniqueKeyField();
    for (Collection<ShardDoc> shardDocs : shardMap.values()) {
      ShardRequest sreq = new ShardRequest();
      sreq.purpose = ShardRequest.PURPOSE_GET_FIELDS;
      sreq.shards = new String[] {shardDocs.iterator().next().shard};
      sreq.params = new ModifiableSolrParams();
      sreq.params.add( rb.req.getParams());
      sreq.params.remove(CommonParams.SORT);
      sreq.params.remove(ResponseBuilder.FIELD_SORT_VALUES);
      String fl = sreq.params.get(CommonParams.FL);
      if (fl != null) {
         fl = fl.trim();
         if (fl.length()!=0 && !"score".equals(fl) && !"*".equals(fl)) {
           sreq.params.set(CommonParams.FL, fl+','+uniqueField.getName());
         }
      }      
      ArrayList<String> ids = new ArrayList<String>(shardDocs.size());
      for (ShardDoc shardDoc : shardDocs) {
        ids.add(shardDoc.id.toString());
      }
      sreq.params.add(ShardParams.IDS, StrUtils.join(ids, ','));
      rb.addRequest(this, sreq);
    }
  }
  private void returnFields(ResponseBuilder rb, ShardRequest sreq) {
    if ((sreq.purpose & ShardRequest.PURPOSE_GET_FIELDS) != 0) {
      boolean returnScores = (rb.getFieldFlags() & SolrIndexSearcher.GET_SCORES) != 0;
      assert(sreq.responses.size() == 1);
      ShardResponse srsp = sreq.responses.get(0);
      SolrDocumentList docs = (SolrDocumentList)srsp.getSolrResponse().getResponse().get("response");
      String keyFieldName = rb.req.getSchema().getUniqueKeyField().getName();
      for (SolrDocument doc : docs) {
        Object id = doc.getFieldValue(keyFieldName);
        ShardDoc sdoc = rb.resultIds.get(id.toString());
        if (sdoc != null) {
          if (returnScores && sdoc.score != null) {
              doc.setField("score", sdoc.score);
          }
          rb._responseDocs.set(sdoc.positionInResponse, doc);
        }
      }      
    }
  }
  @Override
  public String getDescription() {
    return "query";
  }
  @Override
  public String getVersion() {
    return "$Revision: 922957 $";
  }
  @Override
  public String getSourceId() {
    return "$Id: QueryComponent.java 922957 2010-03-14 20:58:32Z markrmiller $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/component/QueryComponent.java $";
  }
  @Override
  public URL[] getDocs() {
    return null;
  }
}
