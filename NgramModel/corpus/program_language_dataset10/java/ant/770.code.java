package org.apache.tools.ant.util;
import java.util.StringTokenizer;
public class DeweyDecimal {
    private int[] components;
    public DeweyDecimal(final int[] components) {
        this.components = new int[components.length];
        System.arraycopy(components, 0, this.components, 0, components.length);
    }
    public DeweyDecimal(final String string)
        throws NumberFormatException {
        final StringTokenizer tokenizer = new StringTokenizer(string, ".", true);
        final int size = tokenizer.countTokens();
        components = new int[ (size + 1) / 2 ];
        for (int i = 0; i < components.length; i++) {
            final String component = tokenizer.nextToken();
            if (component.equals("")) {
                throw new NumberFormatException("Empty component in string");
            }
            components[ i ] = Integer.parseInt(component);
            if (tokenizer.hasMoreTokens()) {
                tokenizer.nextToken();
                if (!tokenizer.hasMoreTokens()) {
                    throw new NumberFormatException("DeweyDecimal ended in a '.'");
                }
            }
        }
    }
    public int getSize() {
        return components.length;
    }
    public int get(final int index) {
        return components[ index ];
    }
    public boolean isEqual(final DeweyDecimal other) {
        final int max = Math.max(other.components.length, components.length);
        for (int i = 0; i < max; i++) {
            final int component1 = (i < components.length) ? components[ i ] : 0;
            final int component2 = (i < other.components.length) ? other.components[ i ] : 0;
            if (component2 != component1) {
                return false;
            }
        }
        return true; 
    }
    public boolean isLessThan(final DeweyDecimal other) {
        return !isGreaterThanOrEqual(other);
    }
    public boolean isLessThanOrEqual(final DeweyDecimal other) {
        return !isGreaterThan(other);
    }
    public boolean isGreaterThan(final DeweyDecimal other) {
        final int max = Math.max(other.components.length, components.length);
        for (int i = 0; i < max; i++) {
            final int component1 = (i < components.length) ? components[ i ] : 0;
            final int component2 = (i < other.components.length) ? other.components[ i ] : 0;
            if (component2 > component1) {
                return false;
            }
            if (component2 < component1) {
                return true;
            }
        }
        return false; 
    }
    public boolean isGreaterThanOrEqual(final DeweyDecimal other) {
        final int max = Math.max(other.components.length, components.length);
        for (int i = 0; i < max; i++) {
            final int component1 = (i < components.length) ? components[ i ] : 0;
            final int component2 = (i < other.components.length) ? other.components[ i ] : 0;
            if (component2 > component1) {
                return false;
            }
            if (component2 < component1) {
                return true;
            }
        }
        return true; 
    }
    public String toString() {
        final StringBuffer sb = new StringBuffer();
        for (int i = 0; i < components.length; i++) {
            if (i != 0) {
                sb.append('.');
            }
            sb.append(components[ i ]);
        }
        return sb.toString();
    }
}