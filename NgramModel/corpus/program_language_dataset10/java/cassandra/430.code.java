package org.apache.cassandra.utils;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
public class SkipNullRepresenter extends Representer
{
    protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) 
    {
        if (propertyValue == null) 
        {
            return null;
        } 
        else 
        {
            return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
        }
    }
}