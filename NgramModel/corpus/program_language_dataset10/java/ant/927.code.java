package org.apache.tools.ant.filters;
import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.StringUtils;
public class ConcatFilterTest extends BuildFileTest {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private static final String lSep = StringUtils.LINE_SEP;
    private static final String FILE_PREPEND_WITH =
          "this-should-be-the-first-line" + lSep
        + "Line  1" + lSep
        + "Line  2" + lSep
        + "Line  3" + lSep
        + "Line  4" + lSep
    ;
    private static final String FILE_PREPEND =
          "Line  1" + lSep
        + "Line  2" + lSep
        + "Line  3" + lSep
        + "Line  4" + lSep
        + "Line  5" + lSep
    ;
    private static final String FILE_APPEND_WITH =
          "Line 57" + lSep
        + "Line 58" + lSep
        + "Line 59" + lSep
        + "Line 60" + lSep
        + "this-should-be-the-last-line" + lSep
    ;
    private static final String FILE_APPEND =
          "Line 56" + lSep
        + "Line 57" + lSep
        + "Line 58" + lSep
        + "Line 59" + lSep
        + "Line 60" + lSep
    ;
    public ConcatFilterTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/filters/concat.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void testFilterReaderNoArgs() throws IOException {
        executeTarget("testFilterReaderNoArgs");
        File expected = FILE_UTILS.resolveFile(getProject().getBaseDir(),"input/concatfilter.test");
        File result = FILE_UTILS.resolveFile(getProject().getBaseDir(), "result/concat.FilterReaderNoArgs.test");
        assertTrue("testFilterReaderNoArgs: Result not like expected", FILE_UTILS.contentEquals(expected, result));
    }
    public void testFilterReaderBefore() {
        doTest("testFilterReaderPrepend", FILE_PREPEND_WITH, FILE_APPEND);
    }
    public void testFilterReaderAfter() {
        doTest("testFilterReaderAppend", FILE_PREPEND, FILE_APPEND_WITH);
    }
    public void testFilterReaderBeforeAfter() {
        doTest("testFilterReaderPrependAppend", FILE_PREPEND_WITH, FILE_APPEND_WITH);
    }
    public void testConcatFilter() {
        doTest("testConcatFilter", FILE_PREPEND, FILE_APPEND);
    }
    public void testConcatFilterBefore() {
        doTest("testConcatFilterPrepend", FILE_PREPEND_WITH, FILE_APPEND);
    }
    public void testConcatFilterAfter() {
        doTest("testConcatFilterAppend", FILE_PREPEND, FILE_APPEND_WITH);
    }
    public void testConcatFilterBeforeAfter() {
        doTest("testConcatFilterPrependAppend", FILE_PREPEND_WITH, FILE_APPEND_WITH);
    }
    protected void doTest(String target, String expectedStart, String expectedEnd) {
        executeTarget(target);
        String resultContent = read("result/concat." + target.substring(4) + ".test");
        assertTrue("First 5 lines differs.", resultContent.startsWith(expectedStart));
        assertTrue("Last 5 lines differs.", resultContent.endsWith(expectedEnd));
    }
    protected String read(String filename) {
        String content = null;
        try {
            File file = FILE_UTILS.resolveFile(getProject().getBaseDir(), filename);
            java.io.FileReader rdr = new java.io.FileReader(file);
            content = FileUtils.readFully(rdr);
            rdr.close();
            rdr = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content;
    }
}
