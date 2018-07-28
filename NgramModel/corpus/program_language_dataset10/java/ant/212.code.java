package org.apache.tools.ant.taskdefs;
import java.util.Vector;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.input.DefaultInputHandler;
import org.apache.tools.ant.input.GreedyInputHandler;
import org.apache.tools.ant.input.InputHandler;
import org.apache.tools.ant.input.InputRequest;
import org.apache.tools.ant.input.MultipleChoiceInputRequest;
import org.apache.tools.ant.input.PropertyFileInputHandler;
import org.apache.tools.ant.input.SecureInputHandler;
import org.apache.tools.ant.types.EnumeratedAttribute;
import org.apache.tools.ant.util.ClasspathUtils;
import org.apache.tools.ant.util.StringUtils;
public class Input extends Task {
    public class Handler extends DefBase {
        private String refid = null;
        private HandlerType type = null;
        private String classname = null;
        public void setRefid(String refid) {
            this.refid = refid;
        }
        public String getRefid() {
            return refid;
        }
        public void setClassname(String classname) {
            this.classname = classname;
        }
        public String getClassname() {
            return classname;
        }
        public void setType(HandlerType type) {
            this.type = type;
        }
        public HandlerType getType() {
            return type;
        }
        private InputHandler getInputHandler() {
            if (type != null) {
               return type.getInputHandler();
            }
            if (refid != null) {
               try {
                   return (InputHandler) (getProject().getReference(refid));
               } catch (ClassCastException e) {
                   throw new BuildException(
                       refid + " does not denote an InputHandler", e);
               }
            }
            if (classname != null) {
               return (InputHandler) (ClasspathUtils.newInstance(classname,
                   createLoader(), InputHandler.class));
            }
            throw new BuildException(
                "Must specify refid, classname or type");
        }
    }
    public static class HandlerType extends EnumeratedAttribute {
        private static final String[] VALUES = { "default", "propertyfile", "greedy", "secure" };
        private static final InputHandler[] HANDLERS
            = { new DefaultInputHandler(),
               new PropertyFileInputHandler(),
               new GreedyInputHandler(),
               new SecureInputHandler() };
        public String[] getValues() {
            return VALUES;
        }
        private InputHandler getInputHandler() {
            return HANDLERS[getIndex()];
        }
    }
    private String validargs = null;
    private String message = "";
    private String addproperty = null;
    private String defaultvalue = null;
    private Handler handler = null;
    private boolean messageAttribute;
    public void setValidargs (String validargs) {
        this.validargs = validargs;
    }
    public void setAddproperty (String addproperty) {
        this.addproperty = addproperty;
    }
    public void setMessage (String message) {
        this.message = message;
        messageAttribute = true;
    }
    public void setDefaultvalue (String defaultvalue) {
        this.defaultvalue = defaultvalue;
    }
    public void addText(String msg) {
        if (messageAttribute && "".equals(msg.trim())) {
            return;
        }
        message += getProject().replaceProperties(msg);
    }
    public Input () {
    }
    public void execute () throws BuildException {
        if (addproperty != null
            && getProject().getProperty(addproperty) != null) {
            log("skipping " + getTaskName() + " as property " + addproperty
                + " has already been set.");
            return;
        }
        InputRequest request = null;
        if (validargs != null) {
            Vector accept = StringUtils.split(validargs, ',');
            request = new MultipleChoiceInputRequest(message, accept);
        } else {
            request = new InputRequest(message);
        }
        request.setDefaultValue(defaultvalue);
        InputHandler h = handler == null
            ? getProject().getInputHandler()
            : handler.getInputHandler();
        h.handleInput(request);
        String value = request.getInput();
        if ((value == null || value.trim().length() == 0)
            && defaultvalue != null) {
            value = defaultvalue;
        }
        if (addproperty != null && value != null) {
            getProject().setNewProperty(addproperty, value);
        }
    }
    public Handler createHandler() {
        if (handler != null) {
            throw new BuildException(
                "Cannot define > 1 nested input handler");
        }
        handler = new Handler();
        return handler;
    }
}