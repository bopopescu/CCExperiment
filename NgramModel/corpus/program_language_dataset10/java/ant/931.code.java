package org.apache.tools.ant.filters;
import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;
public class LineContainsTest extends BuildFileTest {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    public LineContainsTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/filters/build.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void testLineContains() throws IOException {
        executeTarget("testLineContains");
        File expected = FILE_UTILS.resolveFile(getProject().getBaseDir(),"expected/linecontains.test");
        File result = FILE_UTILS.resolveFile(getProject().getBaseDir(),"result/linecontains.test");
        assertTrue(FILE_UTILS.contentEquals(expected, result));
    }
    public void testNegateLineContains() throws IOException {
        executeTarget("testNegateLineContains");
    }
}
