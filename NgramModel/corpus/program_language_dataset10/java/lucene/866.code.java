package org.apache.lucene.benchmark.byTask.stats;
public class Report {
  private String text;
  private int size;
  private int outOf;
  private int reported;
  public Report (String text, int size, int reported, int outOf) {
    this.text = text;
    this.size = size;
    this.reported = reported;
    this.outOf = outOf;
  }
  public int getOutOf() {
    return outOf;
  }
  public int getSize() {
    return size;
  }
  public String getText() {
    return text;
  }
  public int getReported() {
    return reported;
  }
}
