package org.apache.solr.handler;
import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.MoreLikeThisParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.MoreLikeThisParams.TermStyle;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SimpleFacets;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.DocListAndSet;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.SolrPluginUtils;
public class MoreLikeThisHandler extends RequestHandlerBase  
{
  private static final Pattern splitList = Pattern.compile(",| ");
  @Override
  public void init(NamedList args) {
    super.init(args);
  }
  @Override
  public void handleRequestBody(SolrQueryRequest req, SolrQueryResponse rsp) throws Exception 
  {
    SolrParams params = req.getParams();
    SolrIndexSearcher searcher = req.getSearcher();
    MoreLikeThisHelper mlt = new MoreLikeThisHelper( params, searcher );
    List<Query> filters = SolrPluginUtils.parseFilterQueries(req);
    TermStyle termStyle = TermStyle.get( params.get( MoreLikeThisParams.INTERESTING_TERMS ) );
    List<InterestingTerm> interesting = (termStyle == TermStyle.NONE )
      ? null : new ArrayList<InterestingTerm>( mlt.mlt.getMaxQueryTerms() );
    DocListAndSet mltDocs = null;
    String q = params.get( CommonParams.Q );
    Reader reader = null;
    try {
      if (q == null || q.trim().length() < 1) {
        Iterable<ContentStream> streams = req.getContentStreams();
        if (streams != null) {
          Iterator<ContentStream> iter = streams.iterator();
          if (iter.hasNext()) {
            reader = iter.next().getReader();
          }
          if (iter.hasNext()) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                "MoreLikeThis does not support multiple ContentStreams");
          }
        }
      }
      String fl = params.get(CommonParams.FL);
      int flags = 0;
      if (fl != null) {
        flags |= SolrPluginUtils.setReturnFields(fl, rsp);
      }
      int start = params.getInt(CommonParams.START, 0);
      int rows = params.getInt(CommonParams.ROWS, 10);
      if (reader != null) {
        mltDocs = mlt.getMoreLikeThis(reader, start, rows, filters,
            interesting, flags);
      } else if (q != null) {
        boolean includeMatch = params.getBool(MoreLikeThisParams.MATCH_INCLUDE,
            true);
        int matchOffset = params.getInt(MoreLikeThisParams.MATCH_OFFSET, 0);
        Query query = QueryParsing.parseQuery(q, params.get(CommonParams.DF),
            params, req.getSchema());
        DocList match = searcher.getDocList(query, null, null, matchOffset, 1,
            flags); 
        if (includeMatch) {
          rsp.add("match", match);
        }
        DocIterator iterator = match.iterator();
        if (iterator.hasNext()) {
          int id = iterator.nextDoc();
          mltDocs = mlt.getMoreLikeThis(id, start, rows, filters, interesting,
              flags);
        }
      } else {
        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
            "MoreLikeThis requires either a query (?q=) or text to find similar documents.");
      }
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
    if( mltDocs == null ) {
      mltDocs = new DocListAndSet(); 
    }
    rsp.add( "response", mltDocs.docList );
    if( interesting != null ) {
      if( termStyle == TermStyle.DETAILS ) {
        NamedList<Float> it = new NamedList<Float>();
        for( InterestingTerm t : interesting ) {
          it.add( t.term.toString(), t.boost );
        }
        rsp.add( "interestingTerms", it );
      }
      else {
        List<String> it = new ArrayList<String>( interesting.size() );
        for( InterestingTerm t : interesting ) {
          it.add( t.term.text());
        }
        rsp.add( "interestingTerms", it );
      }
    }
    if (params.getBool(FacetParams.FACET,false)) {
      if( mltDocs.docSet == null ) {
        rsp.add( "facet_counts", null );
      }
      else {
        SimpleFacets f = new SimpleFacets(req, mltDocs.docSet, params );
        rsp.add( "facet_counts", f.getFacetCounts() );
      }
    }
    try {
      NamedList<Object> dbg = SolrPluginUtils.doStandardDebug(req, q, mlt.mltquery, mltDocs.docList );
      if (null != dbg) {
        if (null != filters) {
          dbg.add("filter_queries",req.getParams().getParams(CommonParams.FQ));
          List<String> fqs = new ArrayList<String>(filters.size());
          for (Query fq : filters) {
            fqs.add(QueryParsing.toString(fq, req.getSchema()));
          }
          dbg.add("parsed_filter_queries",fqs);
        }
        rsp.add("debug", dbg);
      }
    } catch (Exception e) {
      SolrException.logOnce(SolrCore.log, "Exception during debug", e);
      rsp.add("exception_during_debug", SolrException.toStr(e));
    }
  }
  public static class InterestingTerm
  {
    public Term term;
    public float boost;
    public static Comparator<InterestingTerm> BOOST_ORDER = new Comparator<InterestingTerm>() {
      public int compare(InterestingTerm t1, InterestingTerm t2) {
        float d = t1.boost - t2.boost;
        if( d == 0 ) {
          return 0;
        }
        return (d>0)?1:-1;
      }
    };
  }
  public static class MoreLikeThisHelper 
  { 
    final SolrIndexSearcher searcher;
    final MoreLikeThis mlt;
    final IndexReader reader;
    final SchemaField uniqueKeyField;
    final boolean needDocSet;
    Map<String,Float> boostFields;
    Query mltquery;  
    public MoreLikeThisHelper( SolrParams params, SolrIndexSearcher searcher )
    {
      this.searcher = searcher;
      this.reader = searcher.getReader();
      this.uniqueKeyField = searcher.getSchema().getUniqueKeyField();
      this.needDocSet = params.getBool(FacetParams.FACET,false);
      SolrParams required = params.required();
      String[] fields = splitList.split( required.get(MoreLikeThisParams.SIMILARITY_FIELDS) );
      if( fields.length < 1 ) {
        throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, 
            "MoreLikeThis requires at least one similarity field: "+MoreLikeThisParams.SIMILARITY_FIELDS );
      }
      this.mlt = new MoreLikeThis( reader ); 
      mlt.setFieldNames(fields);
      mlt.setAnalyzer( searcher.getSchema().getAnalyzer() );
      mlt.setMinTermFreq(       params.getInt(MoreLikeThisParams.MIN_TERM_FREQ,         MoreLikeThis.DEFAULT_MIN_TERM_FREQ));
      mlt.setMinDocFreq(        params.getInt(MoreLikeThisParams.MIN_DOC_FREQ,          MoreLikeThis.DEFAULT_MIN_DOC_FREQ));
      mlt.setMinWordLen(        params.getInt(MoreLikeThisParams.MIN_WORD_LEN,          MoreLikeThis.DEFAULT_MIN_WORD_LENGTH));
      mlt.setMaxWordLen(        params.getInt(MoreLikeThisParams.MAX_WORD_LEN,          MoreLikeThis.DEFAULT_MAX_WORD_LENGTH));
      mlt.setMaxQueryTerms(     params.getInt(MoreLikeThisParams.MAX_QUERY_TERMS,       MoreLikeThis.DEFAULT_MAX_QUERY_TERMS));
      mlt.setMaxNumTokensParsed(params.getInt(MoreLikeThisParams.MAX_NUM_TOKENS_PARSED, MoreLikeThis.DEFAULT_MAX_NUM_TOKENS_PARSED));
      mlt.setBoost(            params.getBool(MoreLikeThisParams.BOOST, false ) );
      boostFields = SolrPluginUtils.parseFieldBoosts(params.getParams(MoreLikeThisParams.QF));
    }
    private void setBoosts(Query mltquery) {
      if (boostFields.size() > 0) {
        List clauses = ((BooleanQuery)mltquery).clauses();
        for( Object o : clauses ) {
          TermQuery q = (TermQuery)((BooleanClause)o).getQuery();
          Float b = this.boostFields.get(q.getTerm().field());
          if (b != null) {
            q.setBoost(b*q.getBoost());
          }
        }
      }
    }
    public DocListAndSet getMoreLikeThis( int id, int start, int rows, List<Query> filters, List<InterestingTerm> terms, int flags ) throws IOException
    {
      Document doc = reader.document(id);
      mltquery = mlt.like(id);
      setBoosts(mltquery);
      if( terms != null ) {
        fillInterestingTermsFromMLTQuery( mltquery, terms );
      }
      BooleanQuery mltQuery = new BooleanQuery();
      mltQuery.add(mltquery, BooleanClause.Occur.MUST);
      mltQuery.add(
          new TermQuery(new Term(uniqueKeyField.getName(), uniqueKeyField.getType().storedToIndexed(doc.getFieldable(uniqueKeyField.getName())))), 
            BooleanClause.Occur.MUST_NOT);
      DocListAndSet results = new DocListAndSet();
      if (this.needDocSet) {
        results = searcher.getDocListAndSet(mltQuery, filters, null, start, rows, flags);
      } else {
        results.docList = searcher.getDocList(mltQuery, filters, null, start, rows, flags);
      }
      return results;
    }
    public DocListAndSet getMoreLikeThis( Reader reader, int start, int rows, List<Query> filters, List<InterestingTerm> terms, int flags ) throws IOException
    {
      mltquery = mlt.like(reader);
      setBoosts(mltquery);
      if( terms != null ) {
        fillInterestingTermsFromMLTQuery( mltquery, terms );
      }
      DocListAndSet results = new DocListAndSet();
      if (this.needDocSet) {
        results = searcher.getDocListAndSet(mltquery, filters, null, start, rows, flags);
      } else {
        results.docList = searcher.getDocList(mltquery, filters, null, start, rows, flags);
      }
      return results;
    }
    public NamedList<DocList> getMoreLikeThese( DocList docs, int rows, int flags ) throws IOException
    {
      IndexSchema schema = searcher.getSchema();
      NamedList<DocList> mlt = new SimpleOrderedMap<DocList>();
      DocIterator iterator = docs.iterator();
      while( iterator.hasNext() ) {
        int id = iterator.nextDoc();
        DocListAndSet sim = getMoreLikeThis( id, 0, rows, null, null, flags );
        String name = schema.printableUniqueKey( reader.document( id ) );
        mlt.add(name, sim.docList);
      }
      return mlt;
    }
    private void fillInterestingTermsFromMLTQuery( Query query, List<InterestingTerm> terms )
    { 
      List clauses = ((BooleanQuery)mltquery).clauses();
      for( Object o : clauses ) {
        TermQuery q = (TermQuery)((BooleanClause)o).getQuery();
        InterestingTerm it = new InterestingTerm();
        it.boost = q.getBoost();
        it.term = q.getTerm();
        terms.add( it );
      } 
    }
    public MoreLikeThis getMoreLikeThis()
    {
      return mlt;
    }
  }
  @Override
  public String getVersion() {
    return "$Revision: 906553 $";
  }
  @Override
  public String getDescription() {
    return "Solr MoreLikeThis";
  }
  @Override
  public String getSourceId() {
    return "$Id: MoreLikeThisHandler.java 906553 2010-02-04 16:26:38Z markrmiller $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/MoreLikeThisHandler.java $";
  }
  @Override
  public URL[] getDocs() {
    try {
      return new URL[] { new URL("http://wiki.apache.org/solr/MoreLikeThis") };
    }
    catch( MalformedURLException ex ) { return null; }
  }
}
