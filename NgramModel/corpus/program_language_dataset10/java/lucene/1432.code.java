package org.apache.lucene.analysis;
import java.io.IOException;
import java.io.Reader;
public class KeywordAnalyzer extends Analyzer {
  public KeywordAnalyzer() {
  }
  @Override
  public TokenStream tokenStream(String fieldName,
                                 final Reader reader) {
    return new KeywordTokenizer(reader);
  }
  @Override
  public TokenStream reusableTokenStream(String fieldName,
                                         final Reader reader) throws IOException {
    if (overridesTokenStreamMethod) {
      return tokenStream(fieldName, reader);
    }
    Tokenizer tokenizer = (Tokenizer) getPreviousTokenStream();
    if (tokenizer == null) {
      tokenizer = new KeywordTokenizer(reader);
      setPreviousTokenStream(tokenizer);
    } else
      tokenizer.reset(reader);
    return tokenizer;
  }
}