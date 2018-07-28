package org.apache.tools.ant;
import java.io.File;
public interface FileScanner {
    void addDefaultExcludes();
    File getBasedir();
    String[] getExcludedDirectories();
    String[] getExcludedFiles();
    String[] getIncludedDirectories();
    String[] getIncludedFiles();
    String[] getNotIncludedDirectories();
    String[] getNotIncludedFiles();
    void scan() throws IllegalStateException;
    void setBasedir(String basedir);
    void setBasedir(File basedir);
    void setExcludes(String[] excludes);
    void setIncludes(String[] includes);
    void setCaseSensitive(boolean isCaseSensitive);
}