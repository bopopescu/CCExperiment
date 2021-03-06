package org.apache.tools.ant.types.resources;
import java.io.File;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collections;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.Reference;
import org.apache.tools.ant.types.PatternSet;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.selectors.FileSelector;
import org.apache.tools.ant.types.selectors.AbstractSelectorContainer;
public class Files extends AbstractSelectorContainer
    implements ResourceCollection {
    private static final Iterator EMPTY_ITERATOR
        = Collections.EMPTY_SET.iterator();
    private PatternSet defaultPatterns = new PatternSet();
    private Vector additionalPatterns = new Vector();
    private boolean useDefaultExcludes = true;
    private boolean caseSensitive = true;
    private boolean followSymlinks = true;
    private DirectoryScanner ds = null;
    public Files() {
        super();
    }
    protected Files(Files f) {
        this.defaultPatterns = f.defaultPatterns;
        this.additionalPatterns = f.additionalPatterns;
        this.useDefaultExcludes = f.useDefaultExcludes;
        this.caseSensitive = f.caseSensitive;
        this.followSymlinks = f.followSymlinks;
        this.ds = f.ds;
        setProject(f.getProject());
    }
    public void setRefid(Reference r) throws BuildException {
        if (hasPatterns(defaultPatterns)) {
            throw tooManyAttributes();
        }
        if (!additionalPatterns.isEmpty()) {
            throw noChildrenAllowed();
        }
        if (hasSelectors()) {
            throw noChildrenAllowed();
        }
        super.setRefid(r);
    }
    public synchronized PatternSet createPatternSet() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        PatternSet patterns = new PatternSet();
        additionalPatterns.addElement(patterns);
        ds = null;
        setChecked(false);
        return patterns;
    }
    public synchronized PatternSet.NameEntry createInclude() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        ds = null;
        return defaultPatterns.createInclude();
    }
    public synchronized PatternSet.NameEntry createIncludesFile() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        ds = null;
        return defaultPatterns.createIncludesFile();
    }
    public synchronized PatternSet.NameEntry createExclude() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        ds = null;
        return defaultPatterns.createExclude();
    }
    public synchronized PatternSet.NameEntry createExcludesFile() {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        ds = null;
        return defaultPatterns.createExcludesFile();
    }
    public synchronized void setIncludes(String includes) {
        checkAttributesAllowed();
        defaultPatterns.setIncludes(includes);
        ds = null;
    }
    public synchronized void appendIncludes(String[] includes) {
        checkAttributesAllowed();
        if (includes != null) {
            for (int i = 0; i < includes.length; i++) {
                defaultPatterns.createInclude().setName(includes[i]);
            }
            ds = null;
        }
    }
    public synchronized void setExcludes(String excludes) {
        checkAttributesAllowed();
        defaultPatterns.setExcludes(excludes);
        ds = null;
    }
    public synchronized void appendExcludes(String[] excludes) {
        checkAttributesAllowed();
        if (excludes != null) {
            for (int i = 0; i < excludes.length; i++) {
                defaultPatterns.createExclude().setName(excludes[i]);
            }
            ds = null;
        }
    }
    public synchronized void setIncludesfile(File incl) throws BuildException {
        checkAttributesAllowed();
        defaultPatterns.setIncludesfile(incl);
        ds = null;
    }
    public synchronized void setExcludesfile(File excl) throws BuildException {
        checkAttributesAllowed();
        defaultPatterns.setExcludesfile(excl);
        ds = null;
    }
    public synchronized void setDefaultexcludes(boolean useDefaultExcludes) {
        checkAttributesAllowed();
        this.useDefaultExcludes = useDefaultExcludes;
        ds = null;
    }
    public synchronized boolean getDefaultexcludes() {
        return (isReference())
            ? getRef().getDefaultexcludes() : useDefaultExcludes;
    }
    public synchronized void setCaseSensitive(boolean caseSensitive) {
        checkAttributesAllowed();
        this.caseSensitive = caseSensitive;
        ds = null;
    }
    public synchronized boolean isCaseSensitive() {
        return (isReference())
            ? getRef().isCaseSensitive() : caseSensitive;
    }
    public synchronized void setFollowSymlinks(boolean followSymlinks) {
        checkAttributesAllowed();
        this.followSymlinks = followSymlinks;
        ds = null;
    }
    public synchronized boolean isFollowSymlinks() {
        return (isReference())
            ? getRef().isFollowSymlinks() : followSymlinks;
    }
    public synchronized Iterator iterator() {
        if (isReference()) {
            return getRef().iterator();
        }
        ensureDirectoryScannerSetup();
        ds.scan();
        int fct = ds.getIncludedFilesCount();
        int dct = ds.getIncludedDirsCount();
        if (fct + dct == 0) {
            return EMPTY_ITERATOR;
        }
        FileResourceIterator result = new FileResourceIterator(getProject());
        if (fct > 0) {
            result.addFiles(ds.getIncludedFiles());
        }
        if (dct > 0) {
            result.addFiles(ds.getIncludedDirectories());
        }
        return result;
    }
    public synchronized int size() {
        if (isReference()) {
            return getRef().size();
        }
        ensureDirectoryScannerSetup();
        ds.scan();
        return ds.getIncludedFilesCount() + ds.getIncludedDirsCount();
    }
    public synchronized boolean hasPatterns() {
        if (isReference()) {
            return getRef().hasPatterns();
        }
        dieOnCircularReference();
        if (hasPatterns(defaultPatterns)) {
            return true;
        }
        for (Iterator i = additionalPatterns.iterator(); i.hasNext();) {
            if (hasPatterns((PatternSet) i.next())) {
                return true;
            }
        }
        return false;
    }
    public synchronized void appendSelector(FileSelector selector) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        super.appendSelector(selector);
        ds = null;
    }
    public String toString() {
        if (isReference()) {
            return getRef().toString();
        }
        Iterator i = iterator();
        if (!i.hasNext()) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        while (i.hasNext()) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparatorChar);
            }
            sb.append(i.next());
        }
        return sb.toString();
    }
    public synchronized Object clone() {
        if (isReference()) {
            return getRef().clone();
        }
        Files f = (Files) super.clone();
        f.defaultPatterns = (PatternSet) defaultPatterns.clone();
        f.additionalPatterns = new Vector(additionalPatterns.size());
        for (Iterator iter = additionalPatterns.iterator(); iter.hasNext();) {
            PatternSet ps = (PatternSet) iter.next();
            f.additionalPatterns.add(ps.clone());
        }
        return f;
    }
    public String[] mergeIncludes(Project p) {
        return mergePatterns(p).getIncludePatterns(p);
    }
    public String[] mergeExcludes(Project p) {
        return mergePatterns(p).getExcludePatterns(p);
    }
    public synchronized PatternSet mergePatterns(Project p) {
        if (isReference()) {
            return getRef().mergePatterns(p);
        }
        dieOnCircularReference();
        PatternSet ps = new PatternSet();
        ps.append(defaultPatterns, p);
        final int count = additionalPatterns.size();
        for (int i = 0; i < count; i++) {
            Object o = additionalPatterns.elementAt(i);
            ps.append((PatternSet) o, p);
        }
        return ps;
    }
    public boolean isFilesystemOnly() {
        return true;
    }
    protected Files getRef() {
        return (Files) getCheckedRef();
    }
    private synchronized void ensureDirectoryScannerSetup() {
        dieOnCircularReference();
        if (ds == null) {
            ds = new DirectoryScanner();
            PatternSet ps = mergePatterns(getProject());
            ds.setIncludes(ps.getIncludePatterns(getProject()));
            ds.setExcludes(ps.getExcludePatterns(getProject()));
            ds.setSelectors(getSelectors(getProject()));
            if (useDefaultExcludes) {
                ds.addDefaultExcludes();
            }
            ds.setCaseSensitive(caseSensitive);
            ds.setFollowSymlinks(followSymlinks);
        }
    }
    private boolean hasPatterns(PatternSet ps) {
        String[] includePatterns = ps.getIncludePatterns(getProject());
        String[] excludePatterns = ps.getExcludePatterns(getProject());
        return (includePatterns != null && includePatterns.length > 0)
            || (includePatterns != null && excludePatterns.length > 0);
    }
}
