package org.apache.tools.ant.util.facade;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
public class FacadeTaskHelper {
    private List args = new ArrayList();
    private String userChoice;
    private String magicValue;
    private String defaultValue;
    private Path implementationClasspath;
    public FacadeTaskHelper(String defaultValue) {
        this(defaultValue, null);
    }
    public FacadeTaskHelper(String defaultValue, String magicValue) {
        this.defaultValue = defaultValue;
        this.magicValue = magicValue;
    }
    public void setMagicValue(String magicValue) {
        this.magicValue = magicValue;
    }
    public void setImplementation(String userChoice) {
        this.userChoice = userChoice;
    }
    public String getImplementation() {
        return userChoice != null ? userChoice
                                  : (magicValue != null ? magicValue
                                                        : defaultValue);
    }
    public String getExplicitChoice() {
        return userChoice;
    }
    public void addImplementationArgument(ImplementationSpecificArgument arg) {
        args.add(arg);
    }
    public String[] getArgs() {
        List tmp = new ArrayList(args.size());
        for (Iterator e = args.iterator(); e.hasNext();) {
            ImplementationSpecificArgument arg =
                ((ImplementationSpecificArgument) e.next());
            String[] curr = arg.getParts(getImplementation());
            for (int i = 0; i < curr.length; i++) {
                tmp.add(curr[i]);
            }
        }
        String[] res = new String[tmp.size()];
        return (String[]) tmp.toArray(res);
    }
    public boolean hasBeenSet() {
        return userChoice != null || magicValue != null;
    }
    public Path getImplementationClasspath(Project project) {
        if (implementationClasspath == null) {
            implementationClasspath = new Path(project);
        }
        return implementationClasspath;
    }
}
