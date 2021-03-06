package org.apache.tools.ant.types;
import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Stack;
import java.util.Vector;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.selectors.OrSelector;
import org.apache.tools.ant.types.selectors.AndSelector;
import org.apache.tools.ant.types.selectors.NotSelector;
import org.apache.tools.ant.types.selectors.DateSelector;
import org.apache.tools.ant.types.selectors.FileSelector;
import org.apache.tools.ant.types.selectors.NoneSelector;
import org.apache.tools.ant.types.selectors.SizeSelector;
import org.apache.tools.ant.types.selectors.TypeSelector;
import org.apache.tools.ant.types.selectors.DepthSelector;
import org.apache.tools.ant.types.selectors.DependSelector;
import org.apache.tools.ant.types.selectors.ExtendSelector;
import org.apache.tools.ant.types.selectors.SelectSelector;
import org.apache.tools.ant.types.selectors.PresentSelector;
import org.apache.tools.ant.types.selectors.SelectorScanner;
import org.apache.tools.ant.types.selectors.ContainsSelector;
import org.apache.tools.ant.types.selectors.FilenameSelector;
import org.apache.tools.ant.types.selectors.MajoritySelector;
import org.apache.tools.ant.types.selectors.DifferentSelector;
import org.apache.tools.ant.types.selectors.SelectorContainer;
import org.apache.tools.ant.types.selectors.ContainsRegexpSelector;
import org.apache.tools.ant.types.selectors.ReadableSelector;
import org.apache.tools.ant.types.selectors.WritableSelector;
import org.apache.tools.ant.types.selectors.modifiedselector.ModifiedSelector;
public abstract class AbstractFileSet extends DataType
    implements Cloneable, SelectorContainer {
    private PatternSet defaultPatterns = new PatternSet();
    private Vector additionalPatterns = new Vector();
    private Vector selectors = new Vector();
    private File dir;
    private boolean useDefaultExcludes = true;
    private boolean caseSensitive = true;
    private boolean followSymlinks = true;
    private boolean errorOnMissingDir = true;
    private int maxLevelsOfSymlinks = DirectoryScanner.MAX_LEVELS_OF_SYMLINKS;
    private DirectoryScanner directoryScanner = null;
    public AbstractFileSet() {
        super();
    }
    protected AbstractFileSet(AbstractFileSet fileset) {
        this.dir = fileset.dir;
        this.defaultPatterns = fileset.defaultPatterns;
        this.additionalPatterns = fileset.additionalPatterns;
        this.selectors = fileset.selectors;
        this.useDefaultExcludes = fileset.useDefaultExcludes;
        this.caseSensitive = fileset.caseSensitive;
        this.followSymlinks = fileset.followSymlinks;
        this.errorOnMissingDir = fileset.errorOnMissingDir;
        this.maxLevelsOfSymlinks = fileset.maxLevelsOfSymlinks;
        setProject(fileset.getProject());
    }
    public void setRefid(Reference r) throws BuildException {
        if (dir != null || defaultPatterns.hasPatterns(getProject())) {
            throw tooManyAttributes();
        }
        if (!additionalPatterns.isEmpty()) {
            throw noChildrenAllowed();
        }
        if (!selectors.isEmpty()) {
            throw noChildrenAllowed();
        }
        super.setRefid(r);
    }
    public synchronized void setDir(File dir) throws BuildException {
        if (isReference()) {
            throw tooManyAttributes();
        }
        this.dir = dir;
        directoryScanner = null;
    }
    public File getDir() {
        return getDir(getProject());
    }
    public synchronized File getDir(Project p) {
        if (isReference()) {
            return getRef(p).getDir(p);
        }
        dieOnCircularReference();
        return dir;
    }
    public synchronized PatternSet createPatternSet() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        PatternSet patterns = new PatternSet();
        additionalPatterns.addElement(patterns);
        directoryScanner = null;
        return patterns;
    }
    public synchronized PatternSet.NameEntry createInclude() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        directoryScanner = null;
        return defaultPatterns.createInclude();
    }
    public synchronized PatternSet.NameEntry createIncludesFile() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        directoryScanner = null;
        return defaultPatterns.createIncludesFile();
    }
    public synchronized PatternSet.NameEntry createExclude() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        directoryScanner = null;
        return defaultPatterns.createExclude();
    }
    public synchronized PatternSet.NameEntry createExcludesFile() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        directoryScanner = null;
        return defaultPatterns.createExcludesFile();
    }
    public synchronized void setFile(File file) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        setDir(file.getParentFile());
        createInclude().setName(file.getName());
    }
    public synchronized void setIncludes(String includes) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        defaultPatterns.setIncludes(includes);
        directoryScanner = null;
    }
    public synchronized void appendIncludes(String[] includes) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        if (includes != null) {
            for (int i = 0; i < includes.length; i++) {
                defaultPatterns.createInclude().setName(includes[i]);
            }
            directoryScanner = null;
        }
    }
    public synchronized void setExcludes(String excludes) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        defaultPatterns.setExcludes(excludes);
        directoryScanner = null;
    }
    public synchronized void appendExcludes(String[] excludes) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        if (excludes != null) {
            for (int i = 0; i < excludes.length; i++) {
                defaultPatterns.createExclude().setName(excludes[i]);
            }
            directoryScanner = null;
        }
    }
    public synchronized void setIncludesfile(File incl) throws BuildException {
        if (isReference()) {
            throw tooManyAttributes();
        }
        defaultPatterns.setIncludesfile(incl);
        directoryScanner = null;
    }
    public synchronized void setExcludesfile(File excl) throws BuildException {
        if (isReference()) {
            throw tooManyAttributes();
        }
        defaultPatterns.setExcludesfile(excl);
        directoryScanner = null;
    }
    public synchronized void setDefaultexcludes(boolean useDefaultExcludes) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        this.useDefaultExcludes = useDefaultExcludes;
        directoryScanner = null;
    }
    public synchronized boolean getDefaultexcludes() {
        if (isReference()) {
            return getRef(getProject()).getDefaultexcludes();
        }
        dieOnCircularReference();
        return useDefaultExcludes;
    }
    public synchronized void setCaseSensitive(boolean caseSensitive) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        this.caseSensitive = caseSensitive;
        directoryScanner = null;
    }
    public synchronized boolean isCaseSensitive() {
        if (isReference()) {
            return getRef(getProject()).isCaseSensitive();
        }
        dieOnCircularReference();
        return caseSensitive;
    }
    public synchronized void setFollowSymlinks(boolean followSymlinks) {
        if (isReference()) {
            throw tooManyAttributes();
        }
        this.followSymlinks = followSymlinks;
        directoryScanner = null;
    }
    public synchronized boolean isFollowSymlinks() {
        if (isReference()) {
            return getRef(getProject()).isCaseSensitive();
        }
        dieOnCircularReference();
        return followSymlinks;
    }
    public void setMaxLevelsOfSymlinks(int max) {
        maxLevelsOfSymlinks = max;
    }
    public int getMaxLevelsOfSymlinks() {
        return maxLevelsOfSymlinks;
    }
     public void setErrorOnMissingDir(boolean errorOnMissingDir) {
         this.errorOnMissingDir = errorOnMissingDir;
     }
     public boolean getErrorOnMissingDir() {
         return errorOnMissingDir;
     }
    public DirectoryScanner getDirectoryScanner() {
        return getDirectoryScanner(getProject());
    }
    public DirectoryScanner getDirectoryScanner(Project p) {
        if (isReference()) {
            return getRef(p).getDirectoryScanner(p);
        }
        dieOnCircularReference();
        DirectoryScanner ds = null;
        synchronized (this) {
            if (directoryScanner != null && p == getProject()) {
                ds = directoryScanner;
            } else {
                if (dir == null) {
                    throw new BuildException("No directory specified for "
                                             + getDataTypeName() + ".");
                }
                if (!dir.exists() && errorOnMissingDir) {
                    throw new BuildException(dir.getAbsolutePath()
                                             + DirectoryScanner
                                             .DOES_NOT_EXIST_POSTFIX);
                }
                if (!dir.isDirectory() && dir.exists()) {
                    throw new BuildException(dir.getAbsolutePath()
                                             + " is not a directory.");
                }
                ds = new DirectoryScanner();
                setupDirectoryScanner(ds, p);
                ds.setFollowSymlinks(followSymlinks);
                ds.setErrorOnMissingDir(errorOnMissingDir);
                ds.setMaxLevelsOfSymlinks(maxLevelsOfSymlinks);
                directoryScanner = (p == getProject()) ? ds : directoryScanner;
            }
        }
        ds.scan();
        return ds;
    }
    public void setupDirectoryScanner(FileScanner ds) {
        setupDirectoryScanner(ds, getProject());
    }
    public synchronized void setupDirectoryScanner(FileScanner ds, Project p) {
        if (isReference()) {
            getRef(p).setupDirectoryScanner(ds, p);
            return;
        }
        dieOnCircularReference(p);
        if (ds == null) {
            throw new IllegalArgumentException("ds cannot be null");
        }
        ds.setBasedir(dir);
        PatternSet ps = mergePatterns(p);
        p.log(getDataTypeName() + ": Setup scanner in dir " + dir
            + " with " + ps, Project.MSG_DEBUG);
        ds.setIncludes(ps.getIncludePatterns(p));
        ds.setExcludes(ps.getExcludePatterns(p));
        if (ds instanceof SelectorScanner) {
            SelectorScanner ss = (SelectorScanner) ds;
            ss.setSelectors(getSelectors(p));
        }
        if (useDefaultExcludes) {
            ds.addDefaultExcludes();
        }
        ds.setCaseSensitive(caseSensitive);
    }
    protected AbstractFileSet getRef(Project p) {
        return (AbstractFileSet) getCheckedRef(p);
    }
    public synchronized boolean hasSelectors() {
        if (isReference()) {
            return getRef(getProject()).hasSelectors();
        }
        dieOnCircularReference();
        return !(selectors.isEmpty());
    }
    public synchronized boolean hasPatterns() {
        if (isReference() && getProject() != null) {
            return getRef(getProject()).hasPatterns();
        }
        dieOnCircularReference();
        if (defaultPatterns.hasPatterns(getProject())) {
            return true;
        }
        Enumeration e = additionalPatterns.elements();
        while (e.hasMoreElements()) {
            PatternSet ps = (PatternSet) e.nextElement();
            if (ps.hasPatterns(getProject())) {
                return true;
            }
        }
        return false;
    }
    public synchronized int selectorCount() {
        if (isReference()) {
            return getRef(getProject()).selectorCount();
        }
        dieOnCircularReference();
        return selectors.size();
    }
    public synchronized FileSelector[] getSelectors(Project p) {
        if (isReference()) {
            return getRef(getProject()).getSelectors(p);
        }
        dieOnCircularReference(p);
        return (FileSelector[]) (selectors.toArray(
            new FileSelector[selectors.size()]));
    }
    public synchronized Enumeration selectorElements() {
        if (isReference()) {
            return getRef(getProject()).selectorElements();
        }
        dieOnCircularReference();
        return selectors.elements();
    }
    public synchronized void appendSelector(FileSelector selector) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        selectors.addElement(selector);
        directoryScanner = null;
        setChecked(false);
    }
    public void addSelector(SelectSelector selector) {
        appendSelector(selector);
    }
    public void addAnd(AndSelector selector) {
        appendSelector(selector);
    }
    public void addOr(OrSelector selector) {
        appendSelector(selector);
    }
    public void addNot(NotSelector selector) {
        appendSelector(selector);
    }
    public void addNone(NoneSelector selector) {
        appendSelector(selector);
    }
    public void addMajority(MajoritySelector selector) {
        appendSelector(selector);
    }
    public void addDate(DateSelector selector) {
        appendSelector(selector);
    }
    public void addSize(SizeSelector selector) {
        appendSelector(selector);
    }
    public void addDifferent(DifferentSelector selector) {
        appendSelector(selector);
    }
    public void addFilename(FilenameSelector selector) {
        appendSelector(selector);
    }
    public void addType(TypeSelector selector) {
        appendSelector(selector);
    }
    public void addCustom(ExtendSelector selector) {
        appendSelector(selector);
    }
    public void addContains(ContainsSelector selector) {
        appendSelector(selector);
    }
    public void addPresent(PresentSelector selector) {
        appendSelector(selector);
    }
    public void addDepth(DepthSelector selector) {
        appendSelector(selector);
    }
    public void addDepend(DependSelector selector) {
        appendSelector(selector);
    }
    public void addContainsRegexp(ContainsRegexpSelector selector) {
        appendSelector(selector);
    }
    public void addModified(ModifiedSelector selector) {
        appendSelector(selector);
    }
    public void addReadable(ReadableSelector r) {
        appendSelector(r);
    }
    public void addWritable(WritableSelector w) {
        appendSelector(w);
    }
    public void add(FileSelector selector) {
        appendSelector(selector);
    }
    public String toString() {
        if (isReference()) {
            return getRef(getProject()).toString();
        }
        dieOnCircularReference();
        DirectoryScanner ds = getDirectoryScanner(getProject());
        String[] files = ds.getIncludedFiles();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < files.length; i++) {
            if (i > 0) {
                sb.append(';');
            }
            sb.append(files[i]);
        }
        return sb.toString();
    }
    public synchronized Object clone() {
        if (isReference()) {
            return (getRef(getProject())).clone();
        } else {
            try {
                AbstractFileSet fs = (AbstractFileSet) super.clone();
                fs.defaultPatterns = (PatternSet) defaultPatterns.clone();
                fs.additionalPatterns = new Vector(additionalPatterns.size());
                Enumeration e = additionalPatterns.elements();
                while (e.hasMoreElements()) {
                    fs.additionalPatterns
                        .addElement(((PatternSet) e.nextElement()).clone());
                }
                fs.selectors = new Vector(selectors);
                return fs;
            } catch (CloneNotSupportedException e) {
                throw new BuildException(e);
            }
        }
    }
    public String[] mergeIncludes(Project p) {
        return mergePatterns(p).getIncludePatterns(p);
    }
    public String[] mergeExcludes(Project p) {
        return mergePatterns(p).getExcludePatterns(p);
    }
    public synchronized PatternSet mergePatterns(Project p) {
        if (isReference()) {
            return getRef(p).mergePatterns(p);
        }
        dieOnCircularReference();
        PatternSet ps = (PatternSet) defaultPatterns.clone();
        final int count = additionalPatterns.size();
        for (int i = 0; i < count; i++) {
            Object o = additionalPatterns.elementAt(i);
            ps.append((PatternSet) o, p);
        }
        return ps;
    }
    protected synchronized void dieOnCircularReference(Stack stk, Project p)
        throws BuildException {
        if (isChecked()) {
            return;
        }
        if (isReference()) {
            super.dieOnCircularReference(stk, p);
        } else {
            for (Iterator i = selectors.iterator(); i.hasNext(); ) {
                Object o = i.next();
                if (o instanceof DataType) {
                    pushAndInvokeCircularReferenceCheck((DataType) o, stk, p);
                }
            }
            for (Iterator i = additionalPatterns.iterator(); i.hasNext(); ) {
                PatternSet ps = (PatternSet) i.next();
                pushAndInvokeCircularReferenceCheck(ps, stk, p);
            }
            setChecked(true);
        }
    }
}
