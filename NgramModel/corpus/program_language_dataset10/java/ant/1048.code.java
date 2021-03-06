package org.apache.tools.ant.taskdefs.optional;
import org.apache.tools.ant.BuildFileTest;
public class JavahTest extends BuildFileTest {
    private final static String BUILD_XML = 
        "src/etc/testcases/taskdefs/optional/javah/build.xml";
    public JavahTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject(BUILD_XML);
    }
    public void tearDown() {
        executeTarget("tearDown");
    }
    public void testSimpleCompile() {
        executeTarget("simple-compile");
        assertTrue(getProject().resolveFile("output/org_example_Foo.h")
                   .exists());
    }
    public void testCompileFileset() {
        executeTarget("test-fileset");
        assertTrue(getProject().resolveFile("output/org_example_Foo.h")
                   .exists());
    }
}
