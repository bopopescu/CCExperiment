package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
import java.io.File;
public class TouchTest extends BuildFileTest {
    private static String TOUCH_FILE = "src/etc/testcases/taskdefs/touchtest";
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    public TouchTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/touch.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public long getTargetTime() {
        File file = new File(System.getProperty("root"), TOUCH_FILE);
        if(!file.exists()) {
            throw new BuildException("failed to touch file " + file);
        }
        return file.lastModified();
    }
    public void testNoSeconds() {
        executeTarget("noSeconds");
        long time = getTargetTime();
    }
    public void testSeconds() {
        executeTarget("seconds");
        long time=getTargetTime();
    }
    public void testMillis() {
        touchFile("testMillis", 662256000000L);
    }
    public void testNow() {
        long now=System.currentTimeMillis();
        executeTarget("testNow");
        long time = getTargetTime();
        assertTimesNearlyMatch(time,now,5000);
    }
    public void test2000() {
        touchFile("test2000", 946080000000L);
    }
    public void testFilelist() {
        touchFile("testFilelist", 662256000000L);
    }
    public void testFileset() {
        touchFile("testFileset", 946080000000L);
    }
    public void testResourceCollection() {
        touchFile("testResourceCollection", 1662256000000L);
    }
    public void testMappedFileset() {
        executeTarget("testMappedFileset");
    }
    public void testExplicitMappedFileset() {
        executeTarget("testExplicitMappedFileset");
    }
    public void testMappedFilelist() {
        executeTarget("testMappedFilelist");
    }
    public void testGoodPattern() {
        executeTarget("testGoodPattern");
    }
    public void testBadPattern() {
        expectBuildExceptionContaining("testBadPattern",
            "No parsing exception thrown", "Unparseable");
    }
    private void touchFile(String targetName, long timestamp) {
        executeTarget(targetName);
        long time = getTargetTime();
        assertTimesNearlyMatch(timestamp, time);
    }
    public void assertTimesNearlyMatch(long timestamp,long time) {
        long granularity= FILE_UTILS.getFileTimestampGranularity();
        assertTimesNearlyMatch(timestamp, time, granularity);
    }
    private void assertTimesNearlyMatch(long timestamp, long time, long range) {
        assertTrue("Time " + timestamp + " is not within " + range + " ms of "
            + time, (Math.abs(time - timestamp) <= range));
    }
}
