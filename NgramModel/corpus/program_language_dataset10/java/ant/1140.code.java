package org.apache.tools.ant.util;
import java.io.IOException;
import java.util.Enumeration;
import junit.framework.TestCase;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
public class ClasspathUtilsTest extends TestCase {
    private Project p;
    public ClasspathUtilsTest(String name) {
        super(name);
    }
    public void setUp() {
        p = new Project();
        p.init();
    }
    public void testOnlyOneInstance() {
        Enumeration enumeration;
        String list = "";
        ClassLoader c = ClasspathUtils.getUniqueClassLoaderForPath(p, (Path) null, false);
        try {
            enumeration = c.getResources(
                "org/apache/tools/ant/taskdefs/defaults.properties");
        } catch (IOException e) {
            throw new BuildException(
                "Could not get the defaults.properties resource");
        }
        int count = 0;
        while (enumeration.hasMoreElements()) {
            list = list + " " + enumeration.nextElement();
            count++;
        }
        assertTrue("Should be only one and not " + count + " " + list, count == 1);
    }
}
