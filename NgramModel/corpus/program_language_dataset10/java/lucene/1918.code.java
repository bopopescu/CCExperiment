package org.apache.lucene.messages;
import java.util.Locale;
import junit.framework.TestCase;
public class TestNLS extends TestCase {
  public void testMessageLoading() {
    Message invalidSyntax = new MessageImpl(
        MessagesTestBundle.Q0001E_INVALID_SYNTAX, "XXX");
    assertEquals("Syntax Error: XXX", invalidSyntax.getLocalizedMessage());
  }
  public void testMessageLoading_ja() {
    Message invalidSyntax = new MessageImpl(
        MessagesTestBundle.Q0001E_INVALID_SYNTAX, "XXX");
    assertEquals("構文エラー: XXX", invalidSyntax
        .getLocalizedMessage(Locale.JAPANESE));
  }
  public void testNLSLoading() {
    String message = NLS
        .getLocalizedMessage(MessagesTestBundle.Q0004E_INVALID_SYNTAX_ESCAPE_UNICODE_TRUNCATION);
    assertEquals("Truncated unicode escape sequence.", message);
    message = NLS.getLocalizedMessage(MessagesTestBundle.Q0001E_INVALID_SYNTAX,
        "XXX");
    assertEquals("Syntax Error: XXX", message);
  }
  public void testNLSLoading_ja() {
    String message = NLS.getLocalizedMessage(
        MessagesTestBundle.Q0004E_INVALID_SYNTAX_ESCAPE_UNICODE_TRUNCATION,
        Locale.JAPANESE);
    assertEquals("切り捨てられたユニコード・エスケープ・シーケンス。", message);
    message = NLS.getLocalizedMessage(MessagesTestBundle.Q0001E_INVALID_SYNTAX,
        Locale.JAPANESE, "XXX");
    assertEquals("構文エラー: XXX", message);
  }
  public void testNLSLoading_xx_XX() {
    Locale locale = new Locale("xx", "XX", "");
    String message = NLS.getLocalizedMessage(
        MessagesTestBundle.Q0004E_INVALID_SYNTAX_ESCAPE_UNICODE_TRUNCATION,
        locale);
    assertEquals("Truncated unicode escape sequence.", message);
    message = NLS.getLocalizedMessage(MessagesTestBundle.Q0001E_INVALID_SYNTAX,
        locale, "XXX");
    assertEquals("Syntax Error: XXX", message);
  }
  public void testMissingMessage() {
    Locale locale = Locale.ENGLISH;
    String message = NLS.getLocalizedMessage(
        MessagesTestBundle.Q0005E_MESSAGE_NOT_IN_BUNDLE, locale);
    assertEquals("Message with key:Q0005E_MESSAGE_NOT_IN_BUNDLE and locale: "
        + locale.toString() + " not found.", message);
  }
}
