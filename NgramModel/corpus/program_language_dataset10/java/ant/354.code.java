package org.apache.tools.ant.taskdefs.optional;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Collections;
import java.util.Iterator;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.types.PropertySet;
import org.apache.tools.ant.util.CollectionUtils;
import org.apache.tools.ant.util.DOMElementWriter;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.JavaEnvUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
public class EchoProperties extends Task {
    private static final String PROPERTIES = "properties";
    private static final String PROPERTY = "property";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_VALUE = "value";
    private File inFile = null;
    private File destfile = null;
    private boolean failonerror = true;
    private Vector propertySets = new Vector();
    private String format = "text";
    private String prefix;
    private String regex;
    public void setSrcfile(File file) {
        inFile = file;
    }
    public void setDestfile(File destfile) {
        this.destfile = destfile;
    }
    public void setFailOnError(boolean failonerror) {
        this.failonerror = failonerror;
    }
    public void setPrefix(String prefix) {
        if (prefix != null && prefix.length() != 0) {
            this.prefix = prefix;
            PropertySet ps = new PropertySet();
            ps.setProject(getProject());
            ps.appendPrefix(prefix);
            addPropertyset(ps);
        }
    }
    public void setRegex(String regex) {
        if (regex != null && regex.length() != 0) {
            this.regex = regex;
            PropertySet ps = new PropertySet();
            ps.setProject(getProject());
            ps.appendRegex(regex);
            addPropertyset(ps);
        }
    }
    public void addPropertyset(PropertySet ps) {
        propertySets.addElement(ps);
    }
    public void setFormat(FormatAttribute ea) {
        format = ea.getValue();
    }
    public static class FormatAttribute extends EnumeratedAttribute {
        private String [] formats = new String[]{"xml", "text"};
        public String[] getValues() {
            return formats;
        }
    }
    public void execute() throws BuildException {
        if (prefix != null && regex != null) {
            throw new BuildException("Please specify either prefix"
                    + " or regex, but not both", getLocation());
        }
        Hashtable allProps = new Hashtable();
        if (inFile == null && propertySets.size() == 0) {
            allProps.putAll(getProject().getProperties());
        } else if (inFile != null) {
            if (inFile.exists() && inFile.isDirectory()) {
                String message = "srcfile is a directory!";
                if (failonerror) {
                    throw new BuildException(message, getLocation());
                } else {
                    log(message, Project.MSG_ERR);
                }
                return;
            }
            if (inFile.exists() && !inFile.canRead()) {
                String message = "Can not read from the specified srcfile!";
                if (failonerror) {
                    throw new BuildException(message, getLocation());
                } else {
                    log(message, Project.MSG_ERR);
                }
                return;
            }
            FileInputStream in = null;
            try {
                in = new FileInputStream(inFile);
                Properties props = new Properties();
                props.load(in);
                allProps.putAll(props);
            } catch (FileNotFoundException fnfe) {
                String message =
                    "Could not find file " + inFile.getAbsolutePath();
                if (failonerror) {
                    throw new BuildException(message, fnfe, getLocation());
                } else {
                    log(message, Project.MSG_WARN);
                }
                return;
            } catch (IOException ioe) {
                String message =
                    "Could not read file " + inFile.getAbsolutePath();
                if (failonerror) {
                    throw new BuildException(message, ioe, getLocation());
                } else {
                    log(message, Project.MSG_WARN);
                }
                return;
            } finally {
                FileUtils.close(in);
            }
        }
        Enumeration e = propertySets.elements();
        while (e.hasMoreElements()) {
            PropertySet ps = (PropertySet) e.nextElement();
            allProps.putAll(ps.getProperties());
        }
        OutputStream os = null;
        try {
            if (destfile == null) {
                os = new ByteArrayOutputStream();
                saveProperties(allProps, os);
                log(os.toString(), Project.MSG_INFO);
            } else {
                if (destfile.exists() && destfile.isDirectory()) {
                    String message = "destfile is a directory!";
                    if (failonerror) {
                        throw new BuildException(message, getLocation());
                    } else {
                        log(message, Project.MSG_ERR);
                    }
                    return;
                }
                if (destfile.exists() && !destfile.canWrite()) {
                    String message =
                        "Can not write to the specified destfile!";
                    if (failonerror) {
                        throw new BuildException(message, getLocation());
                    } else {
                        log(message, Project.MSG_ERR);
                    }
                    return;
                }
                os = new FileOutputStream(this.destfile);
                saveProperties(allProps, os);
            }
        } catch (IOException ioe) {
            if (failonerror) {
                throw new BuildException(ioe, getLocation());
            } else {
                log(ioe.getMessage(), Project.MSG_INFO);
            }
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                }
            }
        }
    }
    protected void saveProperties(Hashtable allProps, OutputStream os)
        throws IOException, BuildException {
        final List keyList = new ArrayList(allProps.keySet());
        Collections.sort(keyList);
        Properties props = new Properties() {
            private static final long serialVersionUID = 5090936442309201654L;
            public Enumeration keys() {
                return CollectionUtils.asEnumeration(keyList.iterator());
            }
            public Set entrySet() {
                Set result = super.entrySet();
                if (JavaEnvUtils.isKaffe()) {
                    TreeSet t = new TreeSet(new Comparator() {
                        public int compare(Object o1, Object o2) {
                            String key1 = (String) ((Map.Entry) o1).getKey();
                            String key2 = (String) ((Map.Entry) o2).getKey();
                            return key1.compareTo(key2);
                        }
                    });
                    t.addAll(result);
                    result = t;
                }
                return result;
            }
        };
        for (int i = 0; i < keyList.size(); i++) {
            String name = keyList.get(i).toString();
            String value = allProps.get(name).toString();
            props.setProperty(name, value);
        }
        if ("text".equals(format)) {
            jdkSaveProperties(props, os, "Ant properties");
        } else if ("xml".equals(format)) {
            xmlSaveProperties(props, os);
        }
    }
    private static final class Tuple implements Comparable {
        private String key;
        private String value;
        private Tuple(String key, String value) {
            this.key = key;
            this.value = value;
        }
        public int compareTo(Object o) {
            Tuple that = (Tuple) o;
            return key.compareTo(that.key);
        }
    }
    private List sortProperties(Properties props) {
        List sorted = new ArrayList(props.size());
        Enumeration e = props.propertyNames();
        while (e.hasMoreElements()) {
            String name = (String) e.nextElement();
            sorted.add(new Tuple(name, props.getProperty(name)));
        }
        Collections.sort(sorted);
        return sorted;
    }
    protected void xmlSaveProperties(Properties props,
                                     OutputStream os) throws IOException {
        Document doc = getDocumentBuilder().newDocument();
        Element rootElement = doc.createElement(PROPERTIES);
        List sorted = sortProperties(props);
        Iterator iten = sorted.iterator();
        while (iten.hasNext()) {
            Tuple tuple = (Tuple) iten.next();
            Element propElement = doc.createElement(PROPERTY);
            propElement.setAttribute(ATTR_NAME, tuple.key);
            propElement.setAttribute(ATTR_VALUE, tuple.value);
            rootElement.appendChild(propElement);
        }
        Writer wri = null;
        try {
            wri = new OutputStreamWriter(os, "UTF8");
            wri.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            (new DOMElementWriter()).write(rootElement, wri, 0, "\t");
            wri.flush();
        } catch (IOException ioe) {
            throw new BuildException("Unable to write XML file", ioe);
        } finally {
            FileUtils.close(wri);
        }
    }
    protected void jdkSaveProperties(Properties props, OutputStream os,
                                     String header) throws IOException {
       try {
           props.store(os, header);
       } catch (IOException ioe) {
           throw new BuildException(ioe, getLocation());
       } finally {
           if (os != null) {
               try {
                   os.close();
               } catch (IOException ioex) {
                   log("Failed to close output stream");
               }
           }
       }
    }
    private static DocumentBuilder getDocumentBuilder() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }
}