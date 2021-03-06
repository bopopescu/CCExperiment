package org.apache.lucene.search.regex;
import java.util.regex.Pattern;
public class JavaUtilRegexCapabilities implements RegexCapabilities {
  private Pattern pattern;
  private int flags = 0;
  public static final int FLAG_CANON_EQ = Pattern.CANON_EQ;
  public static final int FLAG_CASE_INSENSITIVE = Pattern.CASE_INSENSITIVE;
  public static final int FLAG_COMMENTS = Pattern.COMMENTS;
  public static final int FLAG_DOTALL = Pattern.DOTALL;
  public static final int FLAG_LITERAL = Pattern.LITERAL;
  public static final int FLAG_MULTILINE = Pattern.MULTILINE;
  public static final int FLAG_UNICODE_CASE = Pattern.UNICODE_CASE;
  public static final int FLAG_UNIX_LINES = Pattern.UNIX_LINES;
  public JavaUtilRegexCapabilities()  {
    this.flags = 0;
  }
  public JavaUtilRegexCapabilities(int flags) {
    this.flags = flags;
  }
  public void compile(String pattern) {
    this.pattern = Pattern.compile(pattern, this.flags);
  }
  public boolean match(String string) {
    return pattern.matcher(string).matches();
  }
  public String prefix() {
    return null;
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    final JavaUtilRegexCapabilities that = (JavaUtilRegexCapabilities) o;
    if (pattern != null ? !pattern.equals(that.pattern) : that.pattern != null) return false;
    return true;
  }
  @Override
  public int hashCode() {
    return (pattern != null ? pattern.hashCode() : 0);
  }
}
