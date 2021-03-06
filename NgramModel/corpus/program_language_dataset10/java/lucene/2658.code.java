package org.apache.solr.client.solrj.response;
import org.apache.solr.common.util.NamedList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class FieldAnalysisResponse extends AnalysisResponseBase {
  private Map<String, Analysis> analysisByFieldTypeName = new HashMap<String, Analysis>();
  private Map<String, Analysis> analysisByFieldName = new HashMap<String, Analysis>();
  @Override
  public void setResponse(NamedList<Object> response) {
    super.setResponse(response);
    NamedList analysisNL = (NamedList) response.get("analysis");
    NamedList<Object> fieldTypesNL = (NamedList<Object>) analysisNL.get("field_types");
    for (Map.Entry<String, Object> entry : fieldTypesNL) {
      Analysis analysis = new Analysis();
      NamedList fieldTypeNL = (NamedList) entry.getValue();
      NamedList<Object> queryNL = (NamedList<Object>) fieldTypeNL.get("query");
      List<AnalysisPhase> phases = (queryNL == null) ? null : buildPhases(queryNL);
      analysis.setQueryPhases(phases);
      NamedList<Object> indexNL = (NamedList<Object>) fieldTypeNL.get("index");
      phases = buildPhases(indexNL);
      analysis.setIndexPhases(phases);
      String fieldTypeName = entry.getKey();
      analysisByFieldTypeName.put(fieldTypeName, analysis);
    }
    NamedList<Object> fieldNamesNL = (NamedList<Object>) analysisNL.get("field_names");
    for (Map.Entry<String, Object> entry : fieldNamesNL) {
      Analysis analysis = new Analysis();
      NamedList fieldNameNL = (NamedList) entry.getValue();
      NamedList<Object> queryNL = (NamedList<Object>) fieldNameNL.get("query");
      List<AnalysisPhase> phases = (queryNL == null) ? null : buildPhases(queryNL);
      analysis.setQueryPhases(phases);
      NamedList<Object> indexNL = (NamedList<Object>) fieldNameNL.get("index");
      phases = buildPhases(indexNL);
      analysis.setIndexPhases(phases);
      String fieldName = entry.getKey();
      analysisByFieldName.put(fieldName, analysis);
    }
  }
  public int getFieldTypeAnalysisCount() {
    return analysisByFieldTypeName.size();
  }
  public Analysis getFieldTypeAnalysis(String fieldTypeName) {
    return analysisByFieldTypeName.get(fieldTypeName);
  }
  public Iterable<Map.Entry<String, Analysis>> getAllFieldTypeAnalysis() {
    return analysisByFieldTypeName.entrySet();
  }
  public int getFieldNameAnalysisCount() {
    return analysisByFieldName.size();
  }
  public Analysis getFieldNameAnalysis(String fieldName) {
    return analysisByFieldName.get(fieldName);
  }
  public Iterable<Map.Entry<String, Analysis>> getAllFieldNameAnalysis() {
    return analysisByFieldName.entrySet();
  }
  public static class Analysis {
    private List<AnalysisPhase> queryPhases;
    private List<AnalysisPhase> indexPhases;
    private Analysis() {
    }
    public int getQueryPhasesCount() {
      return queryPhases == null ? -1 : queryPhases.size();
    }
    public Iterable<AnalysisPhase> getQueryPhases() {
      return queryPhases;
    }
    public int getIndexPhasesCount() {
      return indexPhases.size();
    }
    public Iterable<AnalysisPhase> getIndexPhases() {
      return indexPhases;
    }
    private void setQueryPhases(List<AnalysisPhase> queryPhases) {
      this.queryPhases = queryPhases;
    }
    private void setIndexPhases(List<AnalysisPhase> indexPhases) {
      this.indexPhases = indexPhases;
    }
  }
}
