package org.apache.solr.client.solrj;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.params.HighlightParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.StatsParams;
import org.apache.solr.common.params.TermsParams;
import java.util.regex.Pattern;
public class SolrQuery extends ModifiableSolrParams 
{  
  public enum ORDER { desc, asc;
    public ORDER reverse() {
      return (this == asc) ? desc : asc;
    }
  }
  public SolrQuery() {
    super();
  }
  public SolrQuery(String q) {
    this();
    this.set(CommonParams.Q, q);
  }
  public SolrQuery setTerms(boolean b) {
    if (b) {
      this.set(TermsParams.TERMS, true);
    } else {
      this.remove(TermsParams.TERMS);
      this.remove(TermsParams.TERMS_FIELD);
      this.remove(TermsParams.TERMS_LOWER);
      this.remove(TermsParams.TERMS_UPPER);
      this.remove(TermsParams.TERMS_UPPER_INCLUSIVE);
      this.remove(TermsParams.TERMS_LOWER_INCLUSIVE);
      this.remove(TermsParams.TERMS_LIMIT);
      this.remove(TermsParams.TERMS_PREFIX_STR);
      this.remove(TermsParams.TERMS_MINCOUNT);
      this.remove(TermsParams.TERMS_MAXCOUNT);
      this.remove(TermsParams.TERMS_RAW);
      this.remove(TermsParams.TERMS_SORT);
      this.remove(TermsParams.TERMS_REGEXP_STR);
      this.remove(TermsParams.TERMS_REGEXP_FLAG);
    }
    return this;
  }
  public boolean getTerms() {
    return this.getBool(TermsParams.TERMS, false);
  }
  public SolrQuery addTermsField(String field) {
    this.add(TermsParams.TERMS_FIELD, field);
    return this;
  }
  public String[] getTermsFields() {
    return this.getParams(TermsParams.TERMS_FIELD);
  }
  public SolrQuery setTermsLower(String lower) {
    this.set(TermsParams.TERMS_LOWER, lower);
    return this;
  }
  public String getTermsLower() {
    return this.get(TermsParams.TERMS_LOWER, "");
  }
  public SolrQuery setTermsUpper(String upper) {
    this.set(TermsParams.TERMS_UPPER, upper);
    return this;
  }
  public String getTermsUpper() {
    return this.get(TermsParams.TERMS_UPPER, "");
  }
  public SolrQuery setTermsUpperInclusive(boolean b) {
    this.set(TermsParams.TERMS_UPPER_INCLUSIVE, b);
    return this;
  }
  public boolean getTermsUpperInclusive() {
    return this.getBool(TermsParams.TERMS_UPPER_INCLUSIVE, false);
  }
  public SolrQuery setTermsLowerInclusive(boolean b) {
    this.set(TermsParams.TERMS_LOWER_INCLUSIVE, b);
    return this;
  }
  public boolean getTermsLowerInclusive() {
    return this.getBool(TermsParams.TERMS_LOWER_INCLUSIVE, true);
  }
  public SolrQuery setTermsLimit(int limit) {
    this.set(TermsParams.TERMS_LIMIT, limit);
    return this;
  }
  public int getTermsLimit() {
    return this.getInt(TermsParams.TERMS_LIMIT, 10);
  }
  public SolrQuery setTermsMinCount(int cnt) {
    this.set(TermsParams.TERMS_MINCOUNT, cnt);
    return this;
  }
  public int getTermsMinCount() {
    return this.getInt(TermsParams.TERMS_MINCOUNT, 1);
  }
  public SolrQuery setTermsMaxCount(int cnt) {
    this.set(TermsParams.TERMS_MAXCOUNT, cnt);
    return this;
  }
  public int getTermsMaxCount() {
    return this.getInt(TermsParams.TERMS_MAXCOUNT, -1);
  }
  public SolrQuery setTermsPrefix(String prefix) {
    this.set(TermsParams.TERMS_PREFIX_STR, prefix);
    return this;
  }
  public String getTermsPrefix() {
    return this.get(TermsParams.TERMS_PREFIX_STR, "");
  }
  public SolrQuery setTermsRaw(boolean b) {
    this.set(TermsParams.TERMS_RAW, b);
    return this;
  }
  public boolean getTermsRaw() {
    return this.getBool(TermsParams.TERMS_RAW, false);
  }
  public SolrQuery setTermsSortString(String type) {
    this.set(TermsParams.TERMS_SORT, type);
    return this;
  }
  public String getTermsSortString() {
    return this.get(TermsParams.TERMS_SORT, TermsParams.TERMS_SORT_COUNT);
  }
  public SolrQuery setTermsRegex(String regex)  {
    this.set(TermsParams.TERMS_REGEXP_STR, regex);
    return this;
  }
  public String getTermsRegex() {
    return this.get(TermsParams.TERMS_REGEXP_STR);
  }
  public SolrQuery setTermsRegexFlag(String flag) {
    this.add(TermsParams.TERMS_REGEXP_FLAG, flag);
    return this;
  }
  public String[] getTermsRegexFlags()  {
    return this.getParams(TermsParams.TERMS_REGEXP_FLAG);
  }
  public SolrQuery addFacetField(String ... fields) {
    add(FacetParams.FACET_FIELD, fields);
    this.set(FacetParams.FACET, true);
    return this;
  }
  public String[] getFacetFields() {
    return this.getParams(FacetParams.FACET_FIELD);
  }
  public boolean removeFacetField(String name) {
    boolean b = this.remove(FacetParams.FACET_FIELD, name);
    if (this.get(FacetParams.FACET_FIELD) == null && this.get(FacetParams.FACET_QUERY) == null) {
      this.setFacet(false);
    }
    return b;
  }
  public SolrQuery setFacet(boolean b) {
    if (b) {
      this.set(FacetParams.FACET, true);
    } else {
      this.remove(FacetParams.FACET);
      this.remove(FacetParams.FACET_MINCOUNT);
      this.remove(FacetParams.FACET_FIELD);
      this.remove(FacetParams.FACET_LIMIT);
      this.remove(FacetParams.FACET_MISSING);
      this.remove(FacetParams.FACET_OFFSET);
      this.remove(FacetParams.FACET_PREFIX);
      this.remove(FacetParams.FACET_QUERY);
      this.remove(FacetParams.FACET_SORT);
      this.remove(FacetParams.FACET_ZEROS);
      this.remove(FacetParams.FACET_PREFIX); 
    }
    return this;
  }
  public SolrQuery setFacetPrefix( String prefix )
  {
    this.set( FacetParams.FACET_PREFIX, prefix );
    return this;
  }
  public SolrQuery setFacetPrefix( String field, String prefix )
  {
    this.set( "f."+field+"."+FacetParams.FACET_PREFIX, prefix );
    return this;
  }
  public SolrQuery addFacetQuery(String f) {
    this.add(FacetParams.FACET_QUERY, f);
    return this;
  }
  public String[] getFacetQuery() {
    return this.getParams(FacetParams.FACET_QUERY);
  }
  public boolean removeFacetQuery(String q) {
    boolean b = this.remove(FacetParams.FACET_QUERY, q);
    if (this.get(FacetParams.FACET_FIELD) == null && this.get(FacetParams.FACET_QUERY) == null) {
      this.setFacet(false);
    }
    return b;
  }
  public SolrQuery setFacetLimit(int lim) {
    this.set(FacetParams.FACET_LIMIT, lim);
    return this;
  }
  public int getFacetLimit() {
    return this.getInt(FacetParams.FACET_LIMIT, 25);
  }
  public SolrQuery setFacetMinCount(int cnt) {
    this.set(FacetParams.FACET_MINCOUNT, cnt);
    return this;
  }
  public int getFacetMinCount() {
    return this.getInt(FacetParams.FACET_MINCOUNT, 1);
  }
  public SolrQuery setFacetMissing(Boolean v) {
    this.set(FacetParams.FACET_MISSING, v);
    return this;
  }
  public SolrQuery setMissing(String fld) {
    return setFacetMissing(Boolean.valueOf(fld));
  }
  public String getFacetSortString() {
    return this.get(FacetParams.FACET_SORT, FacetParams.FACET_SORT_COUNT);
  }
  @Deprecated
  public boolean getFacetSort() {
    return this.get(FacetParams.FACET_SORT, FacetParams.FACET_SORT_COUNT).equals(FacetParams.FACET_SORT_COUNT);
  }
  public SolrQuery setFacetSort(String sort) {
    this.set(FacetParams.FACET_SORT, sort);
    return this;
  }
  @Deprecated
  public SolrQuery setFacetSort(boolean sort) { 
    this.set(FacetParams.FACET_SORT, sort == true ? FacetParams.FACET_SORT_COUNT : FacetParams.FACET_SORT_INDEX);
    return this;
  }
  public SolrQuery addHighlightField(String f) {
    this.add(HighlightParams.FIELDS, f);
    this.set(HighlightParams.HIGHLIGHT, true);
    return this;
  }
  public boolean removeHighlightField(String f) {
    boolean b = this.remove(HighlightParams.FIELDS, f);
    if (this.get(HighlightParams.FIELDS) == null) {
      this.setHighlight(false);
    }
    return b;
  }
  public String[] getHighlightFields() {
    return this.getParams(HighlightParams.FIELDS);
  }
  public SolrQuery setHighlightSnippets(int num) {
    this.set(HighlightParams.SNIPPETS, num);
    return this;
  }
  public int getHighlightSnippets() {
    return this.getInt(HighlightParams.SNIPPETS, 1);
  }
  public SolrQuery setHighlightFragsize(int num) {
    this.set(HighlightParams.FRAGSIZE, num);
    return this;
  }
  public int getHighlightFragsize() {
    return this.getInt(HighlightParams.FRAGSIZE, 100);
  }
  public SolrQuery setHighlightRequireFieldMatch(boolean flag) {
    this.set(HighlightParams.FIELD_MATCH, flag);
    return this;
  }
  public boolean getHighlightRequireFieldMatch() {
    return this.getBool(HighlightParams.FIELD_MATCH, false);
  }
  public SolrQuery setHighlightSimplePre(String f) {
    this.set(HighlightParams.SIMPLE_PRE, f);
    return this;
  }
  public String getHighlightSimplePre() {
    return this.get(HighlightParams.SIMPLE_PRE, "");
  }
  public SolrQuery setHighlightSimplePost(String f) {
    this.set(HighlightParams.SIMPLE_POST, f);
    return this;
  }
  public String getHighlightSimplePost() {
    return this.get(HighlightParams.SIMPLE_POST, "");
  }
  public SolrQuery setSortField(String field, ORDER order) {
    this.remove(CommonParams.SORT);
    addValueToParam(CommonParams.SORT, toSortString(field, order));
    return this;
  }
  public SolrQuery addSortField(String field, ORDER order) {
    return addValueToParam(CommonParams.SORT, toSortString(field, order));
  }
  public SolrQuery removeSortField(String field, ORDER order) {
    String s = this.get(CommonParams.SORT);
    String removeSort = toSortString(field, order);
    if (s != null) {
      String[] sorts = s.split(",");
      s = join(sorts, ", ", removeSort);
      if (s.length()==0) s=null;
      this.set(CommonParams.SORT, s);
    }
    return this;
  }
  public String[] getSortFields() {
    String s = getSortField();
    if (s==null) return null;
    return s.split(",");
  }
  public String getSortField() {
    return this.get(CommonParams.SORT);
  }
  public void setGetFieldStatistics( boolean v )
  {
    this.set( StatsParams.STATS, v );
  }
  public void setGetFieldStatistics( String field )
  {
    this.set( StatsParams.STATS, true );
    this.add( StatsParams.STATS_FIELD, field );
  }
  public void addStatsFieldFacets( String field, String ... facets )
  {
    if( field == null ) {
      this.add( StatsParams.STATS_FACET, facets );
    }
    else {
      for( String f : facets ) {
        this.add( "f."+field+"."+StatsParams.STATS_FACET, f );
      }
    }
  }
  public SolrQuery setFilterQueries(String ... fq) {
    this.set(CommonParams.FQ, fq);
    return this;
  }
  public SolrQuery addFilterQuery(String ... fq) {
    this.add(CommonParams.FQ, fq);
    return this;
  }
  public boolean removeFilterQuery(String fq) {
    return this.remove(CommonParams.FQ, fq);
  }
  public String[] getFilterQueries() {
    return this.getParams(CommonParams.FQ);
  }
  public boolean getHighlight() {
    return this.getBool(HighlightParams.HIGHLIGHT, false);
  }
  public SolrQuery setHighlight(boolean b) {
    if (b) {
      this.set(HighlightParams.HIGHLIGHT, true);
    } else {
      this.remove(HighlightParams.HIGHLIGHT);
      this.remove(HighlightParams.FIELD_MATCH);
      this.remove(HighlightParams.FIELDS);
      this.remove(HighlightParams.FORMATTER);
      this.remove(HighlightParams.FRAGSIZE);
      this.remove(HighlightParams.SIMPLE_POST);
      this.remove(HighlightParams.SIMPLE_PRE);
      this.remove(HighlightParams.SNIPPETS);
    }
    return this;
  }
  public SolrQuery setFields(String ... fields) {
    if( fields == null || fields.length == 0 ) {
      this.remove( CommonParams.FL );
      return this;
    }
    StringBuilder sb = new StringBuilder();
    sb.append( fields[0] );
    for( int i=1; i<fields.length; i++ ) {
      sb.append( ',' );
      sb.append( fields[i] );
    }
    this.set(CommonParams.FL, sb.toString() );
    return this;
  }
  public SolrQuery addField(String field) {
    return addValueToParam(CommonParams.FL, field);
  }
  public String getFields() {
    String fields = this.get(CommonParams.FL);
    if (fields!=null && fields.equals("score")) {
      fields = "*, score";
    }
    return fields;
  }
  private static Pattern scorePattern = Pattern.compile("(^|[, ])score");
  public SolrQuery setIncludeScore(boolean includeScore) {
    String fields = get(CommonParams.FL,"*");
    if (includeScore) {
      if (!scorePattern.matcher(fields).find()) {   
        this.set(CommonParams.FL, fields+",score");
      }
    } else {
      this.set(CommonParams.FL, scorePattern.matcher(fields).replaceAll(""));
    }
    return this;
  }
  public SolrQuery setQuery(String query) {
    this.set(CommonParams.Q, query);
    return this;
  }
  public String getQuery() {
    return this.get(CommonParams.Q);
  }
  public SolrQuery setRows(Integer rows) {
    if( rows == null ) {
      this.remove( CommonParams.ROWS );
    }
    else {
      this.set(CommonParams.ROWS, rows);
    }
    return this;
  }
  public Integer getRows()
  {
    return this.getInt(CommonParams.ROWS);
  }
  public void setShowDebugInfo(boolean showDebugInfo) {
    this.set(CommonParams.DEBUG_QUERY, String.valueOf(showDebugInfo));
  }
  public SolrQuery setStart(Integer start) {
    if( start == null ) {
      this.remove( CommonParams.START );
    }
    else {
      this.set(CommonParams.START, start);
    }
    return this;
  }
  public Integer getStart()
  {
    return this.getInt(CommonParams.START);
  }
  public SolrQuery setQueryType(String qt) {
    this.set(CommonParams.QT, qt);
    return this;
  }
  public String getQueryType() {
    return this.get(CommonParams.QT);
  }
  public SolrQuery setParam(String name, String ... values) {
    this.set(name, values);
    return this;
  }
  public SolrQuery setParam(String name, boolean value) {
    this.set(name, value);
    return this;
  }
  public SolrQuery getCopy() {
    SolrQuery q = new SolrQuery();
    for (String name : this.getParameterNames()) {
      q.setParam(name, this.getParams(name));
    }
    return q;
  }
  public SolrQuery setTimeAllowed(Integer milliseconds) {
    if (milliseconds == null) {
      this.remove(CommonParams.TIME_ALLOWED);
    } else {
      this.set(CommonParams.TIME_ALLOWED, milliseconds);
    }
    return this;
  }
  public Integer getTimeAllowed() {
    return this.getInt(CommonParams.TIME_ALLOWED);
  }
  private String toSortString(String field, ORDER order) {
    return field.trim() + ' ' + String.valueOf(order).trim();
  }
  private String join(String a, String b, String sep) {
    StringBuilder sb = new StringBuilder();
    if (a!=null && a.length()>0) {
      sb.append(a);
      sb.append(sep);
    } 
    if (b!=null && b.length()>0) {
      sb.append(b);
    }
    return sb.toString().trim();
  }
  private SolrQuery addValueToParam(String name, String value) {
    String tmp = this.get(name);
    tmp = join(tmp, value, ",");
    this.set(name, tmp);
    return this;
  }
  private String join(String[] vals, String sep, String removeVal) {
    StringBuilder sb = new StringBuilder();
    for (int i=0; i<vals.length; i++) {
      if (removeVal==null || !vals[i].equals(removeVal)) {
        sb.append(vals[i]);
        if (i<vals.length-1) {
          sb.append(sep);
        }
      }
    }
    return sb.toString().trim();
  }
}
