package org.apache.batik.apps.svgbrowser;
import java.io.File;
import javax.swing.filechooser.FileFilter;
public class SquiggleInputHandlerFilter extends FileFilter {
    protected SquiggleInputHandler handler;
    public SquiggleInputHandlerFilter(SquiggleInputHandler handler) {
        this.handler = handler;
    }
    public boolean accept(File f) {
        return f.isDirectory() || handler.accept(f);
    }
    public String getDescription() {
        StringBuffer sb = new StringBuffer();
        String[] extensions = handler.getHandledExtensions();
        int n = extensions != null ? extensions.length : 0;
        for (int i=0; i<n; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(extensions[i]);
        }
        if (n > 0) {
            sb.append( ' ' );
        }
        sb.append(handler.getDescription());
        return sb.toString();
    }
}
