package org.apache.tools.ant.taskdefs.condition;
import org.apache.tools.ant.BuildFileTest;
public class HttpTest extends BuildFileTest {
    public HttpTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/conditions/http.xml");
    }
    public void testNoMethod() {
       expectPropertySet("basic-no-method", "basic-no-method");
       assertPropertyUnset("basic-no-method-bad-url");
    }
    public void testHeadRequest() {
       expectPropertySet("test-head-request", "test-head-request");
       assertPropertyUnset("test-head-request-bad-url");
    }
    public void testGetRequest() {
       expectPropertySet("test-get-request", "test-get-request");
       assertPropertyUnset("test-get-request-bad-url");
    }
    public void testBadRequestMethod() {
        expectSpecificBuildException("bad-request-method",
                                     "invalid HTTP request method specified",
                                     null);
    }
}
