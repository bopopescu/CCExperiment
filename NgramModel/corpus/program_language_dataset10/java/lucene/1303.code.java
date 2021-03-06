package org.apache.lucene.search.spell;
final class SuggestWord {
  public float score;
  public int freq;
  public String string;
  public final int compareTo(SuggestWord a) {
    if (score > a.score) {
      return 1;
    }
    if (score < a.score) {
      return -1;
    }
    if (freq > a.freq) {
      return 1;
    }
    if (freq < a.freq) {
      return -1;
    }
    return 0;
  }
}
