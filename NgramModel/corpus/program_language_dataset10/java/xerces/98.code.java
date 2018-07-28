package org.apache.html.dom;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLCollection;
import org.w3c.dom.html.HTMLElement;
import org.w3c.dom.html.HTMLOptionElement;
import org.w3c.dom.html.HTMLSelectElement;
public class HTMLSelectElementImpl
    extends HTMLElementImpl
    implements HTMLSelectElement, HTMLFormControl
{
    private static final long serialVersionUID = -6998282711006968187L; 
    public String getType()
    {
        return getAttribute( "type" );
    }
      public String getValue()
    {
        return getAttribute( "value" );
    }
    public void setValue( String value )
    {
        setAttribute( "value", value );
    }
    public int getSelectedIndex()
    {
        NodeList    options;
        int            i;
        options = getElementsByTagName( "OPTION" );
        for ( i = 0 ; i < options.getLength() ; ++i )
            if ( ( (HTMLOptionElement) options.item( i ) ).getSelected() )
                return i;
        return -1;
    }
    public void setSelectedIndex( int selectedIndex )
    {
        NodeList    options;
        int            i;
        options = getElementsByTagName( "OPTION" );
        for ( i = 0 ; i < options.getLength() ; ++i )
            ( (HTMLOptionElementImpl) options.item( i ) ).setSelected( i == selectedIndex );
    }
    public HTMLCollection getOptions()
    {
        if ( _options == null )
            _options = new HTMLCollectionImpl( this, HTMLCollectionImpl.OPTION );
        return _options;
    }
    public int getLength()
    {
        return getOptions().getLength();
    }
    public boolean getDisabled()
    {
        return getBinary( "disabled" );
    }
    public void setDisabled( boolean disabled )
    {
        setAttribute( "disabled", disabled );
    }
      public boolean getMultiple()
    {
        return getBinary( "multiple" );
    }
    public void setMultiple( boolean multiple )
    {
        setAttribute( "multiple", multiple );
    }
      public String getName()
    {
        return getAttribute( "name" );
    }
    public void setName( String name )
    {
        setAttribute( "name", name );
    }
    public int getSize()
    {
        return getInteger( getAttribute( "size" ) );
    }
    public void setSize( int size )
    {
        setAttribute( "size", String.valueOf( size ) );
    }
    public int getTabIndex()
    {
        return getInteger( getAttribute( "tabindex" ) );
    }
    public void setTabIndex( int tabIndex )
    {
        setAttribute( "tabindex", String.valueOf( tabIndex ) );
    }
    public void add( HTMLElement element, HTMLElement before )
    {
        insertBefore( element, before );
    }
    public void remove( int index )
    {
        NodeList    options;
        Node        removed;
        options = getElementsByTagName( "OPTION" );
        removed = options.item( index );
        if ( removed != null )
            removed.getParentNode().removeChild ( removed );
    }
    public void               blur()
    {
    }
    public void               focus()
    {
    }
    public NodeList getChildNodes() {
        return getChildNodesUnoptimized();
    }
    public Node cloneNode(boolean deep) {
        HTMLSelectElementImpl clonedNode = (HTMLSelectElementImpl)super.cloneNode( deep );
        clonedNode._options = null;
        return clonedNode;
    }
    public HTMLSelectElementImpl( HTMLDocumentImpl owner, String name )
    {
        super( owner, name );
    }
    private HTMLCollection    _options;
}