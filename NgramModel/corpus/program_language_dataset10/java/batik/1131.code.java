package org.apache.batik.script.rhino;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import org.apache.batik.bridge.InterruptedBridgeException;
import org.apache.batik.script.Interpreter;
import org.apache.batik.script.InterpreterException;
import org.apache.batik.script.Window;
import org.apache.batik.script.ImportInfo;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextAction;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ClassCache;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.SecurityController;
import org.mozilla.javascript.WrapFactory;
import org.mozilla.javascript.WrappedException;
import org.w3c.dom.events.EventTarget;
public class RhinoInterpreter implements Interpreter {
    private static final int MAX_CACHED_SCRIPTS = 32;
    public static final String SOURCE_NAME_SVG = "<SVG>";
    public static final String BIND_NAME_WINDOW = "window";
    protected static List contexts = new LinkedList();
    protected Window window;
    protected ScriptableObject globalObject = null;
    protected LinkedList compiledScripts = new LinkedList();
    protected WrapFactory wrapFactory = new BatikWrapFactory(this);
    protected ClassShutter classShutter = new RhinoClassShutter();
    protected RhinoClassLoader rhinoClassLoader;
    protected SecurityController securityController
        = new BatikSecurityController();
    protected ContextFactory contextFactory = new Factory();
    protected Context defaultContext;
    public RhinoInterpreter(URL documentURL) {
        init(documentURL, null);
    }
    public RhinoInterpreter(URL documentURL,
                            ImportInfo imports) {
        init(documentURL, imports);
    }
    protected void init(URL documentURL,
                        final ImportInfo imports)
    {
        try {
            rhinoClassLoader = new RhinoClassLoader
                (documentURL, getClass().getClassLoader());
        } catch (SecurityException se) {
            rhinoClassLoader = null;
        }
        ContextAction initAction = new ContextAction() {
            public Object run(Context cx) {
                Scriptable scriptable = cx.initStandardObjects(null, false);
                defineGlobalWrapperClass(scriptable);
                globalObject = createGlobalObject(cx);
                ClassCache cache = ClassCache.get(globalObject);
                cache.setCachingEnabled(rhinoClassLoader != null);
                ImportInfo ii = imports;
                if (ii == null) ii = ImportInfo.getImports();
                StringBuffer sb = new StringBuffer();
                Iterator iter;
                iter = ii.getPackages();
                while (iter.hasNext()) {
                    String pkg = (String)iter.next();
                    sb.append("importPackage(Packages.");
                    sb.append(pkg);
                    sb.append(");");
                }
                iter = ii.getClasses();
                while (iter.hasNext()) {
                    String cls = (String)iter.next();
                    sb.append("importClass(Packages.");
                    sb.append(cls);
                    sb.append(");");
                }
                cx.evaluateString(globalObject, sb.toString(), null, 0,
                                  rhinoClassLoader);
                return null;
            }
        };
        contextFactory.call(initAction);
    }
    public String[] getMimeTypes() {
        return RhinoInterpreterFactory.RHINO_MIMETYPES;
    }
    public Window getWindow() {
        return window;
    }
    public ContextFactory getContextFactory() {
        return contextFactory;
    }
    protected void defineGlobalWrapperClass(Scriptable global) {
        try {
            ScriptableObject.defineClass(global, WindowWrapper.class);
        } catch (Exception ex) {
        }
    }
    protected ScriptableObject createGlobalObject(Context ctx) {
        return new WindowWrapper(ctx);
    }
    public AccessControlContext getAccessControlContext() {
        if (rhinoClassLoader == null) return null;
        return rhinoClassLoader.getAccessControlContext();
    }
    protected ScriptableObject getGlobalObject() {
        return globalObject;
    }
    public Object evaluate(Reader scriptreader) throws IOException {
        return evaluate(scriptreader, SOURCE_NAME_SVG);
    }
    public Object evaluate(final Reader scriptReader, final String description)
        throws IOException {
        ContextAction evaluateAction = new ContextAction() {
            public Object run(Context cx) {
                try {
                    return cx.evaluateReader(globalObject,
                                             scriptReader,
                                             description,
                                             1, rhinoClassLoader);
                } catch (IOException ioe) {
                    throw new WrappedException(ioe);
                }
            }
        };
        try {
            return contextFactory.call(evaluateAction);
        } catch (JavaScriptException e) {
            Object value = e.getValue();
            Exception ex = value instanceof Exception ? (Exception) value : e;
            throw new InterpreterException(ex, ex.getMessage(), -1, -1);
        } catch (WrappedException we) {
            Throwable w = we.getWrappedException();
            if (w instanceof Exception) {
                throw new InterpreterException
                    ((Exception) w, w.getMessage(), -1, -1);
            } else {
                throw new InterpreterException(w.getMessage(), -1, -1);
            }
        } catch (InterruptedBridgeException ibe) {
            throw ibe;
        } catch (RuntimeException re) {
            throw new InterpreterException(re, re.getMessage(), -1, -1);
        }
    }
    public Object evaluate(final String scriptStr) {
        ContextAction evalAction = new ContextAction() {
            public Object run(final Context cx) {
                Script script = null;
                Entry entry = null;
                Iterator it = compiledScripts.iterator();
                while (it.hasNext()) {
                    if ((entry = (Entry) it.next()).str.equals(scriptStr)) {
                        script = entry.script;
                        it.remove();
                        break;
                    }
                }
                if (script == null) {
                    PrivilegedAction compile = new PrivilegedAction() {
                        public Object run() {
                            try {
                                return cx.compileReader
                                    (new StringReader(scriptStr),
                                     SOURCE_NAME_SVG, 1, rhinoClassLoader);
                            } catch (IOException ioEx ) {
                                throw new Error( ioEx.getMessage() );
                            }
                        }
                    };
                    script = (Script)AccessController.doPrivileged(compile);
                    if (compiledScripts.size() + 1 > MAX_CACHED_SCRIPTS) {
                        compiledScripts.removeFirst();
                    }
                    compiledScripts.addLast(new Entry(scriptStr, script));
                } else {
                    compiledScripts.addLast(entry);
                }
                return script.exec(cx, globalObject);
            }
        };
        try {
            return contextFactory.call(evalAction);
        } catch (InterpreterException ie) {
            throw ie;
        } catch (JavaScriptException e) {
            Object value = e.getValue();
            Exception ex = value instanceof Exception ? (Exception) value : e;
            throw new InterpreterException(ex, ex.getMessage(), -1, -1);
        } catch (WrappedException we) {
            Throwable w = we.getWrappedException();
            if (w instanceof Exception) {
                throw new InterpreterException
                    ((Exception) w, w.getMessage(), -1, -1);
            } else {
                throw new InterpreterException(w.getMessage(), -1, -1);
            }
        } catch (RuntimeException re) {
            throw new InterpreterException(re, re.getMessage(), -1, -1);
        }
    }
    public void dispose() {
        if (rhinoClassLoader != null) {
            ClassCache cache = ClassCache.get(globalObject);
            cache.setCachingEnabled(false);
        }
    }
    public void bindObject(final String name, final Object object) {
        contextFactory.call(new ContextAction() {
            public Object run(Context cx) {
                Object o = object;
                if (name.equals(BIND_NAME_WINDOW) && object instanceof Window) {
                    ((WindowWrapper) globalObject).window = (Window) object;
                    window = (Window) object;
                    o = globalObject;
                }
                Scriptable jsObject;
                jsObject = Context.toObject(o, globalObject);
                globalObject.put(name, globalObject, jsObject);
                return null;
            }
        });
    }
    void callHandler(final Function handler, final Object arg) {
        contextFactory.call(new ContextAction() {
            public Object run(Context cx) {
                Object a = Context.toObject(arg, globalObject);
                Object[] args = { a };
                handler.call(cx, globalObject, globalObject, args);
                return null;
            }
        });
    }
    void callMethod(final ScriptableObject obj,
                    final String methodName,
                    final ArgumentsBuilder ab) {
        contextFactory.call(new ContextAction() {
            public Object run(Context cx) {
                ScriptableObject.callMethod
                    (obj, methodName, ab.buildArguments());
                return null;
            }
        });
    }
    void callHandler(final Function handler, final Object[] args) {
        contextFactory.call(new ContextAction() {
            public Object run(Context cx) {
                handler.call(cx, globalObject, globalObject, args);
                return null;
            }
        });
    }
    void callHandler(final Function handler, final ArgumentsBuilder ab) {
        contextFactory.call(new ContextAction() {
            public Object run(Context cx) {
                Object[] args = ab.buildArguments();
                handler.call(cx, handler.getParentScope(), globalObject, args);
                return null;
            }
        });
    }
    Object call(ContextAction action) {
        return contextFactory.call(action);
    }
    public interface ArgumentsBuilder {
        Object[] buildArguments();
    }
    Scriptable buildEventTargetWrapper(EventTarget obj) {
        return new EventTargetWrapper(globalObject, obj, this);
    }
    public void setOut(Writer out) {
    }
    public Locale getLocale() {
        return null;
    }
    public void setLocale(Locale locale) {
    }
    public String formatMessage(String key, Object[] args) {
        return null;
    }
    protected static class Entry {
        public String str;
        public Script script;
        public Entry(String str, Script script) {
            this.str = str;
            this.script = script;
        }
    }
    protected class Factory extends ContextFactory {
        protected Context makeContext() {
            Context cx = super.makeContext();
            cx.setWrapFactory(wrapFactory);
            cx.setSecurityController(securityController);
            cx.setClassShutter(classShutter);
            if (rhinoClassLoader == null) {
                cx.setOptimizationLevel(-1);
            }
            return cx;
        }
    }
}
