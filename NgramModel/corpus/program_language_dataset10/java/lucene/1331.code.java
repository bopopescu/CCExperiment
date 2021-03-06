package org.apache.lucene.queryParser.surround.query;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
public abstract class SrndQuery implements Cloneable {
  public SrndQuery() {}
  private float weight = (float) 1.0;
  private boolean weighted = false;
  public void setWeight(float w) {
    weight = w; 
    weighted = true;
  } 
  public boolean isWeighted() {return weighted;}
  public float getWeight() { return weight; }
  public String getWeightString() {return Float.toString(getWeight());}
  public String getWeightOperator() {return "^";}
  protected void weightToString(StringBuilder r) { 
    if (isWeighted()) {
      r.append(getWeightOperator());
      r.append(getWeightString());
    }
  }
  public Query makeLuceneQueryField(String fieldName, BasicQueryFactory qf){
    Query q = makeLuceneQueryFieldNoBoost(fieldName, qf);
    if (isWeighted()) {
      q.setBoost(getWeight() * q.getBoost()); 
    }
    return q;
  }
  public abstract Query makeLuceneQueryFieldNoBoost(String fieldName, BasicQueryFactory qf);
  @Override
  public abstract String toString();
  public boolean isFieldsSubQueryAcceptable() {return true;}
  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException cns) {
      throw new Error(cns);
    }
  }
  public final static Query theEmptyLcnQuery = new BooleanQuery() { 
    @Override
    public void setBoost(float boost) {
      throw new UnsupportedOperationException();
    }
    @Override
    public void add(BooleanClause clause) {
      throw new UnsupportedOperationException();
    }
    @Override
    public void add(Query query, BooleanClause.Occur occur) {
      throw new UnsupportedOperationException();
    }
  };
}
