package org.apache.tools.ant.launch;
import junit.framework.TestCase;
import java.io.File;
import org.apache.tools.ant.taskdefs.condition.Os;
public class LocatorTest extends TestCase {
    private boolean windows;
    private boolean unix;
    private static final String LAUNCHER_JAR = "//morzine/slo/Java/Apache/ant/lib/ant-launcher.jar";
    private static final String SHARED_JAR_URI = "jar:file:"+ LAUNCHER_JAR +"!/org/apache/tools/ant/launch/Launcher.class";
    public LocatorTest() {
    }
    public LocatorTest(String name) {
        super(name);
    }
    protected void setUp() throws Exception {
        super.setUp();
        windows = Os.isFamily(Os.FAMILY_DOS);
        unix = Os.isFamily(Os.FAMILY_UNIX);
    }
    private String resolveTo(String uri, String expectedUnix, String expectedDos) {
        String result = Locator.fromURI(uri);
        assertResolved(uri, expectedUnix, result, unix);
        assertResolved(uri, expectedDos, result, windows);
        return result;
    }
    private void assertResolved(String uri, String expectedResult, String result, boolean enabled) {
        if (enabled && expectedResult != null && expectedResult.length() > 0) {
            assertEquals("Expected " + uri + " to resolve to \n" + expectedResult + "\n but got\n"
                    + result + "\n", expectedResult, result);
        }
    }
    private String assertResolves(String path) {
        String asuri = new File(path).toURI().toASCIIString();
        String fullpath = System.getProperty("user.dir") + File.separator + path;
        String result = resolveTo(asuri, fullpath, fullpath);
        return result.substring(result.lastIndexOf(File.separatorChar) + 1);
    }
    public void testNetworkURI() throws Exception {
        resolveTo("file:\\\\PC03\\jclasses\\lib\\ant-1.7.0.jar", ""
                + "\\\\PC03\\jclasses\\lib\\ant-1.7.0.jar",
                "\\\\PC03\\jclasses\\lib\\ant-1.7.0.jar");
    }
    public void NotestTripleForwardSlashNetworkURI() throws Exception {
        resolveTo("file:///PC03/jclasses/lib/ant-1.7.0.jar",
                "///PC03/jclasses/lib/ant-1.7.0.jar",
                "\\\\PC03\\jclasses\\lib\\ant-1.7.0.jar");
    }
    public void testUnixNetworkPath() throws Exception {
        resolveTo("file://cluster/home/ant/lib",
                "//cluster/home/ant/lib",
                "\\\\cluster\\home\\ant\\lib");
    }
    public void testUnixPath() throws Exception {
        resolveTo("file:/home/ant/lib", "/home/ant/lib", null);
    }
    public void testSpacedURI() throws Exception {
        resolveTo("file:C:\\Program Files\\Ant\\lib",
                "C:\\Program Files\\Ant\\lib",
                "C:\\Program Files\\Ant\\lib");
    }
    public void testAntOnRemoteShare() throws Throwable {
        String resolved=Locator.fromJarURI(SHARED_JAR_URI);
        assertResolved(SHARED_JAR_URI, LAUNCHER_JAR, resolved, unix);
        assertResolved(SHARED_JAR_URI, LAUNCHER_JAR.replace('/', '\\'),
                       resolved, windows);
    }
    public void testFileFromRemoteShare() throws Throwable {
        String resolved = Locator.fromJarURI(SHARED_JAR_URI);
        File f = new File(resolved);
        String path = f.getAbsolutePath();
        if (windows) {
            assertEquals(0, path.indexOf("\\\\"));
        }
    }
    public void testHttpURI() throws Exception {
        String url = "http://ant.apache.org";
        try {
            Locator.fromURI(url);
        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            assertTrue(message, message.indexOf(Locator.ERROR_NOT_FILE_URI) >= 0);
            assertTrue(message, message.indexOf(url) >= 0);
        }
    }
    public void testInternationalURI() throws Exception {
        String result = assertResolves("L\u00f6wenbrau.aus.M\u00fcnchen");
        char umlauted = result.charAt(1);
        assertEquals("expected 0xf6 (\u00f6), but got " + Integer.toHexString(umlauted) + " '"
                + umlauted + "'", 0xf6, umlauted);
        assertEquals("file:/tmp/a%C3%A7a%C3%AD%20berry", Locator.encodeURI("file:/tmp/a\u00E7a\u00ED berry"));
        assertEquals("file:/tmp/a\u00E7a\u00ED berry", Locator.decodeUri("file:/tmp/a%C3%A7a%C3%AD%20berry"));
        assertEquals("file:/tmp/a\u00E7a\u00ED berry", Locator.decodeUri("file:/tmp/a\u00E7a\u00ED%20berry")); 
        assertEquals("file:/tmp/hezky \u010Desky", Locator.decodeUri("file:/tmp/hezky%20\u010Desky")); 
    }
    public void testOddLowAsciiURI() throws Exception {
        assertResolves("hash# and percent%");
    }
}
