package org.apache.tools.ant.taskdefs;
import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.zip.UnixStat;
public class ZipTest extends BuildFileTest {
    ZipFile zfPrefixAddsDir = null;
    public ZipTest(String name) {
        super(name);
    }
    public void setUp() {
        configureProject("src/etc/testcases/taskdefs/zip.xml");
    }
    public void test1() {
        expectBuildException("test1", "required argument not specified");
    }
    public void test2() {
        expectBuildException("test2", "required argument not specified");
    }
    public void test3() {
        expectBuildException("test3", "zip cannot include itself");
    }
    public void tearDown() {
        try {
            if ( zfPrefixAddsDir != null) {
                zfPrefixAddsDir.close();
            }
        } catch (IOException e) {
        }
        executeTarget("cleanup");
    }
    public void test5() {
        executeTarget("test5");
    }
    public void test6() {
        executeTarget("test6");
    }
    public void test7() {
        executeTarget("test7");
    }
    public void test8() {
        executeTarget("test8");
    }
    public void testZipgroupfileset() throws IOException {
        executeTarget("testZipgroupfileset");
        ZipFile zipFile = new ZipFile(new File(getProjectDir(), "zipgroupfileset.zip"));
        assertTrue(zipFile.getEntry("ant.xml") != null);
        assertTrue(zipFile.getEntry("optional/jspc.xml") != null);
        assertTrue(zipFile.getEntry("zip/zipgroupfileset3.zip") != null);
        assertTrue(zipFile.getEntry("test6.mf") == null);
        assertTrue(zipFile.getEntry("test7.mf") == null);
        zipFile.close();
    }
    public void testUpdateNotNecessary() {
        executeTarget("testUpdateNotNecessary");
        assertEquals(-1, getLog().indexOf("Updating"));
    }
    public void testUpdateIsNecessary() {
        expectLogContaining("testUpdateIsNecessary", "Updating");
    }
    public void testPrefixAddsDir() throws IOException {
        executeTarget("testPrefixAddsDir");
        File archive = getProject().resolveFile("test3.zip");
        zfPrefixAddsDir = new ZipFile(archive);
        ZipEntry ze = zfPrefixAddsDir.getEntry("test/");
        assertNotNull("test/ has been added", ze);
    }
    public void testFilesOnlyDoesntCauseRecreate()
        throws InterruptedException {
        executeTarget("testFilesOnlyDoesntCauseRecreateSetup");
        long l = getProject().resolveFile("test3.zip").lastModified();
        Thread.sleep(3000);
        executeTarget("testFilesOnlyDoesntCauseRecreate");
        assertEquals(l, getProject().resolveFile("test3.zip").lastModified());
    }
    public void testEmptySkip() {
        executeTarget("testEmptySkip");
    }
    public void testZipEmptyDir() {
        executeTarget("zipEmptyDir");
    }
    public void testZipEmptyDirFilesOnly() {
        executeTarget("zipEmptyDirFilesOnly");
    }
    public void testZipEmptyCreate() {
        expectLogContaining("zipEmptyCreate", "Note: creating empty");
    }
    public void testCompressionLevel() {
        executeTarget("testCompressionLevel");
    }
    public void testDefaultExcludesAndUpdate() 
        throws ZipException, IOException {
        executeTarget("testDefaultExcludesAndUpdate");
        ZipFile f = null;
        try {
            f = new ZipFile(getProject().resolveFile("test3.zip"));
            assertNotNull("ziptest~ should be included",
                          f.getEntry("ziptest~"));
        } finally {
            if (f != null) {
                f.close();
            }
        }
    }
    public void testFileResource() {
        executeTarget("testFileResource");
    }
    public void testNonFileResource() {
        executeTarget("testNonFileResource");
    }
    public void testTarFileSet() throws IOException {
        executeTarget("testTarFileSet");
        org.apache.tools.zip.ZipFile zf = null;
        try {
            zf = new org.apache.tools.zip.ZipFile(getProject()
                                                  .resolveFile("test3.zip"));
            org.apache.tools.zip.ZipEntry ze = zf.getEntry("asf-logo.gif");
            assertEquals(UnixStat.FILE_FLAG | 0446, ze.getUnixMode());
        } finally {
            if (zf != null) {
                zf.close();
            }
        }
    }
    public void testRewriteZeroPermissions() throws IOException {
        executeTarget("rewriteZeroPermissions");
        org.apache.tools.zip.ZipFile zf = null;
        try {
            zf = new org.apache.tools.zip.ZipFile(getProject()
                                                  .resolveFile("test3.zip"));
            org.apache.tools.zip.ZipEntry ze = zf.getEntry("testdir/test.txt");
            assertEquals(UnixStat.FILE_FLAG | 0644, ze.getUnixMode());
        } finally {
            if (zf != null) {
                zf.close();
            }
        }
    }
    public void testAcceptZeroPermissions() throws IOException {
        executeTarget("acceptZeroPermissions");
        org.apache.tools.zip.ZipFile zf = null;
        try {
            zf = new org.apache.tools.zip.ZipFile(getProject()
                                                  .resolveFile("test3.zip"));
            org.apache.tools.zip.ZipEntry ze = zf.getEntry("testdir/test.txt");
            assertEquals(0000, ze.getUnixMode());
        } finally {
            if (zf != null) {
                zf.close();
            }
        }
    }
    public void testForBugzilla34764() throws IOException {
        executeTarget("testForBugzilla34764");
        org.apache.tools.zip.ZipFile zf = null;
        try {
            zf = new org.apache.tools.zip.ZipFile(getProject()
                                                  .resolveFile("test3.zip"));
            org.apache.tools.zip.ZipEntry ze = zf.getEntry("file1");
            assertEquals(UnixStat.FILE_FLAG | 0644, ze.getUnixMode());
        } finally {
            if (zf != null) {
                zf.close();
            }
        }
    }
}
