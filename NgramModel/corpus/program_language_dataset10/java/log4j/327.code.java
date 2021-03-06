package org.apache.log4j.spi;
import junit.framework.TestCase;
import java.io.PrintWriter;
public class ThrowableInformationTest extends TestCase {
    public ThrowableInformationTest(final String name) {
        super(name);
    }
    private static final class OverriddenThrowable extends Throwable {
        private static final long serialVersionUID = 1L;
        public OverriddenThrowable() {
        }
        public void printStackTrace(final PrintWriter s) {
            s.print((Object) "print(Object)");
            s.print("print(char[])".toCharArray());
            s.print("print(String)");
            s.println((Object) "println(Object)");
            s.println("println(char[])".toCharArray());
            s.println("println(String)");
            s.write("write(char[])".toCharArray());
            s.write("write(char[], int, int)".toCharArray(), 2, 8);
            s.write("write(String, int, int)", 2, 8);
        }
    }
    public void testOverriddenBehavior() {
        ThrowableInformation ti = new ThrowableInformation(new OverriddenThrowable());
        String[] rep = ti.getThrowableStrRep();
        assertEquals(4, rep.length);
        assertEquals("print(Object)print(char[])print(String)println(Object)", rep[0]);
        assertEquals("println(char[])", rep[1]);
        assertEquals("println(String)", rep[2]);
        assertEquals("write(char[])ite(charite(Stri", rep[3]);
    }
    private static final class NotOverriddenThrowable extends Throwable {
        private static final long serialVersionUID = 1L;
        public NotOverriddenThrowable() {
        }
        public void printStackTrace(final PrintWriter s) {
            s.print(true);
            s.print('a');
            s.print(1);
            s.print(2L);
            s.print(Float.MAX_VALUE);
            s.print(Double.MIN_VALUE);
            s.println(true);
            s.println('a');
            s.println(1);
            s.println(2L);
            s.println(Float.MAX_VALUE);
            s.println(Double.MIN_VALUE);
            s.write('C');
        }
    }
    public void testNotOverriddenBehavior() {
        ThrowableInformation ti = new ThrowableInformation(new NotOverriddenThrowable());
        String[] rep = ti.getThrowableStrRep();
        assertEquals(7, rep.length);
        StringBuffer buf = new StringBuffer(String.valueOf(true));
        buf.append('a');
        buf.append(String.valueOf(1));
        buf.append(String.valueOf(2L));
        buf.append(String.valueOf(Float.MAX_VALUE));
        buf.append(String.valueOf(Double.MIN_VALUE));
        buf.append(String.valueOf(true));
        assertEquals(buf.toString(), rep[0]);
        assertEquals("a", rep[1]);
        assertEquals(String.valueOf(1), rep[2]);
        assertEquals(String.valueOf(2L), rep[3]);
        assertEquals(String.valueOf(Float.MAX_VALUE), rep[4]);
        assertEquals(String.valueOf(Double.MIN_VALUE), rep[5]);
        assertEquals("C", rep[6]);
    }
    private static final class NullThrowable extends Throwable {
        private static final long serialVersionUID = 1L;
        public NullThrowable() {
        }
        public void printStackTrace(final PrintWriter s) {
            s.print((Object) null);
            s.print((String) null);
            s.println((Object) null);
            s.println((String) null);
        }
    }
    public void testNull() {
        ThrowableInformation ti = new ThrowableInformation(new NullThrowable());
        String[] rep = ti.getThrowableStrRep();
        assertEquals(2, rep.length);
        String nullStr = String.valueOf((Object) null);
        assertEquals(nullStr + nullStr + nullStr, rep[0]);
        assertEquals(nullStr, rep[1]);
    }
    private static final class EmptyThrowable extends Throwable {
        private static final long serialVersionUID = 1L;
        public EmptyThrowable() {
        }
        public void printStackTrace(final PrintWriter s) {
        }
    }
    public void testEmpty() {
        ThrowableInformation ti = new ThrowableInformation(new EmptyThrowable());
        String[] rep = ti.getThrowableStrRep();
        assertEquals(0, rep.length);
    }
    private static final class StringThrowable extends Throwable {
        private static final long serialVersionUID = 1L;
        private final String stackTrace;
        public StringThrowable(final String trace) {
            stackTrace = trace;
        }
        public void printStackTrace(final PrintWriter s) {
            s.print(stackTrace);
        }
    }
    public void testLineFeed() {
        ThrowableInformation ti = new ThrowableInformation(new StringThrowable("\n"));
        String[] rep = ti.getThrowableStrRep();
        assertEquals(1, rep.length);
        assertEquals("", rep[0]);
    }
    public void testCarriageReturn() {
        ThrowableInformation ti = new ThrowableInformation(new StringThrowable("\r"));
        String[] rep = ti.getThrowableStrRep();
        assertEquals(1, rep.length);
        assertEquals("", rep[0]);
    }
    public void testParsing() {
        ThrowableInformation ti = new ThrowableInformation(
                new StringThrowable("Line1\rLine2\nLine3\r\nLine4\n\rLine6"));
        String[] rep = ti.getThrowableStrRep();
        assertEquals(6, rep.length);
        assertEquals("Line1", rep[0]);
        assertEquals("Line2", rep[1]);
        assertEquals("Line3", rep[2]);
        assertEquals("Line4", rep[3]);
        assertEquals("", rep[4]);
        assertEquals("Line6", rep[5]);
    }
    public void testLineFeedBlank() {
        ThrowableInformation ti = new ThrowableInformation(new StringThrowable("\n "));
        String[] rep = ti.getThrowableStrRep();
        assertEquals(2, rep.length);
        assertEquals("", rep[0]);
        assertEquals(" ", rep[1]);
    }
    public void testGetThrowable() {
        Throwable t = new StringThrowable("Hello, World");
        ThrowableInformation ti = new ThrowableInformation(t);
        assertSame(t, ti.getThrowable());
    }
    public void testIsolation() {
        ThrowableInformation ti = new ThrowableInformation(
                new StringThrowable("Hello, World"));
        String[] rep = ti.getThrowableStrRep();
        assertEquals("Hello, World", rep[0]);
        rep[0] = "Bonjour, Monde";
        String[] rep2 = ti.getThrowableStrRep();
        assertEquals("Hello, World", rep2[0]);
    }
    private static final class NastyThrowable extends Throwable {
        private static final long serialVersionUID = 1L;
        public NastyThrowable() {
        }
        public void printStackTrace(final PrintWriter s) {
            s.print("NastyException");
            throw new RuntimeException("Intentional exception");
        }
    }
    public void testNastyException() {
        ThrowableInformation ti = new ThrowableInformation(
                new NastyThrowable());
        String[] rep = ti.getThrowableStrRep();
        assertEquals("NastyException", rep[0]);
    }
}
