package org.apache.maven.lifecycle;
import java.util.List;
import java.util.Map;
public class Lifecycle
{
    public Lifecycle()
    {
    }
    public Lifecycle( String id, List<String> phases, Map<String, String> defaultPhases )
    {
        this.id = id;
        this.phases = phases;
        this.defaultPhases = defaultPhases;
    }
    private String id;
    private List<String> phases;
    private Map<String, String> defaultPhases;
    public String getId()
    {
        return this.id;
    }
    public List<String> getPhases()
    {
        return this.phases;
    }
    public Map<String, String> getDefaultPhases()
    {
        return defaultPhases;
    }
    @Override
    public String toString()
    {
        return id + " -> " + phases;
    }
}
