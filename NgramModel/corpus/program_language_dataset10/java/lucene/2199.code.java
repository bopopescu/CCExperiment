package org.apache.solr.common.util;
import java.io.Writer;
import java.io.IOException;
import java.util.Map;
public class XML {
  private static final String[] chardata_escapes=
  {"#0;","#1;","#2;","#3;","#4;","#5;","#6;","#7;","#8;",null,null,"#11;","#12;",null,"#14;","#15;","#16;","#17;","#18;","#19;","#20;","#21;","#22;","#23;","#24;","#25;","#26;","#27;","#28;","#29;","#30;","#31;",null,null,null,null,null,null,"&amp;",null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,"&lt;",null,"&gt;"};
  private static final String[] attribute_escapes=
  {"#0;","#1;","#2;","#3;","#4;","#5;","#6;","#7;","#8;",null,null,"#11;","#12;",null,"#14;","#15;","#16;","#17;","#18;","#19;","#20;","#21;","#22;","#23;","#24;","#25;","#26;","#27;","#28;","#29;","#30;","#31;",null,null,"&quot;",null,null,null,"&amp;",null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,null,"&lt;"};
  public static void escapeCharData(String str, Writer out) throws IOException {
    escape(str, out, chardata_escapes);
  }
  public static void escapeAttributeValue(String str, Writer out) throws IOException {
    escape(str, out, attribute_escapes);
  }
  public static void escapeAttributeValue(char [] chars, int start, int length, Writer out) throws IOException {
    escape(chars, start, length, out, attribute_escapes);
  }
  public final static void writeXML(Writer out, String tag, String val) throws IOException {
    out.write('<');
    out.write(tag);
    if (val == null) {
      out.write('/');
      out.write('>');
    } else {
      out.write('>');
      escapeCharData(val,out);
      out.write('<');
      out.write('/');
      out.write(tag);
      out.write('>');
    }
  }
  public final static void writeUnescapedXML(Writer out, String tag, String val, Object... attrs) throws IOException {
    out.write('<');
    out.write(tag);
    for (int i=0; i<attrs.length; i++) {
      out.write(' ');
      out.write(attrs[i++].toString());
      out.write('=');
      out.write('"');
      out.write(attrs[i].toString());
      out.write('"');
    }
    if (val == null) {
      out.write('/');
      out.write('>');
    } else {
      out.write('>');
      out.write(val);
      out.write('<');
      out.write('/');
      out.write(tag);
      out.write('>');
    }
  }
  public final static void writeXML(Writer out, String tag, String val, Object... attrs) throws IOException {
    out.write('<');
    out.write(tag);
    for (int i=0; i<attrs.length; i++) {
      out.write(' ');
      out.write(attrs[i++].toString());
      out.write('=');
      out.write('"');
      escapeAttributeValue(attrs[i].toString(), out);
      out.write('"');
    }
    if (val == null) {
      out.write('/');
      out.write('>');
    } else {
      out.write('>');
      escapeCharData(val,out);
      out.write('<');
      out.write('/');
      out.write(tag);
      out.write('>');
    }
  }
  public static void writeXML(Writer out, String tag, String val, Map<String, String> attrs) throws IOException {
    out.write('<');
    out.write(tag);
    for (Map.Entry<String, String> entry : attrs.entrySet()) {
      out.write(' ');
      out.write(entry.getKey());
      out.write('=');
      out.write('"');
      escapeAttributeValue(entry.getValue(), out);
      out.write('"');
    }
    if (val == null) {
      out.write('/');
      out.write('>');
    } else {
      out.write('>');
      escapeCharData(val,out);
      out.write('<');
      out.write('/');
      out.write(tag);
      out.write('>');
    }
  }
  private static void escape(char [] chars, int offset, int length, Writer out, String [] escapes) throws IOException{
     for (int i=offset; i<length; i++) {
      char ch = chars[i];
      if (ch<escapes.length) {
        String replacement = escapes[ch];
        if (replacement != null) {
          out.write(replacement);
          continue;
        }
      }
      out.write(ch);
    }
  }
  private static void escape(String str, Writer out, String[] escapes) throws IOException {
    for (int i=0; i<str.length(); i++) {
      char ch = str.charAt(i);
      if (ch<escapes.length) {
        String replacement = escapes[ch];
        if (replacement != null) {
          out.write(replacement);
          continue;
        }
      }
      out.write(ch);
    }
  }
}
