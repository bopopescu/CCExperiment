package org.apache.tools.ant.taskdefs.condition;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.taskdefs.Touch;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.Resource;
public class IsLastModified extends ProjectComponent implements Condition {
    private long millis = -1;
    private String dateTime = null;
    private Touch.DateFormatFactory dfFactory = Touch.DEFAULT_DF_FACTORY;
    private Resource resource;
    private CompareMode mode = CompareMode.EQUALS;
    public void setMillis(long millis) {
        this.millis = millis;
    }
    public void setDatetime(String dateTime) {
        this.dateTime = dateTime;
    }
    public void setPattern(final String pattern) {
        dfFactory = new Touch.DateFormatFactory() {
            public DateFormat getPrimaryFormat() {
                return new SimpleDateFormat(pattern);
            }
            public DateFormat getFallbackFormat() {
                return null;
            }
        };
    }
    public void add(Resource r) {
        if (resource != null) {
            throw new BuildException("only one resource can be tested");
        }
        resource = r;
    }
    public void setMode(CompareMode mode) {
        this.mode = mode;
    }
    protected void validate() throws BuildException {
        if (millis >= 0 && dateTime != null) {
            throw new BuildException("Only one of dateTime and millis can be"
                                     + " set");
        }
        if (millis < 0 && dateTime == null) {
            throw new BuildException("millis or dateTime is required");
        }
        if (resource == null) {
            throw new BuildException("resource is required");
        }
    }
    protected long getMillis() throws BuildException {
        if (millis >= 0) {
            return millis;
        }
        if ("now".equalsIgnoreCase(dateTime)) {
            return System.currentTimeMillis();
        }
        DateFormat df = dfFactory.getPrimaryFormat();
        ParseException pe = null;
        try {
            return df.parse(dateTime).getTime();
        } catch (ParseException peOne) {
            df = dfFactory.getFallbackFormat();
            if (df == null) {
                pe = peOne;
            } else {
                try {
                    return df.parse(dateTime).getTime();
                } catch (ParseException peTwo) {
                    pe = peTwo;
                }
            }
        }
        if (pe != null) {
            throw new BuildException(pe.getMessage(), pe, getLocation());
        }
        return 0;
    }
    public boolean eval() throws BuildException {
        validate();
        long expected = getMillis();
        long actual = resource.getLastModified();
        log("expected timestamp: " + expected + " (" + new Date(expected) + ")"
            + ", actual timestamp: " + actual + " (" + new Date(actual) + ")" ,
            Project.MSG_VERBOSE);
        if (CompareMode.EQUALS_TEXT.equals(mode.getValue())) {
            return expected == actual;
        }
        if (CompareMode.BEFORE_TEXT.equals(mode.getValue())) {
            return expected > actual;
        }
        if (CompareMode.NOT_BEFORE_TEXT.equals(mode.getValue())) {
            return expected <= actual;
        }
        if (CompareMode.AFTER_TEXT.equals(mode.getValue())) {
            return expected < actual;
        }
        if (CompareMode.NOT_AFTER_TEXT.equals(mode.getValue())) {
            return expected >= actual;
        }
        throw new BuildException("Unknown mode " + mode.getValue());
    }
    public static class CompareMode extends EnumeratedAttribute {
        private static final String EQUALS_TEXT = "equals";
        private static final String BEFORE_TEXT = "before";
        private static final String AFTER_TEXT = "after";
        private static final String NOT_BEFORE_TEXT = "not-before";
        private static final String NOT_AFTER_TEXT = "not-after";
        private static final CompareMode EQUALS = new CompareMode(EQUALS_TEXT);
        public CompareMode() {
            this(EQUALS_TEXT);
        }
        public CompareMode(String s) {
            super();
            setValue(s);
        }
        public String[] getValues() {
            return new String[] {
                EQUALS_TEXT, BEFORE_TEXT, AFTER_TEXT, NOT_BEFORE_TEXT,
                NOT_AFTER_TEXT,
            };
        }
    }
}
