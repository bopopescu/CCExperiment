package org.apache.log4j;
import junit.framework.TestCase;
import org.apache.log4j.spi.ThrowableRenderer;
public class EnhancedThrowableRendererTest extends TestCase {
    public EnhancedThrowableRendererTest(final String name) {
        super(name);
    }
    public void testEnhancedRender() {
        ThrowableRenderer r = new EnhancedThrowableRenderer();
        Exception ex = new Exception();
        String[] strRep = r.doRender(ex);
        assertNotNull(strRep);
        assertTrue(strRep.length > 0);
        for(int i = 0; i < strRep.length; i++) {
            assertNotNull(strRep[i]);
        }
    }
}
