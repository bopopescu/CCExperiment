package org.apache.lucene.messages;
import java.util.Locale;
public class MessageImpl implements Message {
  private static final long serialVersionUID = -3077643314630884523L;
  private String key;
  private Object[] arguments = new Object[0];
  public MessageImpl(String key) {
    this.key = key;
  }
  public MessageImpl(String key, Object... args) {
    this(key);
    this.arguments = args;
  }
  public Object[] getArguments() {
    return this.arguments;
  }
  public String getKey() {
    return this.key;
  }
  public String getLocalizedMessage() {
    return getLocalizedMessage(Locale.getDefault());
  }
  public String getLocalizedMessage(Locale locale) {
    return NLS.getLocalizedMessage(getKey(), locale, getArguments());
  }
  @Override
  public String toString() {
    Object[] args = getArguments();
    StringBuilder sb = new StringBuilder(getKey());
    if (args != null) {
      for (int i = 0; i < args.length; i++) {
        sb.append(i == 0 ? " " : ", ").append(args[i]);
      }
    }
    return sb.toString();
  }
}
