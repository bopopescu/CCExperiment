package org.apache.batik.bridge;
import org.apache.batik.test.*;
import org.apache.batik.test.svg.SVGOnLoadExceptionTest;
public class JarNoLoadTest extends DefaultTestSuite {
    public JarNoLoadTest() {
        String scripts = "text/ecmascript";
        String[] scriptSource = {"bridge/jarCheckNoLoadAny",
                                 "bridge/jarCheckNoLoadSameAsDocument",
                                 "bridge/jarCheckNoLoadEmbed",
        };
        boolean[] secure = {true, false};
        String[] scriptOrigin = {"ANY", "DOCUMENT", "EMBEDED", "NONE"};
        for (int i=0; i<scriptSource.length; i++) {
            for (int j=0; j<secure.length; j++) {
                for (int k=0; k<scriptOrigin.length; k++) {
                    SVGOnLoadExceptionTest t = buildTest(scripts,
                                                         scriptSource[i],
                                                         scriptOrigin[k],
                                                         secure[j]);
                    addTest(t);
                }
            }
        }
        scripts = "application/java-archive";
        for (int j=0; j<scriptOrigin.length; j++) {
            for (int i=0; i<j; i++) {
                for (int k=0; k<secure.length; k++) {
                    SVGOnLoadExceptionTest t= buildTest(scripts, scriptSource[i],
                                                        scriptOrigin[j],
                                                        secure[k]);
                    addTest(t);
                }
            }
        }
    }
    SVGOnLoadExceptionTest buildTest(String scripts, String id, String origin, boolean secure) {
        SVGOnLoadExceptionTest t = new SVGOnLoadExceptionTest();
        String desc = 
            "(scripts=" + scripts + 
            ")(scriptOrigin=" + origin +
            ")(secure=" + secure + ")";
        t.setId(id + desc);
        t.setScriptOrigin(origin);
        t.setSecure(secure);
        t.setScripts(scripts);
        t.setExpectedExceptionClass("java.lang.SecurityException");
        return t;
    }
}
