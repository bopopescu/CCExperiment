package org.apache.solr.common.params;
import java.util.regex.Pattern;
public interface TermsParams {
  public static final String TERMS = "terms";
  public static final String TERMS_PREFIX = TERMS + ".";
  public static final String TERMS_FIELD = TERMS_PREFIX + "fl";
  public static final String TERMS_LOWER = TERMS_PREFIX + "lower";
  public static final String TERMS_UPPER = TERMS_PREFIX + "upper";
  public static final String TERMS_UPPER_INCLUSIVE = TERMS_PREFIX + "upper.incl";
  public static final String TERMS_LOWER_INCLUSIVE = TERMS_PREFIX + "lower.incl";
  public static final String TERMS_LIMIT = TERMS_PREFIX + "limit";
  public static final String TERMS_PREFIX_STR = TERMS_PREFIX + "prefix";
  public static final String TERMS_REGEXP_STR = TERMS_PREFIX + "regex";
  public static final String TERMS_REGEXP_FLAG = TERMS_REGEXP_STR + ".flag";
  public static enum TermsRegexpFlag {
      UNIX_LINES(Pattern.UNIX_LINES),
      CASE_INSENSITIVE(Pattern.CASE_INSENSITIVE),
      COMMENTS(Pattern.COMMENTS),
      MULTILINE(Pattern.MULTILINE),
      LITERAL(Pattern.LITERAL),
      DOTALL(Pattern.DOTALL),
      UNICODE_CASE(Pattern.UNICODE_CASE),
      CANON_EQ(Pattern.CANON_EQ);
      int value;
      TermsRegexpFlag(int value) {
          this.value = value;
      }
      public int getValue() {
          return value;
      }
  }
  public static final String TERMS_MINCOUNT = TERMS_PREFIX + "mincount";
  public static final String TERMS_MAXCOUNT = TERMS_PREFIX + "maxcount";
  public static final String TERMS_RAW = TERMS_PREFIX + "raw";
  public static final String TERMS_SORT = TERMS_PREFIX + "sort";
  public static final String TERMS_SORT_COUNT = "count";
  public static final String TERMS_SORT_INDEX = "index";
}
