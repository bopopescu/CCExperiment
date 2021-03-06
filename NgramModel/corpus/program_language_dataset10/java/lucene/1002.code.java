package org.apache.lucene.search.highlight;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
public class SimpleFragmenter implements Fragmenter {
  private static final int DEFAULT_FRAGMENT_SIZE = 100;
  private int currentNumFrags;
  private int fragmentSize;
  private OffsetAttribute offsetAtt;
  public SimpleFragmenter() {
    this(DEFAULT_FRAGMENT_SIZE);
  }
  public SimpleFragmenter(int fragmentSize) {
    this.fragmentSize = fragmentSize;
  }
  public void start(String originalText, TokenStream stream) {
    offsetAtt = stream.addAttribute(OffsetAttribute.class);
    currentNumFrags = 1;
  }
  public boolean isNewFragment() {
    boolean isNewFrag = offsetAtt.endOffset() >= (fragmentSize * currentNumFrags);
    if (isNewFrag) {
      currentNumFrags++;
    }
    return isNewFrag;
  }
  public int getFragmentSize() {
    return fragmentSize;
  }
  public void setFragmentSize(int size) {
    fragmentSize = size;
  }
}
