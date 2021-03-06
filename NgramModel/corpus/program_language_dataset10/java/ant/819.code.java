package org.apache.tools.ant.util;
public class UnicodeUtil {
   public static StringBuffer EscapeUnicode(char ch) {
       StringBuffer unicodeBuf = new StringBuffer("u0000");
       String s = Integer.toHexString(ch);
       for (int i = 0; i < s.length(); i++) {
           unicodeBuf.setCharAt(unicodeBuf.length()
                                - s.length() + i,
                                s.charAt(i));
       }
       return unicodeBuf;	   
   }
}
