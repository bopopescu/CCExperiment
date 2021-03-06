package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;
public class AntLikeTasksAtTopLevelTest extends BuildFileTest {
    public AntLikeTasksAtTopLevelTest(String name) {
        super(name);
    }
    public void testAnt() {
        try {
            configureProject("src/etc/testcases/taskdefs/toplevelant.xml");
            fail("no exception thrown");
        } catch (BuildException e) {
            assertEquals("ant task at the top level must not invoke its own"
                         + " build file.", e.getMessage());
        }
    }
    public void testSubant() {
        try {
            configureProject("src/etc/testcases/taskdefs/toplevelsubant.xml");
            fail("no exception thrown");
        } catch (BuildException e) {
            assertEquals("subant task at the top level must not invoke its own"
                         + " build file.", e.getMessage());
        }
    }
    public void testAntcall() {
        try {
            configureProject("src/etc/testcases/taskdefs/toplevelantcall.xml");
            fail("no exception thrown");
        } catch (BuildException e) {
            assertEquals("antcall must not be used at the top level.",
                         e.getMessage());
        }
    }
}
