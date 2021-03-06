package org.apache.lucene.queryParser.core;
import org.apache.lucene.messages.Message;
import org.apache.lucene.messages.MessageImpl;
import org.apache.lucene.queryParser.core.messages.QueryParserMessages;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
import org.apache.lucene.queryParser.core.parser.SyntaxParser;
public class QueryNodeParseException extends QueryNodeException {
  private static final long serialVersionUID = 8197535103538766773L;
  private CharSequence query;
  private int beginColumn = -1;
  private int beginLine = -1;
  private String errorToken = "";
  public QueryNodeParseException(Message message) {
    super(message);
  }
  public QueryNodeParseException(Throwable throwable) {
    super(throwable);
  }
  public QueryNodeParseException(Message message, Throwable throwable) {
    super(message, throwable);
  }
  public void setQuery(CharSequence query) {
    this.query = query;
    this.message = new MessageImpl(
        QueryParserMessages.INVALID_SYNTAX_CANNOT_PARSE, query, "");
  }
  public CharSequence getQuery() {
    return this.query;
  }
  protected void setErrorToken(String errorToken) {
    this.errorToken = errorToken;
  }
  public String getErrorToken() {
    return this.errorToken;
  }
  public void setNonLocalizedMessage(Message message) {
    this.message = message;
  }
  public int getBeginLine() {
    return this.beginLine;
  }
  public int getBeginColumn() {
    return this.beginColumn;
  }
  protected void setBeginLine(int beginLine) {
    this.beginLine = beginLine;
  }
  protected void setBeginColumn(int beginColumn) {
    this.beginColumn = beginColumn;
  }
}
