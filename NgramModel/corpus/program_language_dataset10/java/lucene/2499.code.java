package org.apache.solr.search;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.function.FunctionQuery;
import org.apache.solr.search.function.ValueSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
public class QueryParsing {
  public static final String OP = "q.op";  
  public static final String V = "v";      
  public static final String F = "f";      
  public static final String TYPE = "type";
  public static final String DEFTYPE = "defType"; 
  public static final String LOCALPARAM_START = "{!";
  public static final char LOCALPARAM_END = '}';
  public static final String DOCID = "_docid_";
  public static Query parseQuery(String qs, IndexSchema schema) {
    return parseQuery(qs, null, schema);
  }
  public static Query parseQuery(String qs, String defaultField, IndexSchema schema) {
    try {
      Query query = schema.getSolrQueryParser(defaultField).parse(qs);
      if (SolrCore.log.isTraceEnabled()) {
        SolrCore.log.trace("After QueryParser:" + query);
      }
      return query;
    } catch (ParseException e) {
      SolrCore.log(e);
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Error parsing Lucene query", e);
    }
  }
  public static Query parseQuery(String qs, String defaultField, SolrParams params, IndexSchema schema) {
    try {
      SolrQueryParser parser = schema.getSolrQueryParser(defaultField);
      String opParam = params.get(OP);
      if (opParam != null) {
        parser.setDefaultOperator("AND".equals(opParam) ? QueryParser.Operator.AND : QueryParser.Operator.OR);
      }
      Query query = parser.parse(qs);
      if (SolrCore.log.isTraceEnabled()) {
        SolrCore.log.trace("After QueryParser:" + query);
      }
      return query;
    } catch (ParseException e) {
      SolrCore.log(e);
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Query parsing error: " + e.getMessage(), e);
    }
  }
  static int parseLocalParams(String txt, int start, Map<String, String> target, SolrParams params) throws ParseException {
    int off = start;
    if (!txt.startsWith(LOCALPARAM_START, off)) return start;
    StrParser p = new StrParser(txt, start, txt.length());
    p.pos += 2; 
    for (; ;) {
      char ch = p.peek();
      if (ch == LOCALPARAM_END) {
        return p.pos + 1;
      }
      String id = p.getId();
      if (id.length() == 0) {
        throw new ParseException("Expected identifier '}' parsing local params '" + txt + '"');
      }
      String val = null;
      ch = p.peek();
      if (ch != '=') {
        val = id;
        id = TYPE;
      } else {
        p.pos++;
        ch = p.peek();
        if (ch == '\"' || ch == '\'') {
          val = p.getQuotedString();
        } else if (ch == '$') {
          p.pos++;
          String pname = p.getId();
          if (params != null) {
            val = params.get(pname);
          }
        } else {
          int valStart = p.pos;
          for (; ;) {
            if (p.pos >= p.end) {
              throw new ParseException("Missing end to unquoted value starting at " + valStart + " str='" + txt + "'");
            }
            char c = p.val.charAt(p.pos);
            if (c == LOCALPARAM_END || Character.isWhitespace(c)) {
              val = p.val.substring(valStart, p.pos);
              break;
            }
            p.pos++;
          }
        }
      }
      if (target != null) target.put(id, val);
    }
  }
  public static SolrParams getLocalParams(String txt, SolrParams params) throws ParseException {
    if (txt == null || !txt.startsWith(LOCALPARAM_START)) {
      return null;
    }
    Map<String, String> localParams = new HashMap<String, String>();
    int start = QueryParsing.parseLocalParams(txt, 0, localParams, params);
    String val;
    if (start >= txt.length()) {
      val = localParams.get(V);
      val = val == null ? "" : val;
    } else {
      val = txt.substring(start);
    }
    localParams.put(V, val);
    return new MapSolrParams(localParams);
  }
  public static Sort parseSort(String sortSpec, IndexSchema schema) {
    if (sortSpec == null || sortSpec.length() == 0) return null;
    char[] chars = sortSpec.toCharArray();
    int i = 0;
    StringBuilder buffer = new StringBuilder(sortSpec.length());
    String sort = null;
    String order = null;
    int functionDepth = 0;
    boolean score = true;
    List<SortField> lst = new ArrayList<SortField>(5);
    boolean needOrder = false;
    while (i < chars.length) {
      if (Character.isWhitespace(chars[i]) && functionDepth == 0) {
        if (buffer.length() == 0) {
        } else {
          if (needOrder == false) {
            sort = buffer.toString().trim();
            buffer.setLength(0);
            needOrder = true;
          } else {
            order = buffer.toString().trim();
            buffer.setLength(0);
            needOrder = false;
          }
        }
      } else if (chars[i] == '(' && functionDepth >= 0) {
        buffer.append(chars[i]);
        functionDepth++;
      } else if (chars[i] == ')' && functionDepth > 0) {
        buffer.append(chars[i]);
        functionDepth--;
      } else if (chars[i] == ',' && functionDepth == 0) {
        if (needOrder == true && buffer.length() > 0){
          order = buffer.toString().trim();
          buffer.setLength(0);
          needOrder = false;
        }
        score = processSort(schema, sort, order, lst);
        sort = null;
        order = null;
        buffer.setLength(0);
      } else if (chars[i] == ',' && functionDepth > 0) {
        buffer.append(chars[i]);
      } else {
        buffer.append(chars[i]);
      }
      i++;
    }
    if (buffer.length() > 0 && needOrder){
      order = buffer.toString().trim();
      buffer.setLength(0);
      needOrder = false;
    }
    if (functionDepth != 0){
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Unable to parse sort spec, mismatched parentheses: " + sortSpec);
    }
    if (buffer.length() > 0){
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Unable to parse sort spec: " + sortSpec);
    }
    if (needOrder == false && sort != null && sort.equals("") == false && order != null && order.equals("") == false){
      score = processSort(schema, sort, order, lst);
    }
    if (lst.size() == 1 && score == true && lst.get(0).getReverse() == false) {
      return null; 
    }
    return new Sort((SortField[]) lst.toArray(new SortField[lst.size()]));
  }
  private static boolean processSort(IndexSchema schema, String sort, String order, List<SortField> lst) {
    boolean score = false;
    if (sort != null && order != null) {
      boolean top = true;
      if ("desc".equals(order) || "top".equals(order)) {
        top = true;
      } else if ("asc".equals(order) || "bottom".equals(order)) {
        top = false;
      } else {
        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Unknown sort order: " + order);
      }
      if ("score".equals(sort)) {
        score = true;
        if (top) {
          lst.add(SortField.FIELD_SCORE);
        } else {
          lst.add(new SortField(null, SortField.SCORE, true));
        }
      } else if (DOCID.equals(sort)) {
        lst.add(new SortField(null, SortField.DOC, top));
      } else {
        SchemaField f = null;
        try {
          f = schema.getField(sort);
        }
        catch (SolrException e) {
        }
        if (f != null) {
          if (f == null || !f.indexed()) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "can not sort on unindexed field: " + sort);
          }
          lst.add(f.getType().getSortField(f, top));
        } else {
          FunctionQuery query = null;
          try {
            query = parseFunction(sort, schema);
            if (query != null) {
              ValueSource valueSource = query.getValueSource();
              try {
                lst.add(valueSource.getSortField(top));
              } catch (IOException e) {
                throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "error getting the sort for this function: " + sort, e);
              }
            } else {
              throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "can not sort on undefined function: " + sort);
            }
          } catch (ParseException e) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "can not sort on undefined field or function: " + sort, e);
          }
        }
      }
    } else if (sort == null) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
              "Must declare sort field or function");
    } else if (order == null) {
      throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Missing sort order: ");
    }
    return score;
  }
  static FieldType writeFieldName(String name, IndexSchema schema, Appendable out, int flags) throws IOException {
    FieldType ft = null;
    ft = schema.getFieldTypeNoEx(name);
    out.append(name);
    if (ft == null) {
      out.append("(UNKNOWN FIELD " + name + ')');
    }
    out.append(':');
    return ft;
  }
  static void writeFieldVal(String val, FieldType ft, Appendable out, int flags) throws IOException {
    if (ft != null) {
      try {
        out.append(ft.indexedToReadable(val));
      } catch (Exception e) {
        out.append("EXCEPTION(val=");
        out.append(val);
        out.append(")");
      }
    } else {
      out.append(val);
    }
  }
  public static void toString(Query query, IndexSchema schema, Appendable out, int flags) throws IOException {
    boolean writeBoost = true;
    if (query instanceof TermQuery) {
      TermQuery q = (TermQuery) query;
      Term t = q.getTerm();
      FieldType ft = writeFieldName(t.field(), schema, out, flags);
      writeFieldVal(t.text(), ft, out, flags);
    } else if (query instanceof TermRangeQuery) {
      TermRangeQuery q = (TermRangeQuery) query;
      String fname = q.getField();
      FieldType ft = writeFieldName(fname, schema, out, flags);
      out.append(q.includesLower() ? '[' : '{');
      String lt = q.getLowerTerm();
      String ut = q.getUpperTerm();
      if (lt == null) {
        out.append('*');
      } else {
        writeFieldVal(lt, ft, out, flags);
      }
      out.append(" TO ");
      if (ut == null) {
        out.append('*');
      } else {
        writeFieldVal(ut, ft, out, flags);
      }
      out.append(q.includesUpper() ? ']' : '}');
    } else if (query instanceof NumericRangeQuery) {
      NumericRangeQuery q = (NumericRangeQuery) query;
      String fname = q.getField();
      FieldType ft = writeFieldName(fname, schema, out, flags);
      out.append(q.includesMin() ? '[' : '{');
      Number lt = q.getMin();
      Number ut = q.getMax();
      if (lt == null) {
        out.append('*');
      } else {
        out.append(lt.toString());
      }
      out.append(" TO ");
      if (ut == null) {
        out.append('*');
      } else {
        out.append(ut.toString());
      }
      out.append(q.includesMax() ? ']' : '}');
    } else if (query instanceof BooleanQuery) {
      BooleanQuery q = (BooleanQuery) query;
      boolean needParens = false;
      if (q.getBoost() != 1.0 || q.getMinimumNumberShouldMatch() != 0) {
        needParens = true;
      }
      if (needParens) {
        out.append('(');
      }
      boolean first = true;
      for (BooleanClause c : (List<BooleanClause>) q.clauses()) {
        if (!first) {
          out.append(' ');
        } else {
          first = false;
        }
        if (c.isProhibited()) {
          out.append('-');
        } else if (c.isRequired()) {
          out.append('+');
        }
        Query subQuery = c.getQuery();
        boolean wrapQuery = false;
        if (subQuery instanceof BooleanQuery) {
          wrapQuery = true;
        }
        if (wrapQuery) {
          out.append('(');
        }
        toString(subQuery, schema, out, flags);
        if (wrapQuery) {
          out.append(')');
        }
      }
      if (needParens) {
        out.append(')');
      }
      if (q.getMinimumNumberShouldMatch() > 0) {
        out.append('~');
        out.append(Integer.toString(q.getMinimumNumberShouldMatch()));
      }
    } else if (query instanceof PrefixQuery) {
      PrefixQuery q = (PrefixQuery) query;
      Term prefix = q.getPrefix();
      FieldType ft = writeFieldName(prefix.field(), schema, out, flags);
      out.append(prefix.text());
      out.append('*');
    } else if (query instanceof ConstantScorePrefixQuery) {
      ConstantScorePrefixQuery q = (ConstantScorePrefixQuery) query;
      Term prefix = q.getPrefix();
      FieldType ft = writeFieldName(prefix.field(), schema, out, flags);
      out.append(prefix.text());
      out.append('*');
    } else if (query instanceof WildcardQuery) {
      out.append(query.toString());
      writeBoost = false;
    } else if (query instanceof FuzzyQuery) {
      out.append(query.toString());
      writeBoost = false;
    } else if (query instanceof ConstantScoreQuery) {
      out.append(query.toString());
      writeBoost = false;
    } else {
      out.append(query.getClass().getSimpleName()
              + '(' + query.toString() + ')');
      writeBoost = false;
    }
    if (writeBoost && query.getBoost() != 1.0f) {
      out.append("^");
      out.append(Float.toString(query.getBoost()));
    }
  }
  public static String toString(Query query, IndexSchema schema) {
    try {
      StringBuilder sb = new StringBuilder();
      toString(query, schema, sb, 0);
      return sb.toString();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  public static class StrParser {
    String val;
    int pos;
    int end;
    public StrParser(String val) {
      this(val, 0, val.length());
    }
    public StrParser(String val, int start, int end) {
      this.val = val;
      this.pos = start;
      this.end = end;
    }
    void eatws() {
      while (pos < end && Character.isWhitespace(val.charAt(pos))) pos++;
    }
    void skip(int nChars) {
      pos = Math.max(pos + nChars, end);
    }
    boolean opt(String s) {
      eatws();
      int slen = s.length();
      if (val.regionMatches(pos, s, 0, slen)) {
        pos += slen;
        return true;
      }
      return false;
    }
    boolean opt(char ch) {
      eatws();
      if (val.charAt(pos) == ch) {
        pos++;
        return true;
      }
      return false;
    }
    void expect(String s) throws ParseException {
      eatws();
      int slen = s.length();
      if (val.regionMatches(pos, s, 0, slen)) {
        pos += slen;
      } else {
        throw new ParseException("Expected '" + s + "' at position " + pos + " in '" + val + "'");
      }
    }
    float getFloat() throws ParseException {
      eatws();
      char[] arr = new char[end - pos];
      int i;
      for (i = 0; i < arr.length; i++) {
        char ch = val.charAt(pos);
        if ((ch >= '0' && ch <= '9')
                || ch == '+' || ch == '-'
                || ch == '.' || ch == 'e' || ch == 'E'
                ) {
          pos++;
          arr[i] = ch;
        } else {
          break;
        }
      }
      return Float.parseFloat(new String(arr, 0, i));
    }
    double getDouble() throws ParseException {
      eatws();
      char[] arr = new char[end - pos];
      int i;
      for (i = 0; i < arr.length; i++) {
        char ch = val.charAt(pos);
        if ((ch >= '0' && ch <= '9')
                || ch == '+' || ch == '-'
                || ch == '.' || ch == 'e' || ch == 'E'
                ) {
          pos++;
          arr[i] = ch;
        } else {
          break;
        }
      }
      return Double.parseDouble(new String(arr, 0, i));
    }
    int getInt() throws ParseException {
      eatws();
      char[] arr = new char[end - pos];
      int i;
      for (i = 0; i < arr.length; i++) {
        char ch = val.charAt(pos);
        if ((ch >= '0' && ch <= '9')
                || ch == '+' || ch == '-'
                ) {
          pos++;
          arr[i] = ch;
        } else {
          break;
        }
      }
      return Integer.parseInt(new String(arr, 0, i));
    }
    String getId() throws ParseException {
      eatws();
      int id_start = pos;
      if (pos < end && Character.isJavaIdentifierStart(val.charAt(pos))) {
        pos++;
        while (pos < end) {
          char ch = val.charAt(pos);
          if (!Character.isJavaIdentifierPart(ch) && ch != '.') {
            break;
          }
          pos++;
        }
        return val.substring(id_start, pos);
      }
      throw new ParseException("Expected identifier at pos " + pos + " str='" + val + "'");
    }
    String getQuotedString() throws ParseException {
      eatws();
      char delim = peekChar();
      if (!(delim == '\"' || delim == '\'')) {
        return null;
      }
      int val_start = ++pos;
      StringBuilder sb = new StringBuilder(); 
      for (; ;) {
        if (pos >= end) {
          throw new ParseException("Missing end quote for string at pos " + (val_start - 1) + " str='" + val + "'");
        }
        char ch = val.charAt(pos);
        if (ch == '\\') {
          pos++;
          if (pos >= end) break;
          ch = val.charAt(pos);
          switch (ch) {
            case 'n':
              ch = '\n';
              break;
            case 't':
              ch = '\t';
              break;
            case 'r':
              ch = '\r';
              break;
            case 'b':
              ch = '\b';
              break;
            case 'f':
              ch = '\f';
              break;
            case 'u':
              if (pos + 4 >= end) {
                throw new ParseException("bad unicode escape \\uxxxx at pos" + (val_start - 1) + " str='" + val + "'");
              }
              ch = (char) Integer.parseInt(val.substring(pos + 1, pos + 5), 16);
              pos += 4;
              break;
          }
        } else if (ch == delim) {
          pos++;  
          break;
        }
        sb.append(ch);
        pos++;
      }
      return sb.toString();
    }
    char peek() {
      eatws();
      return pos < end ? val.charAt(pos) : 0;
    }
    char peekChar() {
      return pos < end ? val.charAt(pos) : 0;
    }
    public String toString() {
      return "'" + val + "'" + ", pos=" + pos;
    }
  }
  public static List<String> toString(List<Query> queries, IndexSchema schema) {
    List<String> out = new ArrayList<String>(queries.size());
    for (Query q : queries) {
      out.add(QueryParsing.toString(q, schema));
    }
    return out;
  }
  public static FunctionQuery parseFunction(String func, IndexSchema schema) throws ParseException {
    SolrCore core = SolrCore.getSolrCore();
    return (FunctionQuery) (QParser.getParser(func, "func", new LocalSolrQueryRequest(core, new HashMap())).parse());
  }
}
