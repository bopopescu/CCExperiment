package org.apache.batik.script.jpython;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Locale;
import org.apache.batik.script.InterpreterException;
import org.python.util.PythonInterpreter;
public class JPythonInterpreter implements org.apache.batik.script.Interpreter {
    private PythonInterpreter interpreter = null;
    public JPythonInterpreter() {
        interpreter = new PythonInterpreter();
    }
    public String[] getMimeTypes() {
        return JPythonInterpreterFactory.JPYTHON_MIMETYPES;
    }
    public Object evaluate(Reader scriptreader)
        throws IOException {
        return evaluate(scriptreader, "");
    }
    public Object evaluate(Reader scriptreader, String description)
        throws IOException {
        StringBuffer sbuffer = new StringBuffer();
        char[] buffer = new char[1024];
        int val = 0;
        while ((val = scriptreader.read(buffer)) != -1) {
            sbuffer.append(buffer,0, val);
        }
        String str = sbuffer.toString();
        return evaluate(str);
    }
    public Object evaluate(String script) {
        try {
            interpreter.exec(script);
        } catch (org.python.core.PyException e) {
            throw new InterpreterException(e, e.getMessage(), -1, -1);
        } catch (RuntimeException re) {
            throw new InterpreterException(re, re.getMessage(), -1, -1);
        }
        return null;
    }
    public void dispose() {
    }
    public void bindObject(String name, Object object) {
        interpreter.set(name, object);
    }
    public void setOut(Writer out) {
        interpreter.setOut(out);
    }
    public Locale getLocale() {
        return null;
    }
    public void setLocale(Locale locale) {
    }
    public String formatMessage(String key, Object[] args) {
        return null;
    }
}
