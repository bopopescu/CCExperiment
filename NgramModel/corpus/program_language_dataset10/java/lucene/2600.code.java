package org.apache.solr.util;
import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.text.ParseException;
import java.util.regex.Pattern;
public class DateMathParser  {
  public static final Map<String,Integer> CALENDAR_UNITS = makeUnitsMap();
  private static Map<String,Integer> makeUnitsMap() {
    Map<String,Integer> units = new HashMap<String,Integer>(13);
    units.put("YEAR",        Calendar.YEAR);
    units.put("YEARS",       Calendar.YEAR);
    units.put("MONTH",       Calendar.MONTH);
    units.put("MONTHS",      Calendar.MONTH);
    units.put("DAY",         Calendar.DATE);
    units.put("DAYS",        Calendar.DATE);
    units.put("DATE",        Calendar.DATE);
    units.put("HOUR",        Calendar.HOUR_OF_DAY);
    units.put("HOURS",       Calendar.HOUR_OF_DAY);
    units.put("MINUTE",      Calendar.MINUTE);
    units.put("MINUTES",     Calendar.MINUTE);
    units.put("SECOND",      Calendar.SECOND);
    units.put("SECONDS",     Calendar.SECOND);
    units.put("MILLI",       Calendar.MILLISECOND);
    units.put("MILLIS",      Calendar.MILLISECOND);
    units.put("MILLISECOND", Calendar.MILLISECOND);
    units.put("MILLISECONDS",Calendar.MILLISECOND);
    return units;
  }
  public static void add(Calendar c, int val, String unit) {
    Integer uu = CALENDAR_UNITS.get(unit);
    if (null == uu) {
      throw new IllegalArgumentException("Adding Unit not recognized: "
                                         + unit);
    }
    c.add(uu.intValue(), val);
  }
  public static void round(Calendar c, String unit) {
    Integer uu = CALENDAR_UNITS.get(unit);
    if (null == uu) {
      throw new IllegalArgumentException("Rounding Unit not recognized: "
                                         + unit);
    }
    int u = uu.intValue();
    switch (u) {
    case Calendar.YEAR:
      c.clear(Calendar.MONTH);
    case Calendar.MONTH:
      c.clear(Calendar.DAY_OF_MONTH);
      c.clear(Calendar.DAY_OF_WEEK);
      c.clear(Calendar.DAY_OF_WEEK_IN_MONTH);
      c.clear(Calendar.DAY_OF_YEAR);
      c.clear(Calendar.WEEK_OF_MONTH);
      c.clear(Calendar.WEEK_OF_YEAR);
    case Calendar.DATE:
      c.clear(Calendar.HOUR_OF_DAY);
      c.clear(Calendar.HOUR);
      c.clear(Calendar.AM_PM);
    case Calendar.HOUR_OF_DAY:
      c.clear(Calendar.MINUTE);
    case Calendar.MINUTE:
      c.clear(Calendar.SECOND);
    case Calendar.SECOND:
      c.clear(Calendar.MILLISECOND);
      break;
    default:
      throw new IllegalStateException
        ("No logic for rounding value ("+u+") " + unit);
    }
  }
  private TimeZone zone;
  private Locale loc;
  private Date now;
  public DateMathParser(TimeZone tz, Locale l) {
    zone = tz;
    loc = l;
    setNow(new Date());
  }
  public void setNow(Date n) {
    now = n;
  }
  public Date getNow() {
    return (Date) now.clone();
  }
  public Date parseMath(String math) throws ParseException {
    Calendar cal = Calendar.getInstance(zone, loc);
    cal.setTime(getNow());
    if (0==math.length()) {
      return cal.getTime();
    }
    String[] ops = splitter.split(math);
    int pos = 0;
    while ( pos < ops.length ) {
      if (1 != ops[pos].length()) {
        throw new ParseException
          ("Multi character command found: \"" + ops[pos] + "\"", pos);
      }
      char command = ops[pos++].charAt(0);
      switch (command) {
      case '/':
        if (ops.length < pos + 1) {
          throw new ParseException
            ("Need a unit after command: \"" + command + "\"", pos);
        }
        try {
          round(cal, ops[pos++]);
        } catch (IllegalArgumentException e) {
          throw new ParseException
            ("Unit not recognized: \"" + ops[pos-1] + "\"", pos-1);
        }
        break;
      case '+': 
      case '-':
        if (ops.length < pos + 2) {
          throw new ParseException
            ("Need a value and unit for command: \"" + command + "\"", pos);
        }
        int val = 0;
        try {
          val = Integer.valueOf(ops[pos++]);
        } catch (NumberFormatException e) {
          throw new ParseException
            ("Not a Number: \"" + ops[pos-1] + "\"", pos-1);
        }
        if ('-' == command) {
          val = 0 - val;
        }
        try {
          String unit = ops[pos++];
          add(cal, val, unit);
        } catch (IllegalArgumentException e) {
          throw new ParseException
            ("Unit not recognized: \"" + ops[pos-1] + "\"", pos-1);
        }
        break;
      default:
        throw new ParseException
          ("Unrecognized command: \"" + command + "\"", pos-1);
      }
    }
    return cal.getTime();
  }
  private static Pattern splitter = Pattern.compile("\\b|(?<=\\d)(?=\\D)");
}
