package org.apache.solr.handler;
import org.apache.lucene.analysis.Token;
import org.apache.solr.client.solrj.request.FieldAnalysisRequest;
import org.apache.solr.common.params.AnalysisParams;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.common.util.ContentStream;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.commons.io.IOUtils;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.io.Reader;
import java.io.IOException;
public class FieldAnalysisRequestHandler extends AnalysisRequestHandlerBase {
  protected NamedList doAnalysis(SolrQueryRequest req) throws Exception {
    FieldAnalysisRequest analysisRequest = resolveAnalysisRequest(req);
    IndexSchema indexSchema = req.getCore().getSchema();
    return handleAnalysisRequest(analysisRequest, indexSchema);
  }
  @Override
  public String getDescription() {
    return "Provide a breakdown of the analysis process of field/query text";
  }
  @Override
  public String getVersion() {
    return "$Revision: 805844 $";
  }
  @Override
  public String getSourceId() {
    return "$Id: FieldAnalysisRequestHandler.java 805844 2009-08-19 15:38:11Z ehatcher $";
  }
  @Override
  public String getSource() {
    return "$URL: http://svn.apache.org/repos/asf/lucene/solr/branches/newtrunk/solr/src/java/org/apache/solr/handler/FieldAnalysisRequestHandler.java $";
  }
  FieldAnalysisRequest resolveAnalysisRequest(SolrQueryRequest req) {
    SolrParams solrParams = req.getParams();
    FieldAnalysisRequest analysisRequest = new FieldAnalysisRequest();
    boolean useDefaultSearchField = true;
    if (solrParams.get(AnalysisParams.FIELD_TYPE) != null) {
      analysisRequest.setFieldTypes(Arrays.asList(solrParams.get(AnalysisParams.FIELD_TYPE).split(",")));
      useDefaultSearchField = false;
    }
    if (solrParams.get(AnalysisParams.FIELD_NAME) != null) {
      analysisRequest.setFieldNames(Arrays.asList(solrParams.get(AnalysisParams.FIELD_NAME).split(",")));
      useDefaultSearchField = false;
    }
    if (useDefaultSearchField)  {
      analysisRequest.addFieldName(req.getSchema().getDefaultSearchFieldName());
    }
    analysisRequest.setQuery(solrParams.get(AnalysisParams.QUERY, solrParams.get(CommonParams.Q)));
    String value = solrParams.get(AnalysisParams.FIELD_VALUE);
    Iterable<ContentStream> streams = req.getContentStreams();
    if (streams != null) {
      for (ContentStream stream : streams) {
        Reader reader = null;
        try {
          reader = stream.getReader();
          value = IOUtils.toString(reader);
        } catch (IOException e) {
        }
        finally {
          IOUtils.closeQuietly(reader);
        }
        break;
      }
    }
    analysisRequest.setFieldValue(value);
    analysisRequest.setShowMatch(solrParams.getBool(AnalysisParams.SHOW_MATCH, false));
    return analysisRequest;
  }
  protected NamedList<NamedList> handleAnalysisRequest(FieldAnalysisRequest request, IndexSchema schema) {
    NamedList<NamedList> analysisResults = new SimpleOrderedMap<NamedList>();
    NamedList<NamedList> fieldTypeAnalysisResults = new SimpleOrderedMap<NamedList>();
    if (request.getFieldTypes() != null)  {
      for (String fieldTypeName : request.getFieldTypes()) {
        FieldType fieldType = schema.getFieldTypes().get(fieldTypeName);
        fieldTypeAnalysisResults.add(fieldTypeName, analyzeValues(request, fieldType, null));
      }
    }
    NamedList<NamedList> fieldNameAnalysisResults = new SimpleOrderedMap<NamedList>();
    if (request.getFieldNames() != null)  {
      for (String fieldName : request.getFieldNames()) {
        FieldType fieldType = schema.getFieldType(fieldName);
        fieldNameAnalysisResults.add(fieldName, analyzeValues(request, fieldType, fieldName));
      }
    }
    analysisResults.add("field_types", fieldTypeAnalysisResults);
    analysisResults.add("field_names", fieldNameAnalysisResults);
    return analysisResults;
  }
  private NamedList<NamedList> analyzeValues(FieldAnalysisRequest analysisRequest, FieldType fieldType, String fieldName) {
    Set<String> termsToMatch = new HashSet<String>();
    String queryValue = analysisRequest.getQuery();
    if (queryValue != null && analysisRequest.isShowMatch()) {
      List<Token> tokens = analyzeValue(queryValue, fieldType.getQueryAnalyzer());
      for (Token token : tokens) {
        termsToMatch.add(token.term());
      }
    }
    NamedList<NamedList> analyzeResults = new SimpleOrderedMap<NamedList>();
    if (analysisRequest.getFieldValue() != null) {
      AnalysisContext context = new AnalysisContext(fieldName, fieldType, fieldType.getAnalyzer(), termsToMatch);
      NamedList analyzedTokens = analyzeValue(analysisRequest.getFieldValue(), context);
      analyzeResults.add("index", analyzedTokens);
    }
    if (analysisRequest.getQuery() != null) {
      AnalysisContext context = new AnalysisContext(fieldName, fieldType, fieldType.getQueryAnalyzer());
      NamedList analyzedTokens = analyzeValue(analysisRequest.getQuery(), context);
      analyzeResults.add("query", analyzedTokens);
    }
    return analyzeResults;
  }
}
