package org.apache.tools.ant.types.resources;
import java.util.List;
import java.util.Stack;
import java.util.Iterator;
import java.util.Collection;
import java.util.Collections;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.DataType;
import org.apache.tools.ant.types.ResourceCollection;
import org.apache.tools.ant.types.resources.comparators.ResourceComparator;
import org.apache.tools.ant.types.resources.comparators.DelegatedResourceComparator;
import org.apache.tools.ant.util.CollectionUtils;
public class Sort extends BaseResourceCollectionWrapper {
    private DelegatedResourceComparator comp = new DelegatedResourceComparator();
    protected synchronized Collection getCollection() {
        ResourceCollection rc = getResourceCollection();
        Iterator iter = rc.iterator();
        if (!(iter.hasNext())) {
            return Collections.EMPTY_SET;
        }
        List result = (List) CollectionUtils.asCollection(iter);
        Collections.sort(result, comp);
        return result;
    }
    public synchronized void add(ResourceComparator c) {
        if (isReference()) {
            throw noChildrenAllowed();
        }
        comp.add(c);
        FailFast.invalidate(this);
        setChecked(false);
    }
    protected synchronized void dieOnCircularReference(Stack stk, Project p)
        throws BuildException {
        if (isChecked()) {
            return;
        }
        super.dieOnCircularReference(stk, p);
        if (!isReference()) {
            DataType.pushAndInvokeCircularReferenceCheck(comp, stk, p);
            setChecked(true);
        }
    }
}
