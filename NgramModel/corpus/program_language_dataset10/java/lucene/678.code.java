package org.apache.lucene.analysis.miscellaneous;
import java.io.IOException;
import org.apache.lucene.util.AttributeImpl;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
public final class SingleTokenTokenStream extends TokenStream {
  private boolean exhausted = false;
  private Token singleToken;
  private final AttributeImpl tokenAtt;
  public SingleTokenTokenStream(Token token) {
    super(Token.TOKEN_ATTRIBUTE_FACTORY);
    assert token != null;
    this.singleToken = (Token) token.clone();
    tokenAtt = (AttributeImpl) addAttribute(TermAttribute.class);
    assert (tokenAtt instanceof Token);
  }
  @Override
  public final boolean incrementToken() throws IOException {
    if (exhausted) {
      return false;
    } else {
      clearAttributes();
      singleToken.copyTo(tokenAtt);
      exhausted = true;
      return true;
    }
  }
  @Override
  public void reset() throws IOException {
    exhausted = false;
  }
  public Token getToken() {
    return (Token) singleToken.clone();
  }
  public void setToken(Token token) {
    this.singleToken = (Token) token.clone();
  }
}
