package org.apache.maven.lifecycle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class Lifecycle
{
    private String id;
    private List phases;
    private Map defaultPhases;
    public void addPhase( String phase )
    {
        getPhases().add( phase );
    } 
    public String getId()
    {
        return this.id;
    } 
    public List getPhases()
    {
        if ( this.phases == null )
        {
            this.phases = new ArrayList();
        }
        return this.phases;
    } 
    public void setId( String id )
    {
        this.id = id;
    } 
    public void setPhases( List phases )
    {
        this.phases = phases;
    } 
    public Map getDefaultPhases()
    {
        return defaultPhases;
    }
}
