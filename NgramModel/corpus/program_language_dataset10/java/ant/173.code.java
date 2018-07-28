package org.apache.tools.ant.taskdefs;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.MagicNames;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.Path;
import java.io.File;
public class Classloader extends Task {
    public static final String SYSTEM_LOADER_REF = MagicNames.SYSTEM_LOADER_REF;
    private String name = null;
    private Path classpath;
    private boolean reset = false;
    private boolean parentFirst = true;
    private String parentName = null;
    public Classloader() {
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setReset(boolean b) {
        this.reset = b;
    }
    public void setReverse(boolean b) {
        this.parentFirst = !b;
    }
    public void setParentFirst(boolean b) {
        this.parentFirst = b;
    }
    public void setParentName(String name) {
        this.parentName = name;
    }
    public void setClasspathRef(Reference pathRef) throws BuildException {
        classpath = (Path) pathRef.getReferencedObject(getProject());
    }
    public void setClasspath(Path classpath) {
        if (this.classpath == null) {
            this.classpath = classpath;
        } else {
            this.classpath.append(classpath);
        }
    }
    public Path createClasspath() {
        if (this.classpath == null) {
            this.classpath = new Path(null);
        }
        return this.classpath.createPath();
    }
    public void execute() {
        try {
            if ("only".equals(getProject().getProperty("build.sysclasspath"))
                && (name == null || SYSTEM_LOADER_REF.equals(name))) {
                log("Changing the system loader is disabled "
                    + "by build.sysclasspath=only", Project.MSG_WARN);
                return;
            }
            String loaderName = (name == null) ? SYSTEM_LOADER_REF : name;
            Object obj = getProject().getReference(loaderName);
            if (reset) {
                obj = null; 
            }
            if (obj != null && !(obj instanceof AntClassLoader)) {
                log("Referenced object is not an AntClassLoader",
                        Project.MSG_ERR);
                return;
            }
            AntClassLoader acl = (AntClassLoader) obj;
            boolean existingLoader = acl != null;
            if (acl == null) {
                Object parent = null;
                if (parentName != null) {
                    parent = getProject().getReference(parentName);
                    if (!(parent instanceof ClassLoader)) {
                        parent = null;
                    }
                }
                if (parent == null) {
                    parent = this.getClass().getClassLoader();
                }
                if (name == null) {
                }
                getProject().log("Setting parent loader " + name + " "
                    + parent + " " + parentFirst, Project.MSG_DEBUG);
                acl = AntClassLoader.newAntClassLoader((ClassLoader) parent,
                         getProject(), classpath, parentFirst);
                getProject().addReference(loaderName, acl);
                if (name == null) {
                    acl.addLoaderPackageRoot("org.apache.tools.ant.taskdefs.optional");
                    getProject().setCoreLoader(acl);
                }
            }
            if (existingLoader && classpath != null) {
                String[] list = classpath.list();
                for (int i = 0; i < list.length; i++) {
                    File f = new File(list[i]);
                    if (f.exists()) {
                        log("Adding to class loader " +  acl + " " + f.getAbsolutePath(),
                                Project.MSG_DEBUG);
                        acl.addPathElement(f.getAbsolutePath());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}