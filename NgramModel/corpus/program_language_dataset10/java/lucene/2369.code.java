package org.apache.solr.highlight;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import org.apache.lucene.analysis.CachingTokenFilter;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.search.vectorhighlight.FastVectorHighlighter;
import org.apache.lucene.search.vectorhighlight.FieldQuery;
import org.apache.lucene.search.vectorhighlight.FragListBuilder;
import org.apache.lucene.search.vectorhighlight.FragmentsBuilder;
import org.apache.lucene.util.AttributeSource.State;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.PluginInfo;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.DocIterator;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrIndexSearcher;
import org.apache.solr.util.plugin.PluginInfoInitialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class DefaultSolrHighlighter extends SolrHighlighter implements PluginInfoInitialized
{
  public static Logger log = LoggerFactory.getLogger(DefaultSolrHighlighter.class);
  private SolrCore solrCore;
  public DefaultSolrHighlighter() {
  }
  public DefaultSolrHighlighter(SolrCore solrCore) {
    this.solrCore = solrCore;
  }
  public void init(PluginInfo info) {
    formatters.clear();
    fragmenters.clear();
    fragListBuilders.clear();
    fragmentsBuilders.clear();
    SolrFragmenter frag = solrCore.initPlugins(info.getChildren("fragmenter") , fragmenters,SolrFragmenter.class,null);
    if (frag == null) frag = new GapFragmenter();
    fragmenters.put("", frag);
    fragmenters.put(null, frag);
    SolrFormatter fmt = solrCore.initPlugins(info.getChildren("formatter"), formatters,SolrFormatter.class,null);
    if (fmt == null) fmt = new HtmlFormatter();
    formatters.put("", fmt);
    formatters.put(null, fmt);
    SolrFragListBuilder fragListBuilder = solrCore.initPlugins(info.getChildren("fragListBuilder"),
        fragListBuilders, SolrFragListBuilder.class, null );
    if( fragListBuilder == null ) fragListBuilder = new SimpleFragListBuilder();
    fragListBuilders.put( "", fragListBuilder );
    fragListBuilders.put( null, fragListBuilder );
    SolrFragmentsBuilder fragsBuilder = solrCore.initPlugins(info.getChildren("fragmentsBuilder"),
        fragmentsBuilders, SolrFragmentsBuilder.class, null);
    if( fragsBuilder == null ) fragsBuilder = new ScoreOrderFragmentsBuilder();
    fragmentsBuilders.put( "", fragsBuilder );
    fragmentsBuilders.put( null, fragsBuilder );
    initialized = true;
  }
  private boolean initialized = false;
  @Deprecated
  public void initalize( SolrConfig config) {
    if (initialized) return;
    SolrFragmenter frag = new GapFragmenter();
    fragmenters.put("", frag);
    fragmenters.put(null, frag);
    SolrFormatter fmt = new HtmlFormatter();
    formatters.put("", fmt);
    formatters.put(null, fmt);    
    SolrFragListBuilder fragListBuilder = new SimpleFragListBuilder();
    fragListBuilders.put( "", fragListBuilder );
    fragListBuilders.put( null, fragListBuilder );
    SolrFragmentsBuilder fragsBuilder = new ScoreOrderFragmentsBuilder();
    fragmentsBuilders.put( "", fragsBuilder );
    fragmentsBuilders.put( null, fragsBuilder );
  }
  protected Highlighter getPhraseHighlighter(Query query, String fieldName, SolrQueryRequest request, CachingTokenFilter tokenStream) throws IOException {
    SolrParams params = request.getParams();
    Highlighter highlighter = null;
    highlighter = new Highlighter(getFormatter(fieldName, params), getSpanQueryScorer(query, fieldName, tokenStream, request));
    highlighter.setTextFragmenter(getFragmenter(fieldName, params));
    return highlighter;
  }
  protected Highlighter getHighlighter(Query query, String fieldName, SolrQueryRequest request) {
    SolrParams params = request.getParams(); 
    Highlighter highlighter = new Highlighter(
           getFormatter(fieldName, params), 
           getQueryScorer(query, fieldName, request));
     highlighter.setTextFragmenter(getFragmenter(fieldName, params));
       return highlighter;
  }
  private QueryScorer getSpanQueryScorer(Query query, String fieldName, TokenStream tokenStream, SolrQueryRequest request) throws IOException {
    boolean reqFieldMatch = request.getParams().getFieldBool(fieldName, HighlightParams.FIELD_MATCH, false);
    Boolean highlightMultiTerm = request.getParams().getBool(HighlightParams.HIGHLIGHT_MULTI_TERM, true);
    if(highlightMultiTerm == null) {
      highlightMultiTerm = false;
    }
    QueryScorer scorer;
    if (reqFieldMatch) {
      scorer = new QueryScorer(query, fieldName);
    }
    else {
      scorer = new QueryScorer(query, null);
    }
    scorer.setExpandMultiTermQuery(highlightMultiTerm);
    return scorer;
  }
  private Scorer getQueryScorer(Query query, String fieldName, SolrQueryRequest request) {
     boolean reqFieldMatch = request.getParams().getFieldBool(fieldName, HighlightParams.FIELD_MATCH, false);
     if (reqFieldMatch) {
        return new QueryTermScorer(query, request.getSearcher().getReader(), fieldName);
     }
     else {
        return new QueryTermScorer(query);
     }
  }
  protected int getMaxSnippets(String fieldName, SolrParams params) {
     return params.getFieldInt(fieldName, HighlightParams.SNIPPETS,1);
  }
  protected boolean isMergeContiguousFragments(String fieldName, SolrParams params){
    return params.getFieldBool(fieldName, HighlightParams.MERGE_CONTIGUOUS_FRAGMENTS, false);
  }
  protected Formatter getFormatter(String fieldName, SolrParams params ) 
  {
    String str = params.getFieldParam( fieldName, HighlightParams.FORMATTER );
    SolrFormatter formatter = formatters.get( str );
    if( formatter == null ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "Unknown formatter: "+str );
    }
    return formatter.getFormatter( fieldName, params );
  }
  protected Fragmenter getFragmenter(String fieldName, SolrParams params) 
  {
    String fmt = params.getFieldParam( fieldName, HighlightParams.FRAGMENTER );
    SolrFragmenter frag = fragmenters.get( fmt );
    if( frag == null ) {
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "Unknown fragmenter: "+fmt );
    }
    return frag.getFragmenter( fieldName, params );
  }
  protected FragListBuilder getFragListBuilder( SolrParams params ){
    String flb = params.get( HighlightParams.FRAG_LIST_BUILDER );
    SolrFragListBuilder solrFlb = fragListBuilders.get( flb );
    if( solrFlb == null ){
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "Unknown fragListBuilder: " + flb );
    }
    return solrFlb.getFragListBuilder( params );
  }
  protected FragmentsBuilder getFragmentsBuilder( SolrParams params ){
    String fb = params.get( HighlightParams.FRAGMENTS_BUILDER );
    SolrFragmentsBuilder solrFb = fragmentsBuilders.get( fb );
    if( solrFb == null ){
      throw new SolrException( SolrException.ErrorCode.BAD_REQUEST, "Unknown fragmentsBuilder: " + fb );
    }
    return solrFb.getFragmentsBuilder( params );
  }
  @SuppressWarnings("unchecked")
  public NamedList<Object> doHighlighting(DocList docs, Query query, SolrQueryRequest req, String[] defaultFields) throws IOException {
    SolrParams params = req.getParams(); 
    if (!isHighlightingEnabled(params))
        return null;
    SolrIndexSearcher searcher = req.getSearcher();
    IndexSchema schema = searcher.getSchema();
    NamedList fragments = new SimpleOrderedMap();
    String[] fieldNames = getHighlightFields(query, req, defaultFields);
    Set<String> fset = new HashSet<String>();
    {
      for(String f : fieldNames) { fset.add(f); }
      SchemaField keyField = schema.getUniqueKeyField();
      if(null != keyField)
        fset.add(keyField.getName());  
    }
    FastVectorHighlighter fvh = new FastVectorHighlighter(
        params.getBool( HighlightParams.USE_PHRASE_HIGHLIGHTER, true ),
        params.getBool( HighlightParams.FIELD_MATCH, false ),
        getFragListBuilder( params ),
        getFragmentsBuilder( params ) );
    FieldQuery fieldQuery = fvh.getFieldQuery( query );
    DocIterator iterator = docs.iterator();
    for (int i = 0; i < docs.size(); i++) {
      int docId = iterator.nextDoc();
      Document doc = searcher.doc(docId, fset);
      NamedList docSummaries = new SimpleOrderedMap();
      for (String fieldName : fieldNames) {
        fieldName = fieldName.trim();
        if( useFastVectorHighlighter( params, schema, fieldName ) )
          doHighlightingByFastVectorHighlighter( fvh, fieldQuery, req, docSummaries, docId, doc, fieldName );
        else
          doHighlightingByHighlighter( query, req, docSummaries, docId, doc, fieldName );
      }
      String printId = schema.printableUniqueKey(doc);
      fragments.add(printId == null ? null : printId, docSummaries);
    }
    return fragments;
  }
  private boolean useFastVectorHighlighter( SolrParams params, IndexSchema schema, String fieldName ){
    SchemaField schemaField = schema.getFieldOrNull( fieldName );
    return schemaField != null &&
      schemaField.storeTermPositions() &&
      schemaField.storeTermOffsets() &&
      params.getFieldBool( fieldName, HighlightParams.USE_FVH, false );
  }
  private void doHighlightingByHighlighter( Query query, SolrQueryRequest req, NamedList docSummaries,
      int docId, Document doc, String fieldName ) throws IOException {
    SolrParams params = req.getParams(); 
    String[] docTexts = doc.getValues(fieldName);
    if (docTexts.length == 0) return;
    SolrIndexSearcher searcher = req.getSearcher();
    IndexSchema schema = searcher.getSchema();
    TokenStream tstream = null;
    int numFragments = getMaxSnippets(fieldName, params);
    boolean mergeContiguousFragments = isMergeContiguousFragments(fieldName, params);
    String[] summaries = null;
    List<TextFragment> frags = new ArrayList<TextFragment>();
    TermOffsetsTokenStream tots = null; 
    try {
        TokenStream tvStream = TokenSources.getTokenStream(searcher.getReader(), docId, fieldName);
        if (tvStream != null) {
          tots = new TermOffsetsTokenStream(tvStream);
        }
    }
    catch (IllegalArgumentException e) {
    }
    for (int j = 0; j < docTexts.length; j++) {
      if( tots != null ) {
        tstream = tots.getMultiValuedTokenStream( docTexts[j].length() );
      } else {
        tstream = createAnalyzerTStream(schema, fieldName, docTexts[j]);
      }
      Highlighter highlighter;
      if (Boolean.valueOf(req.getParams().get(HighlightParams.USE_PHRASE_HIGHLIGHTER, "true"))) {
        tstream = new CachingTokenFilter(tstream);
        highlighter = getPhraseHighlighter(query, fieldName, req, (CachingTokenFilter) tstream);
        tstream.reset();
      }
      else {
        highlighter = getHighlighter(query, fieldName, req);
      }
      int maxCharsToAnalyze = params.getFieldInt(fieldName,
          HighlightParams.MAX_CHARS,
          Highlighter.DEFAULT_MAX_CHARS_TO_ANALYZE);
      if (maxCharsToAnalyze < 0) {
        highlighter.setMaxDocCharsToAnalyze(docTexts[j].length());
      } else {
        highlighter.setMaxDocCharsToAnalyze(maxCharsToAnalyze);
      }
      try {
        TextFragment[] bestTextFragments = highlighter.getBestTextFragments(tstream, docTexts[j], mergeContiguousFragments, numFragments);
        for (int k = 0; k < bestTextFragments.length; k++) {
          if ((bestTextFragments[k] != null) && (bestTextFragments[k].getScore() > 0)) {
            frags.add(bestTextFragments[k]);
          }
        }
      } catch (InvalidTokenOffsetsException e) {
        throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e);
      }
    }
    Collections.sort(frags, new Comparator<TextFragment>() {
      public int compare(TextFragment arg0, TextFragment arg1) {
        return Math.round(arg1.getScore() - arg0.getScore());
      }
    });
    if (frags.size() > 0) {
      ArrayList<String> fragTexts = new ArrayList<String>();
      for (TextFragment fragment: frags) {
        if ((fragment != null) && (fragment.getScore() > 0)) {
          fragTexts.add(fragment.toString());
        }
        if (fragTexts.size() >= numFragments) break;
      }
      summaries = fragTexts.toArray(new String[0]);
      if (summaries.length > 0) 
      docSummaries.add(fieldName, summaries);
    }
    if (summaries == null || summaries.length == 0) {
      alternateField( docSummaries, params, doc, fieldName );
    }
  }
  private void doHighlightingByFastVectorHighlighter( FastVectorHighlighter highlighter, FieldQuery fieldQuery,
      SolrQueryRequest req, NamedList docSummaries, int docId, Document doc, String fieldName ) throws IOException {
    SolrParams params = req.getParams(); 
    String[] snippets = highlighter.getBestFragments( fieldQuery, req.getSearcher().getReader(), docId, fieldName,
        params.getFieldInt( fieldName, HighlightParams.FRAGSIZE, 100 ),
        params.getFieldInt( fieldName, HighlightParams.SNIPPETS, 1 ) );
    if( snippets != null && snippets.length > 0 )
      docSummaries.add( fieldName, snippets );
    else
      alternateField( docSummaries, params, doc, fieldName );
  }
  private void alternateField( NamedList docSummaries, SolrParams params, Document doc, String fieldName ){
    String alternateField = params.getFieldParam(fieldName, HighlightParams.ALTERNATE_FIELD);
    if (alternateField != null && alternateField.length() > 0) {
      String[] altTexts = doc.getValues(alternateField);
      if (altTexts != null && altTexts.length > 0){
        int alternateFieldLen = params.getFieldInt(fieldName, HighlightParams.ALTERNATE_FIELD_LENGTH,0);
        if( alternateFieldLen <= 0 ){
          docSummaries.add(fieldName, altTexts);
        }
        else{
          List<String> altList = new ArrayList<String>();
          int len = 0;
          for( String altText: altTexts ){
            altList.add( len + altText.length() > alternateFieldLen ?
                new String(altText.substring( 0, alternateFieldLen - len )) : altText );
            len += altText.length();
            if( len >= alternateFieldLen ) break;
          }
          docSummaries.add(fieldName, altList);
        }
      }
    }
  }
  private TokenStream createAnalyzerTStream(IndexSchema schema, String fieldName, String docText) throws IOException {
    TokenStream tstream;
    TokenStream ts = schema.getAnalyzer().reusableTokenStream(fieldName, new StringReader(docText));
    ts.reset();
    tstream = new TokenOrderingFilter(ts, 10);
    return tstream;
  }
}
class TokenOrderingFilter extends TokenFilter {
  private final int windowSize;
  private final LinkedList<OrderedToken> queue = new LinkedList<OrderedToken>();
  private boolean done=false;
  private final OffsetAttribute offsetAtt = (OffsetAttribute) addAttribute(OffsetAttribute.class);
  protected TokenOrderingFilter(TokenStream input, int windowSize) {
    super(input);
    this.windowSize = windowSize;
  }
  @Override
  public boolean incrementToken() throws IOException {
    while (!done && queue.size() < windowSize) {
      if (!input.incrementToken()) {
        done = true;
        break;
      }
      ListIterator<OrderedToken> iter = queue.listIterator(queue.size());
      while(iter.hasPrevious()) {
        if (offsetAtt.startOffset() >= iter.previous().startOffset) {
          iter.next();
          break;
        }
      }
      OrderedToken ot = new OrderedToken();
      ot.state = captureState();
      ot.startOffset = offsetAtt.startOffset();
      iter.add(ot);
    }
    if (queue.isEmpty()) {
      return false;
    } else {
      restoreState(queue.removeFirst().state);
      return true;
    }
  }
}
class OrderedToken {
  State state;
  int startOffset;
}
class TermOffsetsTokenStream {
  TokenStream bufferedTokenStream = null;
  OffsetAttribute bufferedOffsetAtt;
  State bufferedToken;
  int bufferedStartOffset;
  int bufferedEndOffset;
  int startOffset;
  int endOffset;
  public TermOffsetsTokenStream( TokenStream tstream ){
    bufferedTokenStream = tstream;
    bufferedOffsetAtt = (OffsetAttribute) bufferedTokenStream.addAttribute(OffsetAttribute.class);
    startOffset = 0;
    bufferedToken = null;
  }
  public TokenStream getMultiValuedTokenStream( final int length ){
    endOffset = startOffset + length;
    return new MultiValuedStream(length);
  }
  class MultiValuedStream extends TokenStream {
    private final int length;
    OffsetAttribute offsetAtt = (OffsetAttribute) addAttribute(OffsetAttribute.class);
      MultiValuedStream(int length) { 
        super(bufferedTokenStream.cloneAttributes());
        this.length = length;
      }
      public boolean incrementToken() throws IOException {
        while( true ){
          if( bufferedToken == null ) {
            if (!bufferedTokenStream.incrementToken())
              return false;
            bufferedToken = bufferedTokenStream.captureState();
            bufferedStartOffset = bufferedOffsetAtt.startOffset();
            bufferedEndOffset = bufferedOffsetAtt.endOffset();
          }
          if( startOffset <= bufferedStartOffset &&
              bufferedEndOffset <= endOffset ){
            restoreState(bufferedToken);
            bufferedToken = null;
            offsetAtt.setOffset( offsetAtt.startOffset() - startOffset, offsetAtt.endOffset() - startOffset );
            return true;
          }
          else if( bufferedEndOffset > endOffset ){
            startOffset += length + 1;
            return false;
          }
          bufferedToken = null;
        }
      }
  };
};
