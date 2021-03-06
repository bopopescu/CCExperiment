package org.apache.batik.parser;
import java.util.Iterator;
import java.util.LinkedList;
import org.w3c.dom.svg.SVGLength;
public class LengthArrayProducer extends DefaultLengthListHandler {
    protected LinkedList vs;
    protected float[] v;
    protected LinkedList us;
    protected short[] u;
    protected int index;
    protected int count;
    protected short currentUnit;
    public short[] getLengthTypeArray() {
        return u;
    }
    public float[] getLengthValueArray() {
        return v;
    }
    public void startLengthList() throws ParseException {
        us = new LinkedList();
        u = new short[11];
        vs = new LinkedList();
        v = new float[11];
        count = 0;
        index = 0;
    }
    public void numberValue(float v) throws ParseException {
    }
    public void lengthValue(float val) throws ParseException {
        if (index == v.length) {
            vs.add(v);
            v = new float[v.length * 2 + 1];
            us.add(u);
            u = new short[u.length * 2 + 1];
            index = 0;
        }
        v[index] = val;
    }
    public void startLength() throws ParseException {
        currentUnit = SVGLength.SVG_LENGTHTYPE_NUMBER;
    }
    public void endLength() throws ParseException {
        u[index++] = currentUnit;
        count++;
    }
    public void em() throws ParseException {
        currentUnit = SVGLength.SVG_LENGTHTYPE_EMS;
    }
    public void ex() throws ParseException {
        currentUnit = SVGLength.SVG_LENGTHTYPE_EXS;
    }
    public void in() throws ParseException {
        currentUnit = SVGLength.SVG_LENGTHTYPE_IN;
    }
    public void cm() throws ParseException {
        currentUnit = SVGLength.SVG_LENGTHTYPE_CM;
    }
    public void mm() throws ParseException {
        currentUnit = SVGLength.SVG_LENGTHTYPE_MM;
    }
    public void pc() throws ParseException {
        currentUnit = SVGLength.SVG_LENGTHTYPE_PC;
    }
    public void pt() throws ParseException {
        currentUnit = SVGLength.SVG_LENGTHTYPE_PT;
    }
    public void px() throws ParseException {
        currentUnit = SVGLength.SVG_LENGTHTYPE_PX;
    }
    public void percentage() throws ParseException {
        currentUnit = SVGLength.SVG_LENGTHTYPE_PERCENTAGE;
    }
    public void endLengthList() throws ParseException {
        float[] allValues = new float[count];
        int pos = 0;
        Iterator it = vs.iterator();
        while (it.hasNext()) {
            float[] a = (float[]) it.next();
            System.arraycopy(a, 0, allValues, pos, a.length);
            pos += a.length;
        }
        System.arraycopy(v, 0, allValues, pos, index);
        vs.clear();
        v = allValues;
        short[] allUnits = new short[count];
        pos = 0;
        it = us.iterator();
        while (it.hasNext()) {
            short[] a = (short[]) it.next();
            System.arraycopy(a, 0, allUnits, pos, a.length);
            pos += a.length;
        }
        System.arraycopy(u, 0, allUnits, pos, index);
        us.clear();
        u = allUnits;
    }
}
