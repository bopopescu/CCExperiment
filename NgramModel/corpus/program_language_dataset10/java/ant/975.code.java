package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;
public class InitializeClassTest extends BuildFileTest {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private File f1 = new File(System.getProperty("root"), "src/etc/testcases/taskdefs/forkedout");
    private File f2 = new File(System.getProperty("root"), "src/etc/testcases/taskdefs/unforkedout");
    public InitializeClassTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/initializeclass.xml");
    }
    public void testAll() throws IOException {
        executeTarget("forked");
        PrintStream ps = System.out;
        PrintStream newps = new PrintStream(new FileOutputStream(f2));
        System.setOut(newps);
        project.executeTarget("unforked");
        System.setOut(ps);
        newps.close();
        assertTrue("Forked - non-forked mismatch", FILE_UTILS.contentEquals(f1, f2));
    }
    public void tearDown() {
        f1.delete();
        f2.delete();
    }
}
