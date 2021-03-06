package org.apache.log4j.spi;
import junit.framework.TestCase;
public class LocationInfoTest extends TestCase {
    public void testFourParamConstructor() {
        final String className = LocationInfoTest.class.getName();
        final String methodName = "testFourParamConstructor";
        final String fileName = "LocationInfoTest.java";
        final String lineNumber = "41";
        LocationInfo li = new LocationInfo(fileName,
                className, methodName, lineNumber);
        assertEquals(className, li.getClassName());
        assertEquals(methodName, li.getMethodName());
        assertEquals(fileName, li.getFileName());
        assertEquals(lineNumber, li.getLineNumber());
        assertEquals(className + "."  + methodName
                + "(" + fileName + ":" + lineNumber + ")",
                li.fullInfo);
    }
    private static class NameSubstring {
        public static LocationInfo getInfo() {
            return new LocationInfo(new Throwable(), NameSubstring.class.getName());
        }
    }
    private static class NameSubstringCaller {
        public static LocationInfo getInfo() {
            return NameSubstring.getInfo();
        }
    }
     public void testLocationInfo() {
         LocationInfo li = NameSubstringCaller.getInfo();
         assertEquals(NameSubstringCaller.class.getName(), li.getClassName());
         assertEquals("getInfo", li.getMethodName());
     }
}
