package org.apache.tools.ant;
public class PropertyExpansionTest extends BuildFileTest {
    public PropertyExpansionTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/core/immutable.xml");
    }
    public void testPropertyExpansion() {
        assertExpandsTo("","");
        assertExpandsTo("$","$");
        assertExpandsTo("$$-","$-");
        assertExpandsTo("$$","$");
        project.setProperty("expanded","EXPANDED");
        assertExpandsTo("a${expanded}b","aEXPANDEDb");
        assertExpandsTo("${expanded}${expanded}","EXPANDEDEXPANDED");
        assertExpandsTo("$$$","$$");
        assertExpandsTo("$$$$-","$$-");
        assertExpandsTo("","");
        assertExpandsTo("Class$$subclass","Class$subclass");
    }
    public void testDollarPassthru() {
        assertExpandsTo("$-","$-");
        assertExpandsTo("Class$subclass","Class$subclass");
        assertExpandsTo("$$$-","$$-");
        assertExpandsTo("$$$$$","$$$");
        assertExpandsTo("${unassigned.property}","${unassigned.property}");
        assertExpandsTo("a$b","a$b");
        assertExpandsTo("$}}","$}}");
    }
    public void oldtestQuirkyLegacyBehavior() {
        assertExpandsTo("Class$subclass","Classsubclass");
        assertExpandsTo("$$$-","$-");
        assertExpandsTo("a$b","ab");
        assertExpandsTo("$}}","}}");
    }
    private void assertExpandsTo(String source,String expected) {
        String actual=project.replaceProperties(source);
        assertEquals(source,expected,actual);
    }
}
