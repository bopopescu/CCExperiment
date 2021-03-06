package org.apache.batik.util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
public class Service {
    static HashMap providerMap = new HashMap();
    public static synchronized Iterator providers(Class cls) {
        String serviceFile = "META-INF/services/"+cls.getName();
        List l = (List)providerMap.get(serviceFile);
        if (l != null)
            return l.iterator();
        l = new ArrayList();
        providerMap.put(serviceFile, l);
        ClassLoader cl = null;
        try {
            cl = cls.getClassLoader();
        } catch (SecurityException se) {
        }
        if (cl == null) cl = Service.class.getClassLoader();
        if (cl == null) return l.iterator();
        Enumeration e;
        try {
            e = cl.getResources(serviceFile);
        } catch (IOException ioe) {
            return l.iterator();
        }
        while (e.hasMoreElements()) {
            InputStream    is = null;
            Reader         r  = null;
            BufferedReader br = null;
            try {
                URL u = (URL)e.nextElement();
                is = u.openStream();
                r  = new InputStreamReader(is, "UTF-8");
                br = new BufferedReader(r);
                String line = br.readLine();
                while (line != null) {
                    try {
                        int idx = line.indexOf('#');
                        if (idx != -1)
                            line = line.substring(0, idx);
                        line = line.trim();
                        if (line.length() == 0) {
                            line = br.readLine();
                            continue;
                        }
                        Object obj = cl.loadClass(line).newInstance();
                        l.add(obj);
                    } catch (Exception ex) {
                    }
                    line = br.readLine();
                }
            } catch (Exception ex) {
            } catch (LinkageError le) {
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException ignored) {
                    }
                    is = null;
                }
                if (r != null) {
                    try{
                        r.close();
                    } catch (IOException ignored) {
                    }
                    r = null;
                }
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException ignored) {
                    }
                    br = null;
                }
            }
        }
        return l.iterator();
    }
}
