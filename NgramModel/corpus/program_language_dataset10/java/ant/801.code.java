package org.apache.tools.ant.util;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.regexp.RegexpMatcher;
import org.apache.tools.ant.util.regexp.RegexpMatcherFactory;
import org.apache.tools.ant.util.regexp.RegexpUtil;
public class RegexpPatternMapper implements FileNameMapper {
    private static final int DECIMAL = 10;
    protected RegexpMatcher reg = null;
    protected char[] to = null;
    protected StringBuffer result = new StringBuffer();
    public RegexpPatternMapper() throws BuildException {
        reg = (new RegexpMatcherFactory()).newRegexpMatcher();
    }
    private boolean handleDirSep = false;
    private int     regexpOptions = 0;
    public void setHandleDirSep(boolean handleDirSep) {
        this.handleDirSep = handleDirSep;
    }
    public void setCaseSensitive(boolean caseSensitive) {
        regexpOptions = RegexpUtil.asOptions(caseSensitive);
    }
    public void setFrom(String from) throws BuildException {
        if (from != null) {
            try {
                reg.setPattern(from);
            } catch (NoClassDefFoundError e) {
                throw new BuildException("Cannot load regular expression matcher",
                                         e);
            }
        } else {
            throw new BuildException("this mapper requires a 'from' attribute");
        }
    }
    public void setTo(String to) {
        if (to != null) {
            this.to = to.toCharArray();
        } else {
            throw new BuildException("this mapper requires a 'to' attribute");
        }
    }
    public String[] mapFileName(String sourceFileName) {
        if (handleDirSep) {
            if (sourceFileName.indexOf("\\") != -1) {
                sourceFileName = sourceFileName.replace('\\', '/');
            }
        }
        if (reg == null  || to == null
            || !reg.matches(sourceFileName, regexpOptions)) {
            return null;
        }
        return new String[] {replaceReferences(sourceFileName)};
    }
    protected String replaceReferences(String source) {
        Vector v = reg.getGroups(source, regexpOptions);
        result.setLength(0);
        for (int i = 0; i < to.length; i++) {
            if (to[i] == '\\') {
                if (++i < to.length) {
                    int value = Character.digit(to[i], DECIMAL);
                    if (value > -1) {
                        result.append((String) v.elementAt(value));
                    } else {
                        result.append(to[i]);
                    }
                } else {
                    result.append('\\');
                }
            } else {
                result.append(to[i]);
            }
        }
        return result.substring(0);
    }
}
