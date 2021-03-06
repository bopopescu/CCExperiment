package org.apache.log4j.net;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.DatagramPacket;
import java.net.UnknownHostException;
import java.net.SocketException;
import org.apache.log4j.helpers.OptionConverter;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.SingleLineTracerPrintWriter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.helpers.QuietWriter;
public class DatagramStringAppender extends AppenderSkeleton {
  public static final String DATAGRAM_HOST_OPTION = "DatagramHost";
  public static final String DATAGRAM_PORT_OPTION = "DatagramPort";
  public static final String DATAGRAM_ENCODING_OPTION = "DatagramEncoding";
  public static final String DEFAULT_HOST = "localhost";
  public static final int DEFAULT_PORT = 8200;
  public static final String DEFAULT_ENCODING = null;
  String host = DEFAULT_HOST;
  int port = DEFAULT_PORT;
  String encoding = DEFAULT_ENCODING;
  SingleLineTracerPrintWriter stp;
  QuietWriter qw;
  public
  DatagramStringAppender() {
    this.setDestination(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_ENCODING);
  }
  public
  DatagramStringAppender(Layout layout) {
    this.setLayout(layout);
    this.setDestination(DEFAULT_HOST, DEFAULT_PORT, DEFAULT_ENCODING);
  }
  public
  DatagramStringAppender(Layout layout, String host, int port) {
    this.setLayout(layout);
    this.setDestination(host, port, DEFAULT_ENCODING);
  }
  public
  DatagramStringAppender(Layout layout, String host, int port, String encoding) {
    this.setLayout(layout);
    this.setDestination(host, port, encoding);
  }
  public
  void close() {
    closed = true;
    qw = null;
    stp = null;
  }
  public
  void append(LoggingEvent event) {
    if(!isAsSevereAsThreshold(event.priority))
      return;
    if(qw == null) {
      errorHandler.error(
        "No host is set for DatagramStringAppender named \""
        +	this.name + "\".");
      return;
    }
    String buffer = layout.format(event);
    qw.write(buffer);
    if(event.throwable != null)
      event.throwable.printStackTrace(stp);
    else if (event.throwableInformation != null) {
      qw.write(event.throwableInformation);
    }
  }
  public
  void activateOptions() {
    this.setDestination(this.host, this.port, this.encoding);
  }
  public
  String[] getOptionStrings() {
    return OptionConverter.concatanateArrays(super.getOptionStrings(),
		      new String[] {
            DATAGRAM_HOST_OPTION,
            DATAGRAM_PORT_OPTION,
            DATAGRAM_ENCODING_OPTION});
  }
  public
  boolean requiresLayout() {
    return true;
  }
  public
  void setOption(String option, String value) {
    if(value == null) return;
    super.setOption(option, value);
    if(option.equals(DATAGRAM_HOST_OPTION))
    {
      this.host = value;
    }
    else if(option.equals(DATAGRAM_PORT_OPTION))
    {
      this.port = OptionConverter.toInt(value, DEFAULT_PORT);
    }
    else if(option.equals(DATAGRAM_ENCODING_OPTION))
    {
      this.encoding = value;
    }
  }
  public
  void setDestination(String host, int port, String encoding) {
    if (host==null) {
      LogLog.error("setDestination: host is null");
      host = DEFAULT_HOST;
    }
    this.host = host;
    this.port = port;
    this.encoding = encoding;
    this.qw = new QuietWriter(
        new DatagramStringWriter(host, port, encoding),
        errorHandler);
    this.stp = new SingleLineTracerPrintWriter(qw);
  }
  public
  void setLayout(Layout layout) {
    this.layout = layout;
  }
}
