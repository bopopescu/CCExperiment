package org.apache.maven.plugin.version;
import junit.framework.TestCase;
import java.util.Calendar;
import java.util.Date;
public class IntervalUtilsTest
    extends TestCase
{
    private static final long ONE_MINUTE = 60 * 1000;
    private static final long ONE_HOUR = 60 * ONE_MINUTE;
    private static final long ONE_DAY = 24 * ONE_HOUR;
    private static final long ONE_WEEK = 7 * ONE_DAY;
    public void testOneWeek()
    {
        assertEquals( ONE_WEEK, IntervalUtils.parseInterval( "1w" ) );
    }
    public void testTwoWeeks()
    {
        assertEquals( ( 2 * ONE_WEEK ), IntervalUtils.parseInterval( "2w" ) );
    }
    public void testOneDay()
    {
        assertEquals( ONE_DAY, IntervalUtils.parseInterval( "1d" ) );
    }
    public void testOneHour()
    {
        assertEquals( ONE_HOUR, IntervalUtils.parseInterval( "1h" ) );
    }
    public void testOneMinute()
    {
        assertEquals( ONE_MINUTE, IntervalUtils.parseInterval( "1m" ) );
    }
    public void testTwoDaysThreeHoursAndOneMinute()
    {
        assertEquals( 2 * ONE_DAY + 3 * ONE_HOUR + ONE_MINUTE, IntervalUtils.parseInterval( "2d 3h 1m" ) );
    }
    public void testTwoDaysThreeHoursAndOneMinuteCondensed()
    {
        assertEquals( 2 * ONE_DAY + 3 * ONE_HOUR + ONE_MINUTE, IntervalUtils.parseInterval( "2d3h1m" ) );
    }
    public void testTwoDaysThreeHoursAndOneMinuteCommaSeparated()
    {
        assertEquals( 2 * ONE_DAY + 3 * ONE_HOUR + ONE_MINUTE, IntervalUtils.parseInterval( "2d,3h,1m" ) );
    }
    public void testTwoDaysThreeHoursAndOneMinuteRearranged()
    {
        assertEquals( 2 * ONE_DAY + 3 * ONE_HOUR + ONE_MINUTE, IntervalUtils.parseInterval( "1m 2d 3h" ) );
    }
    public void testThreeDaysPoorlySpecified()
    {
        assertEquals( 3 * ONE_DAY, IntervalUtils.parseInterval( "1d 2d" ) );
    }
    public void testNeverInterval()
    {
        assertFalse( IntervalUtils.isExpired( "never", null ) );
    }
    public void testAlwaysInterval()
    {
        assertTrue( IntervalUtils.isExpired( "always", null ) );
    }
    public void testOneMinuteIntervalShouldBeExpired()
    {
        Calendar cal = Calendar.getInstance();
        cal.add( Calendar.MINUTE, -2 );
        Date lastChecked = cal.getTime();
        assertTrue( IntervalUtils.isExpired( "interval:1m", lastChecked ) );
    }
}
