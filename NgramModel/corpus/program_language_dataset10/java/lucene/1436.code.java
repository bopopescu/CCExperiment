package org.apache.lucene.analysis;
import java.io.Reader;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;
public class LetterTokenizer extends CharTokenizer {
  public LetterTokenizer(Version matchVersion, Reader in) {
    super(matchVersion, in);
  }
  public LetterTokenizer(Version matchVersion, AttributeSource source, Reader in) {
    super(matchVersion, source, in);
  }
  public LetterTokenizer(Version matchVersion, AttributeFactory factory, Reader in) {
    super(matchVersion, factory, in);
  }
  public LetterTokenizer(Reader in) {
    super(Version.LUCENE_30, in);
  }
  public LetterTokenizer(AttributeSource source, Reader in) {
    super(Version.LUCENE_30, source, in);
  }
  public LetterTokenizer(AttributeFactory factory, Reader in) {
    super(Version.LUCENE_30, factory, in);
  }
  @Override
  protected boolean isTokenChar(int c) {
    return Character.isLetter(c);
  }
}
