package org.apache.lucene.index;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import org.apache.lucene.search.Query;
class BufferedDeletes {
  int numTerms;
  Map<Term,Num> terms;
  Map<Query,Integer> queries = new HashMap<Query,Integer>();
  List<Integer> docIDs = new ArrayList<Integer>();
  long bytesUsed;
  private final boolean doTermSort;
  public BufferedDeletes(boolean doTermSort) {
    this.doTermSort = doTermSort;
    if (doTermSort) {
      terms = new TreeMap<Term,Num>();
    } else {
      terms = new HashMap<Term,Num>();
    }
  }
  final static class Num {
    private int num;
    Num(int num) {
      this.num = num;
    }
    int getNum() {
      return num;
    }
    void setNum(int num) {
      if (num > this.num)
        this.num = num;
    }
  }
  int size() {
    return numTerms + queries.size() + docIDs.size();
  }
  void update(BufferedDeletes in) {
    numTerms += in.numTerms;
    bytesUsed += in.bytesUsed;
    terms.putAll(in.terms);
    queries.putAll(in.queries);
    docIDs.addAll(in.docIDs);
    in.clear();
  }
  void clear() {
    terms.clear();
    queries.clear();
    docIDs.clear();
    numTerms = 0;
    bytesUsed = 0;
  }
  void addBytesUsed(long b) {
    bytesUsed += b;
  }
  boolean any() {
    return terms.size() > 0 || docIDs.size() > 0 || queries.size() > 0;
  }
  synchronized void remap(MergeDocIDRemapper mapper,
                          SegmentInfos infos,
                          int[][] docMaps,
                          int[] delCounts,
                          MergePolicy.OneMerge merge,
                          int mergeDocCount) {
    final Map<Term,Num> newDeleteTerms;
    if (terms.size() > 0) {
      if (doTermSort) {
        newDeleteTerms = new TreeMap<Term,Num>();
      } else {
        newDeleteTerms = new HashMap<Term,Num>();
      }
      for(Entry<Term,Num> entry : terms.entrySet()) {
        Num num = entry.getValue();
        newDeleteTerms.put(entry.getKey(),
                           new Num(mapper.remap(num.getNum())));
      }
    } else 
      newDeleteTerms = null;
    final List<Integer> newDeleteDocIDs;
    if (docIDs.size() > 0) {
      newDeleteDocIDs = new ArrayList<Integer>(docIDs.size());
      for (Integer num : docIDs) {
        newDeleteDocIDs.add(Integer.valueOf(mapper.remap(num.intValue())));
      }
    } else 
      newDeleteDocIDs = null;
    final HashMap<Query,Integer> newDeleteQueries;
    if (queries.size() > 0) {
      newDeleteQueries = new HashMap<Query, Integer>(queries.size());
      for(Entry<Query,Integer> entry: queries.entrySet()) {
        Integer num = entry.getValue();
        newDeleteQueries.put(entry.getKey(),
                             Integer.valueOf(mapper.remap(num.intValue())));
      }
    } else
      newDeleteQueries = null;
    if (newDeleteTerms != null)
      terms = newDeleteTerms;
    if (newDeleteDocIDs != null)
      docIDs = newDeleteDocIDs;
    if (newDeleteQueries != null)
      queries = newDeleteQueries;
  }
}