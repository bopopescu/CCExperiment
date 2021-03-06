package org.apache.solr.request;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.*;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.RequiredSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.FacetParams.FacetDateOther;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.common.util.StrUtils;
import org.apache.solr.core.SolrCore;
import org.apache.solr.schema.*;
import org.apache.solr.search.*;
import org.apache.solr.util.BoundedTreeSet;
import org.apache.solr.util.DateMathParser;
import org.apache.solr.handler.component.ResponseBuilder;
import java.io.IOException;
import java.util.*;
public class SimpleFacets {
  protected DocSet docs;
  protected SolrParams params;
  protected SolrIndexSearcher searcher;
  protected SolrQueryRequest req;
  protected ResponseBuilder rb;
  SolrParams localParams; 
  String facetValue;      
  DocSet base;            
  String key;             
  public SimpleFacets(SolrQueryRequest req,
                      DocSet docs,
                      SolrParams params) {
    this(req,docs,params,null);
  }
  public SimpleFacets(SolrQueryRequest req,
                      DocSet docs,
                      SolrParams params,
                      ResponseBuilder rb) {
    this.req = req;
    this.searcher = req.getSearcher();
    this.base = this.docs = docs;
    this.params = params;
    this.rb = rb;
  }
  void parseParams(String type, String param) throws ParseException, IOException {
    localParams = QueryParsing.getLocalParams(param, req.getParams());
    base = docs;
    facetValue = param;
    key = param;
    if (localParams == null) return;
    if (type != FacetParams.FACET_QUERY) {
      facetValue = localParams.get(CommonParams.VALUE);
    }
    key = facetValue;
    key = localParams.get(CommonParams.OUTPUT_KEY, key);
    String excludeStr = localParams.get(CommonParams.EXCLUDE);
    if (excludeStr == null) return;
    Map tagMap = (Map)req.getContext().get("tags");
    if (tagMap != null && rb != null) {
      List<String> excludeTagList = StrUtils.splitSmart(excludeStr,',');
      IdentityHashMap<Query,Boolean> excludeSet = new IdentityHashMap<Query,Boolean>();
      for (String excludeTag : excludeTagList) {
        Object olst = tagMap.get(excludeTag);
        if (!(olst instanceof Collection)) continue;
        for (Object o : (Collection)olst) {
          if (!(o instanceof QParser)) continue;
          QParser qp = (QParser)o;
          excludeSet.put(qp.getQuery(), Boolean.TRUE);
        }
      }
      if (excludeSet.size() == 0) return;
      List<Query> qlist = new ArrayList<Query>();
      qlist.add(rb.getQuery());
      for (Query q : rb.getFilters()) {
        if (!excludeSet.containsKey(q)) {
          qlist.add(q);
        }
      }
      base = searcher.getDocSet(qlist);
    }
  }
  public NamedList getFacetCounts() {
    if (!params.getBool(FacetParams.FACET,true))
      return null;
    NamedList res = new SimpleOrderedMap();
    try {
      res.add("facet_queries", getFacetQueryCounts());
      res.add("facet_fields", getFacetFieldCounts());
      res.add("facet_dates", getFacetDateCounts());
    } catch (Exception e) {
      SolrException.logOnce(SolrCore.log, "Exception during facet counts", e);
      res.add("exception", SolrException.toStr(e));
    }
    return res;
  }
  public NamedList getFacetQueryCounts() throws IOException,ParseException {
    NamedList res = new SimpleOrderedMap();
    String[] facetQs = params.getParams(FacetParams.FACET_QUERY);
    if (null != facetQs && 0 != facetQs.length) {
      for (String q : facetQs) {
        parseParams(FacetParams.FACET_QUERY, q);
        Query qobj = QParser.getParser(q, null, req).getQuery();
        res.add(key, searcher.numDocs(qobj, base));
      }
    }
    return res;
  }
  public NamedList getTermCounts(String field) throws IOException {
    int offset = params.getFieldInt(field, FacetParams.FACET_OFFSET, 0);
    int limit = params.getFieldInt(field, FacetParams.FACET_LIMIT, 100);
    if (limit == 0) return new NamedList();
    Integer mincount = params.getFieldInt(field, FacetParams.FACET_MINCOUNT);
    if (mincount==null) {
      Boolean zeros = params.getFieldBool(field, FacetParams.FACET_ZEROS);
      mincount = (zeros!=null && !zeros) ? 1 : 0;
    }
    boolean missing = params.getFieldBool(field, FacetParams.FACET_MISSING, false);
    String sort = params.getFieldParam(field, FacetParams.FACET_SORT, limit>0 ? FacetParams.FACET_SORT_COUNT : FacetParams.FACET_SORT_INDEX);
    String prefix = params.getFieldParam(field,FacetParams.FACET_PREFIX);
    NamedList counts;
    SchemaField sf = searcher.getSchema().getField(field);
    FieldType ft = sf.getType();
    String method = params.getFieldParam(field, FacetParams.FACET_METHOD);
    boolean enumMethod = FacetParams.FACET_METHOD_enum.equals(method);
    if (method == null && ft instanceof BoolField) {
      enumMethod = true;
    }
    boolean multiToken = sf.multiValued() || ft.multiValuedFieldCache();
    if (TrieField.getMainValuePrefix(ft) != null) {
      enumMethod = false;
      multiToken = true;
    }
    if (enumMethod) {
      counts = getFacetTermEnumCounts(searcher, base, field, offset, limit, mincount,missing,sort,prefix);
    } else {
      if (multiToken) {
        UnInvertedField uif = UnInvertedField.getUnInvertedField(field, searcher);
        counts = uif.getCounts(searcher, base, offset, limit, mincount,missing,sort,prefix);
      } else {
        counts = getFieldCacheCounts(searcher, base, field, offset,limit, mincount, missing, sort, prefix);
      }
    }
    return counts;
  }
  public NamedList getFacetFieldCounts()
          throws IOException, ParseException {
    NamedList res = new SimpleOrderedMap();
    String[] facetFs = params.getParams(FacetParams.FACET_FIELD);
    if (null != facetFs) {
      for (String f : facetFs) {
        parseParams(FacetParams.FACET_FIELD, f);
        String termList = localParams == null ? null : localParams.get(CommonParams.TERMS);
        if (termList != null) {
          res.add(key, getListedTermCounts(facetValue, termList));
        } else {
          res.add(key, getTermCounts(facetValue));
        }
      }
    }
    return res;
  }
  private NamedList getListedTermCounts(String field, String termList) throws IOException {
    FieldType ft = searcher.getSchema().getFieldType(field);
    List<String> terms = StrUtils.splitSmart(termList, ",", true);
    NamedList res = new NamedList();
    Term t = new Term(field);
    for (String term : terms) {
      String internal = ft.toInternal(term);
      int count = searcher.numDocs(new TermQuery(t.createTerm(internal)), base);
      res.add(term, count);
    }
    return res;    
  }
  public static int getFieldMissingCount(SolrIndexSearcher searcher, DocSet docs, String fieldName)
    throws IOException {
    DocSet hasVal = searcher.getDocSet
      (new TermRangeQuery(fieldName, null, null, false, false));
    return docs.andNotSize(hasVal);
  }
  private static final Comparator nullStrComparator = new Comparator() {
        public int compare(Object o1, Object o2) {
          if (o1==null) return (o2==null) ? 0 : -1;
          else if (o2==null) return 1;
          return ((String)o1).compareTo((String)o2);
        }
      }; 
  public static NamedList getFieldCacheCounts(SolrIndexSearcher searcher, DocSet docs, String fieldName, int offset, int limit, int mincount, boolean missing, String sort, String prefix) throws IOException {
    FieldType ft = searcher.getSchema().getFieldType(fieldName);
    NamedList res = new NamedList();
    FieldCache.StringIndex si = FieldCache.DEFAULT.getStringIndex(searcher.getReader(), fieldName);
    final String[] terms = si.lookup;
    final int[] termNum = si.order;
    if (prefix!=null && prefix.length()==0) prefix=null;
    int startTermIndex, endTermIndex;
    if (prefix!=null) {
      startTermIndex = Arrays.binarySearch(terms,prefix,nullStrComparator);
      if (startTermIndex<0) startTermIndex=-startTermIndex-1;
      endTermIndex = Arrays.binarySearch(terms,prefix+"\uffff\uffff\uffff\uffff",nullStrComparator);
      endTermIndex = -endTermIndex-1;
    } else {
      startTermIndex=1;
      endTermIndex=terms.length;
    }
    final int nTerms=endTermIndex-startTermIndex;
    if (nTerms>0 && docs.size() >= mincount) {
      final int[] counts = new int[nTerms];
      DocIterator iter = docs.iterator();
      while (iter.hasNext()) {
        int term = termNum[iter.nextDoc()];
        int arrIdx = term-startTermIndex;
        if (arrIdx>=0 && arrIdx<nTerms) counts[arrIdx]++;
      }
      int off=offset;
      int lim=limit>=0 ? limit : Integer.MAX_VALUE;
      if (sort.equals(FacetParams.FACET_SORT_COUNT) || sort.equals(FacetParams.FACET_SORT_COUNT_LEGACY)) {
        int maxsize = limit>0 ? offset+limit : Integer.MAX_VALUE-1;
        maxsize = Math.min(maxsize, nTerms);
        final BoundedTreeSet<CountPair<String,Integer>> queue = new BoundedTreeSet<CountPair<String,Integer>>(maxsize);
        int min=mincount-1;  
        for (int i=0; i<nTerms; i++) {
          int c = counts[i];
          if (c>min) {
            queue.add(new CountPair<String,Integer>(terms[startTermIndex+i], c));
            if (queue.size()>=maxsize) min=queue.last().val;
          }
        }
        for (CountPair<String,Integer> p : queue) {
          if (--off>=0) continue;
          if (--lim<0) break;
          res.add(ft.indexedToReadable(p.key), p.val);
        }
      } else {
        int i=0;
        if (mincount<=0) {
          i=off;
          off=0;
        }
        for (; i<nTerms; i++) {          
          int c = counts[i];
          if (c<mincount || --off>=0) continue;
          if (--lim<0) break;
          res.add(ft.indexedToReadable(terms[startTermIndex+i]), c);
        }
      }
    }
    if (missing) {
      res.add(null, getFieldMissingCount(searcher,docs,fieldName));
    }
    return res;
  }
  public NamedList getFacetTermEnumCounts(SolrIndexSearcher searcher, DocSet docs, String field, int offset, int limit, int mincount, boolean missing, String sort, String prefix)
    throws IOException {
    int minDfFilterCache = params.getFieldInt(field, FacetParams.FACET_ENUM_CACHE_MINDF, 0);
    IndexSchema schema = searcher.getSchema();
    IndexReader r = searcher.getReader();
    FieldType ft = schema.getFieldType(field);
    final int maxsize = limit>=0 ? offset+limit : Integer.MAX_VALUE-1;    
    final BoundedTreeSet<CountPair<String,Integer>> queue = (sort.equals("count") || sort.equals("true")) ? new BoundedTreeSet<CountPair<String,Integer>>(maxsize) : null;
    final NamedList res = new NamedList();
    int min=mincount-1;  
    int off=offset;
    int lim=limit>=0 ? limit : Integer.MAX_VALUE;
    String startTerm = prefix==null ? "" : ft.toInternal(prefix);
    TermEnum te = r.terms(new Term(field,startTerm));
    TermDocs td = r.termDocs();
    if (docs.size() >= mincount) { 
    do {
      Term t = te.term();
      if (null == t || ! t.field().equals(field))
        break;
      if (prefix!=null && !t.text().startsWith(prefix)) break;
      int df = te.docFreq();
      if (df>0 && df>min) {
        int c;
        if (df >= minDfFilterCache) {
          c = searcher.numDocs(new TermQuery(t), docs);
        } else {
          td.seek(te);
          c=0;
          while (td.next()) {
            if (docs.exists(td.doc())) c++;
          }
        }
        if (sort.equals("count") || sort.equals("true")) {
          if (c>min) {
            queue.add(new CountPair<String,Integer>(t.text(), c));
            if (queue.size()>=maxsize) min=queue.last().val;
          }
        } else {
          if (c >= mincount && --off<0) {
            if (--lim<0) break;
            res.add(ft.indexedToReadable(t.text()), c);
          }
        }
      }
    } while (te.next());
    }
    if (sort.equals("count") || sort.equals("true")) {
      for (CountPair<String,Integer> p : queue) {
        if (--off>=0) continue;
        if (--lim<0) break;
        res.add(ft.indexedToReadable(p.key), p.val);
      }
    }
    if (missing) {
      res.add(null, getFieldMissingCount(searcher,docs,field));
    }
    te.close();
    td.close();    
    return res;
  }
  public NamedList getFacetDateCounts()
          throws IOException, ParseException {
    final SolrParams required = new RequiredSolrParams(params);
    final NamedList resOuter = new SimpleOrderedMap();
    final String[] fields = params.getParams(FacetParams.FACET_DATE);
    final Date NOW = new Date();
    if (null == fields || 0 == fields.length) return resOuter;
    final IndexSchema schema = searcher.getSchema();
    for (String f : fields) {
      parseParams(FacetParams.FACET_DATE, f);
      f = facetValue;
      final NamedList resInner = new SimpleOrderedMap();
      resOuter.add(key, resInner);
      final SchemaField sf = schema.getField(f);
      if (! (sf.getType() instanceof DateField)) {
        throw new SolrException
          (SolrException.ErrorCode.BAD_REQUEST,
           "Can not date facet on a field which is not a DateField: " + f);
      }
      final DateField ft = (DateField) sf.getType();
      final String startS
        = required.getFieldParam(f,FacetParams.FACET_DATE_START);
      final Date start;
      try {
        start = ft.parseMath(NOW, startS);
      } catch (SolrException e) {
        throw new SolrException
          (SolrException.ErrorCode.BAD_REQUEST,
           "date facet 'start' is not a valid Date string: " + startS, e);
      }
      final String endS
        = required.getFieldParam(f,FacetParams.FACET_DATE_END);
      Date end; 
      try {
        end = ft.parseMath(NOW, endS);
      } catch (SolrException e) {
        throw new SolrException
          (SolrException.ErrorCode.BAD_REQUEST,
           "date facet 'end' is not a valid Date string: " + endS, e);
      }
      if (end.before(start)) {
        throw new SolrException
          (SolrException.ErrorCode.BAD_REQUEST,
           "date facet 'end' comes before 'start': "+endS+" < "+startS);
      }
      final String gap = required.getFieldParam(f,FacetParams.FACET_DATE_GAP);
      final DateMathParser dmp = new DateMathParser(ft.UTC, Locale.US);
      dmp.setNow(NOW);
      int minCount = params.getFieldInt(f,FacetParams.FACET_MINCOUNT, 0);
      try {
        Date low = start;
        while (low.before(end)) {
          dmp.setNow(low);
          String label = ft.toExternal(low);
          Date high = dmp.parseMath(gap);
          if (end.before(high)) {
            if (params.getFieldBool(f,FacetParams.FACET_DATE_HARD_END,false)) {
              high = end;
            } else {
              end = high;
            }
          }
          if (high.before(low)) {
            throw new SolrException
              (SolrException.ErrorCode.BAD_REQUEST,
               "date facet infinite loop (is gap negative?)");
          }
          int count = rangeCount(sf,low,high,true,true);
          if (count >= minCount) {
            resInner.add(label, count);
          }
          low = high;
        }
      } catch (java.text.ParseException e) {
        throw new SolrException
          (SolrException.ErrorCode.BAD_REQUEST,
           "date facet 'gap' is not a valid Date Math string: " + gap, e);
      }
      resInner.add("gap", gap);
      resInner.add("end", end);
      final String[] othersP =
        params.getFieldParams(f,FacetParams.FACET_DATE_OTHER);
      if (null != othersP && 0 < othersP.length ) {
        Set<FacetDateOther> others = EnumSet.noneOf(FacetDateOther.class);
        for (final String o : othersP) {
          others.add(FacetDateOther.get(o));
        }
        if (! others.contains(FacetDateOther.NONE) ) {          
          boolean all = others.contains(FacetDateOther.ALL);
          if (all || others.contains(FacetDateOther.BEFORE)) {
            resInner.add(FacetDateOther.BEFORE.toString(),
                         rangeCount(sf,null,start,false,false));
          }
          if (all || others.contains(FacetDateOther.AFTER)) {
            resInner.add(FacetDateOther.AFTER.toString(),
                         rangeCount(sf,end,null,false,false));
          }
          if (all || others.contains(FacetDateOther.BETWEEN)) {
            resInner.add(FacetDateOther.BETWEEN.toString(),
                         rangeCount(sf,start,end,true,true));
          }
        }
      }
    }
    return resOuter;
  }
  protected int rangeCount(SchemaField sf, String low, String high,
                           boolean iLow, boolean iHigh) throws IOException {
    Query rangeQ = sf.getType().getRangeQuery(null, sf,low,high,iLow,iHigh);
    return searcher.numDocs(rangeQ ,base);
  }
  protected int rangeCount(SchemaField sf, Date low, Date high,
                           boolean iLow, boolean iHigh) throws IOException {
    Query rangeQ = ((DateField)(sf.getType())).getRangeQuery(null, sf,low,high,iLow,iHigh);
    return searcher.numDocs(rangeQ ,base);
  }
  public static class CountPair<K extends Comparable<? super K>, V extends Comparable<? super V>>
    implements Comparable<CountPair<K,V>> {
    public CountPair(K k, V v) {
      key = k; val = v;
    }
    public K key;
    public V val;
    public int hashCode() {
      return key.hashCode() ^ val.hashCode();
    }
    public boolean equals(Object o) {
      return (o instanceof CountPair)
        && (0 == this.compareTo((CountPair<K,V>) o));
    }
    public int compareTo(CountPair<K,V> o) {
      int vc = o.val.compareTo(val);
      return (0 != vc ? vc : key.compareTo(o.key));
    }
  }
}
