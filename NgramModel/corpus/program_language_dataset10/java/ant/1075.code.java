package org.apache.tools.ant.taskdefs.optional.junit;
import junit.framework.TestCase;
public class Printer extends TestCase {
    public Printer(String name) {
        super(name);
        System.err.println("constructor print to System.err");
        System.out.println("constructor print to System.out");
    }
    static {
        System.err.println("static print to System.err");
        System.out.println("static print to System.out");
    }
    public void testNoCrash() {
        System.err.println("method print to System.err");
        System.out.println("method print to System.out");
    }
}
