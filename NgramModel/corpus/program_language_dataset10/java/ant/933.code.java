package org.apache.tools.ant.filters;
import java.io.File;
import java.io.IOException;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;
public class ReplaceTokensTest extends BuildFileTest {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    public ReplaceTokensTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/filters/build.xml");
    }
    public void tearDown() {
        executeTarget("cleanup");
    }
    public void testReplaceTokens() throws IOException {
        executeTarget("testReplaceTokens");
        File expected = FILE_UTILS.resolveFile(getProject().getBaseDir(),"expected/replacetokens.test");
        File result = FILE_UTILS.resolveFile(getProject().getBaseDir(),"result/replacetokens.test");
        assertTrue(FILE_UTILS.contentEquals(expected, result));
    }
    public void testReplaceTokensPropertyFile() throws IOException {
        executeTarget("testReplaceTokensPropertyFile");
        File expected = FILE_UTILS.resolveFile(getProjectDir(), "expected/replacetokens.test");
        File result = FILE_UTILS.resolveFile(getProjectDir(), "result/replacetokensPropertyFile.test");
        assertTrue(FILE_UTILS.contentEquals(expected, result));
    }
}
