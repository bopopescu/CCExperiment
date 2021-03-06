package org.apache.lucene.queryParser.surround.query;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import org.apache.lucene.search.Query;
public class FieldsQuery extends SrndQuery { 
  private SrndQuery q;
  private List<String> fieldNames;
  private final char fieldOp;
  private final String OrOperatorName = "OR"; 
  public FieldsQuery(SrndQuery q, List<String> fieldNames, char fieldOp) {
    this.q = q;
    this.fieldNames = fieldNames;
    this.fieldOp = fieldOp;
  }
  public FieldsQuery(SrndQuery q, String fieldName, char fieldOp) {
    this.q = q;
    fieldNames = new ArrayList<String>();
    fieldNames.add(fieldName);
    this.fieldOp = fieldOp;
  }
  @Override
  public boolean isFieldsSubQueryAcceptable() {
    return false;
  }
  public Query makeLuceneQueryNoBoost(BasicQueryFactory qf) {
    if (fieldNames.size() == 1) { 
      return q.makeLuceneQueryFieldNoBoost(fieldNames.get(0), qf);
    } else { 
      List<SrndQuery> queries = new ArrayList<SrndQuery>();
      Iterator<String> fni = getFieldNames().listIterator();
      SrndQuery qc;
      while (fni.hasNext()) {
        qc = (SrndQuery) q.clone();
        queries.add( new FieldsQuery( qc, fni.next(), fieldOp));
      }
      OrQuery oq = new OrQuery(queries,
                              true ,
                              OrOperatorName);
      System.out.println(getClass().toString() + ", fields expanded: " + oq.toString()); 
      return oq.makeLuceneQueryField(null, qf);
    }
  }
  @Override
  public Query makeLuceneQueryFieldNoBoost(String fieldName, BasicQueryFactory qf) {
    return makeLuceneQueryNoBoost(qf); 
  }
  public List<String> getFieldNames() {return fieldNames;}
  public char getFieldOperator() { return fieldOp;}
  @Override
  public String toString() {
    StringBuilder r = new StringBuilder();
    r.append("(");
    fieldNamesToString(r);
    r.append(q.toString());
    r.append(")");
    return r.toString();
  }
  protected void fieldNamesToString(StringBuilder r) {
    Iterator<String> fni = getFieldNames().listIterator();
    while (fni.hasNext()) {
      r.append(fni.next());
      r.append(getFieldOperator());
    }
  }
}
