package org.apache.xerces.xni.parser;
public interface XMLComponentManager {
    public boolean getFeature(String featureId)
        throws XMLConfigurationException;
    public Object getProperty(String propertyId)
        throws XMLConfigurationException;
} 
