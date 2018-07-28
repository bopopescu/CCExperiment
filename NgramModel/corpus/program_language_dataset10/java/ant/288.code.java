package org.apache.tools.ant.taskdefs;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.zip.CRC32;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.ZipFileSet;
import org.apache.tools.ant.types.ZipScanner;
import org.apache.tools.ant.types.resources.ArchiveResource;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.resources.Union;
import org.apache.tools.ant.types.resources.ZipResource;
import org.apache.tools.ant.types.resources.selectors.ResourceSelector;
import org.apache.tools.ant.util.FileNameMapper;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.GlobPatternMapper;
import org.apache.tools.ant.util.IdentityMapper;
import org.apache.tools.ant.util.MergingMapper;
import org.apache.tools.ant.util.ResourceUtils;
import org.apache.tools.zip.UnixStat;
import org.apache.tools.zip.ZipEntry;
import org.apache.tools.zip.ZipExtraField;
import org.apache.tools.zip.ZipFile;
import org.apache.tools.zip.ZipOutputStream;
public class Zip extends MatchingTask {
    private static final int BUFFER_SIZE = 8 * 1024;
    private static final int ROUNDUP_MILLIS = 1999; 
    protected File zipFile;
    private ZipScanner zs;
    private File baseDir;
    protected Hashtable entries = new Hashtable();
    private Vector groupfilesets = new Vector();
    private Vector filesetsFromGroupfilesets = new Vector();
    protected String duplicate = "add";
    private boolean doCompress = true;
    private boolean doUpdate = false;
    private boolean savedDoUpdate = false;
    private boolean doFilesonly = false;
    protected String archiveType = "zip";
    private static final long EMPTY_CRC = new CRC32 ().getValue ();
    protected String emptyBehavior = "skip";
    private Vector resources = new Vector();
    protected Hashtable addedDirs = new Hashtable();
    private Vector addedFiles = new Vector();
    private static final ResourceSelector MISSING_SELECTOR =
        new ResourceSelector() {
            public boolean isSelected(Resource target) {
                return !target.isExists();
            }
        };
    private static final ResourceUtils.ResourceSelectorProvider
        MISSING_DIR_PROVIDER = new ResourceUtils.ResourceSelectorProvider() {
                public ResourceSelector
                    getTargetSelectorForSource(Resource sr) {
                    return MISSING_SELECTOR;
                }
            };
    protected boolean doubleFilePass = false;
    protected boolean skipWriting = false;
    protected final boolean isFirstPass() {
        return !doubleFilePass || skipWriting;
    }
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private boolean updatedFile = false;
    private boolean addingNewFiles = false;
    private String encoding;
    private boolean keepCompression = false;
    private boolean roundUp = true;
    private String comment = "";
    private int level = ZipOutputStream.DEFAULT_COMPRESSION;
    private boolean preserve0Permissions = false;
    private boolean useLanguageEncodingFlag = true;
    private UnicodeExtraField createUnicodeExtraFields =
        UnicodeExtraField.NEVER;
    private boolean fallBackToUTF8 = false;
    public void setZipfile(File zipFile) {
        setDestFile(zipFile);
    }
    public void setFile(File file) {
        setDestFile(file);
    }
    public void setDestFile(File destFile) {
       this.zipFile = destFile;
    }
    public File getDestFile() {
        return zipFile;
    }
    public void setBasedir(File baseDir) {
        this.baseDir = baseDir;
    }
    public void setCompress(boolean c) {
        doCompress = c;
    }
    public boolean isCompress() {
        return doCompress;
    }
    public void setFilesonly(boolean f) {
        doFilesonly = f;
    }
    public void setUpdate(boolean c) {
        doUpdate = c;
        savedDoUpdate = c;
    }
    public boolean isInUpdateMode() {
        return doUpdate;
    }
    public void addFileset(FileSet set) {
        add(set);
    }
    public void addZipfileset(ZipFileSet set) {
        add(set);
    }
    public void add(ResourceCollection a) {
        resources.add(a);
    }
    public void addZipGroupFileset(FileSet set) {
        groupfilesets.addElement(set);
    }
    public void setDuplicate(Duplicate df) {
        duplicate = df.getValue();
    }
    public static class WhenEmpty extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {"fail", "skip", "create"};
        }
    }
    public void setWhenempty(WhenEmpty we) {
        emptyBehavior = we.getValue();
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }
    public String getEncoding() {
        return encoding;
    }
    public void setKeepCompression(boolean keep) {
        keepCompression = keep;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getComment() {
        return comment;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public int getLevel() {
        return level;
    }
    public void setRoundUp(boolean r) {
        roundUp = r;
    }
    public void setPreserve0Permissions(boolean b) {
        preserve0Permissions = b;
    }
    public boolean getPreserve0Permissions() {
        return preserve0Permissions;
    }
    public void setUseLanguageEncodingFlag(boolean b) {
        useLanguageEncodingFlag = b;
    }
    public boolean getUseLanguageEnodingFlag() {
        return useLanguageEncodingFlag;
    }
    public void setCreateUnicodeExtraFields(UnicodeExtraField b) {
        createUnicodeExtraFields = b;
    }
    public UnicodeExtraField getCreateUnicodeExtraFields() {
        return createUnicodeExtraFields;
    }
    public void setFallBackToUTF8(boolean b) {
        fallBackToUTF8 = b;
    }
    public boolean getFallBackToUTF8() {
        return fallBackToUTF8;
    }
    public void execute() throws BuildException {
        if (doubleFilePass) {
            skipWriting = true;
            executeMain();
            skipWriting = false;
            executeMain();
        } else {
            executeMain();
        }
    }
    protected boolean hasUpdatedFile() {
        return updatedFile;
    }
    public void executeMain() throws BuildException {
        checkAttributesAndElements();
        File renamedFile = null;
        addingNewFiles = true;
        processDoUpdate();
        processGroupFilesets();
        Vector vfss = new Vector();
        if (baseDir != null) {
            FileSet fs = (FileSet) getImplicitFileSet().clone();
            fs.setDir(baseDir);
            vfss.addElement(fs);
        }
        for (int i = 0; i < resources.size(); i++) {
            ResourceCollection rc = (ResourceCollection) resources.elementAt(i);
            vfss.addElement(rc);
        }
        ResourceCollection[] fss = new ResourceCollection[vfss.size()];
        vfss.copyInto(fss);
        boolean success = false;
        try {
            ArchiveState state = getResourcesToAdd(fss, zipFile, false);
            if (!state.isOutOfDate()) {
                return;
            }
            File parent = zipFile.getParentFile();
            if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                throw new BuildException("Failed to create missing parent"
                                         + " directory for " + zipFile);
            }
            updatedFile = true;
            if (!zipFile.exists() && state.isWithoutAnyResources()) {
                createEmptyZip(zipFile);
                return;
            }
            Resource[][] addThem = state.getResourcesToAdd();
            if (doUpdate) {
                renamedFile = renameFile();
            }
            String action = doUpdate ? "Updating " : "Building ";
            if (!skipWriting) {
                log(action + archiveType + ": " + zipFile.getAbsolutePath());
            }
            ZipOutputStream zOut = null;
            try {
                if (!skipWriting) {
                    zOut = new ZipOutputStream(zipFile);
                    zOut.setEncoding(encoding);
                    zOut.setUseLanguageEncodingFlag(useLanguageEncodingFlag);
                    zOut.setCreateUnicodeExtraFields(createUnicodeExtraFields.
                                                     getPolicy());
                    zOut.setFallbackToUTF8(fallBackToUTF8);
                    zOut.setMethod(doCompress
                        ? ZipOutputStream.DEFLATED : ZipOutputStream.STORED);
                    zOut.setLevel(level);
                }
                initZipOutputStream(zOut);
                for (int i = 0; i < fss.length; i++) {
                    if (addThem[i].length != 0) {
                        addResources(fss[i], addThem[i], zOut);
                    }
                }
                if (doUpdate) {
                    addingNewFiles = false;
                    ZipFileSet oldFiles = new ZipFileSet();
                    oldFiles.setProject(getProject());
                    oldFiles.setSrc(renamedFile);
                    oldFiles.setDefaultexcludes(false);
                    for (int i = 0; i < addedFiles.size(); i++) {
                        PatternSet.NameEntry ne = oldFiles.createExclude();
                        ne.setName((String) addedFiles.elementAt(i));
                    }
                    DirectoryScanner ds =
                        oldFiles.getDirectoryScanner(getProject());
                    ((ZipScanner) ds).setEncoding(encoding);
                    String[] f = ds.getIncludedFiles();
                    Resource[] r = new Resource[f.length];
                    for (int i = 0; i < f.length; i++) {
                        r[i] = ds.getResource(f[i]);
                    }
                    if (!doFilesonly) {
                        String[] d = ds.getIncludedDirectories();
                        Resource[] dr = new Resource[d.length];
                        for (int i = 0; i < d.length; i++) {
                            dr[i] = ds.getResource(d[i]);
                        }
                        Resource[] tmp = r;
                        r = new Resource[tmp.length + dr.length];
                        System.arraycopy(dr, 0, r, 0, dr.length);
                        System.arraycopy(tmp, 0, r, dr.length, tmp.length);
                    }
                    addResources(oldFiles, r, zOut);
                }
                if (zOut != null) {
                    zOut.setComment(comment);
                }
                finalizeZipOutputStream(zOut);
                if (doUpdate) {
                    if (!renamedFile.delete()) {
                        log ("Warning: unable to delete temporary file "
                            + renamedFile.getName(), Project.MSG_WARN);
                    }
                }
                success = true;
            } finally {
                closeZout(zOut, success);
            }
        } catch (IOException ioe) {
            String msg = "Problem creating " + archiveType + ": "
                + ioe.getMessage();
            if ((!doUpdate || renamedFile != null) && !zipFile.delete()) {
                msg += " (and the archive is probably corrupt but I could not "
                    + "delete it)";
            }
            if (doUpdate && renamedFile != null) {
                try {
                    FILE_UTILS.rename(renamedFile, zipFile);
                } catch (IOException e) {
                    msg += " (and I couldn't rename the temporary file "
                            + renamedFile.getName() + " back)";
                }
            }
            throw new BuildException(msg, ioe, getLocation());
        } finally {
            cleanUp();
        }
    }
    private File renameFile() {
        File renamedFile = FILE_UTILS.createTempFile(
            "zip", ".tmp", zipFile.getParentFile(), true, false);
        try {
            FILE_UTILS.rename(zipFile, renamedFile);
        } catch (SecurityException e) {
            throw new BuildException(
                "Not allowed to rename old file ("
                + zipFile.getAbsolutePath()
                + ") to temporary file");
        } catch (IOException e) {
            throw new BuildException(
                "Unable to rename old file ("
                + zipFile.getAbsolutePath()
                + ") to temporary file");
        }
        return renamedFile;
    }
    private void closeZout(ZipOutputStream zOut, boolean success)
        throws IOException {
        if (zOut == null) {
            return;
        }
        try {
            zOut.close();
        } catch (IOException ex) {
            if (success) {
                throw ex;
            }
        }
    }
    private void checkAttributesAndElements() {
        if (baseDir == null && resources.size() == 0
            && groupfilesets.size() == 0 && "zip".equals(archiveType)) {
            throw new BuildException("basedir attribute must be set, "
                                     + "or at least one "
                                     + "resource collection must be given!");
        }
        if (zipFile == null) {
            throw new BuildException("You must specify the "
                                     + archiveType + " file to create!");
        }
        if (zipFile.exists() && !zipFile.isFile()) {
            throw new BuildException(zipFile + " is not a file.");
        }
        if (zipFile.exists() && !zipFile.canWrite()) {
            throw new BuildException(zipFile + " is read-only.");
        }
    }
    private void processDoUpdate() {
        if (doUpdate && !zipFile.exists()) {
            doUpdate = false;
            logWhenWriting("ignoring update attribute as " + archiveType
                           + " doesn't exist.", Project.MSG_DEBUG);
        }
    }
    private void processGroupFilesets() {
        for (int i = 0; i < groupfilesets.size(); i++) {
            logWhenWriting("Processing groupfileset ", Project.MSG_VERBOSE);
            FileSet fs = (FileSet) groupfilesets.elementAt(i);
            FileScanner scanner = fs.getDirectoryScanner(getProject());
            String[] files = scanner.getIncludedFiles();
            File basedir = scanner.getBasedir();
            for (int j = 0; j < files.length; j++) {
                logWhenWriting("Adding file " + files[j] + " to fileset",
                               Project.MSG_VERBOSE);
                ZipFileSet zf = new ZipFileSet();
                zf.setProject(getProject());
                zf.setSrc(new File(basedir, files[j]));
                add(zf);
                filesetsFromGroupfilesets.addElement(zf);
            }
        }
    }
    protected final boolean isAddingNewFiles() {
        return addingNewFiles;
    }
    protected final void addResources(FileSet fileset, Resource[] resources,
                                      ZipOutputStream zOut)
        throws IOException {
        String prefix = "";
        String fullpath = "";
        int dirMode = ArchiveFileSet.DEFAULT_DIR_MODE;
        int fileMode = ArchiveFileSet.DEFAULT_FILE_MODE;
        ArchiveFileSet zfs = null;
        if (fileset instanceof ArchiveFileSet) {
            zfs = (ArchiveFileSet) fileset;
            prefix = zfs.getPrefix(getProject());
            fullpath = zfs.getFullpath(getProject());
            dirMode = zfs.getDirMode(getProject());
            fileMode = zfs.getFileMode(getProject());
        }
        if (prefix.length() > 0 && fullpath.length() > 0) {
            throw new BuildException("Both prefix and fullpath attributes must"
                                     + " not be set on the same fileset.");
        }
        if (resources.length != 1 && fullpath.length() > 0) {
            throw new BuildException("fullpath attribute may only be specified"
                                     + " for filesets that specify a single"
                                     + " file.");
        }
        if (prefix.length() > 0) {
            if (!prefix.endsWith("/") && !prefix.endsWith("\\")) {
                prefix += "/";
            }
            addParentDirs(null, prefix, zOut, "", dirMode);
        }
        ZipFile zf = null;
        try {
            boolean dealingWithFiles = false;
            File base = null;
            if (zfs == null || zfs.getSrc(getProject()) == null) {
                dealingWithFiles = true;
                base = fileset.getDir(getProject());
            } else if (zfs instanceof ZipFileSet) {
                zf = new ZipFile(zfs.getSrc(getProject()), encoding);
            }
            for (int i = 0; i < resources.length; i++) {
                String name = null;
                if (fullpath.length() > 0) {
                    name = fullpath;
                } else {
                    name = resources[i].getName();
                }
                name = name.replace(File.separatorChar, '/');
                if ("".equals(name)) {
                    continue;
                }
                if (resources[i].isDirectory()) {
                    if (doFilesonly) {
                        continue;
                    }
                    int thisDirMode = zfs != null && zfs.hasDirModeBeenSet()
                        ? dirMode : getUnixMode(resources[i], zf, dirMode);
                    addDirectoryResource(resources[i], name, prefix,
                                         base, zOut,
                                         dirMode, thisDirMode);
                } else { 
                    addParentDirs(base, name, zOut, prefix, dirMode);
                    if (dealingWithFiles) {
                        File f = FILE_UTILS.resolveFile(base,
                                                        resources[i].getName());
                        zipFile(f, zOut, prefix + name, fileMode);
                    } else {
                        int thisFileMode =
                            zfs != null && zfs.hasFileModeBeenSet()
                            ? fileMode : getUnixMode(resources[i], zf,
                                                     fileMode);
                        addResource(resources[i], name, prefix,
                                    zOut, thisFileMode, zf,
                                    zfs == null
                                    ? null : zfs.getSrc(getProject()));
                    }
                }
            }
        } finally {
            if (zf != null) {
                zf.close();
            }
        }
    }
    private void addDirectoryResource(Resource r, String name, String prefix,
                                      File base, ZipOutputStream zOut,
                                      int defaultDirMode, int thisDirMode)
        throws IOException {
        if (!name.endsWith("/")) {
            name = name + "/";
        }
        int nextToLastSlash = name.lastIndexOf("/", name.length() - 2);
        if (nextToLastSlash != -1) {
            addParentDirs(base, name.substring(0, nextToLastSlash + 1),
                          zOut, prefix, defaultDirMode);
        }
        zipDir(r, zOut, prefix + name, thisDirMode,
               r instanceof ZipResource
               ? ((ZipResource) r).getExtraFields() : null);
    }
    private int getUnixMode(Resource r, ZipFile zf, int defaultMode)
        throws IOException {
        int unixMode = defaultMode;
        if (zf != null) {
            ZipEntry ze = zf.getEntry(r.getName());
            unixMode = ze.getUnixMode();
            if ((unixMode == 0 || unixMode == UnixStat.DIR_FLAG)
                && !preserve0Permissions) {
                unixMode = defaultMode;
            }
        } else if (r instanceof ArchiveResource) {
            unixMode = ((ArchiveResource) r).getMode();
        }
        return unixMode;
    }
    private void addResource(Resource r, String name, String prefix,
                             ZipOutputStream zOut, int mode,
                             ZipFile zf, File fromArchive)
        throws IOException {
        if (zf != null) {
            ZipEntry ze = zf.getEntry(r.getName());
            if (ze != null) {
                boolean oldCompress = doCompress;
                if (keepCompression) {
                    doCompress = (ze.getMethod() == ZipEntry.DEFLATED);
                }
                InputStream is = null;
                try {
                    is = zf.getInputStream(ze);
                    zipFile(is, zOut, prefix + name, ze.getTime(),
                            fromArchive, mode, ze.getExtraFields(true));
                } finally {
                    doCompress = oldCompress;
                    FileUtils.close(is);
                }
            }
        } else {
            InputStream is = null;
            try {
                is = r.getInputStream();
                zipFile(is, zOut, prefix + name, r.getLastModified(),
                        fromArchive, mode, r instanceof ZipResource
                        ? ((ZipResource) r).getExtraFields() : null);
            } finally {
                FileUtils.close(is);
            }
        }
    }
    protected final void addResources(ResourceCollection rc,
                                      Resource[] resources,
                                      ZipOutputStream zOut)
        throws IOException {
        if (rc instanceof FileSet) {
            addResources((FileSet) rc, resources, zOut);
            return;
        }
        for (int i = 0; i < resources.length; i++) {
            String name = resources[i].getName().replace(File.separatorChar,
                                                         '/');
            if ("".equals(name)) {
                continue;
            }
            if (resources[i].isDirectory() && doFilesonly) {
                continue;
            }
            File base = null;
            FileProvider fp = (FileProvider) resources[i].as(FileProvider.class);
            if (fp != null) {
                base = ResourceUtils.asFileResource(fp).getBaseDir();
            }
            if (resources[i].isDirectory()) {
                addDirectoryResource(resources[i], name, "", base, zOut,
                                     ArchiveFileSet.DEFAULT_DIR_MODE,
                                     ArchiveFileSet.DEFAULT_DIR_MODE);
            } else {
                addParentDirs(base, name, zOut, "",
                              ArchiveFileSet.DEFAULT_DIR_MODE);
                if (fp != null) {
                    File f = (fp).getFile();
                    zipFile(f, zOut, name, ArchiveFileSet.DEFAULT_FILE_MODE);
                } else {
                    addResource(resources[i], name, "", zOut,
                                ArchiveFileSet.DEFAULT_FILE_MODE,
                                null, null);
                }
            }
        }
    }
    protected void initZipOutputStream(ZipOutputStream zOut)
        throws IOException, BuildException {
    }
    protected void finalizeZipOutputStream(ZipOutputStream zOut)
        throws IOException, BuildException {
    }
    protected boolean createEmptyZip(File zipFile) throws BuildException {
        if (!skipWriting) {
            log("Note: creating empty " + archiveType + " archive " + zipFile,
                Project.MSG_INFO);
        }
        OutputStream os = null;
        try {
            os = new FileOutputStream(zipFile);
            byte[] empty = new byte[22];
            empty[0] = 80; 
            empty[1] = 75; 
            empty[2] = 5;
            empty[3] = 6;
            os.write(empty);
        } catch (IOException ioe) {
            throw new BuildException("Could not create empty ZIP archive "
                                     + "(" + ioe.getMessage() + ")", ioe,
                                     getLocation());
        } finally {
            FileUtils.close(os);
        }
        return true;
    }
    private synchronized ZipScanner getZipScanner() {
        if (zs == null) {
            zs = new ZipScanner();
            zs.setEncoding(encoding);
            zs.setSrc(zipFile);
        }
        return zs;
    }
    protected ArchiveState getResourcesToAdd(ResourceCollection[] rcs,
                                             File zipFile,
                                             boolean needsUpdate)
        throws BuildException {
        ArrayList filesets = new ArrayList();
        ArrayList rest = new ArrayList();
        for (int i = 0; i < rcs.length; i++) {
            if (rcs[i] instanceof FileSet) {
                filesets.add(rcs[i]);
            } else {
                rest.add(rcs[i]);
            }
        }
        ResourceCollection[] rc = (ResourceCollection[])
            rest.toArray(new ResourceCollection[rest.size()]);
        ArchiveState as = getNonFileSetResourcesToAdd(rc, zipFile,
                                                      needsUpdate);
        FileSet[] fs = (FileSet[]) filesets.toArray(new FileSet[filesets
                                                                .size()]);
        ArchiveState as2 = getResourcesToAdd(fs, zipFile, as.isOutOfDate());
        if (!as.isOutOfDate() && as2.isOutOfDate()) {
            as = getNonFileSetResourcesToAdd(rc, zipFile, true);
        }
        Resource[][] toAdd = new Resource[rcs.length][];
        int fsIndex = 0;
        int restIndex = 0;
        for (int i = 0; i < rcs.length; i++) {
            if (rcs[i] instanceof FileSet) {
                toAdd[i] = as2.getResourcesToAdd()[fsIndex++];
            } else {
                toAdd[i] = as.getResourcesToAdd()[restIndex++];
            }
        }
        return new ArchiveState(as2.isOutOfDate(), toAdd);
    }
    private static ThreadLocal haveNonFileSetResourcesToAdd = new ThreadLocal() {
            protected Object initialValue() {
                return Boolean.FALSE;
            }
        };
    protected ArchiveState getResourcesToAdd(FileSet[] filesets,
                                             File zipFile,
                                             boolean needsUpdate)
        throws BuildException {
        Resource[][] initialResources = grabResources(filesets);
        if (isEmpty(initialResources)) {
            if (Boolean.FALSE.equals(haveNonFileSetResourcesToAdd.get())) {
                if (needsUpdate && doUpdate) {
                    return new ArchiveState(true, initialResources);
                }
                if (emptyBehavior.equals("skip")) {
                    if (doUpdate) {
                        logWhenWriting(archiveType + " archive " + zipFile
                                       + " not updated because no new files were"
                                       + " included.", Project.MSG_VERBOSE);
                    } else {
                        logWhenWriting("Warning: skipping " + archiveType
                                       + " archive " + zipFile
                                       + " because no files were included.",
                                       Project.MSG_WARN);
                    }
                } else if (emptyBehavior.equals("fail")) {
                    throw new BuildException("Cannot create " + archiveType
                                             + " archive " + zipFile
                                             + ": no files were included.",
                                             getLocation());
                } else {
                    if (!zipFile.exists())  {
                        needsUpdate = true;
                    }
                }
            }
            return new ArchiveState(needsUpdate, initialResources);
        }
        if (!zipFile.exists()) {
            return new ArchiveState(true, initialResources);
        }
        if (needsUpdate && !doUpdate) {
            return new ArchiveState(true, initialResources);
        }
        Resource[][] newerResources = new Resource[filesets.length][];
        for (int i = 0; i < filesets.length; i++) {
            if (!(fileset instanceof ZipFileSet)
                || ((ZipFileSet) fileset).getSrc(getProject()) == null) {
                File base = filesets[i].getDir(getProject());
                for (int j = 0; j < initialResources[i].length; j++) {
                    File resourceAsFile =
                        FILE_UTILS.resolveFile(base,
                                              initialResources[i][j].getName());
                    if (resourceAsFile.equals(zipFile)) {
                        throw new BuildException("A zip file cannot include "
                                                 + "itself", getLocation());
                    }
                }
            }
        }
        for (int i = 0; i < filesets.length; i++) {
            if (initialResources[i].length == 0) {
                newerResources[i] = new Resource[] {};
                continue;
            }
            FileNameMapper myMapper = new IdentityMapper();
            if (filesets[i] instanceof ZipFileSet) {
                ZipFileSet zfs = (ZipFileSet) filesets[i];
                if (zfs.getFullpath(getProject()) != null
                    && !zfs.getFullpath(getProject()).equals("")) {
                    MergingMapper fm = new MergingMapper();
                    fm.setTo(zfs.getFullpath(getProject()));
                    myMapper = fm;
                } else if (zfs.getPrefix(getProject()) != null
                           && !zfs.getPrefix(getProject()).equals("")) {
                    GlobPatternMapper gm = new GlobPatternMapper();
                    gm.setFrom("*");
                    String prefix = zfs.getPrefix(getProject());
                    if (!prefix.endsWith("/") && !prefix.endsWith("\\")) {
                        prefix += "/";
                    }
                    gm.setTo(prefix + "*");
                    myMapper = gm;
                }
            }
            newerResources[i] = selectOutOfDateResources(initialResources[i],
                                                         myMapper);
            needsUpdate = needsUpdate || (newerResources[i].length > 0);
            if (needsUpdate && !doUpdate) {
                break;
            }
        }
        if (needsUpdate && !doUpdate) {
            return new ArchiveState(true, initialResources);
        }
        return new ArchiveState(needsUpdate, newerResources);
    }
    protected ArchiveState getNonFileSetResourcesToAdd(ResourceCollection[] rcs,
                                                       File zipFile,
                                                       boolean needsUpdate)
        throws BuildException {
        Resource[][] initialResources = grabNonFileSetResources(rcs);
        boolean empty = isEmpty(initialResources);
        haveNonFileSetResourcesToAdd.set(Boolean.valueOf(!empty));
        if (empty) {
            return new ArchiveState(needsUpdate, initialResources);
        }
        if (!zipFile.exists()) {
            return new ArchiveState(true, initialResources);
        }
        if (needsUpdate && !doUpdate) {
            return new ArchiveState(true, initialResources);
        }
        Resource[][] newerResources = new Resource[rcs.length][];
        for (int i = 0; i < rcs.length; i++) {
            if (initialResources[i].length == 0) {
                newerResources[i] = new Resource[] {};
                continue;
            }
            for (int j = 0; j < initialResources[i].length; j++) {
                FileProvider fp =
                    (FileProvider) initialResources[i][j].as(FileProvider.class);
                if (fp != null && zipFile.equals(fp.getFile())) {
                    throw new BuildException("A zip file cannot include "
                                             + "itself", getLocation());
                }
            }
            newerResources[i] = selectOutOfDateResources(initialResources[i],
                                                         new IdentityMapper());
            needsUpdate = needsUpdate || (newerResources[i].length > 0);
            if (needsUpdate && !doUpdate) {
                break;
            }
        }
        if (needsUpdate && !doUpdate) {
            return new ArchiveState(true, initialResources);
        }
        return new ArchiveState(needsUpdate, newerResources);
    }
    private Resource[] selectOutOfDateResources(Resource[] initial,
                                                FileNameMapper mapper) {
        Resource[] rs = selectFileResources(initial);
        Resource[] result =
            ResourceUtils.selectOutOfDateSources(this, rs, mapper,
                                                 getZipScanner());
        if (!doFilesonly) {
            Union u = new Union();
            u.addAll(Arrays.asList(selectDirectoryResources(initial)));
            ResourceCollection rc =
                ResourceUtils.selectSources(this, u, mapper,
                                            getZipScanner(),
                                            MISSING_DIR_PROVIDER);
            if (rc.size() > 0) {
                ArrayList newer = new ArrayList();
                newer.addAll(Arrays.asList(((Union) rc).listResources()));
                newer.addAll(Arrays.asList(result));
                result = (Resource[]) newer.toArray(result);
            }
        }
        return result;
    }
    protected Resource[][] grabResources(FileSet[] filesets) {
        Resource[][] result = new Resource[filesets.length][];
        for (int i = 0; i < filesets.length; i++) {
            boolean skipEmptyNames = true;
            if (filesets[i] instanceof ZipFileSet) {
                ZipFileSet zfs = (ZipFileSet) filesets[i];
                skipEmptyNames = zfs.getPrefix(getProject()).equals("")
                    && zfs.getFullpath(getProject()).equals("");
            }
            DirectoryScanner rs =
                filesets[i].getDirectoryScanner(getProject());
            if (rs instanceof ZipScanner) {
                ((ZipScanner) rs).setEncoding(encoding);
            }
            Vector resources = new Vector();
            if (!doFilesonly) {
                String[] directories = rs.getIncludedDirectories();
                for (int j = 0; j < directories.length; j++) {
                    if (!"".equals(directories[j]) || !skipEmptyNames) {
                        resources.addElement(rs.getResource(directories[j]));
                    }
                }
            }
            String[] files = rs.getIncludedFiles();
            for (int j = 0; j < files.length; j++) {
                if (!"".equals(files[j]) || !skipEmptyNames) {
                    resources.addElement(rs.getResource(files[j]));
                }
            }
            result[i] = new Resource[resources.size()];
            resources.copyInto(result[i]);
        }
        return result;
    }
    protected Resource[][] grabNonFileSetResources(ResourceCollection[] rcs) {
        Resource[][] result = new Resource[rcs.length][];
        for (int i = 0; i < rcs.length; i++) {
            Iterator iter = rcs[i].iterator();
            ArrayList dirs = new ArrayList();
            ArrayList files = new ArrayList();
            while (iter.hasNext()) {
                Resource r = (Resource) iter.next();
                if (r.isExists()) {
                    if (r.isDirectory()) {
                        dirs.add(r);
                    } else {
                        files.add(r);
                    }
                }
            }
            Collections.sort(dirs, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        Resource r1 = (Resource) o1;
                        Resource r2 = (Resource) o2;
                        return r1.getName().compareTo(r2.getName());
                    }
                });
            ArrayList rs = new ArrayList(dirs);
            rs.addAll(files);
            result[i] = (Resource[]) rs.toArray(new Resource[rs.size()]);
        }
        return result;
    }
    protected void zipDir(File dir, ZipOutputStream zOut, String vPath,
                          int mode)
        throws IOException {
        zipDir(dir, zOut, vPath, mode, null);
    }
    protected void zipDir(File dir, ZipOutputStream zOut, String vPath,
                          int mode, ZipExtraField[] extra)
        throws IOException {
        zipDir(dir == null ? (Resource) null : new FileResource(dir),
               zOut, vPath, mode, extra);
    }
    protected void zipDir(Resource dir, ZipOutputStream zOut, String vPath,
                          int mode, ZipExtraField[] extra)
        throws IOException {
        if (doFilesonly) {
            logWhenWriting("skipping directory " + vPath
                           + " for file-only archive",
                           Project.MSG_VERBOSE);
            return;
        }
        if (addedDirs.get(vPath) != null) {
            return;
        }
        logWhenWriting("adding directory " + vPath, Project.MSG_VERBOSE);
        addedDirs.put(vPath, vPath);
        if (!skipWriting) {
            ZipEntry ze = new ZipEntry (vPath);
            int millisToAdd = roundUp ? ROUNDUP_MILLIS : 0;
            if (dir != null && dir.isExists()) {
                ze.setTime(dir.getLastModified() + millisToAdd);
            } else {
                ze.setTime(System.currentTimeMillis() + millisToAdd);
            }
            ze.setSize (0);
            ze.setMethod (ZipEntry.STORED);
            ze.setCrc (EMPTY_CRC);
            ze.setUnixMode(mode);
            if (extra != null) {
                ze.setExtraFields(extra);
            }
            zOut.putNextEntry(ze);
        }
    }
    private static ThreadLocal currentZipExtra = new ThreadLocal() {
            protected Object initialValue() {
                return null;
            }
        };
    protected final ZipExtraField[] getCurrentExtraFields() {
        return (ZipExtraField[]) currentZipExtra.get();
    }
    protected final void setCurrentExtraFields(ZipExtraField[] extra) {
        currentZipExtra.set(extra);
    }
    protected void zipFile(InputStream in, ZipOutputStream zOut, String vPath,
                           long lastModified, File fromArchive, int mode)
        throws IOException {
        if (entries.containsKey(vPath)) {
            if (duplicate.equals("preserve")) {
                logWhenWriting(vPath + " already added, skipping",
                               Project.MSG_INFO);
                return;
            } else if (duplicate.equals("fail")) {
                throw new BuildException("Duplicate file " + vPath
                                         + " was found and the duplicate "
                                         + "attribute is 'fail'.");
            } else {
                logWhenWriting("duplicate file " + vPath
                               + " found, adding.", Project.MSG_VERBOSE);
            }
        } else {
            logWhenWriting("adding entry " + vPath, Project.MSG_VERBOSE);
        }
        entries.put(vPath, vPath);
        if (!skipWriting) {
            ZipEntry ze = new ZipEntry(vPath);
            ze.setTime(lastModified);
            ze.setMethod(doCompress ? ZipEntry.DEFLATED : ZipEntry.STORED);
            if (!zOut.isSeekable() && !doCompress) {
                long size = 0;
                CRC32 cal = new CRC32();
                if (!in.markSupported()) {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int count = 0;
                    do {
                        size += count;
                        cal.update(buffer, 0, count);
                        bos.write(buffer, 0, count);
                        count = in.read(buffer, 0, buffer.length);
                    } while (count != -1);
                    in = new ByteArrayInputStream(bos.toByteArray());
                } else {
                    in.mark(Integer.MAX_VALUE);
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int count = 0;
                    do {
                        size += count;
                        cal.update(buffer, 0, count);
                        count = in.read(buffer, 0, buffer.length);
                    } while (count != -1);
                    in.reset();
                }
                ze.setSize(size);
                ze.setCrc(cal.getValue());
            }
            ze.setUnixMode(mode);
            ZipExtraField[] extra = getCurrentExtraFields();
            if (extra != null) {
                ze.setExtraFields(extra);
            }
            zOut.putNextEntry(ze);
            byte[] buffer = new byte[BUFFER_SIZE];
            int count = 0;
            do {
                if (count != 0) {
                    zOut.write(buffer, 0, count);
                }
                count = in.read(buffer, 0, buffer.length);
            } while (count != -1);
        }
        addedFiles.addElement(vPath);
    }
    protected final void zipFile(InputStream in, ZipOutputStream zOut,
                                 String vPath, long lastModified,
                                 File fromArchive, int mode,
                                 ZipExtraField[] extra)
        throws IOException {
        try {
            setCurrentExtraFields(extra);
            zipFile(in, zOut, vPath, lastModified, fromArchive, mode);
        } finally {
            setCurrentExtraFields(null);
        }
    }
    protected void zipFile(File file, ZipOutputStream zOut, String vPath,
                           int mode)
        throws IOException {
        if (file.equals(zipFile)) {
            throw new BuildException("A zip file cannot include itself",
                                     getLocation());
        }
        FileInputStream fIn = new FileInputStream(file);
        try {
            zipFile(fIn, zOut, vPath,
                    file.lastModified() + (roundUp ? ROUNDUP_MILLIS : 0),
                    null, mode);
        } finally {
            fIn.close();
        }
    }
    protected final void addParentDirs(File baseDir, String entry,
                                       ZipOutputStream zOut, String prefix,
                                       int dirMode)
        throws IOException {
        if (!doFilesonly) {
            Stack directories = new Stack();
            int slashPos = entry.length();
            while ((slashPos = entry.lastIndexOf('/', slashPos - 1)) != -1) {
                String dir = entry.substring(0, slashPos + 1);
                if (addedDirs.get(prefix + dir) != null) {
                    break;
                }
                directories.push(dir);
            }
            while (!directories.isEmpty()) {
                String dir = (String) directories.pop();
                File f = null;
                if (baseDir != null) {
                    f = new File(baseDir, dir);
                } else {
                    f = new File(dir);
                }
                zipDir(f, zOut, prefix + dir, dirMode);
            }
        }
    }
    protected void cleanUp() {
        addedDirs.clear();
        addedFiles.removeAllElements();
        entries.clear();
        addingNewFiles = false;
        doUpdate = savedDoUpdate;
        Enumeration e = filesetsFromGroupfilesets.elements();
        while (e.hasMoreElements()) {
            ZipFileSet zf = (ZipFileSet) e.nextElement();
            resources.removeElement(zf);
        }
        filesetsFromGroupfilesets.removeAllElements();
        haveNonFileSetResourcesToAdd.set(Boolean.FALSE);
    }
    public void reset() {
        resources.removeAllElements();
        zipFile = null;
        baseDir = null;
        groupfilesets.removeAllElements();
        duplicate = "add";
        archiveType = "zip";
        doCompress = true;
        emptyBehavior = "skip";
        doUpdate = false;
        doFilesonly = false;
        encoding = null;
    }
    protected static final boolean isEmpty(Resource[][] r) {
        for (int i = 0; i < r.length; i++) {
            if (r[i].length > 0) {
                return false;
            }
        }
        return true;
    }
    protected Resource[] selectFileResources(Resource[] orig) {
        return selectResources(orig,
                               new ResourceSelector() {
                                   public boolean isSelected(Resource r) {
                                       if (!r.isDirectory()) {
                                           return true;
                                       } else if (doFilesonly) {
                                           logWhenWriting("Ignoring directory "
                                                          + r.getName()
                                                          + " as only files will"
                                                          + " be added.",
                                                          Project.MSG_VERBOSE);
                                       }
                                       return false;
                                   }
                               });
    }
    protected Resource[] selectDirectoryResources(Resource[] orig) {
        return selectResources(orig,
                               new ResourceSelector() {
                                   public boolean isSelected(Resource r) {
                                       return r.isDirectory();
                                   }
                               });
    }
    protected Resource[] selectResources(Resource[] orig,
                                         ResourceSelector selector) {
        if (orig.length == 0) {
            return orig;
        }
        ArrayList v = new ArrayList(orig.length);
        for (int i = 0; i < orig.length; i++) {
            if (selector.isSelected(orig[i])) {
                v.add(orig[i]);
            }
        }
        if (v.size() != orig.length) {
            Resource[] r = new Resource[v.size()];
            return (Resource[]) v.toArray(r);
        }
        return orig;
    }
    protected void logWhenWriting(String msg, int level) {
        if (!skipWriting) {
            log(msg, level);
        }
    }
    public static class Duplicate extends EnumeratedAttribute {
        public String[] getValues() {
            return new String[] {"add", "preserve", "fail"};
        }
    }
    public static class ArchiveState {
        private boolean outOfDate;
        private Resource[][] resourcesToAdd;
        ArchiveState(boolean state, Resource[][] r) {
            outOfDate = state;
            resourcesToAdd = r;
        }
        public boolean isOutOfDate() {
            return outOfDate;
        }
        public Resource[][] getResourcesToAdd() {
            return resourcesToAdd;
        }
        public boolean isWithoutAnyResources() {
            if (resourcesToAdd == null)  {
                return true;
            }
            for (int counter = 0; counter < resourcesToAdd.length; counter++) {
                if (resourcesToAdd[counter] != null) {
                    if (resourcesToAdd[counter].length > 0) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
    public static final class UnicodeExtraField extends EnumeratedAttribute {
        private static final Map POLICIES = new HashMap();
        private static final String NEVER_KEY = "never";
        private static final String ALWAYS_KEY = "always";
        private static final String N_E_KEY = "not-encodeable";
        static {
            POLICIES.put(NEVER_KEY,
                         ZipOutputStream.UnicodeExtraFieldPolicy.NEVER);
            POLICIES.put(ALWAYS_KEY,
                         ZipOutputStream.UnicodeExtraFieldPolicy.ALWAYS);
            POLICIES.put(N_E_KEY,
                         ZipOutputStream.UnicodeExtraFieldPolicy
                         .NOT_ENCODEABLE);
        }
        public String[] getValues() {
            return new String[] {NEVER_KEY, ALWAYS_KEY, N_E_KEY};
        }
        public static final UnicodeExtraField NEVER =
            new UnicodeExtraField(NEVER_KEY);
        private UnicodeExtraField(String name) {
            setValue(name);
        }
        public UnicodeExtraField() {
        }
        public ZipOutputStream.UnicodeExtraFieldPolicy getPolicy() {
            return (ZipOutputStream.UnicodeExtraFieldPolicy)
                POLICIES.get(getValue());
        }
    }
}