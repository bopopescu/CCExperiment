package org.apache.tools.ant;
public interface DynamicElement {
    Object createDynamicElement(String name) throws BuildException;
}