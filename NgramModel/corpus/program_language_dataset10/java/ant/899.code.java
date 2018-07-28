package org.apache.tools.ant;
import org.apache.tools.ant.taskdefs.condition.Os;
import org.apache.tools.ant.types.selectors.TokenizedPath;
import org.apache.tools.ant.util.SymbolicLinkUtils;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
public class DirectoryScannerTest extends BuildFileTest {
    public DirectoryScannerTest(String name) {super(name);}
    private boolean supportsSymlinks = Os.isFamily("unix");
    public void setUp() {
        configureProject("src/etc/testcases/core/directoryscanner.xml");
        getProject().executeTarget("setup");
    }
    public void tearDown() {
        getProject().executeTarget("cleanup");
    }
    public void test1() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha"});
        ds.scan();
        compareFiles(ds, new String[] {} ,new String[] {"alpha"});
    }
    public void test2() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/"});
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/beta.xml",
                                       "alpha/beta/gamma/gamma.xml"},
                     new String[] {"alpha", "alpha/beta", "alpha/beta/gamma"});
    }
    public void test3() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/beta.xml",
                                       "alpha/beta/gamma/gamma.xml"},
                     new String[] {"", "alpha", "alpha/beta",
                                   "alpha/beta/gamma"});
    }
    public void testFullPathMatchesCaseSensitive() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/beta/gamma/GAMMA.XML"});
        ds.scan();
        compareFiles(ds, new String[] {}, new String[] {});
    }
    public void testFullPathMatchesCaseInsensitive() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setCaseSensitive(false);
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/beta/gamma/GAMMA.XML"});
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/gamma/gamma.xml"},
            new String[] {});
    }
    public void test2ButCaseInsensitive() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"ALPHA/"});
        ds.setCaseSensitive(false);
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/beta.xml",
                                       "alpha/beta/gamma/gamma.xml"},
                     new String[] {"alpha", "alpha/beta", "alpha/beta/gamma"});
    }
    public void testAllowSymlinks() {
        if (!supportsSymlinks) {
            return;
        }
        getProject().executeTarget("symlink-setup");
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/beta/gamma/"});
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/gamma/gamma.xml"},
                     new String[] {"alpha/beta/gamma"});
    }
    public void testProhibitSymlinks() {
        if (!supportsSymlinks) {
            return;
        }
        getProject().executeTarget("symlink-setup");
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/beta/gamma/"});
        ds.setFollowSymlinks(false);
        ds.scan();
        compareFiles(ds, new String[] {}, new String[] {});
    }
    public void testOrderOfIncludePatternsIrrelevant() {
        String [] expectedFiles = {"alpha/beta/beta.xml",
                                   "alpha/beta/gamma/gamma.xml"};
        String [] expectedDirectories = {"alpha/beta", "alpha/beta/gamma" };
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/be?a/**", "alpha/beta/gamma/"});
        ds.scan();
        compareFiles(ds, expectedFiles, expectedDirectories);
        ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/beta/gamma/", "alpha/be?a/**"});
        ds.scan();
        compareFiles(ds, expectedFiles, expectedDirectories);
    }
    public void testPatternsDifferInCaseScanningSensitive() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/", "ALPHA/"});
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/beta.xml",
                                       "alpha/beta/gamma/gamma.xml"},
                     new String[] {"alpha", "alpha/beta", "alpha/beta/gamma"});
    }
    public void testPatternsDifferInCaseScanningInsensitive() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/", "ALPHA/"});
        ds.setCaseSensitive(false);
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/beta.xml",
                                       "alpha/beta/gamma/gamma.xml"},
                     new String[] {"alpha", "alpha/beta", "alpha/beta/gamma"});
    }
    public void testFullpathDiffersInCaseScanningSensitive() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {
            "alpha/beta/gamma/gamma.xml",
            "alpha/beta/gamma/GAMMA.XML"
        });
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/gamma/gamma.xml"},
                     new String[] {});
    }
    public void testFullpathDiffersInCaseScanningInsensitive() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {
            "alpha/beta/gamma/gamma.xml",
            "alpha/beta/gamma/GAMMA.XML"
        });
        ds.setCaseSensitive(false);
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/gamma/gamma.xml"},
                     new String[] {});
    }
    public void testParentDiffersInCaseScanningSensitive() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/", "ALPHA/beta/"});
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/beta.xml",
                                       "alpha/beta/gamma/gamma.xml"},
                     new String[] {"alpha", "alpha/beta", "alpha/beta/gamma"});
    }
    public void testParentDiffersInCaseScanningInsensitive() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {"alpha/", "ALPHA/beta/"});
        ds.setCaseSensitive(false);
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/beta.xml",
                                       "alpha/beta/gamma/gamma.xml"},
                     new String[] {"alpha", "alpha/beta", "alpha/beta/gamma"});
    }
    public void testSetFollowLinks() throws IOException {
        if (supportsSymlinks) {
            File linkFile = new File(System.getProperty("root"), "src/main/org/apache/tools/ThisIsALink");
            System.err.println("link exists pre-test? " + linkFile.exists());
            try {
                String[] command = new String[] {
                    "ln", "-s", "ant", linkFile.getAbsolutePath()
                };
                try {
                    Runtime.getRuntime().exec(command);
                    Thread.sleep(1000);
                } catch (IOException ioe) {
                    fail("IOException making link "+ioe);
                } catch (InterruptedException ie) {
                }
                File dir = new File(System.getProperty("root"), "src/main/org/apache/tools");
                System.err.println("link exists after exec? " + linkFile.exists());
                System.err.println("Ant knows it is a link? " + SymbolicLinkUtils.getSymbolicLinkUtils().isSymbolicLink(dir, "ThisIsALink"));
                DirectoryScanner ds = new DirectoryScanner();
                ds.setFollowSymlinks(true);
                ds.setBasedir(dir);
                ds.setExcludes(new String[] {"ant/**"});
                ds.scan();
                boolean haveZipPackage = false;
                boolean haveTaskdefsPackage = false;
                String[] included = ds.getIncludedDirectories();
                for (int i=0; i<included.length; i++) {
                    if (included[i].equals("zip")) {
                        haveZipPackage = true;
                    } else if (included[i].equals("ThisIsALink"
                                                  + File.separator
                                                  + "taskdefs")) {
                        haveTaskdefsPackage = true;
                    }
                }
                assertTrue("(1) zip package included", haveZipPackage);
                assertTrue("(1) taskdefs package included",
                           haveTaskdefsPackage);
                ds = new DirectoryScanner();
                ds.setFollowSymlinks(false);
                ds.setBasedir(dir);
                ds.setExcludes(new String[] {"ant/**"});
                ds.scan();
                haveZipPackage = false;
                haveTaskdefsPackage = false;
                included = ds.getIncludedDirectories();
                for (int i=0; i<included.length; i++) {
                    if (included[i].equals("zip")) {
                        haveZipPackage = true;
                    } else if (included[i].equals("ThisIsALink"
                                                  + File.separator
                                                  + "taskdefs")) {
                        haveTaskdefsPackage = true;
                    }
                }
                assertTrue("(2) zip package included", haveZipPackage);
                assertTrue("(2) taskdefs package not included",
                           !haveTaskdefsPackage);
            } finally {
                System.err.println("link exists pre-delete? " + linkFile.exists());
                if (!linkFile.delete()) {
                    throw new RuntimeException("Failed to delete " + linkFile);
                }
                System.err.println("link exists post-delete? " + linkFile.exists());
            }
        }
    }
    public void testExcludeOneFile() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {
            "**/*.xml"
        });
        ds.setExcludes(new String[] {
            "alpha/beta/b*xml"
        });
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/gamma/gamma.xml"},
                     new String[] {});
    }
    public void testExcludeHasPrecedence() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {
            "alpha/**"
        });
        ds.setExcludes(new String[] {
            "alpha/**"
        });
        ds.scan();
        compareFiles(ds, new String[] {},
                     new String[] {});
    }
    public void testAlternateIncludeExclude() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setIncludes(new String[] {
            "alpha/**",
            "alpha/beta/gamma/**"
        });
        ds.setExcludes(new String[] {
            "alpha/beta/**"
        });
        ds.scan();
        compareFiles(ds, new String[] {},
                     new String[] {"alpha"});
    }
    public void testAlternateExcludeInclude() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setExcludes(new String[] {
            "alpha/**",
            "alpha/beta/gamma/**"
        });
        ds.setIncludes(new String[] {
            "alpha/beta/**"
        });
        ds.scan();
        compareFiles(ds, new String[] {},
                     new String[] {});
    }
    public void testChildrenOfExcludedDirectory() {
        getProject().executeTarget("children-of-excluded-dir-setup");
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setExcludes(new String[] {"alpha/**"});
        ds.setFollowSymlinks(false);
        ds.scan();
        compareFiles(ds, new String[] {"delta/delta.xml"},
                    new String[] {"", "delta"});
        ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setExcludes(new String[] {"alpha"});
        ds.setFollowSymlinks(false);
        ds.scan();
        compareFiles(ds, new String[] {"alpha/beta/beta.xml",
                                       "alpha/beta/gamma/gamma.xml",
                                        "delta/delta.xml"},
                     new String[] {"", "alpha/beta", "alpha/beta/gamma", "delta"});
    }
    public void testIsExcludedDirectoryScanned() {
        String shareclassloader = getProject().getProperty("tests.and.ant.share.classloader");
        if (shareclassloader == null
                || (shareclassloader != null && shareclassloader.indexOf("${") == 0)) {
            System.out.println("cannot execute testIsExcludedDirectoryScanned when tests are forked, " +
                    "package private method called");
            return;
        }
        getProject().executeTarget("children-of-excluded-dir-setup");
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setExcludes(new String[] {"**/gamma/**"});
        ds.setFollowSymlinks(false);
        ds.scan();
        Set set = ds.getScannedDirs();
        assertFalse("empty set", set.isEmpty());
        String s = "alpha/beta/gamma/".replace('/', File.separatorChar);
        assertFalse("scanned " + s, set.contains(s));
    }
    public void testAbsolute1() {
        getProject().executeTarget("extended-setup");
        DirectoryScanner ds = new DirectoryScanner();
        String tmpdir = getProject().getBaseDir().getAbsolutePath().replace(
            File.separatorChar, '/') + "/tmp";
        ds.setIncludes(new String[] {tmpdir + "/**/*"});
        ds.scan();
        compareFiles(ds, new String[] {tmpdir + "/alpha/beta/beta.xml",
                                       tmpdir + "/alpha/beta/gamma/gamma.xml",
                                       tmpdir + "/delta/delta.xml"},
                     new String[] {tmpdir + "/alpha",
                                   tmpdir + "/alpha/beta",
                                   tmpdir + "/alpha/beta/gamma",
                                   tmpdir + "/delta"});
    }
    public void testAbsolute2() {
        getProject().executeTarget("setup");
        DirectoryScanner ds = new DirectoryScanner();
        ds.setIncludes(new String[] {"alpha/**", "alpha/beta/gamma/**"});
        ds.scan();
        String[] mt = new String[0];
        compareFiles(ds, mt, mt);
    }
    public void testAbsolute3() {
        getProject().executeTarget("extended-setup");
        DirectoryScanner ds = new DirectoryScanner();
        String tmpdir = getProject().getBaseDir().getAbsolutePath().replace(
            File.separatorChar, '/') + "/tmp";
        ds.setIncludes(new String[] {tmpdir + "/**/*"});
        ds.setExcludes(new String[] {"**/alpha",
                                     "**/delta/*"});
        ds.scan();
        compareFiles(ds, new String[] {tmpdir + "/alpha/beta/beta.xml",
                                       tmpdir + "/alpha/beta/gamma/gamma.xml"},
                     new String[] {tmpdir + "/alpha/beta",
                                   tmpdir + "/alpha/beta/gamma",
                                   tmpdir + "/delta"});
    }
    public void testAbsolute4() {
        getProject().executeTarget("extended-setup");
        DirectoryScanner ds = new DirectoryScanner();
        String tmpdir = getProject().getBaseDir().getAbsolutePath().replace(
            File.separatorChar, '/') + "/tmp";
        ds.setIncludes(new String[] {tmpdir + "/alpha/beta/**/*",
                                     tmpdir + "/delta/*"});
        ds.setExcludes(new String[] {"**/beta.xml"});
        ds.scan();
        compareFiles(ds, new String[] {tmpdir + "/alpha/beta/gamma/gamma.xml",
                                       tmpdir + "/delta/delta.xml"},
                     new String[] {tmpdir + "/alpha/beta/gamma"});
    }
    public void testAbsolute5() {
        if (!(Os.isFamily("dos") || Os.isFamily("netware"))) {
            return;
        }
        DirectoryScanner ds = new DirectoryScanner();
        String pattern = new File(File.separator).getAbsolutePath().toUpperCase() + "*";
        ds.setIncludes(new String[] {pattern});
        ds.scan();
        assertTrue("should have at least one resident file",
            ds.getIncludedFilesCount() + ds.getIncludedDirsCount() > 0);
    }
    private void compareFiles(DirectoryScanner ds, String[] expectedFiles,
                              String[] expectedDirectories) {
        String includedFiles[] = ds.getIncludedFiles();
        String includedDirectories[] = ds.getIncludedDirectories();
        assertEquals("file present: ", expectedFiles.length,
                     includedFiles.length);
        assertEquals("directories present: ", expectedDirectories.length,
                     includedDirectories.length);
        TreeSet files = new TreeSet();
        for (int counter = 0; counter < includedFiles.length; counter++) {
            files.add(includedFiles[counter].replace(File.separatorChar, '/'));
        }
        TreeSet directories = new TreeSet();
        for (int counter = 0; counter < includedDirectories.length; counter++) {
            directories.add(includedDirectories[counter]
                            .replace(File.separatorChar, '/'));
        }
        String currentfile;
        Iterator i = files.iterator();
        int counter = 0;
        while (i.hasNext()) {
            currentfile = (String) i.next();
            assertEquals(expectedFiles[counter], currentfile);
            counter++;
        }
        String currentdirectory;
        Iterator dirit = directories.iterator();
        counter = 0;
        while (dirit.hasNext()) {
            currentdirectory = (String) dirit.next();
            assertEquals(expectedDirectories[counter], currentdirectory);
            counter++;
        }
    }
    public void testRecursiveExcludes() throws Exception {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File(getProject().getBaseDir(), "tmp"));
        ds.setExcludes(new String[] {"**/beta/**"});
        ds.scan();
        List dirs = Arrays.asList(ds.getExcludedDirectories());
        assertEquals(2, dirs.size());
        assertTrue("beta is excluded",
                   dirs.contains("alpha/beta".replace('/', File.separatorChar)));
        assertTrue("gamma is excluded",
                   dirs.contains("alpha/beta/gamma".replace('/',
                                                            File.separatorChar)));
        List files = Arrays.asList(ds.getExcludedFiles());
        assertEquals(2, files.size());
        assertTrue("beta.xml is excluded",
                   files.contains("alpha/beta/beta.xml"
                                  .replace('/', File.separatorChar)));
        assertTrue("gamma.xml is excluded",
                   files.contains("alpha/beta/gamma/gamma.xml"
                                  .replace('/', File.separatorChar)));
    }
    public void testContentsExcluded() {
        DirectoryScanner ds = new DirectoryScanner();
        ds.setBasedir(new File("."));
        ds.setIncludes(new String[] {"**"});
        ds.addDefaultExcludes();
        ds.ensureNonPatternSetsReady();
        File f = new File(".svn");
        TokenizedPath p = new TokenizedPath(f.getAbsolutePath());
        assertTrue(ds.contentsExcluded(p));
    }
}