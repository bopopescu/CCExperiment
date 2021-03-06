package org.apache.tools.ant.types.resources.comparators;
import java.io.File;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileProvider;
import org.apache.tools.ant.util.FileUtils;
public class FileSystem extends ResourceComparator {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    protected int resourceCompare(Resource foo, Resource bar) {
        FileProvider fooFP = (FileProvider) foo.as(FileProvider.class);
        if (fooFP == null) {
            throw new ClassCastException(foo.getClass()
                                         + " doesn't provide files");
        }
        File foofile = fooFP.getFile();
        FileProvider barFP = (FileProvider) bar.as(FileProvider.class);
        if (barFP == null) {
            throw new ClassCastException(bar.getClass()
                                         + " doesn't provide files");
        }
        File barfile = barFP.getFile();
        return foofile.equals(barfile) ? 0
            : FILE_UTILS.isLeadingPath(foofile, barfile) ? -1
            : FILE_UTILS.normalize(foofile.getAbsolutePath()).compareTo(
                FILE_UTILS.normalize(barfile.getAbsolutePath()));
    }
}
