package org.apache.batik.bridge;
import org.apache.batik.test.*;
import org.apache.batik.test.svg.SVGOnLoadExceptionTest;
public class EcmaNoLoadTest extends DefaultTestSuite {
    public EcmaNoLoadTest() {
        String scripts = "application/java-archive";
        String[] scriptSource = {"bridge/ecmaCheckNoLoadAny",
                                 "bridge/ecmaCheckNoLoadSameAsDocument",
                                 "bridge/ecmaCheckNoLoadEmbed",
                                 "bridge/ecmaCheckNoLoadEmbedAttr",
        };
        boolean[] secure = {true, false};
        String[] scriptOrigin = {"ANY", "DOCUMENT", "EMBEDED", "NONE"};
        for (int i=0; i<scriptSource.length; i++) {
            for (int j=0; j<secure.length; j++) {
                for (int k=0; k<scriptOrigin.length; k++) {
                    SVGOnLoadExceptionTest t = buildTest(scripts,
                                                         scriptSource[i],
                                                         scriptOrigin[k],
                                                         secure[j],
                                                         false,
                                                         false);
                    addTest(t);
                }
            }
        }
        scripts = "text/ecmascript";
        for (int i=0; i<scriptSource.length; i++) {
            for (int k=0; k<scriptOrigin.length; k++) {
                boolean expectSuccess = ((i>=2) && (k <= 2));
                SVGOnLoadExceptionTest t = buildTest(scripts,
                                                     scriptSource[i],
                                                     scriptOrigin[k],
                                                     true,
                                                     true,
                                                     expectSuccess);
                addTest(t);
            }
        }
        for (int j=0; j<scriptOrigin.length; j++) {
            int max = j;
            if (j == scriptOrigin.length - 1) {
                max = j+1;
            }
            for (int i=0; i<max; i++) {
                for (int k=0; k<secure.length; k++) {
                    SVGOnLoadExceptionTest t= buildTest(scripts, scriptSource[i],
                                                        scriptOrigin[j],
                                                        secure[k],
                                                        false,
                                                        false);
                    addTest(t);
                }
            }
        }
    }
    SVGOnLoadExceptionTest buildTest(String scripts, String id, String origin, 
                                     boolean secure, boolean restricted, 
                                     boolean successExpected) {
        SVGOnLoadExceptionTest t = new SVGOnLoadExceptionTest();
        String desc = 
            "(scripts=" + scripts + 
            ")(scriptOrigin=" + origin +
            ")(secure=" + secure +
            ")(restricted=" + restricted + ")";
        t.setId(id + desc);
        t.setScriptOrigin(origin);
        t.setSecure(secure);
        t.setScripts(scripts);
        if (successExpected)
            t.setExpectedExceptionClass(null);
        else
            t.setExpectedExceptionClass("java.lang.SecurityException");
        t.setRestricted(restricted);
        return t;
    }
}
