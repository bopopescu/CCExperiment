package org.apache.tools.ant.taskdefs.optional.ejb;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.FileUtils;
import org.xml.sax.InputSource;
public class WeblogicDeploymentTool extends GenericDeploymentTool {
    public static final String PUBLICID_EJB11
         = "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 1.1//EN";
    public static final String PUBLICID_EJB20
         = "-//Sun Microsystems, Inc.//DTD Enterprise JavaBeans 2.0//EN";
    public static final String PUBLICID_WEBLOGIC_EJB510
         = "-//BEA Systems, Inc.//DTD WebLogic 5.1.0 EJB//EN";
    public static final String PUBLICID_WEBLOGIC_EJB600
         = "-//BEA Systems, Inc.//DTD WebLogic 6.0.0 EJB//EN";
    public static final String PUBLICID_WEBLOGIC_EJB700
         = "-//BEA Systems, Inc.//DTD WebLogic 7.0.0 EJB//EN";
    protected static final String DEFAULT_WL51_EJB11_DTD_LOCATION
         = "/weblogic/ejb/deployment/xml/ejb-jar.dtd";
    protected static final String DEFAULT_WL60_EJB11_DTD_LOCATION
         = "/weblogic/ejb20/dd/xml/ejb11-jar.dtd";
    protected static final String DEFAULT_WL60_EJB20_DTD_LOCATION
         = "/weblogic/ejb20/dd/xml/ejb20-jar.dtd";
    protected static final String DEFAULT_WL51_DTD_LOCATION
         = "/weblogic/ejb/deployment/xml/weblogic-ejb-jar.dtd";
    protected static final String DEFAULT_WL60_51_DTD_LOCATION
         = "/weblogic/ejb20/dd/xml/weblogic510-ejb-jar.dtd";
    protected static final String DEFAULT_WL60_DTD_LOCATION
         = "/weblogic/ejb20/dd/xml/weblogic600-ejb-jar.dtd";
    protected static final String DEFAULT_WL70_DTD_LOCATION
         = "/weblogic/ejb20/dd/xml/weblogic700-ejb-jar.dtd";
    protected static final String DEFAULT_COMPILER = "default";
    protected static final String WL_DD = "weblogic-ejb-jar.xml";
    protected static final String WL_CMP_DD = "weblogic-cmp-rdbms-jar.xml";
    protected static final String COMPILER_EJB11 = "weblogic.ejbc";
    protected static final String COMPILER_EJB20 = "weblogic.ejbc20";
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private String jarSuffix = ".jar";
    private String weblogicDTD;
    private String ejb11DTD;
    private boolean keepgenerated = false;
    private String ejbcClass = null;
    private String additionalArgs = "";
    private String additionalJvmArgs = "";
    private boolean keepGeneric = false;
    private String compiler = null;
    private boolean alwaysRebuild = true;
    private boolean noEJBC = false;
    private boolean newCMP = false;
    private Path wlClasspath = null;
    private Vector sysprops = new Vector();
    private Integer jvmDebugLevel = null;
    private File outputDir;
    public void addSysproperty(Environment.Variable sysp) {
        sysprops.add(sysp);
    }
    public Path createWLClasspath() {
        if (wlClasspath == null) {
            wlClasspath = new Path(getTask().getProject());
        }
        return wlClasspath.createPath();
    }
    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }
    public void setWLClasspath(Path wlClasspath) {
        this.wlClasspath = wlClasspath;
    }
    public void setCompiler(String compiler) {
        this.compiler = compiler;
    }
    public void setRebuild(boolean rebuild) {
        this.alwaysRebuild = rebuild;
    }
    public void setJvmDebugLevel(Integer jvmDebugLevel) {
        this.jvmDebugLevel = jvmDebugLevel;
    }
    public Integer getJvmDebugLevel() {
        return jvmDebugLevel;
    }
    public void setSuffix(String inString) {
        this.jarSuffix = inString;
    }
    public void setKeepgeneric(boolean inValue) {
        this.keepGeneric = inValue;
    }
    public void setKeepgenerated(String inValue) {
        this.keepgenerated = Boolean.valueOf(inValue).booleanValue();
    }
    public void setArgs(String args) {
        this.additionalArgs = args;
    }
    public void setJvmargs(String args) {
        this.additionalJvmArgs = args;
    }
    public void setEjbcClass(String ejbcClass) {
        this.ejbcClass = ejbcClass;
    }
    public String getEjbcClass() {
        return ejbcClass;
    }
    public void setWeblogicdtd(String inString) {
        setEJBdtd(inString);
    }
    public void setWLdtd(String inString) {
        this.weblogicDTD = inString;
    }
    public void setEJBdtd(String inString) {
        this.ejb11DTD = inString;
    }
    public void setOldCMP(boolean oldCMP) {
        this.newCMP = !oldCMP;
    }
    public void setNewCMP(boolean newCMP) {
        this.newCMP = newCMP;
    }
    public void setNoEJBC(boolean noEJBC) {
        this.noEJBC = noEJBC;
    }
    protected void registerKnownDTDs(DescriptorHandler handler) {
        handler.registerDTD(PUBLICID_EJB11, DEFAULT_WL51_EJB11_DTD_LOCATION);
        handler.registerDTD(PUBLICID_EJB11, DEFAULT_WL60_EJB11_DTD_LOCATION);
        handler.registerDTD(PUBLICID_EJB11, ejb11DTD);
        handler.registerDTD(PUBLICID_EJB20, DEFAULT_WL60_EJB20_DTD_LOCATION);
    }
    protected DescriptorHandler getWeblogicDescriptorHandler(final File srcDir) {
        DescriptorHandler handler =
            new DescriptorHandler(getTask(), srcDir) {
                protected void processElement() {
                    if (currentElement.equals("type-storage")) {
                        String fileNameWithMETA = currentText;
                        String fileName
                             = fileNameWithMETA.substring(META_DIR.length(),
                            fileNameWithMETA.length());
                        File descriptorFile = new File(srcDir, fileName);
                        ejbFiles.put(fileNameWithMETA, descriptorFile);
                    }
                }
            };
        handler.registerDTD(PUBLICID_WEBLOGIC_EJB510, DEFAULT_WL51_DTD_LOCATION);
        handler.registerDTD(PUBLICID_WEBLOGIC_EJB510, DEFAULT_WL60_51_DTD_LOCATION);
        handler.registerDTD(PUBLICID_WEBLOGIC_EJB600, DEFAULT_WL60_DTD_LOCATION);
        handler.registerDTD(PUBLICID_WEBLOGIC_EJB700, DEFAULT_WL70_DTD_LOCATION);
        handler.registerDTD(PUBLICID_WEBLOGIC_EJB510, weblogicDTD);
        handler.registerDTD(PUBLICID_WEBLOGIC_EJB600, weblogicDTD);
        for (Iterator i = getConfig().dtdLocations.iterator(); i.hasNext();) {
            EjbJar.DTDLocation dtdLocation = (EjbJar.DTDLocation) i.next();
            handler.registerDTD(dtdLocation.getPublicId(), dtdLocation.getLocation());
        }
        return handler;
    }
    protected void addVendorFiles(Hashtable ejbFiles, String ddPrefix) {
        File weblogicDD = new File(getConfig().descriptorDir, ddPrefix + WL_DD);
        if (weblogicDD.exists()) {
            ejbFiles.put(META_DIR + WL_DD,
                weblogicDD);
        } else {
            log("Unable to locate weblogic deployment descriptor. "
                + "It was expected to be in "
                + weblogicDD.getPath(), Project.MSG_WARN);
            return;
        }
        if (!newCMP) {
            log("The old method for locating CMP files has been DEPRECATED.", Project.MSG_VERBOSE);
            log("Please adjust your weblogic descriptor and set "
                + "newCMP=\"true\" to use the new CMP descriptor "
                + "inclusion mechanism. ", Project.MSG_VERBOSE);
            File weblogicCMPDD = new File(getConfig().descriptorDir, ddPrefix + WL_CMP_DD);
            if (weblogicCMPDD.exists()) {
                ejbFiles.put(META_DIR + WL_CMP_DD,
                    weblogicCMPDD);
            }
        } else {
            try {
                File ejbDescriptor = (File) ejbFiles.get(META_DIR + EJB_DD);
                SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
                saxParserFactory.setValidating(true);
                SAXParser saxParser = saxParserFactory.newSAXParser();
                DescriptorHandler handler
                    = getWeblogicDescriptorHandler(ejbDescriptor.getParentFile());
                saxParser.parse(new InputSource
                    (new FileInputStream(weblogicDD)),
                        handler);
                Hashtable ht = handler.getFiles();
                Enumeration e = ht.keys();
                while (e.hasMoreElements()) {
                    String key = (String) e.nextElement();
                    ejbFiles.put(key, ht.get(key));
                }
            } catch (Exception e) {
                String msg = "Exception while adding Vendor specific files: " + e.toString();
                throw new BuildException(msg, e);
            }
        }
    }
    File getVendorOutputJarFile(String baseName) {
        return new File(getDestDir(), baseName + jarSuffix);
    }
    private void buildWeblogicJar(File sourceJar, File destJar, String publicId) {
        Java javaTask = null;
        if (noEJBC) {
            try {
                FILE_UTILS.copyFile(sourceJar, destJar);
                if (!keepgenerated) {
                    sourceJar.delete();
                }
                return;
            } catch (IOException e) {
                throw new BuildException("Unable to write EJB jar", e);
            }
        }
        String ejbcClassName = ejbcClass;
        try {
            javaTask = new Java(getTask());
            javaTask.setTaskName("ejbc");
            javaTask.createJvmarg().setLine(additionalJvmArgs);
            if (!(sysprops.isEmpty())) {
                for (Enumeration en = sysprops.elements(); en.hasMoreElements();) {
                    Environment.Variable entry
                        = (Environment.Variable) en.nextElement();
                    javaTask.addSysproperty(entry);
                }
            }
            if (getJvmDebugLevel() != null) {
                javaTask.createJvmarg().setLine(" -Dweblogic.StdoutSeverityLevel=" + jvmDebugLevel);
            }
            if (ejbcClassName == null) {
                if (PUBLICID_EJB11.equals(publicId)) {
                    ejbcClassName = COMPILER_EJB11;
                } else if (PUBLICID_EJB20.equals(publicId)) {
                    ejbcClassName = COMPILER_EJB20;
                } else {
                    log("Unrecognized publicId " + publicId
                        + " - using EJB 1.1 compiler", Project.MSG_WARN);
                    ejbcClassName = COMPILER_EJB11;
                }
            }
            javaTask.setClassname(ejbcClassName);
            javaTask.createArg().setLine(additionalArgs);
            if (keepgenerated) {
                javaTask.createArg().setValue("-keepgenerated");
            }
            if (compiler == null) {
                String buildCompiler
                    = getTask().getProject().getProperty("build.compiler");
                if (buildCompiler != null && buildCompiler.equals("jikes")) {
                    javaTask.createArg().setValue("-compiler");
                    javaTask.createArg().setValue("jikes");
                }
            } else {
                if (!compiler.equals(DEFAULT_COMPILER)) {
                    javaTask.createArg().setValue("-compiler");
                    javaTask.createArg().setLine(compiler);
                }
            }
            Path combinedClasspath = getCombinedClasspath();
            if (wlClasspath != null && combinedClasspath != null
                 && combinedClasspath.toString().trim().length() > 0) {
                javaTask.createArg().setValue("-classpath");
                javaTask.createArg().setPath(combinedClasspath);
            }
            javaTask.createArg().setValue(sourceJar.getPath());
            if (outputDir == null) {
                javaTask.createArg().setValue(destJar.getPath());
            } else {
                javaTask.createArg().setValue(outputDir.getPath());
            }
            Path classpath = wlClasspath;
            if (classpath == null) {
                classpath = getCombinedClasspath();
            }
            javaTask.setFork(true);
            if (classpath != null) {
                javaTask.setClasspath(classpath);
            }
            log("Calling " + ejbcClassName + " for " + sourceJar.toString(),
                Project.MSG_VERBOSE);
            if (javaTask.executeJava() != 0) {
                throw new BuildException("Ejbc reported an error");
            }
        } catch (Exception e) {
            String msg = "Exception while calling " + ejbcClassName
                + ". Details: " + e.toString();
            throw new BuildException(msg, e);
        }
    }
    protected void writeJar(String baseName, File jarFile, Hashtable files,
                            String publicId) throws BuildException {
        File genericJarFile = super.getVendorOutputJarFile(baseName);
        super.writeJar(baseName, genericJarFile, files, publicId);
        if (alwaysRebuild || isRebuildRequired(genericJarFile, jarFile)) {
            buildWeblogicJar(genericJarFile, jarFile, publicId);
        }
        if (!keepGeneric) {
            log("deleting generic jar " + genericJarFile.toString(),
                Project.MSG_VERBOSE);
            genericJarFile.delete();
        }
    }
    public void validateConfigured() throws BuildException {
        super.validateConfigured();
    }
    protected boolean isRebuildRequired(File genericJarFile, File weblogicJarFile) {
        boolean rebuild = false;
        JarFile genericJar = null;
        JarFile wlJar = null;
        File newWLJarFile = null;
        JarOutputStream newJarStream = null;
        ClassLoader genericLoader = null;
        try {
            log("Checking if weblogic Jar needs to be rebuilt for jar " + weblogicJarFile.getName(),
                Project.MSG_VERBOSE);
            if (genericJarFile.exists() && genericJarFile.isFile()
                 && weblogicJarFile.exists() && weblogicJarFile.isFile()) {
                genericJar = new JarFile(genericJarFile);
                wlJar = new JarFile(weblogicJarFile);
                Hashtable genericEntries = new Hashtable();
                Hashtable wlEntries = new Hashtable();
                Hashtable replaceEntries = new Hashtable();
                for (Enumeration e = genericJar.entries(); e.hasMoreElements();) {
                    JarEntry je = (JarEntry) e.nextElement();
                    genericEntries.put(je.getName().replace('\\', '/'), je);
                }
                for (Enumeration e = wlJar.entries(); e.hasMoreElements();) {
                    JarEntry je = (JarEntry) e.nextElement();
                    wlEntries.put(je.getName(), je);
                }
                genericLoader = getClassLoaderFromJar(genericJarFile);
                for (Enumeration e = genericEntries.keys(); e.hasMoreElements();) {
                    String filepath = (String) e.nextElement();
                    if (wlEntries.containsKey(filepath)) {
                        JarEntry genericEntry = (JarEntry) genericEntries.get(filepath);
                        JarEntry wlEntry = (JarEntry) wlEntries.get(filepath);
                        if ((genericEntry.getCrc() != wlEntry.getCrc())
                            || (genericEntry.getSize() != wlEntry.getSize())) {
                            if (genericEntry.getName().endsWith(".class")) {
                                String classname
                                    = genericEntry.getName()
                                    .replace(File.separatorChar, '.')
                                    .replace('/', '.');
                                classname = classname.substring(0, classname.lastIndexOf(".class"));
                                Class genclass = genericLoader.loadClass(classname);
                                if (genclass.isInterface()) {
                                    log("Interface " + genclass.getName()
                                        + " has changed", Project.MSG_VERBOSE);
                                    rebuild = true;
                                    break;
                                } else {
                                    replaceEntries.put(filepath, genericEntry);
                                }
                            } else {
                                if (!genericEntry.getName().equals("META-INF/MANIFEST.MF")) {
                                    log("Non class file " + genericEntry.getName()
                                        + " has changed", Project.MSG_VERBOSE);
                                    rebuild = true;
                                    break;
                                }
                            }
                        }
                    } else {
                        log("File " + filepath + " not present in weblogic jar",
                            Project.MSG_VERBOSE);
                        rebuild = true;
                        break;
                    }
                }
                if (!rebuild) {
                    log("No rebuild needed - updating jar", Project.MSG_VERBOSE);
                    newWLJarFile = new File(weblogicJarFile.getAbsolutePath() + ".temp");
                    if (newWLJarFile.exists()) {
                        newWLJarFile.delete();
                    }
                    newJarStream = new JarOutputStream(new FileOutputStream(newWLJarFile));
                    newJarStream.setLevel(0);
                    for (Enumeration e = wlEntries.elements(); e.hasMoreElements();) {
                        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                        int bytesRead;
                        InputStream is;
                        JarEntry je = (JarEntry) e.nextElement();
                        if (je.getCompressedSize() == -1
                            || je.getCompressedSize() == je.getSize()) {
                            newJarStream.setLevel(0);
                        } else {
                            newJarStream.setLevel(JAR_COMPRESS_LEVEL);
                        }
                        if (replaceEntries.containsKey(je.getName())) {
                            log("Updating Bean class from generic Jar "
                                + je.getName(), Project.MSG_VERBOSE);
                            je = (JarEntry) replaceEntries.get(je.getName());
                            is = genericJar.getInputStream(je);
                        } else {
                            is = wlJar.getInputStream(je);
                        }
                        newJarStream.putNextEntry(new JarEntry(je.getName()));
                        while ((bytesRead = is.read(buffer)) != -1) {
                            newJarStream.write(buffer, 0, bytesRead);
                        }
                        is.close();
                    }
                } else {
                    log("Weblogic Jar rebuild needed due to changed "
                         + "interface or XML", Project.MSG_VERBOSE);
                }
            } else {
                rebuild = true;
            }
        } catch (ClassNotFoundException cnfe) {
            String cnfmsg = "ClassNotFoundException while processing ejb-jar file"
                 + ". Details: "
                 + cnfe.getMessage();
            throw new BuildException(cnfmsg, cnfe);
        } catch (IOException ioe) {
            String msg = "IOException while processing ejb-jar file "
                 + ". Details: "
                 + ioe.getMessage();
            throw new BuildException(msg, ioe);
        } finally {
            if (genericJar != null) {
                try {
                    genericJar.close();
                } catch (IOException closeException) {
                }
            }
            if (wlJar != null) {
                try {
                    wlJar.close();
                } catch (IOException closeException) {
                }
            }
            if (newJarStream != null) {
                try {
                    newJarStream.close();
                } catch (IOException closeException) {
                }
                try {
                    FILE_UTILS.rename(newWLJarFile, weblogicJarFile);
                } catch (IOException renameException) {
                    log(renameException.getMessage(), Project.MSG_WARN);
                    rebuild = true;
                }
            }
            if (genericLoader != null
                && genericLoader instanceof AntClassLoader) {
                AntClassLoader loader = (AntClassLoader) genericLoader;
                loader.cleanup();
            }
        }
        return rebuild;
    }
    protected ClassLoader getClassLoaderFromJar(File classjar) throws IOException {
        Path lookupPath = new Path(getTask().getProject());
        lookupPath.setLocation(classjar);
        Path classpath = getCombinedClasspath();
        if (classpath != null) {
            lookupPath.append(classpath);
        }
        return getTask().getProject().createClassLoader(lookupPath);
    }
}