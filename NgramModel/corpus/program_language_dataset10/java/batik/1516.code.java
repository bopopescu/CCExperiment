package org.apache.batik.gvt;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGTextContentElement;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.JSVGCanvasHandler;
import org.apache.batik.test.DefaultTestReport;
import org.apache.batik.test.TestReport;
import org.apache.batik.test.svg.JSVGRenderingAccuracyTest;
public class TextSelectionTest extends JSVGRenderingAccuracyTest {
    public static final String REFERENCE_DIR
        = "test-references/org/apache/batik/gvt/";
    public static final String VARIATION_DIR
        = "variation/";
    public static final String CANDIDATE_DIR
        = "candidate/";
    public static final String ERROR_READING_SVG
        = "TextSelectionTest.error.reading.svg";
    public static final String ERROR_BAD_ID
        = "TextSelectionTest.error.bad.id";
    public static final String ERROR_ID_NOT_TEXT
        = "TextSelectionTest.error.id.not.text";
    public static final String ERROR_GETTING_SELECTION
        = "TextSelectionTest.error.getting.selection";
    public static final String ERROR_CANNOT_READ_REF_URL
        = "TextSelectionTest.error.cannot.read.ref.url";
    public static final String ERROR_WRONG_RESULT
        = "TextSelectionTest.error.wrong.result";
    public static final String ERROR_NO_REFERENCE
        = "TextSelectionTest.error.no.reference";
    public static final String ENTRY_KEY_ERROR_DESCRIPTION
        = "TextSelectionTest.entry.key.error.description";
    protected String textID    = null;
    protected int    start;
    protected int    end;
    public void setId(String id) { this.id = id; }
    public TextSelectionTest(String file,   String textID,
                             Integer start, Integer end) {
        this.textID    = textID;
        this.start = start.intValue();
        this.end   = end.intValue();
        super.setFile(file);
    }
    protected String buildRefImgURL(String svgDir, String svgFile){
        return getRefImagePrefix() + svgDir + getRefImageSuffix() +
            svgFile + '-' +textID+ '-' + start + '-' + end +PNG_EXTENSION;
    }
    public String buildVariationURL(String svgDir, String svgFile){
        return getVariationPrefix() + svgDir + getVariationSuffix() +
            svgFile + '-' +textID+ '-' + start + '-' + end +PNG_EXTENSION;
    }
    public String  buildSaveVariationFile(String svgDir, String svgFile){
        return getSaveVariationPrefix() + svgDir + getSaveVariationSuffix() +
            svgFile + '-' +textID+ '-' + start + '-' + end +PNG_EXTENSION;
    }
    public String  buildCandidateReferenceFile(String svgDir, String svgFile){
        return getCandidateReferencePrefix() + svgDir + getCandidateReferenceSuffix() +
            svgFile + '-' +textID+ '-' + start + '-' + end +PNG_EXTENSION;
    }
    public String getName() {
        return super.getName() + '#' +textID+ '(' + start + ',' + end + ')';
    }
    public JSVGCanvasHandler createCanvasHandler() {
        return new JSVGCanvasHandler(this, this) {
                public JSVGCanvas createCanvas() {
                    JSVGCanvas ret = new JSVGCanvas();
                    ret.setDocumentState(JSVGCanvas.ALWAYS_DYNAMIC);
                    return ret;
                }
            };
    }
    public void canvasRendered(JSVGCanvas canvas) {
        DefaultTestReport report = new DefaultTestReport(this);
        try {
            Element e = canvas.getSVGDocument().getElementById(textID);
            if (e == null) {
                report.setErrorCode(ERROR_BAD_ID);
                report.setDescription(new TestReport.Entry[] {
                    new TestReport.Entry
                        (Messages.formatMessage
                         (ENTRY_KEY_ERROR_DESCRIPTION, null),
                         Messages.formatMessage
                         (ERROR_BAD_ID, new String[]{textID}))
                        });
                report.setPassed(false);
                failReport = report;
                return;
            }
            if (!(e instanceof SVGTextContentElement)) {
                report.setErrorCode(ERROR_ID_NOT_TEXT);
                report.setDescription(new TestReport.Entry[] {
                    new TestReport.Entry
                        (Messages.formatMessage
                         (ENTRY_KEY_ERROR_DESCRIPTION, null),
                         Messages.formatMessage
                         (ERROR_ID_NOT_TEXT, new String[]{id, e.toString()}))
                        });
                report.setPassed(false);
                failReport = report;
                return;
            }
            SVGTextContentElement tce = (SVGTextContentElement)e;
            tce.selectSubString(start, end);
        } catch(Exception e) {
            StringWriter trace = new StringWriter();
            e.printStackTrace(new PrintWriter(trace));
            report.setErrorCode(ERROR_GETTING_SELECTION);
            report.setDescription(new TestReport.Entry[] {
                new TestReport.Entry
                    (Messages.formatMessage(ENTRY_KEY_ERROR_DESCRIPTION, null),
                     Messages.formatMessage
                     (ERROR_GETTING_SELECTION,
                      new String[]{id, String.valueOf( start ), String.valueOf( end ), trace.toString()}))
                    });
            report.setPassed(false);
            failReport = report;
        }
        finally {
            scriptDone();
        }
    }
}
