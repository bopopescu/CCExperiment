package org.apache.solr.util;
import java.io.Serializable;
public class OpenBitSet extends org.apache.lucene.util.OpenBitSet implements Cloneable, Serializable {
  public OpenBitSet(long numBits) {
    super(numBits);
  }
  public OpenBitSet() {
    super();
  }
  public OpenBitSet(long[] bits, int numWords) {
    super();
  }
}
