package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
public class ConcatTest
    extends BuildFileTest {
    private static final String tempFile = "concat.tmp";
    private static final String tempFile2 = "concat.tmp.2";
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    public ConcatTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/concat.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void test1() {
        expectBuildException("test1", "Insufficient information.");
    }
    public void test2() {
        expectBuildException("test2", "Invalid destination file.");
    }
    public void test3() {
        File file = new File(getProjectDir(), tempFile);
        if (file.exists()) {
            file.delete();
        }
        executeTarget("test3");
        assertTrue(file.exists());
    }
    public void test4() {
        test3();
        File file = new File(getProjectDir(), tempFile);
        final long origSize = file.length();
        executeTarget("test4");
        File file2 = new File(getProjectDir(), tempFile2);
        final long newSize = file2.length();
        assertEquals(origSize * 3, newSize);
    }
    public void test5() {
        expectLog("test5", "Hello, World!");
    }
    public void test6() {
        String filename = "src/etc/testcases/taskdefs/thisfiledoesnotexist"
            .replace('/', File.separatorChar);
        expectLogContaining("test6", filename +" does not exist.");
    }
    public void testConcatNoNewline() {
        expectLog("testConcatNoNewline", "ab");
    }
    public void testConcatNoNewlineEncoding() {
        expectLog("testConcatNoNewlineEncoding", "ab");
    }
    public void testPath() {
        test3();
        File file = new File(getProjectDir(), tempFile);
        final long origSize = file.length();
        executeTarget("testPath");
        File file2 = new File(getProjectDir(), tempFile2);
        final long newSize = file2.length();
        assertEquals(origSize, newSize);
    }
    public void testAppend() {
        test3();
        File file = new File(getProjectDir(), tempFile);
        final long origSize = file.length();
        executeTarget("testAppend");
        File file2 = new File(getProjectDir(), tempFile2);
        final long newSize = file2.length();
        assertEquals(origSize*2, newSize);
    }
    public void testFilter() {
        executeTarget("testfilter");
        assertTrue(getLog().indexOf("REPLACED") > -1);
    }
    public void testNoOverwrite() {
        executeTarget("testnooverwrite");
        File file2 = new File(getProjectDir(), tempFile2);
        long size = file2.length();
        assertEquals(size, 0);
    }
    public void testOverwrite() {
        executeTarget("testoverwrite");
        File file2 = new File(getProjectDir(), tempFile2);
        long size = file2.length();
        assertTrue(size > 0);
    }
    public void testheaderfooter() {
        test3();
        expectLog("testheaderfooter", "headerHello, World!footer");
    }
    public void testfileheader() {
        test3();
        expectLog("testfileheader", "Hello, World!Hello, World!");
    }
    public void testsame() {
        expectBuildException("samefile", "output file same as input");
    }
    public void testfilterinline() {
        executeTarget("testfilterinline");
        assertTrue(getLog().indexOf("REPLACED") > -1);
    }
    public void testmultireader() {
        executeTarget("testmultireader");
        assertTrue(getLog().indexOf("Bye") > -1);
        assertTrue(getLog().indexOf("Hello") == -1);
    }
    public void testfixlastline()
        throws IOException
    {
        expectFileContains(
            "testfixlastline", "concat.line4",
            "end of line" + System.getProperty("line.separator")
            + "This has");
    }
    public void testfixlastlineeol()
        throws IOException
    {
        expectFileContains(
            "testfixlastlineeol", "concat.linecr",
            "end of line\rThis has");
    }
    private String getFileString(String filename)
        throws IOException
    {
        Reader r = null;
        try {
            r = new FileReader(getProject().resolveFile(filename));
            return  FileUtils.readFully(r);
        }
        finally {
            FileUtils.close(r);
        }
    }
    private String getFileString(String target, String filename)
        throws IOException
    {
        executeTarget(target);
        return getFileString(filename);
    }
    private void expectFileContains(
        String target, String filename, String contains)
        throws IOException
    {
        String content = getFileString(target, filename);
        assertTrue(
            "expecting file " + filename + " to contain " +
            contains +
            " but got " + content, content.indexOf(contains) > -1);
    }
    public void testTranscoding() throws IOException {
        executeTarget("testTranscoding");
        File f1 = getProject().resolveFile("copy/expected/utf-8");
        File f2 = getProject().resolveFile("concat.utf8");
        assertTrue(f1.toString() + " differs from " + f2.toString(),
                FILE_UTILS.contentEquals(f1, f2));
    }
}
