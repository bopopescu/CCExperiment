package org.apache.tools.ant.taskdefs.optional;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import netrexx.lang.Rexx;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.util.FileUtils;
public class NetRexxC extends MatchingTask {
    private boolean binary;
    private String classpath;
    private boolean comments;
    private boolean compact = true; 
    private boolean compile = true;
    private boolean console;
    private boolean crossref;
    private boolean decimal = true;
    private File destDir;
    private boolean diag;
    private boolean explicit;
    private boolean format;
    private boolean keep;
    private boolean logo = true;
    private boolean replace;
    private boolean savelog;
    private File srcDir;
    private boolean sourcedir = true; 
    private boolean strictargs;
    private boolean strictassign;
    private boolean strictcase;
    private boolean strictimport;
    private boolean strictprops;
    private boolean strictsignal;
    private boolean symbols;
    private boolean time;
    private String trace = "trace2";
    private boolean utf8;
    private String verbose = "verbose3";
    private boolean suppressMethodArgumentNotUsed = false;
    private boolean suppressPrivatePropertyNotUsed = false;
    private boolean suppressVariableNotUsed = false;
    private boolean suppressExceptionNotSignalled = false;
    private boolean suppressDeprecation = false;
    private boolean removeKeepExtension = false;
    static final String MSG_METHOD_ARGUMENT_NOT_USED
        = "Warning: Method argument is not used";
    static final String MSG_PRIVATE_PROPERTY_NOT_USED
        = "Warning: Private property is defined but not used";
    static final String MSG_VARIABLE_NOT_USED
        = "Warning: Variable is set but not used";
    static final String MSG_EXCEPTION_NOT_SIGNALLED
        = "is in SIGNALS list but is not signalled within the method";
    static final String MSG_DEPRECATION = "has been deprecated";
    private Vector compileList = new Vector();
    private Hashtable filecopyList = new Hashtable();
    public void setBinary(boolean binary) {
        this.binary = binary;
    }
    public void setClasspath(String classpath) {
        this.classpath = classpath;
    }
    public void setComments(boolean comments) {
        this.comments = comments;
    }
    public void setCompact(boolean compact) {
        this.compact = compact;
    }
    public void setCompile(boolean compile) {
        this.compile = compile;
        if (!this.compile && !this.keep) {
            this.keep = true;
        }
    }
    public void setConsole(boolean console) {
        this.console = console;
    }
    public void setCrossref(boolean crossref) {
        this.crossref = crossref;
    }
    public void setDecimal(boolean decimal) {
        this.decimal = decimal;
    }
    public void setDestDir(File destDirName) {
        destDir = destDirName;
    }
    public void setDiag(boolean diag) {
        this.diag = diag;
    }
    public void setExplicit(boolean explicit) {
        this.explicit = explicit;
    }
    public void setFormat(boolean format) {
        this.format = format;
    }
    public void setJava(boolean java) {
        log("The attribute java is currently unused.", Project.MSG_WARN);
    }
    public void setKeep(boolean keep) {
        this.keep = keep;
    }
    public void setLogo(boolean logo) {
        this.logo = logo;
    }
    public void setReplace(boolean replace) {
        this.replace = replace;
    }
    public void setSavelog(boolean savelog) {
        this.savelog = savelog;
    }
    public void setSourcedir(boolean sourcedir) {
        this.sourcedir = sourcedir;
    }
    public void setSrcDir(File srcDirName) {
        srcDir = srcDirName;
    }
    public void setStrictargs(boolean strictargs) {
        this.strictargs = strictargs;
    }
    public void setStrictassign(boolean strictassign) {
        this.strictassign = strictassign;
    }
    public void setStrictcase(boolean strictcase) {
        this.strictcase = strictcase;
    }
    public void setStrictimport(boolean strictimport) {
        this.strictimport = strictimport;
    }
    public void setStrictprops(boolean strictprops) {
        this.strictprops = strictprops;
    }
    public void setStrictsignal(boolean strictsignal) {
        this.strictsignal = strictsignal;
    }
    public void setSymbols(boolean symbols) {
        this.symbols = symbols;
    }
    public void setTime(boolean time) {
        this.time = time;
    }
    public void setTrace(TraceAttr trace) {
        this.trace = trace.getValue();
    }
    public void setTrace(String trace) {
        TraceAttr t = new TraceAttr();
        t.setValue(trace);
        setTrace(t);
    }
    public void setUtf8(boolean utf8) {
        this.utf8 = utf8;
    }
    public void setVerbose(VerboseAttr verbose) {
        this.verbose = verbose.getValue();
    }
    public void setVerbose(String verbose) {
        VerboseAttr v = new VerboseAttr();
        v.setValue(verbose);
        setVerbose(v);
    }
    public void setSuppressMethodArgumentNotUsed(boolean suppressMethodArgumentNotUsed) {
        this.suppressMethodArgumentNotUsed = suppressMethodArgumentNotUsed;
    }
    public void setSuppressPrivatePropertyNotUsed(boolean suppressPrivatePropertyNotUsed) {
        this.suppressPrivatePropertyNotUsed = suppressPrivatePropertyNotUsed;
    }
    public void setSuppressVariableNotUsed(boolean suppressVariableNotUsed) {
        this.suppressVariableNotUsed = suppressVariableNotUsed;
    }
    public void setSuppressExceptionNotSignalled(boolean suppressExceptionNotSignalled) {
        this.suppressExceptionNotSignalled = suppressExceptionNotSignalled;
    }
    public void setSuppressDeprecation(boolean suppressDeprecation) {
        this.suppressDeprecation = suppressDeprecation;
    }
    public void setRemoveKeepExtension(boolean removeKeepExtension) {
        this.removeKeepExtension = removeKeepExtension;
    }
    public void init() {
        String p;
        if ((p = getProject().getProperty("ant.netrexxc.binary")) != null) {
            this.binary = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.comments")) != null) {
            this.comments = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.compact")) != null) {
            this.compact = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.compile")) != null) {
            this.compile = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.console")) != null) {
            this.console = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.crossref")) != null) {
            this.crossref = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.decimal")) != null) {
            this.decimal = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.diag")) != null) {
            this.diag = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.explicit")) != null) {
            this.explicit = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.format")) != null) {
            this.format = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.keep")) != null) {
            this.keep = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.logo")) != null) {
            this.logo = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.replace")) != null) {
            this.replace = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.savelog")) != null) {
            this.savelog = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.sourcedir")) != null) {
            this.sourcedir = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.strictargs")) != null) {
            this.strictargs = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.strictassign")) != null) {
            this.strictassign = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.strictcase")) != null) {
            this.strictcase = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.strictimport")) != null) {
            this.strictimport = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.strictprops")) != null) {
            this.strictprops = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.strictsignal")) != null) {
            this.strictsignal = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.symbols")) != null) {
            this.symbols = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.time")) != null) {
            this.time = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.trace")) != null) {
            setTrace(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.utf8")) != null) {
            this.utf8 = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.verbose")) != null) {
            setVerbose(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.suppressMethodArgumentNotUsed")) != null) {
            this.suppressMethodArgumentNotUsed = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.suppressPrivatePropertyNotUsed")) != null) {
            this.suppressPrivatePropertyNotUsed = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.suppressVariableNotUsed")) != null) {
            this.suppressVariableNotUsed = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.suppressExceptionNotSignalled")) != null) {
            this.suppressExceptionNotSignalled = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.suppressDeprecation")) != null) {
            this.suppressDeprecation = Project.toBoolean(p);
        }
        if ((p = getProject().getProperty("ant.netrexxc.removeKeepExtension")) != null) {
            this.removeKeepExtension = Project.toBoolean(p);
        }
    }
    public void execute() throws BuildException {
        if (srcDir == null || destDir == null) {
            throw new BuildException("srcDir and destDir attributes must be set!");
        }
        DirectoryScanner ds = getDirectoryScanner(srcDir);
        String[] files = ds.getIncludedFiles();
        scanDir(srcDir, destDir, files);
        copyFilesToDestination();
        if (compileList.size() > 0) {
            log("Compiling " + compileList.size() + " source file"
                 + (compileList.size() == 1 ? "" : "s")
                 + " to " + destDir);
            doNetRexxCompile();
            if (removeKeepExtension && (!compile || keep)) {
                removeKeepExtensions();
            }
        }
    }
    private void scanDir(File srcDir, File destDir, String[] files) {
        for (int i = 0; i < files.length; i++) {
            File srcFile = new File(srcDir, files[i]);
            File destFile = new File(destDir, files[i]);
            String filename = files[i];
            if (filename.toLowerCase().endsWith(".nrx")) {
                File classFile =
                    new File(destDir,
                    filename.substring(0, filename.lastIndexOf('.')) + ".class");
                File javaFile =
                    new File(destDir,
                    filename.substring(0, filename.lastIndexOf('.'))
                    + (removeKeepExtension ? ".java" : ".java.keep"));
                if (!compile && srcFile.lastModified() > javaFile.lastModified()) {
                    filecopyList.put(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
                    compileList.addElement(destFile.getAbsolutePath());
                }
                else if (compile && srcFile.lastModified() > classFile.lastModified()) {
                    filecopyList.put(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
                    compileList.addElement(destFile.getAbsolutePath());
                }
            } else {
                if (srcFile.lastModified() > destFile.lastModified()) {
                    filecopyList.put(srcFile.getAbsolutePath(), destFile.getAbsolutePath());
                }
            }
        }
    }
    private void copyFilesToDestination() {
        if (filecopyList.size() > 0) {
            log("Copying " + filecopyList.size() + " file"
                 + (filecopyList.size() == 1 ? "" : "s")
                 + " to " + destDir.getAbsolutePath());
            Enumeration e = filecopyList.keys();
            while (e.hasMoreElements()) {
                String fromFile = (String) e.nextElement();
                String toFile = (String) filecopyList.get(fromFile);
                try {
                    FileUtils.getFileUtils().copyFile(fromFile, toFile);
                } catch (IOException ioe) {
                    String msg = "Failed to copy " + fromFile + " to " + toFile
                         + " due to " + ioe.getMessage();
                    throw new BuildException(msg, ioe);
                }
            }
        }
    }
    private void removeKeepExtensions() {
        if (compileList.size() > 0) {
            log("Removing .keep extension on " + compileList.size() + " file"
                 + (compileList.size() == 1 ? "" : "s"));
            Enumeration e = compileList.elements();
            while (e.hasMoreElements()) {
                String nrxName = (String) e.nextElement();
                String baseName = nrxName.substring(0, nrxName.lastIndexOf('.'));
                File fromFile = new File(baseName + ".java.keep");
                File toFile = new File(baseName + ".java");
                if (fromFile.renameTo(toFile)) {
                    log("Successfully renamed " + fromFile + " to " + toFile, Project.MSG_VERBOSE);
                } else {
                    log("Failed to rename " + fromFile + " to " + toFile);
                }
            }
        }
    }
    private void doNetRexxCompile() throws BuildException {
        log("Using NetRexx compiler", Project.MSG_VERBOSE);
        String classpath = getCompileClasspath();
        StringBuffer compileOptions = new StringBuffer();
        String[] compileOptionsArray = getCompileOptionsAsArray();
        String[] fileListArray = new String[compileList.size()];
        Enumeration e = compileList.elements();
        int j = 0;
        while (e.hasMoreElements()) {
            fileListArray[j] = (String) e.nextElement();
            j++;
        }
        String[] compileArgs = new String[compileOptionsArray.length + fileListArray.length];
        for (int i = 0; i < compileOptionsArray.length; i++) {
            compileArgs[i] = compileOptionsArray[i];
        }
        for (int i = 0; i < fileListArray.length; i++) {
            compileArgs[i + compileOptionsArray.length] = fileListArray[i];
        }
        compileOptions.append("Compilation args: ");
        for (int i = 0; i < compileOptionsArray.length; i++) {
            compileOptions.append(compileOptionsArray[i]);
            compileOptions.append(" ");
        }
        log(compileOptions.toString(), Project.MSG_VERBOSE);
        String eol = System.getProperty("line.separator");
        StringBuffer niceSourceList = new StringBuffer("Files to be compiled:" + eol);
        for (int i = 0; i < compileList.size(); i++) {
            niceSourceList.append("    ");
            niceSourceList.append(compileList.elementAt(i).toString());
            niceSourceList.append(eol);
        }
        log(niceSourceList.toString(), Project.MSG_VERBOSE);
        String currentClassPath = System.getProperty("java.class.path");
        Properties currentProperties = System.getProperties();
        currentProperties.put("java.class.path", classpath);
        try {
            StringWriter out = new StringWriter();
            PrintWriter w = null;
            int rc =
                COM.ibm.netrexx.process.NetRexxC.main(new Rexx(compileArgs),
                                                      w = new PrintWriter(out));
            String sdir = srcDir.getAbsolutePath();
            String ddir = destDir.getAbsolutePath();
            boolean doReplace = !(sdir.equals(ddir));
            int dlen = ddir.length();
            String l;
            BufferedReader in = new BufferedReader(new StringReader(out.toString()));
            log("replacing destdir '" + ddir + "' through sourcedir '"
                + sdir + "'", Project.MSG_VERBOSE);
            while ((l = in.readLine()) != null) {
                int idx;
                while (doReplace && ((idx = l.indexOf(ddir)) != -1)) {
                    l = (new StringBuffer(l)).replace(idx, idx + dlen, sdir).toString();
                }
                if (suppressMethodArgumentNotUsed
                    && l.indexOf(MSG_METHOD_ARGUMENT_NOT_USED) != -1) {
                    log(l, Project.MSG_VERBOSE);
                } else if (suppressPrivatePropertyNotUsed
                    && l.indexOf(MSG_PRIVATE_PROPERTY_NOT_USED) != -1) {
                    log(l, Project.MSG_VERBOSE);
                } else if (suppressVariableNotUsed
                    && l.indexOf(MSG_VARIABLE_NOT_USED) != -1) {
                    log(l, Project.MSG_VERBOSE);
                } else if (suppressExceptionNotSignalled
                    && l.indexOf(MSG_EXCEPTION_NOT_SIGNALLED) != -1) {
                    log(l, Project.MSG_VERBOSE);
                } else if (suppressDeprecation
                    && l.indexOf(MSG_DEPRECATION) != -1) {
                    log(l, Project.MSG_VERBOSE);
                } else if (l.indexOf("Error:") != -1) {
                    log(l, Project.MSG_ERR);
                } else if (l.indexOf("Warning:") != -1) {
                    log(l, Project.MSG_WARN);
                } else {
                    log(l, Project.MSG_INFO); 
                }
            }
            if (rc > 1) {
                throw new BuildException("Compile failed, messages should "
                    + "have been provided.");
            }
            if (w.checkError()) {
                throw new IOException("Encountered an error");
            }
        } catch (IOException ioe) {
            throw new BuildException("Unexpected IOException while "
                + "playing with Strings", ioe);
        } finally {
            currentProperties = System.getProperties();
            currentProperties.put("java.class.path", currentClassPath);
        }
    }
    private String getCompileClasspath() {
        StringBuffer classpath = new StringBuffer();
        classpath.append(destDir.getAbsolutePath());
        if (this.classpath != null) {
            addExistingToClasspath(classpath, this.classpath);
        }
        return classpath.toString();
    }
    private String[] getCompileOptionsAsArray() {
        Vector options = new Vector();
        options.addElement(binary ? "-binary" : "-nobinary");
        options.addElement(comments ? "-comments" : "-nocomments");
        options.addElement(compile ? "-compile" : "-nocompile");
        options.addElement(compact ? "-compact" : "-nocompact");
        options.addElement(console ? "-console" : "-noconsole");
        options.addElement(crossref ? "-crossref" : "-nocrossref");
        options.addElement(decimal ? "-decimal" : "-nodecimal");
        options.addElement(diag ? "-diag" : "-nodiag");
        options.addElement(explicit ? "-explicit" : "-noexplicit");
        options.addElement(format ? "-format" : "-noformat");
        options.addElement(keep ? "-keep" : "-nokeep");
        options.addElement(logo ? "-logo" : "-nologo");
        options.addElement(replace ? "-replace" : "-noreplace");
        options.addElement(savelog ? "-savelog" : "-nosavelog");
        options.addElement(sourcedir ? "-sourcedir" : "-nosourcedir");
        options.addElement(strictargs ? "-strictargs" : "-nostrictargs");
        options.addElement(strictassign ? "-strictassign" : "-nostrictassign");
        options.addElement(strictcase ? "-strictcase" : "-nostrictcase");
        options.addElement(strictimport ? "-strictimport" : "-nostrictimport");
        options.addElement(strictprops ? "-strictprops" : "-nostrictprops");
        options.addElement(strictsignal ? "-strictsignal" : "-nostrictsignal");
        options.addElement(symbols ? "-symbols" : "-nosymbols");
        options.addElement(time ? "-time" : "-notime");
        options.addElement("-" + trace);
        options.addElement(utf8 ? "-utf8" : "-noutf8");
        options.addElement("-" + verbose);
        String[] results = new String[options.size()];
        options.copyInto(results);
        return results;
    }
    private void addExistingToClasspath(StringBuffer target, String source) {
        StringTokenizer tok = new StringTokenizer(source,
            System.getProperty("path.separator"), false);
        while (tok.hasMoreTokens()) {
            File f = getProject().resolveFile(tok.nextToken());
            if (f.exists()) {
                target.append(File.pathSeparator);
                target.append(f.getAbsolutePath());
            } else {
                log("Dropping from classpath: "
                    + f.getAbsolutePath(), Project.MSG_VERBOSE);
            }
        }
    }
    public static class TraceAttr extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[]{"trace", "trace1", "trace2", "notrace"};
        }
    }
    public static class VerboseAttr extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[]{"verbose", "verbose0", "verbose1",
                "verbose2", "verbose3", "verbose4",
                "verbose5", "noverbose"};
        }
    }
}
