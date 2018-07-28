package org.apache.tools.ant.taskdefs;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.util.FileUtils;
public class GUnzip extends Unpack {
    private static final int BUFFER_SIZE = 8 * 1024;
    private static final String DEFAULT_EXTENSION = ".gz";
    protected String getDefaultExtension() {
        return DEFAULT_EXTENSION;
    }
    protected void extract() {
        if (source.lastModified() > dest.lastModified()) {
            log("Expanding " + source.getAbsolutePath() + " to "
                        + dest.getAbsolutePath());
            FileOutputStream out = null;
            GZIPInputStream zIn = null;
            InputStream fis = null;
            try {
                out = new FileOutputStream(dest);
                fis = srcResource.getInputStream();
                zIn = new GZIPInputStream(fis);
                byte[] buffer = new byte[BUFFER_SIZE];
                int count = 0;
                do {
                    out.write(buffer, 0, count);
                    count = zIn.read(buffer, 0, buffer.length);
                } while (count != -1);
            } catch (IOException ioe) {
                String msg = "Problem expanding gzip " + ioe.getMessage();
                throw new BuildException(msg, ioe, getLocation());
            } finally {
                FileUtils.close(fis);
                FileUtils.close(out);
                FileUtils.close(zIn);
            }
        }
    }
    protected boolean supportsNonFileResources() {
        return getClass().equals(GUnzip.class);
    }
}