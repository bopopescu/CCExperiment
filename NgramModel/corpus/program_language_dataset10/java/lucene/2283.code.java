package org.apache.solr.analysis;
import org.apache.lucene.analysis.th.*;
import java.io.IOException;
import java.util.Locale;
import java.lang.Character.UnicodeBlock;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import java.text.BreakIterator;
import java.util.Map;
public class ThaiWordFilterFactory extends BaseTokenFilterFactory {
  public ThaiWordFilter create(TokenStream input) {
    return new ThaiWordFilter(input);
  }
}
