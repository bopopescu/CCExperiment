package org.apache.log4j.net;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.CyclicBuffer;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.spi.ErrorCode;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.OptionHandler;
import org.apache.log4j.spi.TriggeringEventEvaluator;
import org.apache.log4j.xml.UnrecognizedElementHandler;
import org.w3c.dom.Element;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Date;
import java.util.Properties;
public class SMTPAppender extends AppenderSkeleton
        implements UnrecognizedElementHandler {
  private String to;
  private String cc;  
  private String bcc;  
  private String from;
  private String replyTo;
  private String subject;
  private String smtpHost;
  private String smtpUsername;
  private String smtpPassword;
  private String smtpProtocol;
  private int smtpPort = -1;
  private boolean smtpDebug = false;
  private int bufferSize = 512;
  private boolean locationInfo = false;
  private boolean sendOnClose = false;
  protected CyclicBuffer cb = new CyclicBuffer(bufferSize);
  protected Message msg;
  protected TriggeringEventEvaluator evaluator;
  public
  SMTPAppender() {
    this(new DefaultEvaluator());
  }
  public
  SMTPAppender(TriggeringEventEvaluator evaluator) {
    this.evaluator = evaluator;
  }
  public
  void activateOptions() {
    Session session = createSession();
    msg = new MimeMessage(session);
     try {
        addressMessage(msg);
        if(subject != null) {
           try {
                msg.setSubject(MimeUtility.encodeText(subject, "UTF-8", null));
           } catch(UnsupportedEncodingException ex) {
                LogLog.error("Unable to encode SMTP subject", ex);
           }
        }
     } catch(MessagingException e) {
       LogLog.error("Could not activate SMTPAppender options.", e );
     }
     if (evaluator instanceof OptionHandler) {
         ((OptionHandler) evaluator).activateOptions();
     }
  }
  protected void addressMessage(final Message msg) throws MessagingException {
       if (from != null) {
	 		msg.setFrom(getAddress(from));
       } else {
	 		msg.setFrom();
	   }
         if (replyTo != null && replyTo.length() > 0) {
               msg.setReplyTo(parseAddress(replyTo));
         }
       if (to != null && to.length() > 0) {
             msg.setRecipients(Message.RecipientType.TO, parseAddress(to));
       }
	  if (cc != null && cc.length() > 0) {
		msg.setRecipients(Message.RecipientType.CC, parseAddress(cc));
	  }
	  if (bcc != null && bcc.length() > 0) {
		msg.setRecipients(Message.RecipientType.BCC, parseAddress(bcc));
	  }
  }
  protected Session createSession() {
    Properties props = null;
    try {
        props = new Properties (System.getProperties());
    } catch(SecurityException ex) {
        props = new Properties();
    }
    String prefix = "mail.smtp";
    if (smtpProtocol != null) {
        props.put("mail.transport.protocol", smtpProtocol);
        prefix = "mail." + smtpProtocol;
    }
    if (smtpHost != null) {
      props.put(prefix + ".host", smtpHost);
    }
    if (smtpPort > 0) {
        props.put(prefix + ".port", String.valueOf(smtpPort));
    }
    Authenticator auth = null;
    if(smtpPassword != null && smtpUsername != null) {
      props.put(prefix + ".auth", "true");
      auth = new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
          return new PasswordAuthentication(smtpUsername, smtpPassword);
        }
      };
    }
    Session session = Session.getInstance(props, auth);
    if (smtpProtocol != null) {
        session.setProtocolForAddress("rfc822", smtpProtocol);
    }
    if (smtpDebug) {
        session.setDebug(smtpDebug);
    }
    return session;
  }
  public
  void append(LoggingEvent event) {
    if(!checkEntryConditions()) {
      return;
    }
    event.getThreadName();
    event.getNDC();
    event.getMDCCopy();
    if(locationInfo) {
      event.getLocationInformation();
    }
    event.getRenderedMessage();
    event.getThrowableStrRep();
    cb.add(event);
    if(evaluator.isTriggeringEvent(event)) {
      sendBuffer();
    }
  }
  protected
  boolean checkEntryConditions() {
    if(this.msg == null) {
      errorHandler.error("Message object not configured.");
      return false;
    }
    if(this.evaluator == null) {
      errorHandler.error("No TriggeringEventEvaluator is set for appender ["+
			 name+"].");
      return false;
    }
    if(this.layout == null) {
      errorHandler.error("No layout set for appender named ["+name+"].");
      return false;
    }
    return true;
  }
  synchronized
  public
  void close() {
    this.closed = true;
    if (sendOnClose && cb.length() > 0) {
        sendBuffer();
    }
  }
  InternetAddress getAddress(String addressStr) {
    try {
      return new InternetAddress(addressStr);
    } catch(AddressException e) {
      errorHandler.error("Could not parse address ["+addressStr+"].", e,
			 ErrorCode.ADDRESS_PARSE_FAILURE);
      return null;
    }
  }
  InternetAddress[] parseAddress(String addressStr) {
    try {
      return InternetAddress.parse(addressStr, true);
    } catch(AddressException e) {
      errorHandler.error("Could not parse address ["+addressStr+"].", e,
			 ErrorCode.ADDRESS_PARSE_FAILURE);
      return null;
    }
  }
  public
  String getTo() {
    return to;
  }
  public
  boolean requiresLayout() {
    return true;
  }
  protected String formatBody() {
      StringBuffer sbuf = new StringBuffer();
      String t = layout.getHeader();
      if(t != null)
	sbuf.append(t);
      int len =  cb.length();
      for(int i = 0; i < len; i++) {
	LoggingEvent event = cb.get();
	sbuf.append(layout.format(event));
	if(layout.ignoresThrowable()) {
	  String[] s = event.getThrowableStrRep();
	  if (s != null) {
	    for(int j = 0; j < s.length; j++) {
	      sbuf.append(s[j]);
	      sbuf.append(Layout.LINE_SEP);
	    }
	  }
	}
      }
      t = layout.getFooter();
      if(t != null) {
	    sbuf.append(t);
      }
      return sbuf.toString();
  }
  protected
  void sendBuffer() {
    try {
      String s = formatBody();
      boolean allAscii = true;
      for(int i = 0; i < s.length() && allAscii; i++) {
          allAscii = s.charAt(i) <= 0x7F;
      }
      MimeBodyPart part;
      if (allAscii) {
          part = new MimeBodyPart();
          part.setContent(s, layout.getContentType());
      } else {
          try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(
                    MimeUtility.encode(os, "quoted-printable"), "UTF-8");
            writer.write(s);
            writer.close();
            InternetHeaders headers = new InternetHeaders();
            headers.setHeader("Content-Type", layout.getContentType() + "; charset=UTF-8");
            headers.setHeader("Content-Transfer-Encoding", "quoted-printable");
            part = new MimeBodyPart(headers, os.toByteArray());
          } catch(Exception ex) {
              StringBuffer sbuf = new StringBuffer(s);
              for (int i = 0; i < sbuf.length(); i++) {
                  if (sbuf.charAt(i) >= 0x80) {
                      sbuf.setCharAt(i, '?');
                  }
              }
              part = new MimeBodyPart();
              part.setContent(sbuf.toString(), layout.getContentType());
          }
      }
      Multipart mp = new MimeMultipart();
      mp.addBodyPart(part);
      msg.setContent(mp);
      msg.setSentDate(new Date());
      Transport.send(msg);
    } catch(MessagingException e) {
      LogLog.error("Error occured while sending e-mail notification.", e);
    } catch(RuntimeException e) {
      LogLog.error("Error occured while sending e-mail notification.", e);
    }
  }
  public
  String getEvaluatorClass() {
    return evaluator == null ? null : evaluator.getClass().getName();
  }
  public
  String getFrom() {
    return from;
  }
  public
  String getReplyTo() {
    return replyTo;
  }
  public
  String getSubject() {
    return subject;
  }
  public
  void setFrom(String from) {
    this.from = from;
  }
  public
  void setReplyTo(final String addresses) {
    this.replyTo = addresses;
  }
  public
  void setSubject(String subject) {
    this.subject = subject;
  }
  public
  void setBufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
    cb.resize(bufferSize);
  }
  public
  void setSMTPHost(String smtpHost) {
    this.smtpHost = smtpHost;
  }
  public
  String getSMTPHost() {
    return smtpHost;
  }
  public
  void setTo(String to) {
    this.to = to;
  }
  public
  int getBufferSize() {
    return bufferSize;
  }
  public
  void setEvaluatorClass(String value) {
      evaluator = (TriggeringEventEvaluator)
                OptionConverter.instantiateByClassName(value,
					   TriggeringEventEvaluator.class,
						       evaluator);
  }
  public
  void setLocationInfo(boolean locationInfo) {
    this.locationInfo = locationInfo;
  }
  public
  boolean getLocationInfo() {
    return locationInfo;
  }
   public void setCc(final String addresses) {
     this.cc = addresses;
   }
    public String getCc() {
     return cc;
    }
   public void setBcc(final String addresses) {
     this.bcc = addresses;
   }
    public String getBcc() {
     return bcc;
    }
  public void setSMTPPassword(final String password) {
    this.smtpPassword = password;
  }
  public void setSMTPUsername(final String username) {
    this.smtpUsername = username;
  }
  public void setSMTPDebug(final boolean debug) {
    this.smtpDebug = debug;
  }
  public String getSMTPPassword() {
    return smtpPassword;
  }
  public String getSMTPUsername() {
    return smtpUsername;
  }
  public boolean getSMTPDebug() {
    return smtpDebug;
  }
  public final void setEvaluator(final TriggeringEventEvaluator trigger) {
      if (trigger == null) {
          throw new NullPointerException("trigger");
      }
      this.evaluator = trigger;
  }
  public final TriggeringEventEvaluator getEvaluator() {
      return evaluator;
  }
  public boolean parseUnrecognizedElement(final Element element,
                                          final Properties props) throws Exception {
      if ("triggeringPolicy".equals(element.getNodeName())) {
          Object triggerPolicy =
                  org.apache.log4j.xml.DOMConfigurator.parseElement(
                          element, props, TriggeringEventEvaluator.class);
          if (triggerPolicy instanceof TriggeringEventEvaluator) {
              setEvaluator((TriggeringEventEvaluator) triggerPolicy);
          }
          return true;
      }
      return false;
  }
  public final String getSMTPProtocol() {
      return smtpProtocol;
  }
  public final void setSMTPProtocol(final String val) {
      smtpProtocol = val;
  }
  public final int getSMTPPort() {
        return smtpPort;
  }
  public final void setSMTPPort(final int val) {
        smtpPort = val;
  }
  public final boolean getSendOnClose() {
        return sendOnClose;
  }
  public final void setSendOnClose(final boolean val) {
        sendOnClose = val;
  }
}
class DefaultEvaluator implements TriggeringEventEvaluator {
  public
  boolean isTriggeringEvent(LoggingEvent event) {
    return event.getLevel().isGreaterOrEqual(Level.ERROR);
  }
}
