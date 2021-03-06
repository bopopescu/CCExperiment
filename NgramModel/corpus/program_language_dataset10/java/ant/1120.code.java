package org.apache.tools.ant.types.resources;
import org.apache.tools.ant.BuildFileTest;
public class JavaResourceTest extends BuildFileTest {
    public JavaResourceTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/types/resources/javaresource.xml");
    }
    public void testLoadManifest() {
        executeTarget("loadManifest");
        assertNotNull(getProject().getProperty("manifest"));
        assertTrue(getProject().getProperty("manifest")
                   .startsWith("Manifest-Version:"));
    }
    public void testIsURLProvider() {
        JavaResource r = new JavaResource();
        assertSame(r, r.as(URLProvider.class));
    }
    public void testGetURLOfManifest() {
        JavaResource r = new JavaResource();
        r.setName("META-INF/MANIFEST.MF");
        assertNotNull(r.getURL());
    }
}
