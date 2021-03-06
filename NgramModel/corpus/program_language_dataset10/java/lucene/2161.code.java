package org.apache.solr.common.params;
public interface CommonParams {
  public static final String QT ="qt";
  public static final String WT ="wt";
  public static final String Q ="q";
  public static final String SORT ="sort";
  public static final String FQ ="fq";
  public static final String START ="start";
  public static final String ROWS ="rows";
  public static final String XSL ="xsl";
  public static final String VERSION ="version";
  public static final String FL = "fl";
  public static final String DF = "df";
  public static final String DEBUG_QUERY = "debugQuery";
  public static final String EXPLAIN_OTHER = "explainOther";
  public static final String STREAM_URL = "stream.url";
  public static final String STREAM_FILE = "stream.file";
  public static final String STREAM_BODY = "stream.body";
  public static final String STREAM_CONTENTTYPE = "stream.contentType";
  public static final String TIME_ALLOWED = "timeAllowed";
  public static final String HEADER_ECHO_HANDLER = "echoHandler";
  public static final String HEADER_ECHO_PARAMS = "echoParams";
  public static final String OMIT_HEADER = "omitHeader";
  public enum EchoParamStyle {
    EXPLICIT,
    ALL,
    NONE;
    public static EchoParamStyle get( String v ) {
      if( v != null ) {
        v = v.toUpperCase();
        if( v.equals( "EXPLICIT" ) ) {
          return EXPLICIT;
        }
        if( v.equals( "ALL") ) {
          return ALL;
        }
        if( v.equals( "NONE") ) {  
          return NONE;
        }
      }
      return null;
    }
  };
  public static final String EXCLUDE = "ex";
  public static final String TAG = "tag";
  public static final String TERMS = "terms";
  public static final String OUTPUT_KEY = "key";
  public static final String FIELD = "f";
  public static final String VALUE = "v";
  public static final String TRUE = Boolean.TRUE.toString();
  public static final String FALSE = Boolean.FALSE.toString();
}
