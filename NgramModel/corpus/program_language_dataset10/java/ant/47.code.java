package org.apache.tools.ant;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.util.CollectionUtils;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.JavaEnvUtils;
import org.apache.tools.ant.util.LoaderUtils;
import org.apache.tools.ant.util.ReflectUtil;
import org.apache.tools.ant.util.VectorSet;
import org.apache.tools.ant.launch.Locator;
public class AntClassLoader extends ClassLoader implements SubBuildListener {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private class ResourceEnumeration implements Enumeration {
        private String resourceName;
        private int pathElementsIndex;
        private URL nextResource;
        ResourceEnumeration(String name) {
            this.resourceName = name;
            this.pathElementsIndex = 0;
            findNextResource();
        }
        public boolean hasMoreElements() {
            return (this.nextResource != null);
        }
        public Object nextElement() {
            URL ret = this.nextResource;
            findNextResource();
            return ret;
        }
        private void findNextResource() {
            URL url = null;
            while ((pathElementsIndex < pathComponents.size()) && (url == null)) {
                try {
                    File pathComponent = (File) pathComponents.elementAt(pathElementsIndex);
                    url = getResourceURL(pathComponent, this.resourceName);
                    pathElementsIndex++;
                } catch (BuildException e) {
                }
            }
            this.nextResource = url;
        }
    }
    private static final int BUFFER_SIZE = 8192;
    private static final int NUMBER_OF_STRINGS = 256;
    private Vector pathComponents  = new VectorSet();
    private Project project;
    private boolean parentFirst = true;
    private Vector systemPackages = new Vector();
    private Vector loaderPackages = new Vector();
    private boolean ignoreBase = false;
    private ClassLoader parent = null;
    private Hashtable jarFiles = new Hashtable();
    private static Map pathMap = Collections.synchronizedMap(new HashMap());
    private ClassLoader savedContextLoader = null;
    private boolean isContextLoaderSaved = false;
    public AntClassLoader(ClassLoader parent, Project project, Path classpath) {
        setParent(parent);
        setClassPath(classpath);
        setProject(project);
    }
    public AntClassLoader() {
        setParent(null);
    }
    public AntClassLoader(Project project, Path classpath) {
        setParent(null);
        setProject(project);
        setClassPath(classpath);
    }
    public AntClassLoader(
        ClassLoader parent, Project project, Path classpath, boolean parentFirst) {
        this(project, classpath);
        if (parent != null) {
            setParent(parent);
        }
        setParentFirst(parentFirst);
        addJavaLibraries();
    }
    public AntClassLoader(Project project, Path classpath, boolean parentFirst) {
        this(null, project, classpath, parentFirst);
    }
    public AntClassLoader(ClassLoader parent, boolean parentFirst) {
        setParent(parent);
        project = null;
        this.parentFirst = parentFirst;
    }
    public void setProject(Project project) {
        this.project = project;
        if (project != null) {
            project.addBuildListener(this);
        }
    }
    public void setClassPath(Path classpath) {
        pathComponents.removeAllElements();
        if (classpath != null) {
            Path actualClasspath = classpath.concatSystemClasspath("ignore");
            String[] pathElements = actualClasspath.list();
            for (int i = 0; i < pathElements.length; ++i) {
                try {
                    addPathElement(pathElements[i]);
                } catch (BuildException e) {
                }
            }
        }
    }
    public void setParent(ClassLoader parent) {
        this.parent = parent == null ? AntClassLoader.class.getClassLoader() : parent;
    }
    public void setParentFirst(boolean parentFirst) {
        this.parentFirst = parentFirst;
    }
    protected void log(String message, int priority) {
        if (project != null) {
            project.log(message, priority);
        }
    }
    public void setThreadContextLoader() {
        if (isContextLoaderSaved) {
            throw new BuildException("Context loader has not been reset");
        }
        if (LoaderUtils.isContextLoaderAvailable()) {
            savedContextLoader = LoaderUtils.getContextClassLoader();
            ClassLoader loader = this;
            if (project != null && "only".equals(project.getProperty("build.sysclasspath"))) {
                loader = this.getClass().getClassLoader();
            }
            LoaderUtils.setContextClassLoader(loader);
            isContextLoaderSaved = true;
        }
    }
    public void resetThreadContextLoader() {
        if (LoaderUtils.isContextLoaderAvailable() && isContextLoaderSaved) {
            LoaderUtils.setContextClassLoader(savedContextLoader);
            savedContextLoader = null;
            isContextLoaderSaved = false;
        }
    }
    public void addPathElement(String pathElement) throws BuildException {
        File pathComponent = project != null ? project.resolveFile(pathElement) : new File(
                pathElement);
        try {
            addPathFile(pathComponent);
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }
    public void addPathComponent(File file) {
        if (pathComponents.contains(file)) {
            return;
        }
        pathComponents.addElement(file);
    }
    protected void addPathFile(File pathComponent) throws IOException {
        if (!pathComponents.contains(pathComponent)) {
            pathComponents.addElement(pathComponent);
        }
        if (pathComponent.isDirectory()) {
            return;
        }
        String absPathPlusTimeAndLength = pathComponent.getAbsolutePath()
                + pathComponent.lastModified() + "-" + pathComponent.length();
        String classpath = (String) pathMap.get(absPathPlusTimeAndLength);
        if (classpath == null) {
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(pathComponent);
                Manifest manifest = jarFile.getManifest();
                if (manifest == null) {
                    return;
                }
                classpath = manifest.getMainAttributes()
                    .getValue(Attributes.Name.CLASS_PATH);
            } finally {
                if (jarFile != null) {
                    jarFile.close();
                }
            }
            if (classpath == null) {
                classpath = "";
            }
            pathMap.put(absPathPlusTimeAndLength, classpath);
        }
        if (!"".equals(classpath)) {
            URL baseURL = FILE_UTILS.getFileURL(pathComponent);
            StringTokenizer st = new StringTokenizer(classpath);
            while (st.hasMoreTokens()) {
                String classpathElement = st.nextToken();
                URL libraryURL = new URL(baseURL, classpathElement);
                if (!libraryURL.getProtocol().equals("file")) {
                    log("Skipping jar library " + classpathElement
                            + " since only relative URLs are supported by this" + " loader",
                            Project.MSG_VERBOSE);
                    continue;
                }
                String decodedPath = Locator.decodeUri(libraryURL.getFile());
                File libraryFile = new File(decodedPath);
                if (libraryFile.exists() && !isInPath(libraryFile)) {
                    addPathFile(libraryFile);
                }
            }
        }
    }
    public String getClasspath() {
        StringBuffer sb = new StringBuffer();
        boolean firstPass = true;
        Enumeration componentEnum = pathComponents.elements();
        while (componentEnum.hasMoreElements()) {
            if (!firstPass) {
                sb.append(System.getProperty("path.separator"));
            } else {
                firstPass = false;
            }
            sb.append(((File) componentEnum.nextElement()).getAbsolutePath());
        }
        return sb.toString();
    }
    public synchronized void setIsolated(boolean isolated) {
        ignoreBase = isolated;
    }
    public static void initializeClass(Class theClass) {
        final Constructor[] cons = theClass.getDeclaredConstructors();
        if (cons != null) {
            if (cons.length > 0 && cons[0] != null) {
                final String[] strs = new String[NUMBER_OF_STRINGS];
                try {
                    cons[0].newInstance((Object[]) strs);
                } catch (Exception e) {
                }
            }
        }
    }
    public void addSystemPackageRoot(String packageRoot) {
        systemPackages.addElement(packageRoot + (packageRoot.endsWith(".") ? "" : "."));
    }
    public void addLoaderPackageRoot(String packageRoot) {
        loaderPackages.addElement(packageRoot + (packageRoot.endsWith(".") ? "" : "."));
    }
    public Class forceLoadClass(String classname) throws ClassNotFoundException {
        log("force loading " + classname, Project.MSG_DEBUG);
        Class theClass = findLoadedClass(classname);
        if (theClass == null) {
            theClass = findClass(classname);
        }
        return theClass;
    }
    public Class forceLoadSystemClass(String classname) throws ClassNotFoundException {
        log("force system loading " + classname, Project.MSG_DEBUG);
        Class theClass = findLoadedClass(classname);
        if (theClass == null) {
            theClass = findBaseClass(classname);
        }
        return theClass;
    }
    public InputStream getResourceAsStream(String name) {
        InputStream resourceStream = null;
        if (isParentFirst(name)) {
            resourceStream = loadBaseResource(name);
        }
        if (resourceStream != null) {
            log("ResourceStream for " + name
                + " loaded from parent loader", Project.MSG_DEBUG);
        } else {
            resourceStream = loadResource(name);
            if (resourceStream != null) {
                log("ResourceStream for " + name
                    + " loaded from ant loader", Project.MSG_DEBUG);
            }
        }
        if (resourceStream == null && !isParentFirst(name)) {
            if (ignoreBase) {
                resourceStream = getRootLoader() == null ? null : getRootLoader().getResourceAsStream(name);
            } else {
                resourceStream = loadBaseResource(name);
            }
            if (resourceStream != null) {
                log("ResourceStream for " + name + " loaded from parent loader",
                    Project.MSG_DEBUG);
            }
        }
        if (resourceStream == null) {
            log("Couldn't load ResourceStream for " + name, Project.MSG_DEBUG);
        }
        return resourceStream;
    }
    private InputStream loadResource(String name) {
        InputStream stream = null;
        Enumeration e = pathComponents.elements();
        while (e.hasMoreElements() && stream == null) {
            File pathComponent = (File) e.nextElement();
            stream = getResourceStream(pathComponent, name);
        }
        return stream;
    }
    private InputStream loadBaseResource(String name) {
        return parent == null ? super.getResourceAsStream(name) : parent.getResourceAsStream(name);
    }
    private InputStream getResourceStream(File file, String resourceName) {
        try {
            JarFile jarFile = (JarFile) jarFiles.get(file);
            if (jarFile == null && file.isDirectory()) {
                File resource = new File(file, resourceName);
                if (resource.exists()) {
                    return new FileInputStream(resource);
                }
            } else {
                if (jarFile == null) {
                    if (file.exists()) {
                        jarFile = new JarFile(file);
                        jarFiles.put(file, jarFile);
                    } else {
                        return null;
                    }
                    jarFile = (JarFile) jarFiles.get(file);
                }
                JarEntry entry = jarFile.getJarEntry(resourceName);
                if (entry != null) {
                    return jarFile.getInputStream(entry);
                }
            }
        } catch (Exception e) {
            log("Ignoring Exception " + e.getClass().getName() + ": " + e.getMessage()
                    + " reading resource " + resourceName + " from " + file, Project.MSG_VERBOSE);
        }
        return null;
    }
    private boolean isParentFirst(String resourceName) {
        boolean useParentFirst = parentFirst;
        for (Enumeration e = systemPackages.elements(); e.hasMoreElements();) {
            String packageName = (String) e.nextElement();
            if (resourceName.startsWith(packageName)) {
                useParentFirst = true;
                break;
            }
        }
        for (Enumeration e = loaderPackages.elements(); e.hasMoreElements();) {
            String packageName = (String) e.nextElement();
            if (resourceName.startsWith(packageName)) {
                useParentFirst = false;
                break;
            }
        }
        return useParentFirst;
    }
    private ClassLoader getRootLoader() {
        ClassLoader ret = getClass().getClassLoader();
        while (ret != null && ret.getParent() != null) {
            ret = ret.getParent();
        }
        return ret;
    }
    public URL getResource(String name) {
        URL url = null;
        if (isParentFirst(name)) {
            url = parent == null ? super.getResource(name) : parent.getResource(name);
        }
        if (url != null) {
            log("Resource " + name + " loaded from parent loader", Project.MSG_DEBUG);
        } else {
            Enumeration e = pathComponents.elements();
            while (e.hasMoreElements() && url == null) {
                File pathComponent = (File) e.nextElement();
                url = getResourceURL(pathComponent, name);
                if (url != null) {
                    log("Resource " + name + " loaded from ant loader", Project.MSG_DEBUG);
                }
            }
        }
        if (url == null && !isParentFirst(name)) {
            if (ignoreBase) {
                url = getRootLoader() == null ? null : getRootLoader().getResource(name);
            } else {
                url = parent == null ? super.getResource(name) : parent.getResource(name);
            }
            if (url != null) {
                log("Resource " + name + " loaded from parent loader", Project.MSG_DEBUG);
            }
        }
        if (url == null) {
            log("Couldn't load Resource " + name, Project.MSG_DEBUG);
        }
        return url;
    }
    public Enumeration getNamedResources(String name)
        throws IOException {
        return findResources(name, false);
    }
    protected Enumeration findResources(String name) throws IOException {
        return findResources(name, true);
    }
    protected Enumeration findResources(String name,
                                                 boolean parentHasBeenSearched)
        throws IOException {
        Enumeration mine = new ResourceEnumeration(name);
        Enumeration base;
        if (parent != null && (!parentHasBeenSearched || parent != getParent())) {
            base = parent.getResources(name);
        } else {
            base = new CollectionUtils.EmptyEnumeration();
        }
        if (isParentFirst(name)) {
            return CollectionUtils.append(base, mine);
        }
        if (ignoreBase) {
            return getRootLoader() == null ? mine : CollectionUtils.append(mine, getRootLoader()
                    .getResources(name));
        }
        return CollectionUtils.append(mine, base);
    }
    protected URL getResourceURL(File file, String resourceName) {
        try {
            JarFile jarFile = (JarFile) jarFiles.get(file);
            if (jarFile == null && file.isDirectory()) {
                File resource = new File(file, resourceName);
                if (resource.exists()) {
                    try {
                        return FILE_UTILS.getFileURL(resource);
                    } catch (MalformedURLException ex) {
                        return null;
                    }
                }
            } else {
                if (jarFile == null) {
                    if (file.exists()) {
                        jarFile = new JarFile(file);
                        jarFiles.put(file, jarFile);
                    } else {
                        return null;
                    }
                    jarFile = (JarFile) jarFiles.get(file);
                }
                JarEntry entry = jarFile.getJarEntry(resourceName);
                if (entry != null) {
                    try {
                        return new URL("jar:" + FILE_UTILS.getFileURL(file) + "!/" + entry);
                    } catch (MalformedURLException ex) {
                        return null;
                    }
                }
            }
        } catch (Exception e) {
            String msg = "Unable to obtain resource from " + file + ": ";
            log(msg + e, Project.MSG_WARN);
            System.err.println(msg);
            e.printStackTrace();
        }
        return null;
    }
    protected synchronized Class loadClass(String classname, boolean resolve)
            throws ClassNotFoundException {
        Class theClass = findLoadedClass(classname);
        if (theClass != null) {
            return theClass;
        }
        if (isParentFirst(classname)) {
            try {
                theClass = findBaseClass(classname);
                log("Class " + classname + " loaded from parent loader " + "(parentFirst)",
                        Project.MSG_DEBUG);
            } catch (ClassNotFoundException cnfe) {
                theClass = findClass(classname);
                log("Class " + classname + " loaded from ant loader " + "(parentFirst)",
                        Project.MSG_DEBUG);
            }
        } else {
            try {
                theClass = findClass(classname);
                log("Class " + classname + " loaded from ant loader", Project.MSG_DEBUG);
            } catch (ClassNotFoundException cnfe) {
                if (ignoreBase) {
                    throw cnfe;
                }
                theClass = findBaseClass(classname);
                log("Class " + classname + " loaded from parent loader", Project.MSG_DEBUG);
            }
        }
        if (resolve) {
            resolveClass(theClass);
        }
        return theClass;
    }
    private String getClassFilename(String classname) {
        return classname.replace('.', '/') + ".class";
    }
    protected Class defineClassFromData(File container, byte[] classData, String classname)
            throws IOException {
        definePackage(container, classname);
        ProtectionDomain currentPd = Project.class.getProtectionDomain();
        String classResource = getClassFilename(classname);
        CodeSource src = new CodeSource(FILE_UTILS.getFileURL(container),
                                        getCertificates(container,
                                                        classResource));
        ProtectionDomain classesPd =
            new ProtectionDomain(src, currentPd.getPermissions(),
                                 this,
                                 currentPd.getPrincipals());
        return defineClass(classname, classData, 0, classData.length,
                           classesPd);
    }
    protected void definePackage(File container, String className) throws IOException {
        int classIndex = className.lastIndexOf('.');
        if (classIndex == -1) {
            return;
        }
        String packageName = className.substring(0, classIndex);
        if (getPackage(packageName) != null) {
            return;
        }
        Manifest manifest = getJarManifest(container);
        if (manifest == null) {
            definePackage(packageName, null, null, null, null, null, null, null);
        } else {
            definePackage(container, packageName, manifest);
        }
    }
    private Manifest getJarManifest(File container) throws IOException {
        if (container.isDirectory()) {
            return null;
        }
        JarFile jarFile = (JarFile) jarFiles.get(container);
        if (jarFile == null) {
            return null;
        }
        return jarFile.getManifest();
    }
    private Certificate[] getCertificates(File container, String entry)
        throws IOException {
        if (container.isDirectory()) {
            return null;
        }
        JarFile jarFile = (JarFile) jarFiles.get(container);
        if (jarFile == null) {
            return null;
        }
        JarEntry ent = jarFile.getJarEntry(entry);
        return ent == null ? null : ent.getCertificates();
    }
    protected void definePackage(File container, String packageName, Manifest manifest) {
        String sectionName = packageName.replace('.', '/') + "/";
        String specificationTitle = null;
        String specificationVendor = null;
        String specificationVersion = null;
        String implementationTitle = null;
        String implementationVendor = null;
        String implementationVersion = null;
        String sealedString = null;
        URL sealBase = null;
        Attributes sectionAttributes = manifest.getAttributes(sectionName);
        if (sectionAttributes != null) {
            specificationTitle = sectionAttributes.getValue(Name.SPECIFICATION_TITLE);
            specificationVendor = sectionAttributes.getValue(Name.SPECIFICATION_VENDOR);
            specificationVersion = sectionAttributes.getValue(Name.SPECIFICATION_VERSION);
            implementationTitle = sectionAttributes.getValue(Name.IMPLEMENTATION_TITLE);
            implementationVendor = sectionAttributes.getValue(Name.IMPLEMENTATION_VENDOR);
            implementationVersion = sectionAttributes.getValue(Name.IMPLEMENTATION_VERSION);
            sealedString = sectionAttributes.getValue(Name.SEALED);
        }
        Attributes mainAttributes = manifest.getMainAttributes();
        if (mainAttributes != null) {
            if (specificationTitle == null) {
                specificationTitle = mainAttributes.getValue(Name.SPECIFICATION_TITLE);
            }
            if (specificationVendor == null) {
                specificationVendor = mainAttributes.getValue(Name.SPECIFICATION_VENDOR);
            }
            if (specificationVersion == null) {
                specificationVersion = mainAttributes.getValue(Name.SPECIFICATION_VERSION);
            }
            if (implementationTitle == null) {
                implementationTitle = mainAttributes.getValue(Name.IMPLEMENTATION_TITLE);
            }
            if (implementationVendor == null) {
                implementationVendor = mainAttributes.getValue(Name.IMPLEMENTATION_VENDOR);
            }
            if (implementationVersion == null) {
                implementationVersion = mainAttributes.getValue(Name.IMPLEMENTATION_VERSION);
            }
            if (sealedString == null) {
                sealedString = mainAttributes.getValue(Name.SEALED);
            }
        }
        if (sealedString != null && sealedString.equalsIgnoreCase("true")) {
            try {
                sealBase = new URL(FileUtils.getFileUtils().toURI(container.getAbsolutePath()));
            } catch (MalformedURLException e) {
            }
        }
        definePackage(packageName, specificationTitle, specificationVersion, specificationVendor,
                implementationTitle, implementationVersion, implementationVendor, sealBase);
    }
    private Class getClassFromStream(InputStream stream, String classname, File container)
            throws IOException, SecurityException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead = -1;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((bytesRead = stream.read(buffer, 0, BUFFER_SIZE)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        byte[] classData = baos.toByteArray();
        return defineClassFromData(container, classData, classname);
    }
    public Class findClass(String name) throws ClassNotFoundException {
        log("Finding class " + name, Project.MSG_DEBUG);
        return findClassInComponents(name);
    }
    protected boolean isInPath(File component) {
        return pathComponents.contains(component);
    }
    private Class findClassInComponents(String name)
        throws ClassNotFoundException {
        InputStream stream = null;
        String classFilename = getClassFilename(name);
        try {
            Enumeration e = pathComponents.elements();
            while (e.hasMoreElements()) {
                File pathComponent = (File) e.nextElement();
                try {
                    stream = getResourceStream(pathComponent, classFilename);
                    if (stream != null) {
                        log("Loaded from " + pathComponent + " "
                            + classFilename, Project.MSG_DEBUG);
                        return getClassFromStream(stream, name, pathComponent);
                    }
                } catch (SecurityException se) {
                    throw se;
                } catch (IOException ioe) {
                    log("Exception reading component " + pathComponent + " (reason: "
                            + ioe.getMessage() + ")", Project.MSG_VERBOSE);
                }
            }
            throw new ClassNotFoundException(name);
        } finally {
            FileUtils.close(stream);
        }
    }
    private Class findBaseClass(String name) throws ClassNotFoundException {
        return parent == null ? findSystemClass(name) : parent.loadClass(name);
    }
    public synchronized void cleanup() {
        for (Enumeration e = jarFiles.elements(); e.hasMoreElements();) {
            JarFile jarFile = (JarFile) e.nextElement();
            try {
                jarFile.close();
            } catch (IOException ioe) {
            }
        }
        jarFiles = new Hashtable();
        if (project != null) {
            project.removeBuildListener(this);
        }
        project = null;
    }
    public ClassLoader getConfiguredParent() {
        return parent;
    }
    public void buildStarted(BuildEvent event) {
    }
    public void buildFinished(BuildEvent event) {
        cleanup();
    }
    public void subBuildFinished(BuildEvent event) {
        if (event.getProject() == project) {
            cleanup();
        }
    }
    public void subBuildStarted(BuildEvent event) {
    }
    public void targetStarted(BuildEvent event) {
    }
    public void targetFinished(BuildEvent event) {
    }
    public void taskStarted(BuildEvent event) {
    }
    public void taskFinished(BuildEvent event) {
    }
    public void messageLogged(BuildEvent event) {
    }
    public void addJavaLibraries() {
        Vector packages = JavaEnvUtils.getJrePackages();
        Enumeration e = packages.elements();
        while (e.hasMoreElements()) {
            String packageName = (String) e.nextElement();
            addSystemPackageRoot(packageName);
        }
    }
    public String toString() {
        return "AntClassLoader[" + getClasspath() + "]";
    }
    private static Class subClassToLoad = null;
    private static final Class[] CONSTRUCTOR_ARGS = new Class[] {
        ClassLoader.class, Project.class, Path.class, Boolean.TYPE
    };
    static {
        if (JavaEnvUtils.isAtLeastJavaVersion(JavaEnvUtils.JAVA_1_5)) {
            try {
                subClassToLoad =
                    Class.forName("org.apache.tools.ant.loader.AntClassLoader5");
            } catch (ClassNotFoundException e) {
            }
        }
    }
    public static AntClassLoader newAntClassLoader(ClassLoader parent,
                                                   Project project,
                                                   Path path,
                                                   boolean parentFirst) {
        if (subClassToLoad != null) {
            return (AntClassLoader)
                ReflectUtil.newInstance(subClassToLoad,
                                        CONSTRUCTOR_ARGS,
                                        new Object[] {
                                            parent, project, path,
                                            Boolean.valueOf(parentFirst)
                                        });
        }
        return new AntClassLoader(parent, project, path, parentFirst);
    }
}