package org.apache.tools.ant.types.resources;
import java.io.File;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.apache.tools.ant.Project;
public class FileResourceIterator implements Iterator {
    private Project project;
    private File basedir;
    private String[] files;
    private int pos = 0;
    public FileResourceIterator() {
    }
    public FileResourceIterator(Project project) {
        this.project = project;
    }
    public FileResourceIterator(File basedir) {
        this(null, basedir);
    }
    public FileResourceIterator(Project project, File basedir) {
        this(project);
        this.basedir = basedir;
    }
    public FileResourceIterator(File basedir, String[] filenames) {
        this(null, basedir, filenames);
    }
    public FileResourceIterator(Project project, File basedir, String[] filenames) {
        this(project, basedir);
        addFiles(filenames);
    }
    public void addFiles(String[] s) {
        int start = (files == null) ? 0 : files.length;
        String[] newfiles = new String[start + s.length];
        if (start > 0) {
            System.arraycopy(files, 0, newfiles, 0, start);
        }
        files = newfiles;
        System.arraycopy(s, 0, files, start, s.length);
    }
    public boolean hasNext() {
        return pos < files.length;
    }
    public Object next() {
        return nextResource();
    }
    public void remove() {
        throw new UnsupportedOperationException();
    }
    public FileResource nextResource() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        FileResource result = new FileResource(basedir, files[pos++]);
        result.setProject(project);
        return result;
    }
}
