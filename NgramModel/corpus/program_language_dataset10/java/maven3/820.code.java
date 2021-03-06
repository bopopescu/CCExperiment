package org.apache.maven.settings.building;
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.settings.io.SettingsParseException;
class DefaultSettingsProblemCollector
    implements SettingsProblemCollector
{
    private List<SettingsProblem> problems;
    private String source;
    public DefaultSettingsProblemCollector( List<SettingsProblem> problems )
    {
        this.problems = ( problems != null ) ? problems : new ArrayList<SettingsProblem>();
    }
    public List<SettingsProblem> getProblems()
    {
        return problems;
    }
    public void setSource( String source )
    {
        this.source = source;
    }
    public void add( SettingsProblem.Severity severity, String message, int line, int column, Exception cause )
    {
        if ( line <= 0 && column <= 0 && ( cause instanceof SettingsParseException ) )
        {
            SettingsParseException e = (SettingsParseException) cause;
            line = e.getLineNumber();
            column = e.getColumnNumber();
        }
        SettingsProblem problem = new DefaultSettingsProblem( message, severity, source, line, column, cause );
        problems.add( problem );
    }
}
