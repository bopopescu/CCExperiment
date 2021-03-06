package org.apache.lucene.queryParser.surround.query;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.IndexReader;
import java.io.IOException;
public class SrndPrefixQuery extends SimpleTerm {
  public SrndPrefixQuery(String prefix, boolean quoted, char truncator) {
    super(quoted);
    this.prefix = prefix;
    this.truncator = truncator;
  }
  private final String prefix;
  public String getPrefix() {return prefix;}
  private final char truncator;
  public char getSuffixOperator() {return truncator;}
  public Term getLucenePrefixTerm(String fieldName) {
    return new Term(fieldName, getPrefix());
  }
  @Override
  public String toStringUnquoted() {return getPrefix();}
  @Override
  protected void suffixToString(StringBuilder r) {r.append(getSuffixOperator());}
  @Override
  public void visitMatchingTerms(
    IndexReader reader,
    String fieldName,
    MatchingTermVisitor mtv) throws IOException
  {
    TermEnum enumerator = reader.terms(getLucenePrefixTerm(fieldName));
    try {
      do {
        Term term = enumerator.term();
        if ((term != null)
            && term.text().startsWith(getPrefix())
            && term.field().equals(fieldName)) {
          mtv.visitMatchingTerm(term);
        } else {
          break;
        }
      } while (enumerator.next());
    } finally {
      enumerator.close();
    }
  }
}
