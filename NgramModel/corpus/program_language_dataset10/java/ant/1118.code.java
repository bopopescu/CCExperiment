package org.apache.tools.ant.types.optional;
import org.apache.tools.ant.BuildFileTest;
public class ScriptSelectorTest extends BuildFileTest {
    public ScriptSelectorTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/types/selectors/scriptselector.xml");
    }
    public void testNolanguage() {
        expectBuildExceptionContaining("testNolanguage",
                "Absence of language attribute not detected",
                "script language must be specified");
    }
    public void testSelectionSetByDefault() {
        executeTarget("testSelectionSetByDefault");
    }
    public void testSelectionSetWorks() {
        executeTarget("testSelectionSetWorks");
    }
    public void testSelectionClearWorks() {
        executeTarget("testSelectionClearWorks");
    }
    public void testFilenameAttribute() {
        executeTarget("testFilenameAttribute");
    }
    public void testFileAttribute() {
        executeTarget("testFileAttribute");
    }
    public void testBasedirAttribute() {
        executeTarget("testBasedirAttribute");
    }
}
