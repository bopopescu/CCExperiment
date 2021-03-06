package org.apache.tools.ant.taskdefs.optional.javah;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.ClasspathUtils;
import org.apache.tools.ant.util.JavaEnvUtils;
public class JavahAdapterFactory {
    public static String getDefault() {
        if (JavaEnvUtils.isKaffe()) {
            return Kaffeh.IMPLEMENTATION_NAME;
        } else if (JavaEnvUtils.isGij()) {
            return Gcjh.IMPLEMENTATION_NAME;
        }
        return SunJavah.IMPLEMENTATION_NAME;
    }
    public static JavahAdapter getAdapter(String choice,
                                          ProjectComponent log)
        throws BuildException {
        return getAdapter(choice, log, null);
    }
    public static JavahAdapter getAdapter(String choice,
                                          ProjectComponent log,
                                          Path classpath)
        throws BuildException {
        if ((JavaEnvUtils.isKaffe() && choice == null)
            || Kaffeh.IMPLEMENTATION_NAME.equals(choice)) {
            return new Kaffeh();
        } else if ((JavaEnvUtils.isGij() && choice == null)
            || Gcjh.IMPLEMENTATION_NAME.equals(choice)) {
            return new Gcjh();
        } else if (SunJavah.IMPLEMENTATION_NAME.equals(choice)) {
            return new SunJavah();
        } else if (choice != null) {
            return resolveClassName(choice,
                                    log.getProject()
                                    .createClassLoader(classpath));
        }
        return new SunJavah();
    }
    private static JavahAdapter resolveClassName(String className,
                                                 ClassLoader loader)
            throws BuildException {
        return (JavahAdapter) ClasspathUtils.newInstance(className,
                loader != null ? loader :
                JavahAdapterFactory.class.getClassLoader(), JavahAdapter.class);
    }
}
