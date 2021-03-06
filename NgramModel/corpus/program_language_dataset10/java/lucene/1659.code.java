package org.apache.lucene.search;
public class FieldDoc extends ScoreDoc {
  public Comparable[] fields;
  public FieldDoc (int doc, float score) {
    super (doc, score);
  }
  public FieldDoc (int doc, float score, Comparable[] fields) {
    super (doc, score);
    this.fields = fields;
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(super.toString());
    sb.append("[");
    for (int i = 0; i < fields.length; i++) {
            sb.append(fields[i]).append(", ");
          }
    sb.setLength(sb.length() - 2); 
    sb.append("]");
    return super.toString();
  }
}
