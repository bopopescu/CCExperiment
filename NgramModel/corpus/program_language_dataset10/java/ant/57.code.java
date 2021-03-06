package org.apache.tools.ant;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.JAXPUtils;
import org.apache.tools.ant.util.ProxySetup;
import org.apache.tools.ant.util.JavaEnvUtils;
import org.apache.tools.ant.launch.Launcher;
import org.xml.sax.XMLReader;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Calendar;
import java.util.TimeZone;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
public final class Diagnostics {
    private static final int JAVA_1_5_NUMBER = 15;
    private static final int BIG_DRIFT_LIMIT = 10000;
    private static final int TEST_FILE_SIZE = 32;
    private static final int KILOBYTE = 1024;
    private static final int SECONDS_PER_MILLISECOND = 1000;
    private static final int SECONDS_PER_MINUTE = 60;
    private static final int MINUTES_PER_HOUR = 60;
    protected static final String ERROR_PROPERTY_ACCESS_BLOCKED
            = "Access to this property blocked by a security manager";
    private Diagnostics() {
    }
    public static boolean isOptionalAvailable() {
        return true;
    }
    public static void validateVersion() throws BuildException {
    }
    public static File[] listLibraries() {
        String home = System.getProperty(MagicNames.ANT_HOME);
        if (home == null) {
            return null;
        }
        File libDir = new File(home, "lib");
        return listJarFiles(libDir);
    }
    private static File[] listJarFiles(File libDir) {
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        };
        File[] files  = libDir.listFiles(filter);
        return files;
    }
    public static void main(String[] args) {
        doReport(System.out);
    }
    private static String getImplementationVersion(Class clazz) {
        return clazz.getPackage().getImplementationVersion();
    }
    private static URL getClassLocation(Class clazz) {
        if (clazz.getProtectionDomain().getCodeSource() == null) {
            return null;
        }
        return clazz.getProtectionDomain().getCodeSource().getLocation();
    }
    private static String getXMLParserName() {
        SAXParser saxParser = getSAXParser();
        if (saxParser == null) {
            return "Could not create an XML Parser";
        }
        String saxParserName = saxParser.getClass().getName();
        return saxParserName;
    }
    private static String getXSLTProcessorName() {
        Transformer transformer = getXSLTProcessor();
        if (transformer == null) {
            return "Could not create an XSLT Processor";
        }
        String processorName = transformer.getClass().getName();
        return processorName;
    }
    private static SAXParser getSAXParser() {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        if (saxParserFactory == null) {
            return null;
        }
        SAXParser saxParser = null;
        try {
            saxParser = saxParserFactory.newSAXParser();
        } catch (Exception e) {
            ignoreThrowable(e);
        }
        return saxParser;
    }
    private static Transformer getXSLTProcessor() {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        if (transformerFactory == null) {
            return null;
        }
        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (Exception e) {
            ignoreThrowable(e);
        }
        return transformer;
    }
    private static String getXMLParserLocation() {
        SAXParser saxParser = getSAXParser();
        if (saxParser == null) {
            return null;
        }
        URL location = getClassLocation(saxParser.getClass());
        return location != null ? location.toString() : null;
    }
    private static String getNamespaceParserName() {
        try {
            XMLReader reader = JAXPUtils.getNamespaceXMLReader();
            return reader.getClass().getName();
        } catch (BuildException e) {
            ignoreThrowable(e);
            return null;
        }
    }
    private static String getNamespaceParserLocation() {
        try {
            XMLReader reader = JAXPUtils.getNamespaceXMLReader();
            URL location = getClassLocation(reader.getClass());
            return location != null ? location.toString() : null;
        } catch (BuildException e) {
            ignoreThrowable(e);
            return null;
        }
    }
    private static String getXSLTProcessorLocation() {
        Transformer transformer = getXSLTProcessor();
        if (transformer == null) {
            return null;
        }
        URL location = getClassLocation(transformer.getClass());
        return location != null ? location.toString() : null;
    }
    private static void ignoreThrowable(Throwable thrown) {
    }
    public static void doReport(PrintStream out) {
        doReport(out, Project.MSG_INFO);
    }
    public static void doReport(PrintStream out, int logLevel) {
        out.println("------- Ant diagnostics report -------");
        out.println(Main.getAntVersion());
        header(out, "Implementation Version");
        out.println("core tasks     : " + getImplementationVersion(Main.class)
                    + " in " + getClassLocation(Main.class));
        header(out, "ANT PROPERTIES");
        doReportAntProperties(out);
        header(out, "ANT_HOME/lib jar listing");
        doReportAntHomeLibraries(out);
        header(out, "USER_HOME/.ant/lib jar listing");
        doReportUserHomeLibraries(out);
        header(out, "Tasks availability");
        doReportTasksAvailability(out);
        header(out, "org.apache.env.Which diagnostics");
        doReportWhich(out);
        header(out, "XML Parser information");
        doReportParserInfo(out);
        header(out, "XSLT Processor information");
        doReportXSLTProcessorInfo(out);
        header(out, "System properties");
        doReportSystemProperties(out);
        header(out, "Temp dir");
        doReportTempDir(out);
        header(out, "Locale information");
        doReportLocale(out);
        header(out, "Proxy information");
        doReportProxy(out);
        out.println();
    }
    private static void header(PrintStream out, String section) {
        out.println();
        out.println("-------------------------------------------");
        out.print(" ");
        out.println(section);
        out.println("-------------------------------------------");
    }
    private static void doReportSystemProperties(PrintStream out) {
        Properties sysprops = null;
        try {
            sysprops = System.getProperties();
        } catch (SecurityException  e) {
            ignoreThrowable(e);
            out.println("Access to System.getProperties() blocked " + "by a security manager");
        }
        for (Enumeration keys = sysprops.propertyNames();
            keys.hasMoreElements();) {
            String key = (String) keys.nextElement();
            String value = getProperty(key);
            out.println(key + " : " + value);
        }
    }
    private static String getProperty(String key) {
        String value;
        try {
            value = System.getProperty(key);
        } catch (SecurityException e) {
            value = ERROR_PROPERTY_ACCESS_BLOCKED;
        }
        return value;
    }
    private static void doReportAntProperties(PrintStream out) {
        Project p = new Project();
        p.initProperties();
        out.println(MagicNames.ANT_VERSION + ": " + p.getProperty(MagicNames.ANT_VERSION));
        out.println(MagicNames.ANT_JAVA_VERSION + ": "
                + p.getProperty(MagicNames.ANT_JAVA_VERSION));
        out.println("Is this the Apache Harmony VM? "
                    + (JavaEnvUtils.isApacheHarmony() ? "yes" : "no"));
        out.println("Is this the Kaffe VM? "
                    + (JavaEnvUtils.isKaffe() ? "yes" : "no"));
        out.println("Is this gij/gcj? "
                    + (JavaEnvUtils.isGij() ? "yes" : "no"));
        out.println(MagicNames.ANT_LIB + ": " + p.getProperty(MagicNames.ANT_LIB));
        out.println(MagicNames.ANT_HOME + ": " + p.getProperty(MagicNames.ANT_HOME));
    }
    private static void doReportAntHomeLibraries(PrintStream out) {
        out.println(MagicNames.ANT_HOME + ": " + System.getProperty(MagicNames.ANT_HOME));
        File[] libs = listLibraries();
        printLibraries(libs, out);
    }
    private static void doReportUserHomeLibraries(PrintStream out) {
        String home = System.getProperty(Launcher.USER_HOMEDIR);
        out.println("user.home: " + home);
        File libDir = new File(home, Launcher.USER_LIBDIR);
        File[] libs = listJarFiles(libDir);
        printLibraries(libs, out);
    }
    private static void printLibraries(File[] libs, PrintStream out) {
        if (libs == null) {
            out.println("No such directory.");
            return;
        }
        for (int i = 0; i < libs.length; i++) {
            out.println(libs[i].getName() + " (" + libs[i].length() + " bytes)");
        }
    }
    private static void doReportWhich(PrintStream out) {
        Throwable error = null;
        try {
            Class which = Class.forName("org.apache.env.Which");
            Method method = which.getMethod(
                "main", new Class[] {String[].class});
            method.invoke(null, new Object[]{new String[]{}});
        } catch (ClassNotFoundException e) {
            out.println("Not available.");
            out.println("Download it at http://xml.apache.org/commons/");
        } catch (InvocationTargetException e) {
            error = e.getTargetException() == null ? e : e.getTargetException();
        } catch (Throwable e) {
            error = e;
        }
        if (error != null) {
            out.println("Error while running org.apache.env.Which");
            error.printStackTrace();
        }
    }
    private static void doReportTasksAvailability(PrintStream out) {
        InputStream is = Main.class.getResourceAsStream(
                MagicNames.TASKDEF_PROPERTIES_RESOURCE);
        if (is == null) {
            out.println("None available");
        } else {
            Properties props = new Properties();
            try {
                props.load(is);
                for (Enumeration keys = props.keys(); keys.hasMoreElements();) {
                    String key = (String) keys.nextElement();
                    String classname = props.getProperty(key);
                    try {
                        Class.forName(classname);
                        props.remove(key);
                    } catch (ClassNotFoundException e) {
                        out.println(key + " : Not Available "
                                + "(the implementation class is not present)");
                    } catch (NoClassDefFoundError e) {
                        String pkg = e.getMessage().replace('/', '.');
                        out.println(key + " : Missing dependency " + pkg);
                    } catch (LinkageError e) {
                        out.println(key + " : Initialization error");
                    }
                }
                if (props.size() == 0) {
                    out.println("All defined tasks are available");
                } else {
                    out.println("A task being missing/unavailable should only "
                            + "matter if you are trying to use it");
                }
            } catch (IOException e) {
                out.println(e.getMessage());
            }
        }
    }
    private static void doReportParserInfo(PrintStream out) {
        String parserName = getXMLParserName();
        String parserLocation = getXMLParserLocation();
        printParserInfo(out, "XML Parser", parserName, parserLocation);
        printParserInfo(out, "Namespace-aware parser", getNamespaceParserName(),
                getNamespaceParserLocation());
    }
    private static void doReportXSLTProcessorInfo(PrintStream out) {
        String processorName = getXSLTProcessorName();
        String processorLocation = getXSLTProcessorLocation();
        printParserInfo(out, "XSLT Processor", processorName, processorLocation);
    }
    private static void printParserInfo(PrintStream out, String parserType, String parserName,
            String parserLocation) {
        if (parserName == null) {
            parserName = "unknown";
        }
        if (parserLocation == null) {
            parserLocation = "unknown";
        }
        out.println(parserType + " : " + parserName);
        out.println(parserType + " Location: " + parserLocation);
    }
    private static void doReportTempDir(PrintStream out) {
        String tempdir = System.getProperty("java.io.tmpdir");
        if (tempdir == null) {
            out.println("Warning: java.io.tmpdir is undefined");
            return;
        }
        out.println("Temp dir is " + tempdir);
        File tempDirectory = new File(tempdir);
        if (!tempDirectory.exists()) {
            out.println("Warning, java.io.tmpdir directory does not exist: " + tempdir);
            return;
        }
        long now = System.currentTimeMillis();
        File tempFile = null;
        FileOutputStream fileout = null;
        FileInputStream filein = null;
        try {
            tempFile = File.createTempFile("diag", "txt", tempDirectory);
            fileout = new FileOutputStream(tempFile);
            byte[] buffer = new byte[KILOBYTE];
            for (int i = 0; i < TEST_FILE_SIZE; i++) {
                fileout.write(buffer);
            }
            fileout.close();
            fileout = null;
            Thread.sleep(1000);
            filein = new FileInputStream(tempFile);
            int total = 0;
            int read = 0;
            while ((read = filein.read(buffer, 0, KILOBYTE)) > 0) {
                total += read;
            }
            filein.close();
            filein = null;
            long filetime = tempFile.lastModified();
            long drift = filetime - now;
            tempFile.delete();
            out.print("Temp dir is writeable");
            if (total != TEST_FILE_SIZE * KILOBYTE) {
                out.println(", but seems to be full.  Wrote "
                            + (TEST_FILE_SIZE * KILOBYTE)
                            + "but could only read " + total + " bytes.");
            } else {
                out.println();
            }
            out.println("Temp dir alignment with system clock is " + drift + " ms");
            if (Math.abs(drift) > BIG_DRIFT_LIMIT) {
                out.println("Warning: big clock drift -maybe a network filesystem");
            }
        } catch (IOException e) {
            ignoreThrowable(e);
            out.println("Failed to create a temporary file in the temp dir " + tempdir);
            out.println("File  " + tempFile + " could not be created/written to");
        } catch (InterruptedException e) {
            ignoreThrowable(e);
            out.println("Failed to check whether tempdir is writable");
        } finally {
            FileUtils.close(fileout);
            FileUtils.close(filein);
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    private static void doReportLocale(PrintStream out) {
        Calendar cal = Calendar.getInstance();
        TimeZone tz = cal.getTimeZone();
        out.println("Timezone "
                + tz.getDisplayName()
                + " offset="
                + tz.getOffset(cal.get(Calendar.ERA), cal.get(Calendar.YEAR), cal
                        .get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH), cal
                        .get(Calendar.DAY_OF_WEEK), ((cal.get(Calendar.HOUR_OF_DAY)
                        * MINUTES_PER_HOUR + cal.get(Calendar.MINUTE))
                        * SECONDS_PER_MINUTE + cal.get(Calendar.SECOND))
                        * SECONDS_PER_MILLISECOND + cal.get(Calendar.MILLISECOND)));
    }
    private static void printProperty(PrintStream out, String key) {
        String value = getProperty(key);
        if (value != null) {
            out.print(key);
            out.print(" = ");
            out.print('"');
            out.print(value);
            out.println('"');
        }
    }
    private static void doReportProxy(PrintStream out) {
        printProperty(out, ProxySetup.HTTP_PROXY_HOST);
        printProperty(out, ProxySetup.HTTP_PROXY_PORT);
        printProperty(out, ProxySetup.HTTP_PROXY_USERNAME);
        printProperty(out, ProxySetup.HTTP_PROXY_PASSWORD);
        printProperty(out, ProxySetup.HTTP_NON_PROXY_HOSTS);
        printProperty(out, ProxySetup.HTTPS_PROXY_HOST);
        printProperty(out, ProxySetup.HTTPS_PROXY_PORT);
        printProperty(out, ProxySetup.HTTPS_NON_PROXY_HOSTS);
        printProperty(out, ProxySetup.FTP_PROXY_HOST);
        printProperty(out, ProxySetup.FTP_PROXY_PORT);
        printProperty(out, ProxySetup.FTP_NON_PROXY_HOSTS);
        printProperty(out, ProxySetup.SOCKS_PROXY_HOST);
        printProperty(out, ProxySetup.SOCKS_PROXY_PORT);
        printProperty(out, ProxySetup.SOCKS_PROXY_USERNAME);
        printProperty(out, ProxySetup.SOCKS_PROXY_PASSWORD);
        if (JavaEnvUtils.getJavaVersionNumber() < JAVA_1_5_NUMBER) {
            return;
        }
        printProperty(out, ProxySetup.USE_SYSTEM_PROXIES);
        final String proxyDiagClassname = "org.apache.tools.ant.util.java15.ProxyDiagnostics";
        try {
            Class proxyDiagClass = Class.forName(proxyDiagClassname);
            Object instance = proxyDiagClass.newInstance();
            out.println("Java1.5+ proxy settings:");
            out.println(instance.toString());
        } catch (ClassNotFoundException e) {
        } catch (IllegalAccessException e) {
        } catch (InstantiationException e) {
        } catch (NoClassDefFoundError e) {
        }
    }
}
