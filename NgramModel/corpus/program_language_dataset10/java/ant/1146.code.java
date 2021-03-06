package org.apache.tools.ant.util;
import java.io.File;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.apache.tools.ant.taskdefs.condition.Os;
public class JavaEnvUtilsTest extends TestCase {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    public JavaEnvUtilsTest(String s) {
        super(s);
    }
    public void testGetExecutableNetware() {
        if (Os.isName("netware")) {
            assertEquals("java", JavaEnvUtils.getJreExecutable("java"));
            assertEquals("javac", JavaEnvUtils.getJdkExecutable("javac"));
            assertEquals("foo", JavaEnvUtils.getJreExecutable("foo"));
            assertEquals("foo", JavaEnvUtils.getJdkExecutable("foo"));
        }
    }
    public void testGetExecutableWindows() {
        if (Os.isFamily("windows")) {
            String javaHome =
                FILE_UTILS.normalize(System.getProperty("java.home"))
                .getAbsolutePath();
            String j = JavaEnvUtils.getJreExecutable("java");
            assertTrue(j.endsWith(".exe"));
            assertTrue(j+" is absolute", (new File(j)).isAbsolute());
            try {
                assertTrue(j+" is normalized and in the JRE dir",
                           j.startsWith(javaHome));
            } catch (AssertionFailedError e) {
                assertEquals("java.exe", j);
            }
            j = JavaEnvUtils.getJdkExecutable("javac");
            assertTrue(j.endsWith(".exe"));
            try {
                assertTrue(j+" is absolute", (new File(j)).isAbsolute());
                String javaHomeParent =
                    FILE_UTILS.normalize(javaHome+"/..").getAbsolutePath();
                assertTrue(j+" is normalized and in the JDK dir",
                           j.startsWith(javaHomeParent));
                assertTrue(j+" is normalized and not in the JRE dir",
                           !j.startsWith(javaHome));
            } catch (AssertionFailedError e) {
                assertEquals("javac.exe", j);
            }
            assertEquals("foo.exe", JavaEnvUtils.getJreExecutable("foo"));
            assertEquals("foo.exe", JavaEnvUtils.getJdkExecutable("foo"));
        }
    }
    public void testGetExecutableMostPlatforms() {
        if (!Os.isName("netware") && !Os.isFamily("windows")) {
            String javaHome =
                FILE_UTILS.normalize(System.getProperty("java.home"))
                .getAbsolutePath();
            String extension = Os.isFamily("dos") ? ".exe" : "";
            String j = JavaEnvUtils.getJreExecutable("java");
            if (!extension.equals("")) {
                assertTrue(j.endsWith(extension));
            }
            assertTrue(j+" is absolute", (new File(j)).isAbsolute());
            assertTrue(j+" is normalized and in the JRE dir",
                       j.startsWith(javaHome));
            j = JavaEnvUtils.getJdkExecutable("javac");
            if (!extension.equals("")) {
                assertTrue(j.endsWith(extension));
            }
            assertTrue(j+" is absolute", (new File(j)).isAbsolute());
            String javaHomeParent =
                FILE_UTILS.normalize(javaHome+"/..").getAbsolutePath();
            assertTrue(j+" is normalized and in the JDK dir",
                       j.startsWith(javaHomeParent));
            if (Os.isFamily("mac")) {
                assertTrue(j+" is normalized and in the JRE dir",
                           j.startsWith(javaHome));
            } else {
                assertTrue(j+" is normalized and not in the JRE dir",
                           !j.startsWith(javaHome));
            }
            assertEquals("foo"+extension,
                         JavaEnvUtils.getJreExecutable("foo"));
            assertEquals("foo"+extension,
                         JavaEnvUtils.getJdkExecutable("foo"));
        }
    }
    public void testIsAtLeastJavaVersion()
    {
        assertTrue(
                "Current java version is not at least the current java version...",
                JavaEnvUtils.isAtLeastJavaVersion(JavaEnvUtils.getJavaVersion()));
        assertFalse(
                "In case the current java version is higher than 9.0 definitely a new algorithem will be needed",
                JavaEnvUtils.isAtLeastJavaVersion("9.0"));
    }
}
