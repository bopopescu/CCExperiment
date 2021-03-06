package org.apache.tools.ant.util;
import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.Vector;
import org.apache.tools.ant.taskdefs.condition.Os;
public final class JavaEnvUtils {
    private JavaEnvUtils() {
    }
    private static final boolean IS_DOS = Os.isFamily("dos");
    private static final boolean IS_NETWARE = Os.isName("netware");
    private static final boolean IS_AIX = Os.isName("aix");
    private static final String JAVA_HOME = System.getProperty("java.home");
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private static String javaVersion;
    private static int javaVersionNumber;
    public static final String JAVA_1_0 = "1.0";
    public static final int VERSION_1_0 = 10;
    public static final String JAVA_1_1 = "1.1";
    public static final int VERSION_1_1 = 11;
    public static final String JAVA_1_2 = "1.2";
    public static final int VERSION_1_2 = 12;
    public static final String JAVA_1_3 = "1.3";
    public static final int VERSION_1_3 = 13;
    public static final String JAVA_1_4 = "1.4";
    public static final int VERSION_1_4 = 14;
    public static final String JAVA_1_5 = "1.5";
    public static final int VERSION_1_5 = 15;
    public static final String JAVA_1_6 = "1.6";
    public static final int VERSION_1_6 = 16;
    public static final String JAVA_1_7 = "1.7";
    public static final int VERSION_1_7 = 17;
    private static boolean kaffeDetected;
    private static boolean gijDetected;
    private static boolean harmonyDetected;
    private static Vector jrePackages;
    static {
        try {
            javaVersion = JAVA_1_0;
            javaVersionNumber = VERSION_1_0;
            Class.forName("java.lang.Void");
            javaVersion = JAVA_1_1;
            javaVersionNumber++;
            Class.forName("java.lang.ThreadLocal");
            javaVersion = JAVA_1_2;
            javaVersionNumber++;
            Class.forName("java.lang.StrictMath");
            javaVersion = JAVA_1_3;
            javaVersionNumber++;
            Class.forName("java.lang.CharSequence");
            javaVersion = JAVA_1_4;
            javaVersionNumber++;
            Class.forName("java.net.Proxy");
            javaVersion = JAVA_1_5;
            javaVersionNumber++;
            Class.forName("java.net.CookieStore");
            javaVersion = JAVA_1_6;
            javaVersionNumber++;
            Class.forName("java.nio.file.FileSystem");
            javaVersion = JAVA_1_7;
            javaVersionNumber++;
        } catch (Throwable t) {
        }
        kaffeDetected = false;
        try {
            Class.forName("kaffe.util.NotImplemented");
            kaffeDetected = true;
        } catch (Throwable t) {
        }
        gijDetected = false;
        try {
            Class.forName("gnu.gcj.Core");
            gijDetected = true;
        } catch (Throwable t) {
        }
        harmonyDetected = false;
        try {
            Class.forName("org.apache.harmony.luni.util.Base64");
            harmonyDetected = true;
        } catch (Throwable t) {
        }
    }
    public static String getJavaVersion() {
        return javaVersion;
    }
    public static int getJavaVersionNumber() {
        return javaVersionNumber;
    }
    public static boolean isJavaVersion(String version) {
        return javaVersion.equals(version);
    }
    public static boolean isAtLeastJavaVersion(String version) {
        return javaVersion.compareTo(version) >= 0;
    }
    public static boolean isKaffe() {
        return kaffeDetected;
    }
    public static boolean isGij() {
        return gijDetected;
    }
    public static boolean isApacheHarmony() {
        return harmonyDetected;
    }
    public static String getJreExecutable(String command) {
        if (IS_NETWARE) {
            return command;
        }
        File jExecutable = null;
        if (IS_AIX) {
            jExecutable = findInDir(JAVA_HOME + "/sh", command);
        }
        if (jExecutable == null) {
            jExecutable = findInDir(JAVA_HOME + "/bin", command);
        }
        if (jExecutable != null) {
            return jExecutable.getAbsolutePath();
        } else {
            return addExtension(command);
        }
    }
    public static String getJdkExecutable(String command) {
        if (IS_NETWARE) {
            return command;
        }
        File jExecutable = null;
        if (IS_AIX) {
            jExecutable = findInDir(JAVA_HOME + "/../sh", command);
        }
        if (jExecutable == null) {
            jExecutable = findInDir(JAVA_HOME + "/../bin", command);
        }
        if (jExecutable != null) {
            return jExecutable.getAbsolutePath();
        } else {
            return getJreExecutable(command);
        }
    }
    private static String addExtension(String command) {
        return command + (IS_DOS ? ".exe" : "");
    }
    private static File findInDir(String dirName, String commandName) {
        File dir = FILE_UTILS.normalize(dirName);
        File executable = null;
        if (dir.exists()) {
            executable = new File(dir, addExtension(commandName));
            if (!executable.exists()) {
                executable = null;
            }
        }
        return executable;
    }
    private static void buildJrePackages() {
        jrePackages = new Vector();
        switch(javaVersionNumber) {
            case VERSION_1_7:
            case VERSION_1_6:
            case VERSION_1_5:
                jrePackages.addElement("com.sun.org.apache");
            case VERSION_1_4:
                if (javaVersionNumber == VERSION_1_4) {
                    jrePackages.addElement("org.apache.crimson");
                    jrePackages.addElement("org.apache.xalan");
                    jrePackages.addElement("org.apache.xml");
                    jrePackages.addElement("org.apache.xpath");
                }
                jrePackages.addElement("org.ietf.jgss");
                jrePackages.addElement("org.w3c.dom");
                jrePackages.addElement("org.xml.sax");
            case VERSION_1_3:
                jrePackages.addElement("org.omg");
                jrePackages.addElement("com.sun.corba");
                jrePackages.addElement("com.sun.jndi");
                jrePackages.addElement("com.sun.media");
                jrePackages.addElement("com.sun.naming");
                jrePackages.addElement("com.sun.org.omg");
                jrePackages.addElement("com.sun.rmi");
                jrePackages.addElement("sunw.io");
                jrePackages.addElement("sunw.util");
            case VERSION_1_2:
                jrePackages.addElement("com.sun.java");
                jrePackages.addElement("com.sun.image");
            case VERSION_1_1:
            default:
                jrePackages.addElement("sun");
                jrePackages.addElement("java");
                jrePackages.addElement("javax");
                break;
        }
    }
    public static Vector getJrePackageTestCases() {
        Vector tests = new Vector();
        tests.addElement("java.lang.Object");
        switch(javaVersionNumber) {
            case VERSION_1_7:
            case VERSION_1_6:
            case VERSION_1_5:
                tests.addElement(
                    "com.sun.org.apache.xerces.internal.jaxp.datatype.DatatypeFactoryImpl ");
            case VERSION_1_4:
                tests.addElement("sun.audio.AudioPlayer");
                if (javaVersionNumber == VERSION_1_4) {
                    tests.addElement("org.apache.crimson.parser.ContentModel");
                    tests.addElement("org.apache.xalan.processor.ProcessorImport");
                    tests.addElement("org.apache.xml.utils.URI");
                    tests.addElement("org.apache.xpath.XPathFactory");
                }
                tests.addElement("org.ietf.jgss.Oid");
                tests.addElement("org.w3c.dom.Attr");
                tests.addElement("org.xml.sax.XMLReader");
            case VERSION_1_3:
                tests.addElement("org.omg.CORBA.Any");
                tests.addElement("com.sun.corba.se.internal.corba.AnyImpl");
                tests.addElement("com.sun.jndi.ldap.LdapURL");
                tests.addElement("com.sun.media.sound.Printer");
                tests.addElement("com.sun.naming.internal.VersionHelper");
                tests.addElement("com.sun.org.omg.CORBA.Initializer");
                tests.addElement("sunw.io.Serializable");
                tests.addElement("sunw.util.EventListener");
            case VERSION_1_2:
                tests.addElement("javax.accessibility.Accessible");
                tests.addElement("sun.misc.BASE64Encoder");
                tests.addElement("com.sun.image.codec.jpeg.JPEGCodec");
            case VERSION_1_1:
            default:
                tests.addElement("sun.reflect.SerializationConstructorAccessorImpl");
                tests.addElement("sun.net.www.http.HttpClient");
                tests.addElement("sun.audio.AudioPlayer");
                break;
        }
        return tests;
    }
    public static Vector getJrePackages() {
        if (jrePackages == null) {
            buildJrePackages();
        }
        return jrePackages;
    }
    public static File createVmsJavaOptionFile(String[] cmd)
            throws IOException {
        File script = FILE_UTILS.createTempFile("ANT", ".JAVA_OPTS", null, false, true);
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(script));
            for (int i = 0; i < cmd.length; i++) {
                out.write(cmd[i]);
                out.newLine();
            }
        } finally {
            FileUtils.close(out);
        }
        return script;
    }
    public static String getJavaHome() {
        return JAVA_HOME;
    }
}
