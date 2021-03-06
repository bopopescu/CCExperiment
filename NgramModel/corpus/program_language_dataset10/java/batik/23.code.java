package org.apache.batik.anim.timing;
import java.util.Iterator;
import java.util.LinkedList;
public class Interval {
    protected float begin;
    protected float end;
    protected InstanceTime beginInstanceTime;
    protected InstanceTime endInstanceTime;
    protected LinkedList beginDependents = new LinkedList();
    protected LinkedList endDependents = new LinkedList();
    public Interval(float begin, float end, InstanceTime beginInstanceTime,
                    InstanceTime endInstanceTime) {
        this.begin = begin;
        this.end = end;
        this.beginInstanceTime = beginInstanceTime;
        this.endInstanceTime = endInstanceTime;
    }
    public String toString() {
        return TimedElement.toString(begin) + ".." + TimedElement.toString(end);
    }
    public float getBegin() {
        return begin;
    }
    public float getEnd() {
        return end;
    }
    public InstanceTime getBeginInstanceTime() {
        return beginInstanceTime;
    }
    public InstanceTime getEndInstanceTime() {
        return endInstanceTime;
    }
    void addDependent(InstanceTime dependent, boolean forBegin) {
        if (forBegin) {
            beginDependents.add(dependent);
        } else {
            endDependents.add(dependent);
        }
    }
    void removeDependent(InstanceTime dependent, boolean forBegin) {
        if (forBegin) {
            beginDependents.remove(dependent);
        } else {
            endDependents.remove(dependent);
        }
    }
    float setBegin(float begin) {
        float minTime = Float.POSITIVE_INFINITY;
        this.begin = begin;
        Iterator i = beginDependents.iterator();
        while (i.hasNext()) {
            InstanceTime it = (InstanceTime) i.next();
            float t = it.dependentUpdate(begin);
            if (t < minTime) {
                minTime = t;
            }
        }
        return minTime;
    }
    float setEnd(float end, InstanceTime endInstanceTime) {
        float minTime = Float.POSITIVE_INFINITY;
        this.end = end;
        this.endInstanceTime = endInstanceTime;
        Iterator i = endDependents.iterator();
        while (i.hasNext()) {
            InstanceTime it = (InstanceTime) i.next();
            float t = it.dependentUpdate(end);
            if (t < minTime) {
                minTime = t;
            }
        }
        return minTime;
    }
}
