package org.apache.batik.util;
import org.apache.batik.test.AbstractTest;
import org.apache.batik.test.DefaultTestReport;
import org.apache.batik.test.TestReport;
import java.io.StringWriter;
import java.io.PrintWriter;
public class ParsedURLTest extends AbstractTest {
    public static final String ERROR_CANNOT_PARSE_URL
        = "ParsedURLTest.error.cannot.parse.url";
    public static final String ERROR_WRONG_RESULT
        = "ParsedURLTest.error.wrong.result";
    public static final String ENTRY_KEY_ERROR_DESCRIPTION
        = "ParsedURLTest.entry.key.error.description";
    protected String base = null;
    protected String sub  = null;
    protected String ref  = null;
    public ParsedURLTest(String url, String ref ){
        this.base = url;
        this.ref  = ref;
    }
    public ParsedURLTest(String base, String sub, String ref){
        this.base = base;
        this.sub  = sub;
        this.ref  = ref;
    }
    public String getName() {
        return ref + " -- " + super.getName();
    }
    public TestReport runImpl() throws Exception {
        DefaultTestReport report
            = new DefaultTestReport(this);
        ParsedURL url;
        try {
            url = new ParsedURL(base);
            if (sub != null) {
                url  = new ParsedURL(url, sub);
            }
        } catch(Exception e) {
            StringWriter trace = new StringWriter();
            e.printStackTrace(new PrintWriter(trace));
            report.setErrorCode(ERROR_CANNOT_PARSE_URL);
            report.setDescription(new TestReport.Entry[] {
                new TestReport.Entry
                    (TestMessages.formatMessage
                     (ENTRY_KEY_ERROR_DESCRIPTION, null),
                     TestMessages.formatMessage
                     (ERROR_CANNOT_PARSE_URL,
                      new String[]{base, 
                                   (sub == null) ? "null" : sub,
                                   trace.toString()}))
                    });
            report.setPassed(false);
            return report;
        }
        if (ref.equals(url.toString())) {
            report.setPassed(true);
            return report;
        }
        report.setErrorCode(ERROR_WRONG_RESULT);
        report.setDescription(new TestReport.Entry[] {
          new TestReport.Entry
            (TestMessages.formatMessage(ENTRY_KEY_ERROR_DESCRIPTION, null),
             TestMessages.formatMessage
             (ERROR_WRONG_RESULT, new String[]{url.toString(), ref }))
            });
        report.setPassed(false);
        return report;
    }
}