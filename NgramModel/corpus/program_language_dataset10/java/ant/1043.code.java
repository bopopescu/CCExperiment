package org.apache.tools.ant.taskdefs.email;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import junit.framework.TestCase;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.FileUtils;
public class MessageTest extends TestCase {
    private File f = new File(System.getProperty("java.io.tmpdir"),
                              "message.txt");
    public void testPrintStreamDoesNotGetClosed() {
        Message ms = new Message();
        Project p = new Project();
        ms.setProject(p);
        ms.addText("hi, this is an email");
        FileOutputStream fis = null;
        try {
            fis = new FileOutputStream(f);
            ms.print(new PrintStream(fis));
            fis.write(120);
        } catch (IOException ioe) {
            fail("we should not have issues writing after having called Message.print");
        } finally {
            FileUtils.close(fis);
        }
    }
    public void tearDown() {
        if (f.exists()) {
            FileUtils fu = FileUtils.getFileUtils();
            fu.tryHardToDelete(f);
        }
    }
}
