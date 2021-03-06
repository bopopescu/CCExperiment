package org.apache.solr.handler.component;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.util.PriorityQueue;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.search.MissingStringLastComparatorSource;
import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
public class ShardDoc {
  public String shard;
  public String shardAddress;  
  int orderInShard;
  Object id;
  Float score;
  NamedList sortFieldValues;
  int positionInResponse;
  public String toString(){
    return "id="+id
            +" ,score="+score
            +" ,shard="+shard
            +" ,orderInShard="+orderInShard
            +" ,positionInResponse="+positionInResponse
            +" ,sortFieldValues="+sortFieldValues;
  }
}
class ShardFieldSortedHitQueue extends PriorityQueue {
  protected Comparator[] comparators;
  protected SortField[] fields;
  protected List<String> fieldNames = new ArrayList<String>();
  public ShardFieldSortedHitQueue(SortField[] fields, int size) {
    final int n = fields.length;
    comparators = new Comparator[n];
    this.fields = new SortField[n];
    for (int i = 0; i < n; ++i) {
      int type = fields[i].getType();
      if (type!=SortField.SCORE && type!=SortField.DOC) {
        fieldNames.add(fields[i].getField());
      }
      String fieldname = fields[i].getField();
      comparators[i] = getCachedComparator(fieldname, fields[i]
          .getType(), fields[i].getLocale(), fields[i].getComparatorSource());
     if (fields[i].getType() == SortField.STRING) {
        this.fields[i] = new SortField(fieldname, fields[i].getLocale(),
            fields[i].getReverse());
      } else {
        this.fields[i] = new SortField(fieldname, fields[i].getType(),
            fields[i].getReverse());
      }
    }
    initialize(size);
  }
  @Override
  protected boolean lessThan(Object objA, Object objB) {
    ShardDoc docA = (ShardDoc)objA;
    ShardDoc docB = (ShardDoc)objB;
    if (docA.shard == docB.shard) {
      return !(docA.orderInShard < docB.orderInShard);
    }
    final int n = comparators.length;
    int c = 0;
    for (int i = 0; i < n && c == 0; i++) {
      c = (fields[i].getReverse()) ? comparators[i].compare(docB, docA)
          : comparators[i].compare(docA, docB);
    }
    if (c == 0) {
      c = -docA.shard.compareTo(docB.shard);
    }
    return c < 0;
  }
  Comparator getCachedComparator(String fieldname, int type, Locale locale, FieldComparatorSource factory) {
    Comparator comparator = null;
    switch (type) {
    case SortField.SCORE:
      comparator = comparatorScore(fieldname);
      break;
    case SortField.STRING:
      if (locale != null)
        comparator = comparatorStringLocale(fieldname, locale);
      else
        comparator = comparatorNatural(fieldname);
      break;
    case SortField.CUSTOM:
      if (factory instanceof MissingStringLastComparatorSource){
        comparator = comparatorMissingStringLast(fieldname);
      } else {
        comparator = comparatorNatural(fieldname);
      }
      break;
    case SortField.DOC:
      throw new RuntimeException("Doc sort not supported");
    default:
      comparator = comparatorNatural(fieldname);
      break;
    }
    return comparator;
  }
  class ShardComparator implements Comparator {
    String fieldName;
    int fieldNum;
    public ShardComparator(String fieldName) {
      this.fieldName = fieldName;
      this.fieldNum=0;
      for (int i=0; i<fieldNames.size(); i++) {
        if (fieldNames.get(i).equals(fieldName)) {
          this.fieldNum = i;
          break;
        }
      }
    }
    Object sortVal(ShardDoc shardDoc) {
      assert(shardDoc.sortFieldValues.getName(fieldNum).equals(fieldName));
      List lst = (List)shardDoc.sortFieldValues.getVal(fieldNum);
      return lst.get(shardDoc.orderInShard);
    }
    public int compare(Object o1, Object o2) {
      return 0;
    }
  }
  static Comparator comparatorScore(final String fieldName) {
    return new Comparator() {
      public final int compare(final Object o1, final Object o2) {
        ShardDoc e1 = (ShardDoc) o1;
        ShardDoc e2 = (ShardDoc) o2;
        final float f1 = e1.score;
        final float f2 = e2.score;
        if (f1 < f2)
          return -1;
        if (f1 > f2)
          return 1;
        return 0;
      }
    };
  }
  Comparator comparatorNatural(String fieldName) {
    return new ShardComparator(fieldName) {
      public final int compare(final Object o1, final Object o2) {
        ShardDoc sd1 = (ShardDoc) o1;
        ShardDoc sd2 = (ShardDoc) o2;
        Comparable v1 = (Comparable)sortVal(sd1);
        Comparable v2 = (Comparable)sortVal(sd2);
        if (v1==v2)
          return 0;
        if (v1==null)
          return 1;
        if(v2==null)
          return -1;
        return -v1.compareTo(v2);
      }
    };
  }
  Comparator comparatorStringLocale(final String fieldName,
      Locale locale) {
    final Collator collator = Collator.getInstance(locale);
    return new ShardComparator(fieldName) {
      public final int compare(final Object o1, final Object o2) {
        ShardDoc sd1 = (ShardDoc) o1;
        ShardDoc sd2 = (ShardDoc) o2;
        Comparable v1 = (Comparable)sortVal(sd1);
        Comparable v2 = (Comparable)sortVal(sd2);
        if (v1==v2)
          return 0;
        if (v1==null)
          return 1;
        if(v2==null)
          return -1;
        return -collator.compare(v1,v2);
      }
    };
  }
  Comparator comparatorMissingStringLast(final String fieldName) {
     return new ShardComparator(fieldName) {
      public final int compare(final Object o1, final Object o2) {
        ShardDoc sd1 = (ShardDoc) o1;
        ShardDoc sd2 = (ShardDoc) o2;
        Comparable v1 = (Comparable)sortVal(sd1);
        Comparable v2 = (Comparable)sortVal(sd2);
        if (v1==v2)
          return 0;
        if (v1==null)
          return -1;
        if(v2==null)
          return 1;
        return -v1.compareTo(v2);
      }
    };
  }
}