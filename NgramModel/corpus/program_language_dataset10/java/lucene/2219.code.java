package org.apache.solr.analysis;
import java.io.IOException;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import static org.apache.solr.analysis.CommonGramsFilter.GRAM_TYPE;
public final class CommonGramsQueryFilter extends TokenFilter {
  private final TypeAttribute typeAttribute = (TypeAttribute) addAttribute(TypeAttribute.class);
  private final PositionIncrementAttribute posIncAttribute = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
  private State previous;
  private String previousType;
  public CommonGramsQueryFilter(CommonGramsFilter input) {
    super(input);
  }
  public void reset() throws IOException {
    super.reset();
    previous = null;
    previousType = null;
  }
  public boolean incrementToken() throws IOException {
    while (input.incrementToken()) {
      State current = captureState();
      if (previous != null && !isGramType()) {
        restoreState(previous);
        previous = current;
        previousType = typeAttribute.type();
        if (isGramType()) {
          posIncAttribute.setPositionIncrement(1);
        }
        return true;
      }
      previous = current;
    }
    if (previous == null || GRAM_TYPE.equals(previousType)) {
      return false;
    }
    restoreState(previous);
    previous = null;
    if (isGramType()) {
      posIncAttribute.setPositionIncrement(1);
    }
    return true;
  }
  public boolean isGramType() {
    return GRAM_TYPE.equals(typeAttribute.type());
  }
}
