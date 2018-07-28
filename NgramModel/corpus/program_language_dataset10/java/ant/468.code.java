package org.apache.tools.ant.taskdefs.optional.jsp;
import java.io.File;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
public class WLJspc extends MatchingTask {
    private File destinationDirectory;
    private File sourceDirectory;
    private String destinationPackage;
    private Path compileClasspath;
    private String pathToPackage = "";
    private Vector filesToDo = new Vector();
    public void execute() throws BuildException {
        if (!destinationDirectory.isDirectory()) {
            throw new BuildException("destination directory "
                + destinationDirectory.getPath() + " is not valid");
        }
        if (!sourceDirectory.isDirectory()) {
            throw new BuildException("src directory "
                + sourceDirectory.getPath() + " is not valid");
        }
        if (destinationPackage == null) {
            throw new BuildException("package attribute must be present.",
                                     getLocation());
        }
        pathToPackage
            = this.destinationPackage.replace('.', File.separatorChar);
        DirectoryScanner ds = super.getDirectoryScanner(sourceDirectory);
        if (compileClasspath == null) {
            compileClasspath = new Path(getProject());
        }
        compileClasspath = compileClasspath.concatSystemClasspath();
        String[] files = ds.getIncludedFiles();
        Java helperTask = new Java(this);
        helperTask.setFork(true);
        helperTask.setClassname("weblogic.jspc");
        helperTask.setTaskName(getTaskName());
        String[] args = new String[12];
        File jspFile = null;
        String parents = "";
        int j = 0;
        args[j++] = "-d";
        args[j++] = destinationDirectory.getAbsolutePath().trim();
        args[j++] = "-docroot";
        args[j++] = sourceDirectory.getAbsolutePath().trim();
        args[j++] = "-keepgenerated";
        args[j++] =  "-compilerclass";
        args[j++] = "sun.tools.javac.Main";
        args[j++] = "-classpath";
        args[j++] = compileClasspath.toString();
        this.scanDir(files);
        log("Compiling " + filesToDo.size() + " JSP files");
        for (int i = 0; i < filesToDo.size(); i++) {
            String filename = (String) filesToDo.elementAt(i);
            jspFile = new File(filename);
            args[j] = "-package";
            parents = jspFile.getParent();
            if ((parents != null)  && (!("").equals(parents))) {
                parents =  this.replaceString(parents, File.separator, "_.");
                args[j + 1] = destinationPackage + "." + "_" + parents;
            } else {
                args[j + 1] = destinationPackage;
            }
            args[j + 2] =  sourceDirectory + File.separator + filename;
            helperTask.clearArgs();
            for (int x = 0; x < j + 3; x++) {
                helperTask.createArg().setValue(args[x]);
            }
            helperTask.setClasspath(compileClasspath);
            if (helperTask.executeJava() != 0) {
                log(filename + " failed to compile", Project.MSG_WARN);
            }
        }
    }
    public void setClasspath(Path classpath) {
        if (compileClasspath == null) {
            compileClasspath = classpath;
        } else {
            compileClasspath.append(classpath);
        }
    }
    public Path createClasspath() {
        if (compileClasspath == null) {
            compileClasspath = new Path(getProject());
        }
        return compileClasspath;
    }
    public void setSrc(File dirName) {
        sourceDirectory = dirName;
    }
    public void setDest(File dirName) {
        destinationDirectory = dirName;
    }
    public void setPackage(String packageName) {
        destinationPackage = packageName;
    }
    protected void scanDir(String[] files) {
        long now = (new Date()).getTime();
        File jspFile = null;
        String parents = null;
        String pack = "";
        for (int i = 0; i < files.length; i++) {
            File srcFile = new File(this.sourceDirectory, files[i]);
            jspFile = new File(files[i]);
            parents = jspFile.getParent();
            if ((parents != null)  && (!("").equals(parents))) {
                parents =  this.replaceString(parents, File.separator, "_/");
                pack = pathToPackage + File.separator + "_" + parents;
            } else {
                pack = pathToPackage;
            }
            String filePath = pack + File.separator + "_";
            int startingIndex = files[i].lastIndexOf(File.separator) != -1
                    ? files[i].lastIndexOf(File.separator) + 1 : 0;
            int endingIndex = files[i].indexOf(".jsp");
            if (endingIndex == -1) {
                log("Skipping " + files[i] + ". Not a JSP",
                    Project.MSG_VERBOSE);
                continue;
            }
            filePath += files[i].substring(startingIndex, endingIndex);
            filePath += ".class";
            File classFile = new File(this.destinationDirectory, filePath);
            if (srcFile.lastModified() > now) {
                log("Warning: file modified in the future: "
                    + files[i], Project.MSG_WARN);
            }
            if (srcFile.lastModified() > classFile.lastModified()) {
                filesToDo.addElement(files[i]);
                log("Recompiling File " + files[i], Project.MSG_VERBOSE);
            }
        }
    }
    protected String replaceString(String inpString, String escapeChars,
                                   String replaceChars) {
        String localString = "";
        int numTokens = 0;
        StringTokenizer st = new StringTokenizer(inpString, escapeChars, true);
        numTokens = st.countTokens();
        for (int i = 0; i < numTokens; i++) {
            String test = st.nextToken();
            test = (test.equals(escapeChars) ? replaceChars : test);
            localString += test;
        }
        return localString;
    }
}