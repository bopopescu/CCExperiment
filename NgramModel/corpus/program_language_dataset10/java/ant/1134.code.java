package org.apache.tools.ant.types.selectors;
import java.io.File;
import java.text.RuleBasedCollator;
import java.util.Comparator;
import java.util.Iterator;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Parameter;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.selectors.modifiedselector.Algorithm;
import org.apache.tools.ant.types.selectors.modifiedselector.Cache;
import org.apache.tools.ant.types.selectors.modifiedselector.ChecksumAlgorithm;
import org.apache.tools.ant.types.selectors.modifiedselector.DigestAlgorithm;
import org.apache.tools.ant.types.selectors.modifiedselector.EqualComparator;
import org.apache.tools.ant.types.selectors.modifiedselector.HashvalueAlgorithm;
import org.apache.tools.ant.types.selectors.modifiedselector.ModifiedSelector;
import org.apache.tools.ant.types.selectors.modifiedselector.PropertiesfileCache;
import org.apache.tools.ant.util.FileUtils;
public class ModifiedSelectorTest extends BaseSelectorTest {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private Path testclasses = null;
    public ModifiedSelectorTest(String name) {
        super(name);
    }
    public BaseSelector getInstance() {
        return new ModifiedSelector();
    }
    public void setUp() {
        super.setUp();
        Project prj = getProject();
        if (prj != null) {
            testclasses = new Path(prj, prj.getProperty("build.tests.value"));
        }
    }
    public void testValidateWrongCache() {
        String name = "this-is-not-a-valid-cache-name";
        try {
            ModifiedSelector.CacheName cacheName = new ModifiedSelector.CacheName();
            cacheName.setValue(name);
            fail("CacheSelector.CacheName accepted invalid value.");
        } catch (BuildException be) {
            assertEquals(name + " is not a legal value for this attribute",
                         be.getMessage());
        }
    }
    public void testValidateWrongAlgorithm() {
        String name = "this-is-not-a-valid-algorithm-name";
        try {
            ModifiedSelector.AlgorithmName algoName
                = new ModifiedSelector.AlgorithmName();
            algoName.setValue(name);
            fail("CacheSelector.AlgorithmName accepted invalid value.");
        } catch (BuildException be) {
            assertEquals(name + " is not a legal value for this attribute",
                         be.getMessage());
        }
    }
    public void testValidateWrongComparator() {
        String name = "this-is-not-a-valid-comparator-name";
        try {
            ModifiedSelector.ComparatorName compName
                = new ModifiedSelector.ComparatorName();
            compName.setValue(name);
            fail("ModifiedSelector.ComparatorName accepted invalid value.");
        } catch (BuildException be) {
            assertEquals(name + " is not a legal value for this attribute",
                         be.getMessage());
        }
    }
    public void testIllegalCustomAlgorithm() {
        try {
            getAlgoName("java.lang.Object");
            fail("Illegal classname used.");
        } catch (Exception e) {
            assertTrue("Wrong exception type: " + e.getClass().getName(), e instanceof BuildException);
            assertEquals("Wrong exception message.",
                         "Specified class (java.lang.Object) is not an Algorithm.",
                         e.getMessage());
        }
    }
    public void testNonExistentCustomAlgorithm() {
        boolean noExcThrown = false;
        try {
            getAlgoName("non.existent.custom.Algorithm");
            noExcThrown = true;
        } catch (Exception e) {
            if (noExcThrown) {
                fail("does 'non.existent.custom.Algorithm' really exist?");
            }
            assertTrue("Wrong exception type: " + e.getClass().getName(), e instanceof BuildException);
            assertEquals("Wrong exception message.",
                         "Specified class (non.existent.custom.Algorithm) not found.",
                         e.getMessage());
        }
    }
    public void testCustomAlgorithm() {
        String algo = getAlgoName("org.apache.tools.ant.types.selectors.modifiedselector.HashvalueAlgorithm");
        assertTrue("Wrong algorithm used: "+algo, algo.startsWith("HashvalueAlgorithm"));
    }
    public void testCustomAlgorithm2() {
        String algo = getAlgoName("org.apache.tools.ant.types.selectors.MockAlgorithm");
        assertTrue("Wrong algorithm used: "+algo, algo.startsWith("MockAlgorithm"));
    }
    public void testCustomClasses() {
        BFT bft = new BFT();
        bft.setUp();
        try {
            bft.doTarget("modifiedselectortest-customClasses");
            String fsFullValue = bft.getProperty("fs.full.value");
            String fsModValue  = bft.getProperty("fs.mod.value");
            assertNotNull("'fs.full.value' must be set.", fsFullValue);
            assertTrue("'fs.full.value' must not be null.", !"".equals(fsFullValue));
            assertTrue("'fs.full.value' must contain ant.bat.", fsFullValue.indexOf("ant.bat")>-1);
            assertNotNull("'fs.mod.value' must be set.", fsModValue);
            assertTrue("'fs.mod.value' must be empty.", "".equals(fsModValue));
        } finally {
            bft.doTarget("modifiedselectortest-scenario-clean");
            bft.deletePropertiesfile();
            bft.tearDown();
        }
    }
    public void testDelayUpdateTaskFinished() {
        doDelayUpdateTest(1);
    }
    public void testDelayUpdateTargetFinished() {
        doDelayUpdateTest(2);
    }
    public void testDelayUpdateBuildFinished() {
        doDelayUpdateTest(3);
    }
    public void doDelayUpdateTest(int kind) {
        String[] kinds = {"task", "target", "build"};
        MockProject project = new MockProject();
        File base  = new File("base");
        File file1 = new File("file1");
        File file2 = new File("file2");
        ModifiedSelector sel = new ModifiedSelector();
        sel.setProject(project);
        sel.setUpdate(true);
        sel.setDelayUpdate(true);
        sel.setClassLoader(this.getClass().getClassLoader());
        sel.addClasspath(testclasses);
        sel.setAlgorithmClass("org.apache.tools.ant.types.selectors.MockAlgorithm");
        sel.setCacheClass("org.apache.tools.ant.types.selectors.MockCache");
        sel.configure();
        MockCache cache = (MockCache)sel.getCache();
        assertFalse("Cache must not be saved before 1st selection.", cache.saved);
        sel.isSelected(base, "file1", file1);
        assertFalse("Cache must not be saved after 1st selection.", cache.saved);
        sel.isSelected(base, "file2", file2);
        assertFalse("Cache must not be saved after 2nd selection.", cache.saved);
        switch (kind) {
            case 1 : project.fireTaskFinished();   break;
            case 2 : project.fireTargetFinished(); break;
            case 3 : project.fireBuildFinished();  break;
        }
        assertTrue("Cache must be saved after " + kinds[kind-1] + "Finished-Event.", cache.saved);
    }
    private String getAlgoName(String classname) {
        ModifiedSelector sel = new ModifiedSelector();
        sel.addClasspath(testclasses);
        sel.setAlgorithmClass(classname);
        sel.validate();
        String s1 = sel.toString();
        int posStart = s1.indexOf("algorithm=") + 10;
        int posEnd   = s1.indexOf(" comparator=");
        String algo  = s1.substring(posStart, posEnd);
        if (algo.startsWith("<")) algo = algo.substring(1);
        if (algo.endsWith(">"))   algo = algo.substring(0, algo.length()-1);
        return algo;
    }
    public void testPropcacheInvalid() {
        Cache cache = new PropertiesfileCache();
        if (cache.isValid())
            fail("PropertyfilesCache does not check its configuration.");
    }
    public void testPropertyfileCache() {
        PropertiesfileCache cache = new PropertiesfileCache();
        File cachefile = new File("cache.properties");
        cache.setCachefile(cachefile);
        doTest(cache);
        assertFalse("Cache file not deleted.", cachefile.exists());
    }
    public void testCreatePropertiesCacheDirect() {
        File cachefile = new File(basedir, "cachefile.properties");
        PropertiesfileCache cache = new PropertiesfileCache();
        cache.setCachefile(cachefile);
        cache.put("key", "value");
        cache.save();
        assertTrue("Cachefile not created.", cachefile.exists());
        cache.delete();
        assertFalse("Cachefile not deleted.", cachefile.exists());
    }
    public void testCreatePropertiesCacheViaModifiedSelector() {
        File cachefile = new File(basedir, "cachefile.properties");
        try {
            makeBed();
            ModifiedSelector s = (ModifiedSelector)getSelector();
            s.setDelayUpdate(false);
            s.addParam("cache.cachefile", cachefile);
            ModifiedSelector.CacheName cacheName = new ModifiedSelector.CacheName();
            cacheName.setValue("propertyfile");
            s.setCache(cacheName);
            s.setUpdate(true);
            selectionString(s);
            assertTrue("Cache file is not created.", cachefile.exists());
        } finally {
            cleanupBed();
            if (cachefile!=null) cachefile.delete();
        }
    }
    public void testCreatePropertiesCacheViaCustomSelector() {
        File cachefile = FILE_UTILS.createTempFile("tmp-cache-", ".properties", null, false, false);
        try {
            makeBed();
            ExtendSelector s = new ExtendSelector();
            s.setClassname("org.apache.tools.ant.types.selectors.modifiedselector.ModifiedSelector");
            s.addParam(createParam("update", "true"));
            s.addParam(createParam("cache.cachefile", cachefile.getAbsolutePath()));
            s.addParam(createParam("cache", "propertyfile"));
            selectionString(s);
            assertTrue("Cache file is not created.", cachefile.exists());
        } finally {
            cleanupBed();
            if (cachefile!=null) cachefile.delete();
        }
    }
    public void _testCustomCache() {
    }
    protected void doTest(Cache cache) {
        assertTrue("Cache not proper configured.", cache.isValid());
        String key1   = "key1";
        String value1 = "value1";
        String key2   = "key2";
        String value2 = "value2";
        Iterator it1 = cache.iterator();
        assertFalse("Cache is not empty", it1.hasNext());
        cache.put(key1, value1);
        cache.put(key2, value2);
        assertEquals("cache returned wrong value", value1, cache.get(key1));
        assertEquals("cache returned wrong value", value2, cache.get(key2));
        Iterator it2 = cache.iterator();
        Object   returned = it2.next();
        boolean ok = (key1.equals(returned) || key2.equals(returned));
        String msg = "Iterator returned unexpected value."
                   + "  key1.equals(returned)="+key1.equals(returned)
                   + "  key2.equals(returned)="+key2.equals(returned)
                   + "  returned="+returned
                   + "  ok="+ok;
        assertTrue(msg, ok);
        cache.delete();
        Iterator it3 = cache.iterator();
        assertFalse("Cache is not empty", it3.hasNext());
    }
    public void testHashvalueAlgorithm() {
        HashvalueAlgorithm algo = new HashvalueAlgorithm();
        doTest(algo);
    }
    public void testDigestAlgorithmMD5() {
        DigestAlgorithm algo = new DigestAlgorithm();
        algo.setAlgorithm("MD5");
        doTest(algo);
    }
    public void testDigestAlgorithmSHA() {
        DigestAlgorithm algo = new DigestAlgorithm();
        algo.setAlgorithm("SHA");
        doTest(algo);
    }
    public void testChecksumAlgorithm() {
        ChecksumAlgorithm algo = new ChecksumAlgorithm();
        doTest(algo);
    }
    public void testChecksumAlgorithmCRC() {
        ChecksumAlgorithm algo = new ChecksumAlgorithm();
        algo.setAlgorithm("CRC");
        doTest(algo);
    }
    public void testChecksumAlgorithmAdler() {
        ChecksumAlgorithm algo = new ChecksumAlgorithm();
        algo.setAlgorithm("Adler");
        doTest(algo);
    }
    protected void doTest(Algorithm algo) {
        assertTrue("Algorithm not proper configured.", algo.isValid());
        try {
            makeBed();
            for (int i=0; i<files.length; i++) {
                File file = files[i];  
                if (file.isFile()) {
                    String hash1 = algo.getValue(file);
                    String hash2 = algo.getValue(file);
                    String hash3 = algo.getValue(file);
                    String hash4 = algo.getValue(file);
                    String hash5 = algo.getValue(new File(file.getAbsolutePath()));
                    assertNotNull("Hashvalue was null for "+file.getAbsolutePath(), hash1);
                    assertNotNull("Hashvalue was null for "+file.getAbsolutePath(), hash2);
                    assertNotNull("Hashvalue was null for "+file.getAbsolutePath(), hash3);
                    assertNotNull("Hashvalue was null for "+file.getAbsolutePath(), hash4);
                    assertNotNull("Hashvalue was null for "+file.getAbsolutePath(), hash5);
                    assertEquals("getHashvalue() returned different value for "+file.getAbsolutePath(), hash1, hash2);
                    assertEquals("getHashvalue() returned different value for "+file.getAbsolutePath(), hash1, hash3);
                    assertEquals("getHashvalue() returned different value for "+file.getAbsolutePath(), hash1, hash4);
                    assertEquals("getHashvalue() returned different value for "+file.getAbsolutePath(), hash1, hash5);
                }
            }
        } finally {
            cleanupBed();
        }
    }
    public void testEqualComparator() {
        EqualComparator comp = new EqualComparator();
        doTest(comp);
    }
    public void testRuleComparator() {
        RuleBasedCollator comp = (RuleBasedCollator)RuleBasedCollator.getInstance();
        doTest(comp);
    }
    public void testEqualComparatorViaSelector() {
        ModifiedSelector s = (ModifiedSelector)getSelector();
        ModifiedSelector.ComparatorName compName = new ModifiedSelector.ComparatorName();
        compName.setValue("equal");
        s.setComparator(compName);
        try {
            performTests(s, "TTTTTTTTTTTT");
        } finally {
            s.getCache().delete();
        }
    }
    public void _testRuleComparatorViaSelector() { 
        ModifiedSelector s = (ModifiedSelector)getSelector();
        ModifiedSelector.ComparatorName compName = new ModifiedSelector.ComparatorName();
        compName.setValue("rule");
        s.setComparator(compName);
        try {
            performTests(s, "TTTTTTTTTTTT");
        } finally {
            s.getCache().delete();
        }
    }
    public void _testCustomComparator() {
    }
    public void testResourceSelectorSimple() {
        BFT bft = new BFT("modifiedselector");
        bft.doTarget("modifiedselectortest-ResourceSimple");
        bft.deleteCachefile();
    }
    public void testResourceSelectorSelresTrue() {
        BFT bft = new BFT("modifiedselector");
        bft.doTarget("modifiedselectortest-ResourceSelresTrue");
        bft.assertLogContaining("does not provide an InputStream");
        bft.deleteCachefile();
    }
    public void testResourceSelectorSelresFalse() {
        BFT bft = new BFT("modifiedselector");
        bft.doTarget("modifiedselectortest-ResourceSelresFalse");
        bft.deleteCachefile();
    }
    public void testResourceSelectorScenarioSimple() {
        BFT bft = new BFT("modifiedselector");
        bft.doTarget("modifiedselectortest-scenario-resourceSimple");
        bft.doTarget("modifiedselectortest-scenario-clean");
        bft.deleteCachefile();
    }
    protected void doTest(Comparator comp) {
        Object o1 = new String("string1");
        Object o2 = new String("string2");
        Object o3 = new String("string2"); 
        assertTrue("Comparator gave wrong value.", comp.compare(o1, o2) != 0);
        assertTrue("Comparator gave wrong value.", comp.compare(o1, o3) != 0);
        assertTrue("Comparator gave wrong value.", comp.compare(o2, o3) == 0);
    }
    public void testSeldirs() {
        ModifiedSelector s = (ModifiedSelector)getSelector();
        try {
            makeBed();
            StringBuffer sbTrue  = new StringBuffer();
            StringBuffer sbFalse = new StringBuffer();
            for (int i=0; i<filenames.length; i++) {
                if (files[i].isDirectory()) {
                    sbTrue.append("T");
                    sbFalse.append("F");
                } else {
                    sbTrue.append("T");
                    sbFalse.append("T");
                }
            }
            s.setSeldirs(true);
            performTests(s, sbTrue.toString());
            s.getCache().delete();
            s.setSeldirs(false);
            performTests(s, sbFalse.toString());
            s.getCache().delete();
        } finally {
            cleanupBed();
            if (s!=null) s.getCache().delete();
        }
    }
    public void testScenario1() {
        BFT bft = null;
        ModifiedSelector s = null;
        try {
            makeBed();
            String results = null;
            s = (ModifiedSelector)getSelector();
            performTests(s, "TTTTTTTTTTTT");
            performTests(s, "TFFFFFFFFFFT");
            String f2name = "tar/bz2/asf-logo-huge.tar.bz2";
            String f3name = "asf-logo.gif.md5";
            String f4name = "copy.filterset.filtered";
            bft = new BFT();
            bft.writeProperties("f2name="+f2name);
            bft.writeProperties("f3name="+f3name);
            bft.writeProperties("f4name="+f4name);
            bft.doTarget("modifiedselectortest-makeDirty");
            results = selectionString(s);
            StringBuffer expected = new StringBuffer();
            for (int i=0; i<filenames.length; i++) {
                String ch = "F";
                if (files[i].isDirectory()) ch = "T";
                if (filenames[i].equalsIgnoreCase(f3name)) ch = "T";
                if (filenames[i].equalsIgnoreCase(f4name)) ch = "T";
                expected.append(ch);
            }
            assertEquals(
                "Wrong files selected. Differing files: "       
                + resolve(diff(expected.toString(), results)),  
                expected.toString(),                            
                results                                         
            );
        } finally {
            cleanupBed();
            if (s!=null) s.getCache().delete();
            if (bft!=null) bft.deletePropertiesfile();
        }
    }
    public void _testScenario2() { 
        ExtendSelector s = new ExtendSelector();
        BFT bft = new BFT();
        String cachefile = System.getProperty("java.io.tmpdir")+"/mycache.txt";
        try {
            makeBed();
            s.setClassname("org.apache.tools.ant.types.selectors.modifiedselector.ModifiedSelector");
            s.addParam(createParam("cache.cachefile", cachefile));
            s.addParam(createParam("cache","propertyfile"));
            s.addParam(createParam("update","true"));
            s.addParam(createParam("comparator","rule"));
            s.addParam(createParam("algorithm.name","sha"));
            s.addParam(createParam("algorithm","digest"));
            performTests(s, "TTTTTTTTTTTT");
            performTests(s, "TFFFFFFFFFFT");
            String f2name = "tar/bz2/asf-logo-huge.tar.bz2";
            String f3name = "asf-logo.gif.md5";
            String f4name = "copy.filterset.filtered";
            bft.writeProperties("f2name="+f2name);
            bft.writeProperties("f3name="+f3name);
            bft.writeProperties("f4name="+f4name);
            bft.doTarget("modifiedselectortest-makeDirty");
            String results = selectionString(s);
            StringBuffer expected = new StringBuffer();
            for (int i=0; i<filenames.length; i++) {
                String ch = "F";
                if (files[i].isDirectory()) ch = "T";
                if (filenames[i].equalsIgnoreCase(f3name)) ch = "T";
                if (filenames[i].equalsIgnoreCase(f4name)) ch = "T";
                expected.append(ch);
            }
            assertEquals(
                "Wrong files selected. Differing files: "       
                + resolve(diff(expected.toString(), results)),  
                expected.toString(),                            
                results                                         
            );
        } finally {
            cleanupBed();
            (new java.io.File(cachefile)).delete();
            if (bft!=null) bft.deletePropertiesfile();
        }
    }
    public void testScenarioCoreSelectorDefaults() {
        doScenarioTest("modifiedselectortest-scenario-coreselector-defaults", "cache.properties");
    }
    public void testScenarioCoreSelectorSettings() {
        doScenarioTest("modifiedselectortest-scenario-coreselector-settings", "core.cache.properties");
    }
    public void testScenarioCustomSelectorSettings() {
        doScenarioTest("modifiedselectortest-scenario-customselector-settings", "core.cache.properties");
    }
    public void doScenarioTest(String target, String cachefilename) {
        BFT bft = new BFT();
        bft.setUp();
        File cachefile = new File(basedir, cachefilename);
        try {
            bft.doTarget("modifiedselectortest-scenario-clean");
            bft.doTarget(target);
            File to1 = new File(basedir, "selectortest/to-1");
            File to2 = new File(basedir, "selectortest/to-2");
            File to3 = new File(basedir, "selectortest/to-3");
            assertTrue("Cache file not created.", cachefile.exists());
            assertTrue("Not enough files copied on first time.", to1.list().length>5);
            assertTrue("Too much files copied on second time.", to2.list().length==0);
            assertTrue("Too much files copied on third time.", to3.list().length==2);
        } finally {
            bft.doTarget("modifiedselectortest-scenario-clean");
            bft.deletePropertiesfile();
            bft.tearDown();
            cachefile.delete();
        }
    }
    private Parameter createParam(String name, String value) {
        Parameter p = new Parameter();
        p.setName(name);
        p.setValue(value);
        return p;
    }
    private class BFT extends org.apache.tools.ant.BuildFileTest {
        String buildfile = "src/etc/testcases/types/selectors.xml";
        BFT() { super("nothing"); }
        BFT(String name) {
            super(name);
        }
        String propfile = "ModifiedSelectorTest.properties";
        boolean isConfigured = false;
        public void setUp() {
            configureProject(buildfile);
            isConfigured = true;
        }
        public void tearDown() {
            try {
                super.tearDown();
            } catch (Exception e) {
            }
        }
        public void doTarget(String target) {
            if (!isConfigured) setUp();
            executeTarget(target);
        }
        public String getProperty(String property) {
            return project.getProperty(property);
        }
        public void writeProperties(String line) {
            if (!isConfigured) setUp();
            File dir = getProject().getBaseDir();
            File file = new File(dir, propfile);
            try {
                java.io.FileWriter out =
                    new java.io.FileWriter(file.getAbsolutePath(), true);
                out.write(line);
                out.write(System.getProperty("line.separator"));
                out.flush();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        public void deletePropertiesfile() {
            if (!isConfigured) setUp();
            new File(getProject().getBaseDir(), propfile).delete();
        }
        public void deleteCachefile() {
            File basedir = new File(buildfile).getParentFile();
            File cacheFile = new File(basedir, "cache.properties");
            cacheFile.delete();
        }
        public String getBuildfile() {
            return buildfile;
        }
        public void setBuildfile(String buildfile) {
            this.buildfile = buildfile;
        }
    }
    private class MockProject extends Project {
        private Task   task;
        private Target target;
        public MockProject() {
            task = new Task(){
                public void execute() {
                }
            };
            task.setTaskName("testTask");
            target = new Target();
            target.setName("testTarget");
            target.setProject(this);
            target.addTask(task);
            task.setOwningTarget(target);
        }
        public void fireBuildFinished() {
            super.fireBuildFinished(null);
        }
        public void fireSubBuildFinished() {
            super.fireSubBuildFinished(null);
        }
        public void fireTargetStarted() {
            super.fireTargetStarted(target);
        }
        public void fireTargetFinished() {
            super.fireTargetFinished(target, null);
        }
        public void fireTaskStarted() {
            super.fireTaskStarted(task);
        }
        public void fireTaskFinished() {
            super.fireTaskFinished(task, null);
        }
    }
}
