package org.apache.lucene.analysis;
import java.io.Reader;
import org.apache.lucene.util.AttributeSource;
import org.apache.lucene.util.Version;
public final class LowerCaseTokenizer extends LetterTokenizer {
  public LowerCaseTokenizer(Version matchVersion, Reader in) {
    super(matchVersion, in);
  }
  public LowerCaseTokenizer(Version matchVersion, AttributeSource source, Reader in) {
    super(matchVersion, source, in);
  }
  public LowerCaseTokenizer(Version matchVersion, AttributeFactory factory, Reader in) {
    super(matchVersion, factory, in);
  }
  @Deprecated
  public LowerCaseTokenizer(Reader in) {
    super(Version.LUCENE_30, in);
  }
  public LowerCaseTokenizer(AttributeSource source, Reader in) {
    super(Version.LUCENE_30, source, in);
  }
  public LowerCaseTokenizer(AttributeFactory factory, Reader in) {
    super(Version.LUCENE_30, factory, in);
  }
  @Override
  protected int normalize(int c) {
    return Character.toLowerCase(c);
  }
}
