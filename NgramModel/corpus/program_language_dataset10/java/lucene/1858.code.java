package org.apache.lucene.document;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Locale;
import org.apache.lucene.util.LocalizedTestCase;
public class TestDateTools extends LocalizedTestCase {
  public void testStringToDate() throws ParseException {
    Date d = null;
    d = DateTools.stringToDate("2004");
    assertEquals("2004-01-01 00:00:00:000", isoFormat(d));
    d = DateTools.stringToDate("20040705");
    assertEquals("2004-07-05 00:00:00:000", isoFormat(d));
    d = DateTools.stringToDate("200407050910");
    assertEquals("2004-07-05 09:10:00:000", isoFormat(d));
    d = DateTools.stringToDate("20040705091055990");
    assertEquals("2004-07-05 09:10:55:990", isoFormat(d));
    try {
      d = DateTools.stringToDate("97");    
      fail();
    } catch(ParseException e) {  }
    try {
      d = DateTools.stringToDate("200401011235009999");    
      fail();
    } catch(ParseException e) {  }
    try {
      d = DateTools.stringToDate("aaaa");    
      fail();
    } catch(ParseException e) {  }
  }
  public void testStringtoTime() throws ParseException {
    long time = DateTools.stringToTime("197001010000");
    Calendar cal = new GregorianCalendar();
    cal.clear();
    cal.set(1970, 0, 1,    
        0, 0, 0);          
    cal.set(Calendar.MILLISECOND, 0);
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    assertEquals(cal.getTime().getTime(), time);
    cal.set(1980, 1, 2,    
        11, 5, 0);          
    cal.set(Calendar.MILLISECOND, 0);
    time = DateTools.stringToTime("198002021105");
    assertEquals(cal.getTime().getTime(), time);
  }
  public void testDateAndTimetoString() throws ParseException {
    Calendar cal = new GregorianCalendar();
    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    cal.set(2004, 1, 3,   
        22, 8, 56);       
    cal.set(Calendar.MILLISECOND, 333);
    String dateString;
    dateString = DateTools.dateToString(cal.getTime(), DateTools.Resolution.YEAR);
    assertEquals("2004", dateString);
    assertEquals("2004-01-01 00:00:00:000", isoFormat(DateTools.stringToDate(dateString)));
    dateString = DateTools.dateToString(cal.getTime(), DateTools.Resolution.MONTH);
    assertEquals("200402", dateString);
    assertEquals("2004-02-01 00:00:00:000", isoFormat(DateTools.stringToDate(dateString)));
    dateString = DateTools.dateToString(cal.getTime(), DateTools.Resolution.DAY);
    assertEquals("20040203", dateString);
    assertEquals("2004-02-03 00:00:00:000", isoFormat(DateTools.stringToDate(dateString)));
    dateString = DateTools.dateToString(cal.getTime(), DateTools.Resolution.HOUR);
    assertEquals("2004020322", dateString);
    assertEquals("2004-02-03 22:00:00:000", isoFormat(DateTools.stringToDate(dateString)));
    dateString = DateTools.dateToString(cal.getTime(), DateTools.Resolution.MINUTE);
    assertEquals("200402032208", dateString);
    assertEquals("2004-02-03 22:08:00:000", isoFormat(DateTools.stringToDate(dateString)));
    dateString = DateTools.dateToString(cal.getTime(), DateTools.Resolution.SECOND);
    assertEquals("20040203220856", dateString);
    assertEquals("2004-02-03 22:08:56:000", isoFormat(DateTools.stringToDate(dateString)));
    dateString = DateTools.dateToString(cal.getTime(), DateTools.Resolution.MILLISECOND);
    assertEquals("20040203220856333", dateString);
    assertEquals("2004-02-03 22:08:56:333", isoFormat(DateTools.stringToDate(dateString)));
    cal.set(1961, 2, 5,   
        23, 9, 51);       
    cal.set(Calendar.MILLISECOND, 444);
    dateString = DateTools.dateToString(cal.getTime(), DateTools.Resolution.MILLISECOND);
    assertEquals("19610305230951444", dateString);
    assertEquals("1961-03-05 23:09:51:444", isoFormat(DateTools.stringToDate(dateString)));
    dateString = DateTools.dateToString(cal.getTime(), DateTools.Resolution.HOUR);
    assertEquals("1961030523", dateString);
    assertEquals("1961-03-05 23:00:00:000", isoFormat(DateTools.stringToDate(dateString)));
    cal.set(1970, 0, 1, 
        0, 0, 0); 
    cal.set(Calendar.MILLISECOND, 0);
    dateString = DateTools.timeToString(cal.getTime().getTime(),
        DateTools.Resolution.MILLISECOND);
    assertEquals("19700101000000000", dateString);
    cal.set(1970, 0, 1, 
        1, 2, 3); 
    cal.set(Calendar.MILLISECOND, 0);
    dateString = DateTools.timeToString(cal.getTime().getTime(),
        DateTools.Resolution.MILLISECOND);
    assertEquals("19700101010203000", dateString);
  }
  public void testRound() {
    Calendar cal = new GregorianCalendar();
    cal.clear();
    cal.setTimeZone(TimeZone.getTimeZone("GMT"));
    cal.set(2004, 1, 3,   
        22, 8, 56);       
    cal.set(Calendar.MILLISECOND, 333);
    Date date = cal.getTime();
    assertEquals("2004-02-03 22:08:56:333", isoFormat(date));
    Date dateYear = DateTools.round(date, DateTools.Resolution.YEAR);
    assertEquals("2004-01-01 00:00:00:000", isoFormat(dateYear));
    Date dateMonth = DateTools.round(date, DateTools.Resolution.MONTH);
    assertEquals("2004-02-01 00:00:00:000", isoFormat(dateMonth));
    Date dateDay = DateTools.round(date, DateTools.Resolution.DAY);
    assertEquals("2004-02-03 00:00:00:000", isoFormat(dateDay));
    Date dateHour = DateTools.round(date, DateTools.Resolution.HOUR);
    assertEquals("2004-02-03 22:00:00:000", isoFormat(dateHour));
    Date dateMinute = DateTools.round(date, DateTools.Resolution.MINUTE);
    assertEquals("2004-02-03 22:08:00:000", isoFormat(dateMinute));
    Date dateSecond = DateTools.round(date, DateTools.Resolution.SECOND);
    assertEquals("2004-02-03 22:08:56:000", isoFormat(dateSecond));
    Date dateMillisecond = DateTools.round(date, DateTools.Resolution.MILLISECOND);
    assertEquals("2004-02-03 22:08:56:333", isoFormat(dateMillisecond));
    long dateYearLong = DateTools.round(date.getTime(), DateTools.Resolution.YEAR);
    assertEquals("2004-01-01 00:00:00:000", isoFormat(new Date(dateYearLong)));
    long dateMillisecondLong = DateTools.round(date.getTime(), DateTools.Resolution.MILLISECOND);
    assertEquals("2004-02-03 22:08:56:333", isoFormat(new Date(dateMillisecondLong)));
  }
  private String isoFormat(Date date) {
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS", Locale.US);
    sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    return sdf.format(date);
  }
  public void testDateToolsUTC() throws Exception {
    long time = 1130630400;
    try {
        TimeZone.setDefault(TimeZone.getTimeZone( "Europe/London"));
        String d1 = DateTools.dateToString(new Date(time*1000), DateTools.Resolution.MINUTE);
        String d2 = DateTools.dateToString(new Date((time+3600)*1000), DateTools.Resolution.MINUTE);
        assertFalse("different times", d1.equals(d2));
        assertEquals("midnight", DateTools.stringToTime(d1), time*1000);
        assertEquals("later", DateTools.stringToTime(d2), (time+3600)*1000);
    } finally {
        TimeZone.setDefault(null);
    }
  }
}
