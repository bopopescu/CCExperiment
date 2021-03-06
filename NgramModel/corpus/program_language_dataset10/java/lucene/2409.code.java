package org.apache.solr.request;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.solr.common.params.FacetParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.SolrException;
import org.apache.solr.core.SolrCore;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.TrieField;
import org.apache.solr.search.*;
import org.apache.solr.util.BoundedTreeSet;
import org.apache.solr.handler.component.StatsValues;
import org.apache.solr.handler.component.FieldFacetStats;
import org.apache.lucene.util.OpenBitSet;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
public class UnInvertedField {
  private static int TNUM_OFFSET=2;
  static class TopTerm {
    Term term;
    int termNum;
    long memSize() {
      return 8 +   
             8 + 8 +(term.text().length()<<1) +  
             4;    
    }
  }
  String field;
  int numTermsInField;
  int termsInverted;  
  long termInstances; 
  final TermIndex ti;
  long memsz;
  int total_time;  
  int phase1_time;  
  final AtomicLong use = new AtomicLong(); 
  int[] index;
  byte[][] tnums = new byte[256][];
  int[] maxTermCounts;
  final Map<Integer,TopTerm> bigTerms = new LinkedHashMap<Integer,TopTerm>();
  public long memSize() {
    if (memsz!=0) return memsz;
    long sz = 8*8 + 32; 
    sz += bigTerms.size() * 64;
    for (TopTerm tt : bigTerms.values()) {
      sz += tt.memSize();
    }
    if (index != null) sz += index.length * 4;
    if (tnums!=null) {
      for (byte[] arr : tnums)
        if (arr != null) sz += arr.length;
    }
    if (maxTermCounts != null)
      sz += maxTermCounts.length * 4;
    sz += ti.memSize();
    memsz = sz;
    return sz;
  }
  static int vIntSize(int x) {
    if ((x & (0xffffffff << (7*1))) == 0 ) {
      return 1;
    }
    if ((x & (0xffffffff << (7*2))) == 0 ) {
      return 2;
    }
    if ((x & (0xffffffff << (7*3))) == 0 ) {
      return 3;
    }
    if ((x & (0xffffffff << (7*4))) == 0 ) {
      return 4;
    }
    return 5;
  }
  static int writeInt(int x, byte[] arr, int pos) {
    int a;
    a = (x >>> (7*4));
    if (a != 0) {
      arr[pos++] = (byte)(a | 0x80);
    }
    a = (x >>> (7*3));
    if (a != 0) {
      arr[pos++] = (byte)(a | 0x80);
    }
    a = (x >>> (7*2));
    if (a != 0) {
      arr[pos++] = (byte)(a | 0x80);
    }
    a = (x >>> (7*1));
    if (a != 0) {
      arr[pos++] = (byte)(a | 0x80);
    }
    arr[pos++] = (byte)(x & 0x7f);
    return pos;
  }
  public UnInvertedField(String field, SolrIndexSearcher searcher) throws IOException {
    this.field = field;
    this.ti = new TermIndex(field,
            TrieField.getMainValuePrefix(searcher.getSchema().getFieldType(field)));
    uninvert(searcher);
  }
  private void uninvert(SolrIndexSearcher searcher) throws IOException {
    long startTime = System.currentTimeMillis();
    IndexReader reader = searcher.getReader();
    int maxDoc = reader.maxDoc();
    int[] index = new int[maxDoc];       
    this.index = index;
    final int[] lastTerm = new int[maxDoc];    
    final byte[][] bytes = new byte[maxDoc][]; 
    maxTermCounts = new int[1024];
    NumberedTermEnum te = ti.getEnumerator(reader);
    int threshold = maxDoc / 20 + 2;
    int[] docs = new int[1000];
    int[] freqs = new int[1000];
    byte[] tempArr = new byte[12];
    for (;;) {
      Term t = te.term();
      if (t==null) break;
      int termNum = te.getTermNumber();
      if (termNum >= maxTermCounts.length) {
        int[] newMaxTermCounts = new int[maxTermCounts.length*2];
        System.arraycopy(maxTermCounts, 0, newMaxTermCounts, 0, termNum);
        maxTermCounts = newMaxTermCounts;
      }
      int df = te.docFreq();
      if (df >= threshold) {
        TopTerm topTerm = new TopTerm();
        topTerm.term = t;
        topTerm.termNum = termNum;
        bigTerms.put(topTerm.termNum, topTerm);
        DocSet set = searcher.getDocSet(new TermQuery(topTerm.term));
        maxTermCounts[termNum] = set.size();
        te.next();
        continue;
      }
      termsInverted++;
      TermDocs td = te.getTermDocs();
      td.seek(te);
      for(;;) {
        int n = td.read(docs,freqs);
        if (n <= 0) break;
        maxTermCounts[termNum] += n;
        for (int i=0; i<n; i++) {
          termInstances++;
          int doc = docs[i];
          int delta = termNum - lastTerm[doc] + TNUM_OFFSET;
          lastTerm[doc] = termNum;
          int val = index[doc];
          if ((val & 0xff)==1) {
            int pos = val >>> 8;
            int ilen = vIntSize(delta);
            byte[] arr = bytes[doc];
            int newend = pos+ilen;
            if (newend > arr.length) {
              int newLen = (newend + 3) & 0xfffffffc;  
              byte[] newarr = new byte[newLen];
              System.arraycopy(arr, 0, newarr, 0, pos);
              arr = newarr;
              bytes[doc] = newarr;
            }
            pos = writeInt(delta, arr, pos);
            index[doc] = (pos<<8) | 1;  
          } else {
            int ipos;
            if (val==0) {
              ipos=0;
            } else if ((val & 0x0000ff80)==0) {
              ipos=1;
            } else if ((val & 0x00ff8000)==0) {
              ipos=2;
            } else if ((val & 0xff800000)==0) {
              ipos=3;
            } else {
              ipos=4;
            }
            int endPos = writeInt(delta, tempArr, ipos);
            if (endPos <= 4) {
              for (int j=ipos; j<endPos; j++) {
                val |= (tempArr[j] & 0xff) << (j<<3);
              }
              index[doc] = val;
            } else {
              for (int j=0; j<ipos; j++) {
                tempArr[j] = (byte)val;
                val >>>=8;
              }
              index[doc] = (endPos<<8) | 1;
              bytes[doc] = tempArr;
              tempArr = new byte[12];
            }
          }
        }
      }
      te.next();
    }
    numTermsInField = te.getTermNumber();
    te.close();
    if ((maxTermCounts.length - numTermsInField) > 1024) { 
      int[] newMaxTermCounts = new int[numTermsInField];
      System.arraycopy(maxTermCounts, 0, newMaxTermCounts, 0, numTermsInField);
      maxTermCounts = newMaxTermCounts;
   }
    long midPoint = System.currentTimeMillis();
    if (termInstances == 0) {
      index = this.index = null;
      tnums = null;
    } else {
      for (int pass = 0; pass<256; pass++) {
        byte[] target = tnums[pass];
        int pos=0;  
        if (target != null) {
          pos = target.length;
        } else {
          target = new byte[4096];
        }
        for (int docbase = pass<<16; docbase<maxDoc; docbase+=(1<<24)) {
          int lim = Math.min(docbase + (1<<16), maxDoc);
          for (int doc=docbase; doc<lim; doc++) {
            int val = index[doc];
            if ((val&0xff) == 1) {
              int len = val >>> 8;
              index[doc] = (pos<<8)|1; 
              if ((pos & 0xff000000) != 0) {
                throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Too many values for UnInvertedField faceting on field "+field);
              }
              byte[] arr = bytes[doc];
              bytes[doc] = null;        
              if (target.length <= pos + len) {
                int newlen = target.length;
                while (newlen <= pos + len) newlen<<=1;  
                byte[] newtarget = new byte[newlen];
                System.arraycopy(target, 0, newtarget, 0, pos);
                target = newtarget;
              }
              System.arraycopy(arr, 0, target, pos, len);
              pos += len + 1;  
            }
          }
        }
        if (pos < target.length) {
          byte[] newtarget = new byte[pos];
          System.arraycopy(target, 0, newtarget, 0, pos);
          target = newtarget;
          if (target.length > (1<<24)*.9) {
            SolrCore.log.warn("Approaching too many values for UnInvertedField faceting on field '"+field+"' : bucket size=" + target.length);
          }
        }
        tnums[pass] = target;
        if ((pass << 16) > maxDoc)
          break;
      }
    }
    long endTime = System.currentTimeMillis();
    total_time = (int)(endTime-startTime);
    phase1_time = (int)(midPoint-startTime);
    SolrCore.log.info("UnInverted multi-valued field " + toString());
  }
  public NamedList getCounts(SolrIndexSearcher searcher, DocSet baseDocs, int offset, int limit, Integer mincount, boolean missing, String sort, String prefix) throws IOException {
    use.incrementAndGet();
    FieldType ft = searcher.getSchema().getFieldType(field);
    NamedList res = new NamedList();  
    DocSet docs = baseDocs;
    int baseSize = docs.size();
    int maxDoc = searcher.maxDoc();
    if (baseSize >= mincount) {
      final int[] index = this.index;
      final int[] counts = new int[numTermsInField];
      int startTerm = 0;
      int endTerm = numTermsInField;  
      NumberedTermEnum te = ti.getEnumerator(searcher.getReader());
      if (prefix != null && prefix.length() > 0) {
        te.skipTo(prefix);
        startTerm = te.getTermNumber();
        te.skipTo(prefix + "\uffff\uffff\uffff\uffff");
        endTerm = te.getTermNumber();
      }
      boolean doNegative = baseSize > maxDoc >> 1 && termInstances > 0
              && startTerm==0 && endTerm==numTermsInField
              && docs instanceof BitDocSet;
      if (doNegative) {
        OpenBitSet bs = (OpenBitSet)((BitDocSet)docs).getBits().clone();
        bs.flip(0, maxDoc);
        docs = new BitDocSet(bs, maxDoc - baseSize);
      }
      for (TopTerm tt : bigTerms.values()) {
        if (tt.termNum >= startTerm && tt.termNum < endTerm) {
          counts[tt.termNum] = searcher.numDocs(new TermQuery(tt.term), docs);
        }
      }
      if (termInstances > 0) {
        DocIterator iter = docs.iterator();
        while (iter.hasNext()) {
          int doc = iter.nextDoc();
          int code = index[doc];
          if ((code & 0xff)==1) {
            int pos = code>>>8;
            int whichArray = (doc >>> 16) & 0xff;
            byte[] arr = tnums[whichArray];
            int tnum = 0;
            for(;;) {
              int delta = 0;
              for(;;) {
                byte b = arr[pos++];
                delta = (delta << 7) | (b & 0x7f);
                if ((b & 0x80) == 0) break;
              }
              if (delta == 0) break;
              tnum += delta - TNUM_OFFSET;
              counts[tnum]++;
            }
          } else {
            int tnum = 0;
            int delta = 0;
            for (;;) {
              delta = (delta << 7) | (code & 0x7f);
              if ((code & 0x80)==0) {
                if (delta==0) break;
                tnum += delta - TNUM_OFFSET;
                counts[tnum]++;
                delta = 0;
              }
              code >>>= 8;
            }
          }
        }
      }
      int off=offset;
      int lim=limit>=0 ? limit : Integer.MAX_VALUE;
      if (sort.equals(FacetParams.FACET_SORT_COUNT) || sort.equals(FacetParams.FACET_SORT_COUNT_LEGACY)) {
        int maxsize = limit>0 ? offset+limit : Integer.MAX_VALUE-1;
        maxsize = Math.min(maxsize, numTermsInField);
        final BoundedTreeSet<Long> queue = new BoundedTreeSet<Long>(maxsize);
        int min=mincount-1;  
        for (int i=startTerm; i<endTerm; i++) {
          int c = doNegative ? maxTermCounts[i] - counts[i] : counts[i];
          if (c>min) {
            long pair = (((long)-c)<<32) | i;
            queue.add(new Long(pair));
            if (queue.size()>=maxsize) min=-(int)(queue.last().longValue() >>> 32);
          }
        }
        for (Long p : queue) {
          if (--off>=0) continue;
          if (--lim<0) break;
          int c = -(int)(p.longValue() >>> 32);
          int tnum = (int)p.longValue();
          String label = ft.indexedToReadable(getTermText(te, tnum));
          res.add(label, c);
        }
      } else {
        int i=startTerm;
        if (mincount<=0) {
          i=startTerm+off;
          off=0;
        }
        for (; i<endTerm; i++) {
          int c = doNegative ? maxTermCounts[i] - counts[i] : counts[i];
          if (c<mincount || --off>=0) continue;
          if (--lim<0) break;
          String label = ft.indexedToReadable(getTermText(te, i));
          res.add(label, c);
        }
      }
      te.close();
    }
    if (missing) {
      res.add(null, SimpleFacets.getFieldMissingCount(searcher, baseDocs, field));
    }
    return res;
  }
  public StatsValues getStats(SolrIndexSearcher searcher, DocSet baseDocs, String[] facet) throws IOException {
    use.incrementAndGet();
    StatsValues allstats = new StatsValues();
    DocSet docs = baseDocs;
    int baseSize = docs.size();
    int maxDoc = searcher.maxDoc();
    if (baseSize <= 0) return allstats;
    FieldType ft = searcher.getSchema().getFieldType(field);
    DocSet missing = docs.andNot( searcher.getDocSet(new TermRangeQuery(field, null, null, false, false)) );
    int i = 0;
    final FieldFacetStats[] finfo = new FieldFacetStats[facet.length];
    FieldCache.StringIndex si;
    for (String f : facet) {
      FieldType facet_ft = searcher.getSchema().getFieldType(f);
      try {
        si = FieldCache.DEFAULT.getStringIndex(searcher.getReader(), f);
      }
      catch (IOException e) {
        throw new RuntimeException("failed to open field cache for: " + f, e);
      }
      finfo[i] = new FieldFacetStats(f, si, facet_ft, numTermsInField);
      i++;
    }
    final int[] index = this.index;
    final int[] counts = new int[numTermsInField];
    NumberedTermEnum te = ti.getEnumerator(searcher.getReader());
    boolean doNegative = false;
    if (finfo.length == 0) {
      doNegative = baseSize > maxDoc >> 1 && termInstances > 0
              && docs instanceof BitDocSet;
    }
    if (doNegative) {
      OpenBitSet bs = (OpenBitSet) ((BitDocSet) docs).getBits().clone();
      bs.flip(0, maxDoc);
      docs = new BitDocSet(bs, maxDoc - baseSize);
    }
    for (TopTerm tt : bigTerms.values()) {
      if (tt.termNum >= 0 && tt.termNum < numTermsInField) {
        if (finfo.length == 0) {
          counts[tt.termNum] = searcher.numDocs(new TermQuery(tt.term), docs);
        } else {
          DocSet bigTermDocSet = searcher.getDocSet(new TermQuery(tt.term)).intersection(docs);
          DocIterator iter = bigTermDocSet.iterator();
          while (iter.hasNext()) {
            int doc = iter.nextDoc();
            counts[tt.termNum]++;
            for (FieldFacetStats f : finfo) {
              f.facetTermNum(doc, tt.termNum);
            }
          }
        }
      }
    }
    if (termInstances > 0) {
      DocIterator iter = docs.iterator();
      while (iter.hasNext()) {
        int doc = iter.nextDoc();
        int code = index[doc];
        if ((code & 0xff) == 1) {
          int pos = code >>> 8;
          int whichArray = (doc >>> 16) & 0xff;
          byte[] arr = tnums[whichArray];
          int tnum = 0;
          for (; ;) {
            int delta = 0;
            for (; ;) {
              byte b = arr[pos++];
              delta = (delta << 7) | (b & 0x7f);
              if ((b & 0x80) == 0) break;
            }
            if (delta == 0) break;
            tnum += delta - TNUM_OFFSET;
            counts[tnum]++;
            for (FieldFacetStats f : finfo) {
              f.facetTermNum(doc, tnum);
            }
          }
        } else {
          int tnum = 0;
          int delta = 0;
          for (; ;) {
            delta = (delta << 7) | (code & 0x7f);
            if ((code & 0x80) == 0) {
              if (delta == 0) break;
              tnum += delta - TNUM_OFFSET;
              counts[tnum]++;
              for (FieldFacetStats f : finfo) {
                f.facetTermNum(doc, tnum);
              }
              delta = 0;
            }
            code >>>= 8;
          }
        }
      }
    }
    for (i = 0; i < numTermsInField; i++) {
      int c = doNegative ? maxTermCounts[i] - counts[i] : counts[i];
      if (c == 0) continue;
      Double value = Double.parseDouble(ft.indexedToReadable(getTermText(te, i)));
      allstats.accumulate(value, c);
      for (FieldFacetStats f : finfo) {
        f.accumulateTermNum(i, value);
      }
    }
    te.close();
    int c = missing.size();
    allstats.addMissing(c);
    if (finfo.length > 0) {
      allstats.facets = new HashMap<String, Map<String, StatsValues>>();
      for (FieldFacetStats f : finfo) {
        Map<String, StatsValues> facetStatsValues = f.facetStatsValues;
        FieldType facetType = searcher.getSchema().getFieldType(f.name);
        for (Map.Entry<String,StatsValues> entry : facetStatsValues.entrySet()) {
          String termLabel = entry.getKey();
          int missingCount = searcher.numDocs(new TermQuery(new Term(f.name, facetType.toInternal(termLabel))), missing);
          entry.getValue().addMissing(missingCount);
        }
        allstats.facets.put(f.name, facetStatsValues);
      }
    }
    return allstats;
  }
  String getTermText(NumberedTermEnum te, int termNum) throws IOException {
    if (bigTerms.size() > 0) {
      TopTerm tt = bigTerms.get(termNum);
      if (tt != null) {
        return tt.term.text();
      }
    }
    te.skipTo(termNum);
    return te.term().text();
  }
  public String toString() {
    return "{field=" + field
            + ",memSize="+memSize()
            + ",tindexSize="+ti.memSize()
            + ",time="+total_time
            + ",phase1="+phase1_time
            + ",nTerms="+numTermsInField
            + ",bigTerms="+bigTerms.size()
            + ",termInstances="+termInstances
            + ",uses="+use.get()
            + "}";
  }
  public static UnInvertedField getUnInvertedField(String field, SolrIndexSearcher searcher) throws IOException {
    SolrCache cache = searcher.getFieldValueCache();
    if (cache == null) {
      return new UnInvertedField(field, searcher);
    }
    UnInvertedField uif = (UnInvertedField)cache.get(field);
    if (uif == null) {
      synchronized (cache) {
        uif = (UnInvertedField)cache.get(field);
        if (uif == null) {
          uif = new UnInvertedField(field, searcher);
          cache.put(field, uif);
        }
      }
    }
    return uif;
  }
}
class NumberedTermEnum extends TermEnum {
  protected final IndexReader reader;
  protected final TermIndex tindex;
  protected TermEnum tenum;
  protected int pos=-1;
  protected Term t;
  protected TermDocs termDocs;
  NumberedTermEnum(IndexReader reader, TermIndex tindex) throws IOException {
    this.reader = reader;
    this.tindex = tindex;
  }
  NumberedTermEnum(IndexReader reader, TermIndex tindex, String termValue, int pos) throws IOException {
    this.reader = reader;
    this.tindex = tindex;
    this.pos = pos;
    tenum = reader.terms(tindex.createTerm(termValue));
    setTerm();
  }
  public TermDocs getTermDocs() throws IOException {
    if (termDocs==null) termDocs = reader.termDocs(t);
    else termDocs.seek(t);
    return termDocs;
  }
  protected boolean setTerm() {
    t = tenum.term();
    if (t==null
            || t.field() != tindex.fterm.field()  
            || (tindex.prefix != null && !t.text().startsWith(tindex.prefix,0)) )
    {
      t = null;
      return false;
    }
    return true;
  }
  public boolean next() throws IOException {
    pos++;
    boolean b = tenum.next();
    if (!b) {
      t = null;
      return false;
    }
    return setTerm();  
  }
  public Term term() {
    return t;
  }
  public int docFreq() {
    return tenum.docFreq();
  }
  public void close() throws IOException {
    if (tenum!=null) tenum.close();
  }
  public boolean skipTo(String target) throws IOException {
    return skipTo(tindex.fterm.createTerm(target));
  }
  public boolean skipTo(Term target) throws IOException {
    if (t != null && t.equals(target)) return true;
    int startIdx = Arrays.binarySearch(tindex.index,target.text());
    if (startIdx >= 0) {
      if (tenum != null) tenum.close();
      tenum = reader.terms(target);
      pos = startIdx << tindex.intervalBits;
      return setTerm();
    }
    startIdx=-startIdx-1;
    if (startIdx == 0) {
      if (tenum != null) tenum.close();
      tenum = reader.terms(target);
      pos = 0;
      return setTerm();
    }
    startIdx--;
    if ((pos >> tindex.intervalBits) == startIdx && t != null && t.text().compareTo(target.text())<=0) {
    } else {
      if (tenum != null) tenum.close();            
      tenum = reader.terms(target.createTerm(tindex.index[startIdx]));
      pos = startIdx << tindex.intervalBits;
      setTerm();  
    }
    while (t != null && t.text().compareTo(target.text()) < 0) {
      next();
    }
    return t != null;
  }
  public boolean skipTo(int termNumber) throws IOException {
    int delta = termNumber - pos;
    if (delta < 0 || delta > tindex.interval || tenum==null) {
      int idx = termNumber >>> tindex.intervalBits;
      String base = tindex.index[idx];
      pos = idx << tindex.intervalBits;
      delta = termNumber - pos;
      if (tenum != null) tenum.close();
      tenum = reader.terms(tindex.createTerm(base));
    }
    while (--delta >= 0) {
      boolean b = tenum.next();
      if (b==false) {
        t = null;
        return false;
      }
      ++pos;
    }
    return setTerm();
  }
  public int getTermNumber() {
    return pos;
  }
}
class TermIndex {
  final static int intervalBits = 7;  
  final static int intervalMask = 0xffffffff >>> (32-intervalBits);
  final static int interval = 1 << intervalBits;
  final Term fterm; 
  final String prefix;
  String[] index;
  int nTerms;
  long sizeOfStrings;
  TermIndex(String field) {
    this(field, null);
  }
  TermIndex(String field, String prefix) {
    this.fterm = new Term(field, "");
    this.prefix = prefix;
  }
  Term createTerm(String termVal) {
    return fterm.createTerm(termVal);
  }
  NumberedTermEnum getEnumerator(IndexReader reader, int termNumber) throws IOException {
    NumberedTermEnum te = new NumberedTermEnum(reader, this);
    te.skipTo(termNumber);
    return te;
  }
  NumberedTermEnum getEnumerator(IndexReader reader) throws IOException {
    if (index==null) return new NumberedTermEnum(reader,this, prefix==null?"":prefix, 0) {
      ArrayList<String> lst;
      protected boolean setTerm() {
        boolean b = super.setTerm();
        if (b && (pos & intervalMask)==0) {
          String text = term().text();
          sizeOfStrings += text.length() << 1;
          if (lst==null) {
            lst = new ArrayList<String>();
          }
          lst.add(text);
        }
        return b;
      }
      public boolean skipTo(Term target) throws IOException {
        throw new UnsupportedOperationException();
      }
      public boolean skipTo(int termNumber) throws IOException {
        throw new UnsupportedOperationException();
      }
      public void close() throws IOException {
        nTerms=pos;
        super.close();
        index = lst!=null ? lst.toArray(new String[lst.size()]) : new String[0];
      }
    };
    else return new NumberedTermEnum(reader,this,"",0);
  }
  public long memSize() {
    return 8+8+8+8+(index.length<<3)+sizeOfStrings;
  }
}
