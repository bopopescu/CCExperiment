package org.apache.maven.model.building;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.apache.maven.model.InputLocation;
import org.apache.maven.model.Model;
import org.apache.maven.model.building.ModelProblem.Severity;
import org.apache.maven.model.io.ModelParseException;
class DefaultModelProblemCollector
    implements ModelProblemCollector
{
    private List<ModelProblem> problems;
    private String source;
    private Model sourceModel;
    private Model rootModel;
    private Set<ModelProblem.Severity> severities = EnumSet.noneOf( ModelProblem.Severity.class );
    public DefaultModelProblemCollector( List<ModelProblem> problems )
    {
        this.problems = ( problems != null ) ? problems : new ArrayList<ModelProblem>();
        for ( ModelProblem problem : this.problems )
        {
            severities.add( problem.getSeverity() );
        }
    }
    public boolean hasFatalErrors()
    {
        return severities.contains( ModelProblem.Severity.FATAL );
    }
    public boolean hasErrors()
    {
        return severities.contains( ModelProblem.Severity.ERROR ) || severities.contains( ModelProblem.Severity.FATAL );
    }
    public List<ModelProblem> getProblems()
    {
        return problems;
    }
    public void setSource( String source )
    {
        this.source = source;
        this.sourceModel = null;
    }
    public void setSource( Model source )
    {
        this.sourceModel = source;
        this.source = null;
        if ( rootModel == null )
        {
            rootModel = source;
        }
    }
    private String getSource()
    {
        if ( source == null && sourceModel != null )
        {
            source = ModelProblemUtils.toPath( sourceModel );
        }
        return source;
    }
    private String getModelId()
    {
        return ModelProblemUtils.toId( sourceModel );
    }
    public void setRootModel( Model rootModel )
    {
        this.rootModel = rootModel;
    }
    public Model getRootModel()
    {
        return rootModel;
    }
    public String getRootModelId()
    {
        return ModelProblemUtils.toId( rootModel );
    }
    public void add( ModelProblem problem )
    {
        problems.add( problem );
        severities.add( problem.getSeverity() );
    }
    public void addAll( List<ModelProblem> problems )
    {
        this.problems.addAll( problems );
        for ( ModelProblem problem : problems )
        {
            severities.add( problem.getSeverity() );
        }
    }
    public void add( Severity severity, String message, InputLocation location, Exception cause )
    {
        int line = -1;
        int column = -1;
        String source = null;
        String modelId = null;
        if ( location != null )
        {
            line = location.getLineNumber();
            column = location.getColumnNumber();
            if ( location.getSource() != null )
            {
                modelId = location.getSource().getModelId();
                source = location.getSource().getLocation();
            }
        }
        if ( modelId == null )
        {
            modelId = getModelId();
            source = getSource();
        }
        if ( line <= 0 && column <= 0 && cause instanceof ModelParseException )
        {
            ModelParseException e = (ModelParseException) cause;
            line = e.getLineNumber();
            column = e.getColumnNumber();
        }
        ModelProblem problem = new DefaultModelProblem( message, severity, source, line, column, modelId, cause );
        add( problem );
    }
}
