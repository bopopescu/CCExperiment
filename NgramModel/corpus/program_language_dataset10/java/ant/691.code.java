package org.apache.tools.ant.types.resources;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.ResourceCollection;
public class Union extends BaseResourceCollectionContainer {
    public static Union getInstance(ResourceCollection rc) {
        return rc instanceof Union ? (Union) rc : new Union(rc);
    }
    public Union() {
    }
    public Union(Project project) {
        super(project);
    }
    public Union(ResourceCollection rc) {
        this(Project.getProject(rc), rc);
    }
    public Union(Project project, ResourceCollection rc) {
        super(project);
        add(rc);
    }
    public String[] list() {
        if (isReference()) {
            return ((Union) getCheckedRef()).list();
        }
        Collection result = getCollection(true);
        return (String[]) (result.toArray(new String[result.size()]));
    }
    public Resource[] listResources() {
        if (isReference()) {
            return ((Union) getCheckedRef()).listResources();
        }
        Collection result = getCollection();
        return (Resource[]) (result.toArray(new Resource[result.size()]));
    }
    protected Collection getCollection() {
        return getCollection(false);
    }
    protected Collection getCollection(boolean asString) {
        List rc = getResourceCollections();
        if (rc.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        LinkedHashSet set = new LinkedHashSet(rc.size() * 2);
        for (Iterator rcIter = rc.iterator(); rcIter.hasNext();) {
            for (Iterator r = nextRC(rcIter).iterator(); r.hasNext();) {
                Object o = r.next();
                if (asString) {
                    o = o.toString();
                }
                set.add(o);
            }
        }
        return set;
    }
    private static ResourceCollection nextRC(Iterator i) {
        return (ResourceCollection) i.next();
    }
}
