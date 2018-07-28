package org.apache.tools.ant.taskdefs;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.ArchiveFileSet;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.ArchiveResource;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.types.resources.FileResource;
import org.apache.tools.ant.types.selectors.SelectorUtils;
import org.apache.tools.ant.types.resources.TarResource;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.MergingMapper;
import org.apache.tools.ant.util.ResourceUtils;
import org.apache.tools.ant.util.SourceFileScanner;
import org.apache.tools.bzip2.CBZip2OutputStream;
import org.apache.tools.tar.TarConstants;
import org.apache.tools.tar.TarEntry;
import org.apache.tools.tar.TarOutputStream;
public class Tar extends MatchingTask {
    private static final int BUFFER_SIZE = 8 * 1024;
    public static final String WARN = "warn";
    public static final String FAIL = "fail";
    public static final String TRUNCATE = "truncate";
    public static final String GNU = "gnu";
    public static final String OMIT = "omit";
    File tarFile;
    File baseDir;
    private TarLongFileMode longFileMode = new TarLongFileMode();
    Vector filesets = new Vector();
    private Vector resourceCollections = new Vector();
    Vector fileSetFiles = new Vector();
    private boolean longWarningGiven = false;
    private TarCompressionMethod compression = new TarCompressionMethod();
    public TarFileSet createTarFileSet() {
        TarFileSet fs = new TarFileSet();
        fs.setProject(getProject());
        filesets.addElement(fs);
        return fs;
    }
    public void add(ResourceCollection res) {
        resourceCollections.add(res);
    }
    public void setTarfile(File tarFile) {
        this.tarFile = tarFile;
    }
    public void setDestFile(File destFile) {
        this.tarFile = destFile;
    }
    public void setBasedir(File baseDir) {
        this.baseDir = baseDir;
    }
    public void setLongfile(String mode) {
        log("DEPRECATED - The setLongfile(String) method has been deprecated."
            + " Use setLongfile(Tar.TarLongFileMode) instead.");
        this.longFileMode = new TarLongFileMode();
        longFileMode.setValue(mode);
    }
    public void setLongfile(TarLongFileMode mode) {
        this.longFileMode = mode;
    }
    public void setCompression(TarCompressionMethod mode) {
        this.compression = mode;
    }
    public void execute() throws BuildException {
        if (tarFile == null) {
            throw new BuildException("tarfile attribute must be set!",
                                     getLocation());
        }
        if (tarFile.exists() && tarFile.isDirectory()) {
            throw new BuildException("tarfile is a directory!",
                                     getLocation());
        }
        if (tarFile.exists() && !tarFile.canWrite()) {
            throw new BuildException("Can not write to the specified tarfile!",
                                     getLocation());
        }
        Vector savedFileSets = (Vector) filesets.clone();
        try {
            if (baseDir != null) {
                if (!baseDir.exists()) {
                    throw new BuildException("basedir does not exist!",
                                             getLocation());
                }
                TarFileSet mainFileSet = new TarFileSet(fileset);
                mainFileSet.setDir(baseDir);
                filesets.addElement(mainFileSet);
            }
            if (filesets.size() == 0 && resourceCollections.size() == 0) {
                throw new BuildException("You must supply either a basedir "
                                         + "attribute or some nested resource"
                                         + " collections.",
                                         getLocation());
            }
            boolean upToDate = true;
            for (Enumeration e = filesets.elements(); e.hasMoreElements();) {
                upToDate &= check((TarFileSet) e.nextElement());
            }
            for (Enumeration e = resourceCollections.elements();
                 e.hasMoreElements();) {
                upToDate &= check((ResourceCollection) e.nextElement());
            }
            if (upToDate) {
                log("Nothing to do: " + tarFile.getAbsolutePath()
                    + " is up to date.", Project.MSG_INFO);
                return;
            }
            File parent = tarFile.getParentFile();
            if (parent != null && !parent.isDirectory() && !parent.mkdirs()) {
                throw new BuildException("Failed to create missing parent"
                                         + " directory for " + tarFile);
            }
            log("Building tar: " + tarFile.getAbsolutePath(), Project.MSG_INFO);
            TarOutputStream tOut = null;
            try {
                tOut = new TarOutputStream(
                    compression.compress(
                        new BufferedOutputStream(
                            new FileOutputStream(tarFile))));
                tOut.setDebug(true);
                if (longFileMode.isTruncateMode()) {
                    tOut.setLongFileMode(TarOutputStream.LONGFILE_TRUNCATE);
                } else if (longFileMode.isFailMode()
                            || longFileMode.isOmitMode()) {
                    tOut.setLongFileMode(TarOutputStream.LONGFILE_ERROR);
                } else {
                    tOut.setLongFileMode(TarOutputStream.LONGFILE_GNU);
                }
                longWarningGiven = false;
                for (Enumeration e = filesets.elements();
                     e.hasMoreElements();) {
                    tar((TarFileSet) e.nextElement(), tOut);
                }
                for (Enumeration e = resourceCollections.elements();
                     e.hasMoreElements();) {
                    tar((ResourceCollection) e.nextElement(), tOut);
                }
            } catch (IOException ioe) {
                String msg = "Problem creating TAR: " + ioe.getMessage();
                throw new BuildException(msg, ioe, getLocation());
            } finally {
                FileUtils.close(tOut);
            }
        } finally {
            filesets = savedFileSets;
        }
    }
    protected void tarFile(File file, TarOutputStream tOut, String vPath,
                           TarFileSet tarFileSet)
        throws IOException {
        if (file.equals(tarFile)) {
            return;
        }
        tarResource(new FileResource(file), tOut, vPath, tarFileSet);
    }
    protected void tarResource(Resource r, TarOutputStream tOut, String vPath,
                               TarFileSet tarFileSet)
        throws IOException {
        if (!r.isExists()) {
            return;
        }
        boolean preserveLeadingSlashes = false;
        if (tarFileSet != null) {
            String fullpath = tarFileSet.getFullpath(this.getProject());
            if (fullpath.length() > 0) {
                vPath = fullpath;
            } else {
                if (vPath.length() <= 0) {
                    return;
                }
                String prefix = tarFileSet.getPrefix(this.getProject());
                if (prefix.length() > 0 && !prefix.endsWith("/")) {
                    prefix = prefix + "/";
                }
                vPath = prefix + vPath;
            }
            preserveLeadingSlashes = tarFileSet.getPreserveLeadingSlashes();
            if (vPath.startsWith("/") && !preserveLeadingSlashes) {
                int l = vPath.length();
                if (l <= 1) {
                    return;
                }
                vPath = vPath.substring(1, l);
            }
        }
        if (r.isDirectory() && !vPath.endsWith("/")) {
            vPath += "/";
        }
        if (vPath.length() >= TarConstants.NAMELEN) {
            if (longFileMode.isOmitMode()) {
                log("Omitting: " + vPath, Project.MSG_INFO);
                return;
            } else if (longFileMode.isWarnMode()) {
                log("Entry: " + vPath + " longer than "
                    + TarConstants.NAMELEN + " characters.",
                    Project.MSG_WARN);
                if (!longWarningGiven) {
                    log("Resulting tar file can only be processed "
                        + "successfully by GNU compatible tar commands",
                        Project.MSG_WARN);
                    longWarningGiven = true;
                }
            } else if (longFileMode.isFailMode()) {
                throw new BuildException("Entry: " + vPath
                        + " longer than " + TarConstants.NAMELEN
                        + "characters.", getLocation());
            }
        }
        TarEntry te = new TarEntry(vPath, preserveLeadingSlashes);
        te.setModTime(r.getLastModified());
        if (r instanceof ArchiveResource) {
            ArchiveResource ar = (ArchiveResource) r;
            te.setMode(ar.getMode());
            if (r instanceof TarResource) {
                TarResource tr = (TarResource) r;
                te.setUserName(tr.getUserName());
                te.setUserId(tr.getUid());
                te.setGroupName(tr.getGroup());
                te.setGroupId(tr.getGid());
            }
        }
        if (!r.isDirectory()) {
            if (r.size() > TarConstants.MAXSIZE) {
                throw new BuildException(
                    "Resource: " + r + " larger than "
                    + TarConstants.MAXSIZE + " bytes.");
            }
            te.setSize(r.getSize());
            if (tarFileSet != null && tarFileSet.hasFileModeBeenSet()) {
                te.setMode(tarFileSet.getMode());
            }
        } else if (tarFileSet != null && tarFileSet.hasDirModeBeenSet()) {
            te.setMode(tarFileSet.getDirMode(this.getProject()));
        }
        if (tarFileSet != null) {
            if (tarFileSet.hasUserNameBeenSet()) {
                te.setUserName(tarFileSet.getUserName());
            }
            if (tarFileSet.hasGroupBeenSet()) {
                te.setGroupName(tarFileSet.getGroup());
            }
            if (tarFileSet.hasUserIdBeenSet()) {
                te.setUserId(tarFileSet.getUid());
            }
            if (tarFileSet.hasGroupIdBeenSet()) {
                te.setGroupId(tarFileSet.getGid());
            }
        }
        InputStream in = null;
        try {
            tOut.putNextEntry(te);
            if (!r.isDirectory()) {
                in = r.getInputStream();
                byte[] buffer = new byte[BUFFER_SIZE];
                int count = 0;
                do {
                    tOut.write(buffer, 0, count);
                    count = in.read(buffer, 0, buffer.length);
                } while (count != -1);
            }
            tOut.closeEntry();
        } finally {
            FileUtils.close(in);
        }
    }
    protected boolean archiveIsUpToDate(String[] files) {
        return archiveIsUpToDate(files, baseDir);
    }
    protected boolean archiveIsUpToDate(String[] files, File dir) {
        SourceFileScanner sfs = new SourceFileScanner(this);
        MergingMapper mm = new MergingMapper();
        mm.setTo(tarFile.getAbsolutePath());
        return sfs.restrict(files, dir, null, mm).length == 0;
    }
    protected boolean archiveIsUpToDate(Resource r) {
        return SelectorUtils.isOutOfDate(new FileResource(tarFile), r,
                                         FileUtils.getFileUtils()
                                         .getFileTimestampGranularity());
    }
    protected boolean supportsNonFileResources() {
        return getClass().equals(Tar.class);
    }
    protected boolean check(ResourceCollection rc) {
        boolean upToDate = true;
        if (isFileFileSet(rc)) {
            FileSet fs = (FileSet) rc;
            upToDate = check(fs.getDir(getProject()), getFileNames(fs));
        } else if (!rc.isFilesystemOnly() && !supportsNonFileResources()) {
            throw new BuildException("only filesystem resources are supported");
        } else if (rc.isFilesystemOnly()) {
            HashSet basedirs = new HashSet();
            HashMap basedirToFilesMap = new HashMap();
            Iterator iter = rc.iterator();
            while (iter.hasNext()) {
                Resource res = (Resource) iter.next();
                FileResource r = ResourceUtils
                    .asFileResource((FileProvider) res.as(FileProvider.class));
                File base = r.getBaseDir();
                if (base == null) {
                    base = Copy.NULL_FILE_PLACEHOLDER;
                }
                basedirs.add(base);
                Vector files = (Vector) basedirToFilesMap.get(base);
                if (files == null) {
                    files = new Vector();
                    basedirToFilesMap.put(base, files);
                }
                if (base == Copy.NULL_FILE_PLACEHOLDER) {
                    files.add(r.getFile().getAbsolutePath());
                } else {
                    files.add(r.getName());
                }
            }
            iter = basedirs.iterator();
            while (iter.hasNext()) {
                File base = (File) iter.next();
                Vector f = (Vector) basedirToFilesMap.get(base);
                String[] files = (String[]) f.toArray(new String[f.size()]);
                upToDate &=
                    check(base == Copy.NULL_FILE_PLACEHOLDER ? null : base,
                          files);
            }
        } else { 
            Iterator iter = rc.iterator();
            while (upToDate && iter.hasNext()) {
                Resource r = (Resource) iter.next();
                upToDate = archiveIsUpToDate(r);
            }
        }
        return upToDate;
    }
    protected boolean check(File basedir, String[] files) {
        boolean upToDate = true;
        if (!archiveIsUpToDate(files, basedir)) {
            upToDate = false;
        }
        for (int i = 0; i < files.length; ++i) {
            if (tarFile.equals(new File(basedir, files[i]))) {
                throw new BuildException("A tar file cannot include "
                                         + "itself", getLocation());
            }
        }
        return upToDate;
    }
    protected void tar(ResourceCollection rc, TarOutputStream tOut)
        throws IOException {
        ArchiveFileSet afs = null;
        if (rc instanceof ArchiveFileSet) {
            afs = (ArchiveFileSet) rc;
        }
        if (afs != null && afs.size() > 1
            && afs.getFullpath(this.getProject()).length() > 0) {
            throw new BuildException("fullpath attribute may only "
                                     + "be specified for "
                                     + "filesets that specify a "
                                     + "single file.");
        }
        TarFileSet tfs = asTarFileSet(afs);
        if (isFileFileSet(rc)) {
            FileSet fs = (FileSet) rc;
            String[] files = getFileNames(fs);
            for (int i = 0; i < files.length; i++) {
                File f = new File(fs.getDir(getProject()), files[i]);
                String name = files[i].replace(File.separatorChar, '/');
                tarFile(f, tOut, name, tfs);
            }
        } else if (rc.isFilesystemOnly()) {
            Iterator iter = rc.iterator();
            while (iter.hasNext()) {
                Resource r = (Resource) iter.next();
                File f = ((FileProvider) r.as(FileProvider.class)).getFile();
                tarFile(f, tOut, f.getName(), tfs);
            }
        } else { 
            Iterator iter = rc.iterator();
            while (iter.hasNext()) {
                Resource r = (Resource) iter.next();
                tarResource(r, tOut, r.getName(), tfs);
            }
        }
    }
    protected static boolean isFileFileSet(ResourceCollection rc) {
        return rc instanceof FileSet && rc.isFilesystemOnly();
    }
    protected static String[] getFileNames(FileSet fs) {
        DirectoryScanner ds = fs.getDirectoryScanner(fs.getProject());
        String[] directories = ds.getIncludedDirectories();
        String[] filesPerSe = ds.getIncludedFiles();
        String[] files = new String [directories.length + filesPerSe.length];
        System.arraycopy(directories, 0, files, 0, directories.length);
        System.arraycopy(filesPerSe, 0, files, directories.length,
                         filesPerSe.length);
        return files;
    }
    protected TarFileSet asTarFileSet(ArchiveFileSet archiveFileSet) {
        TarFileSet tfs = null;
        if (archiveFileSet != null && archiveFileSet instanceof TarFileSet) {
            tfs = (TarFileSet) archiveFileSet;
        } else {
            tfs = new TarFileSet();
            tfs.setProject(getProject());
            if (archiveFileSet != null) {
                tfs.setPrefix(archiveFileSet.getPrefix(getProject()));
                tfs.setFullpath(archiveFileSet.getFullpath(getProject()));
                if (archiveFileSet.hasFileModeBeenSet()) {
                    tfs.integerSetFileMode(archiveFileSet
                                           .getFileMode(getProject()));
                }
                if (archiveFileSet.hasDirModeBeenSet()) {
                    tfs.integerSetDirMode(archiveFileSet
                                          .getDirMode(getProject()));
                }
                if (archiveFileSet
                    instanceof org.apache.tools.ant.types.TarFileSet) {
                    org.apache.tools.ant.types.TarFileSet t =
                        (org.apache.tools.ant.types.TarFileSet) archiveFileSet;
                    if (t.hasUserNameBeenSet()) {
                        tfs.setUserName(t.getUserName());
                    }
                    if (t.hasGroupBeenSet()) {
                        tfs.setGroup(t.getGroup());
                    }
                    if (t.hasUserIdBeenSet()) {
                        tfs.setUid(t.getUid());
                    }
                    if (t.hasGroupIdBeenSet()) {
                        tfs.setGid(t.getGid());
                    }
                }
            }
        }
        return tfs;
    }
    public static class TarFileSet
        extends org.apache.tools.ant.types.TarFileSet {
        private String[] files = null;
        private boolean preserveLeadingSlashes = false;
        public TarFileSet(FileSet fileset) {
            super(fileset);
        }
        public TarFileSet() {
            super();
        }
        public String[] getFiles(Project p) {
            if (files == null) {
                files = getFileNames(this);
            }
            return files;
        }
        public void setMode(String octalString) {
            setFileMode(octalString);
        }
        public int getMode() {
            return getFileMode(this.getProject());
        }
        public void setPreserveLeadingSlashes(boolean b) {
            this.preserveLeadingSlashes = b;
        }
        public boolean getPreserveLeadingSlashes() {
            return preserveLeadingSlashes;
        }
    }
    public static class TarLongFileMode extends EnumeratedAttribute {
        public static final String
            WARN = "warn",
            FAIL = "fail",
            TRUNCATE = "truncate",
            GNU = "gnu",
            OMIT = "omit";
        private final String[] validModes = {WARN, FAIL, TRUNCATE, GNU, OMIT};
        public TarLongFileMode() {
            super();
            setValue(WARN);
        }
        public String[] getValues() {
            return validModes;
        }
        public boolean isTruncateMode() {
            return TRUNCATE.equalsIgnoreCase(getValue());
        }
        public boolean isWarnMode() {
            return WARN.equalsIgnoreCase(getValue());
        }
        public boolean isGnuMode() {
            return GNU.equalsIgnoreCase(getValue());
        }
        public boolean isFailMode() {
            return FAIL.equalsIgnoreCase(getValue());
        }
        public boolean isOmitMode() {
            return OMIT.equalsIgnoreCase(getValue());
        }
    }
    public static final class TarCompressionMethod extends EnumeratedAttribute {
        private static final String NONE = "none";
        private static final String GZIP = "gzip";
        private static final String BZIP2 = "bzip2";
        public TarCompressionMethod() {
            super();
            setValue(NONE);
        }
        public String[] getValues() {
            return new String[] {NONE, GZIP, BZIP2 };
        }
        private OutputStream compress(final OutputStream ostream)
            throws IOException {
            final String v = getValue();
            if (GZIP.equals(v)) {
                return new GZIPOutputStream(ostream);
            } else {
                if (BZIP2.equals(v)) {
                    ostream.write('B');
                    ostream.write('Z');
                    return new CBZip2OutputStream(ostream);
                }
            }
            return ostream;
        }
    }
}