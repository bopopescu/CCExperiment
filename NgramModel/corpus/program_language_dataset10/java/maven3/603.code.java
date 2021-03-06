package org.apache.maven.project.harness;
import java.util.Map;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
class Xpp3DomAttributePointer
    extends NodePointer
{
    private Map.Entry<String, String> attrib;
    public Xpp3DomAttributePointer( NodePointer parent, Map.Entry<String, String> attrib )
    {
        super( parent );
        this.attrib = attrib;
    }
    @Override
    public int compareChildNodePointers( NodePointer pointer1, NodePointer pointer2 )
    {
        return 0;
    }
    @Override
    public Object getValue()
    {
        return attrib.getValue();
    }
    @Override
    public Object getBaseValue()
    {
        return attrib;
    }
    @Override
    public Object getImmediateNode()
    {
        return attrib;
    }
    @Override
    public int getLength()
    {
        return 1;
    }
    @Override
    public QName getName()
    {
        return new QName( null, attrib.getKey() );
    }
    @Override
    public boolean isActual()
    {
        return true;
    }
    @Override
    public boolean isCollection()
    {
        return false;
    }
    @Override
    public boolean isLeaf()
    {
        return true;
    }
    @Override
    public void setValue( Object value )
    {
        throw new UnsupportedOperationException();
    }
}
