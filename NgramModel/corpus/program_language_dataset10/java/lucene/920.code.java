package org.apache.lucene.benchmark.byTask.utils;
import java.text.NumberFormat;
public class Format {
  private static NumberFormat numFormat [] = { 
    NumberFormat.getInstance(), 
    NumberFormat.getInstance(),
    NumberFormat.getInstance(),
  };
  private static final String padd = "                                                 ";
  static {
    numFormat[0].setMaximumFractionDigits(0);
    numFormat[0].setMinimumFractionDigits(0);
    numFormat[1].setMaximumFractionDigits(1);
    numFormat[1].setMinimumFractionDigits(1);
    numFormat[2].setMaximumFractionDigits(2);
    numFormat[2].setMinimumFractionDigits(2);
  }
  public static String format(int numFracDigits, float f, String col) {
    String res = padd + numFormat[numFracDigits].format(f);
    return res.substring(res.length() - col.length());
  }
  public static String format(int numFracDigits, double f, String col) {
    String res = padd + numFormat[numFracDigits].format(f);
    return res.substring(res.length() - col.length());
  }
  public static String formatPaddRight(int numFracDigits, float f, String col) {
    String res = numFormat[numFracDigits].format(f) + padd;
    return res.substring(0, col.length());
  }
  public static String formatPaddRight(int numFracDigits, double f, String col) {
    String res = numFormat[numFracDigits].format(f) + padd;
    return res.substring(0, col.length());
  }
  public static String format(int n, String col) {
    String res = padd + n;
    return res.substring(res.length() - col.length());
  }
  public static String format(String s, String col) {
    String s1 = (s + padd);
    return s1.substring(0, Math.min(col.length(), s1.length()));
  }
  public static String formatPaddLeft(String s, String col) {
    String res = padd + s;
    return res.substring(res.length() - col.length());
  }
  public static String simpleName (Class<?> cls) {
    String c = cls.getName();
    String p = cls.getPackage().getName();
    int k = c.lastIndexOf(p+".");
    if (k<0) {
      return c;
    }
    return c.substring(k+1+p.length());
  }
}
