package org.apache.lucene.queryParser.core;
import java.util.Locale;
import org.apache.lucene.messages.Message;
import org.apache.lucene.messages.MessageImpl;
import org.apache.lucene.messages.NLS;
import org.apache.lucene.messages.NLSException;
import org.apache.lucene.queryParser.core.messages.QueryParserMessages;
import org.apache.lucene.queryParser.core.nodes.QueryNode;
public class QueryNodeException extends Exception implements NLSException {
  private static final long serialVersionUID = -5962648855261624214L;
  protected Message message = new MessageImpl(QueryParserMessages.EMPTY_MESSAGE);
  public QueryNodeException(Message message) {
    super(message.getKey());
    this.message = message;
  }
  public QueryNodeException(Throwable throwable) {
    super(throwable);
  }
  public QueryNodeException(Message message, Throwable throwable) {
    super(message.getKey(), throwable);
    this.message = message;
  }
  public Message getMessageObject() {
    return this.message;
  }
  @Override
  public String getMessage() {
    return getLocalizedMessage();
  }
  @Override
  public String getLocalizedMessage() {
    return getLocalizedMessage(Locale.getDefault());
  }
  public String getLocalizedMessage(Locale locale) {
    return this.message.getLocalizedMessage(locale);
  }
  @Override
  public String toString() {
    return this.message.getKey() + ": " + getLocalizedMessage();
  }
}
