package org.apache.tools.ant.helper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.IntrospectionHelper;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.apache.tools.ant.RuntimeConfigurable;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.TypeAdapter;
import org.apache.tools.ant.TaskContainer;
import org.apache.tools.ant.UnknownElement;
import org.apache.tools.ant.util.FileUtils;
import org.apache.tools.ant.util.JAXPUtils;
import org.xml.sax.AttributeList;
import org.xml.sax.DocumentHandler;
import org.xml.sax.HandlerBase;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.XMLReaderAdapter;
public class ProjectHelperImpl extends ProjectHelper {
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();
    private org.xml.sax.Parser parser;
    private Project project;
    private File buildFile;
    private File buildFileParent;
    private Locator locator;
    private Target implicitTarget = new Target();
    public ProjectHelperImpl() {
        implicitTarget.setName("");
    }
    public void parse(Project project, Object source) throws BuildException {
        if (!(source instanceof File)) {
            throw new BuildException("Only File source supported by "
                + "default plugin");
        }
        File bFile = (File) source;
        FileInputStream inputStream = null;
        InputSource inputSource = null;
        this.project = project;
        this.buildFile = new File(bFile.getAbsolutePath());
        buildFileParent = new File(this.buildFile.getParent());
        try {
            try {
                parser = JAXPUtils.getParser();
            } catch (BuildException e) {
                parser = new XMLReaderAdapter(JAXPUtils.getXMLReader());
            }
            String uri = FILE_UTILS.toURI(bFile.getAbsolutePath());
            inputStream = new FileInputStream(bFile);
            inputSource = new InputSource(inputStream);
            inputSource.setSystemId(uri);
            project.log("parsing buildfile " + bFile + " with URI = " + uri, Project.MSG_VERBOSE);
            HandlerBase hb = new RootHandler(this);
            parser.setDocumentHandler(hb);
            parser.setEntityResolver(hb);
            parser.setErrorHandler(hb);
            parser.setDTDHandler(hb);
            parser.parse(inputSource);
        } catch (SAXParseException exc) {
            Location location = new Location(exc.getSystemId(), exc.getLineNumber(), exc
                    .getColumnNumber());
            Throwable t = exc.getException();
            if (t instanceof BuildException) {
                BuildException be = (BuildException) t;
                if (be.getLocation() == Location.UNKNOWN_LOCATION) {
                    be.setLocation(location);
                }
                throw be;
            }
            throw new BuildException(exc.getMessage(), t, location);
        } catch (SAXException exc) {
            Throwable t = exc.getException();
            if (t instanceof BuildException) {
                throw (BuildException) t;
            }
            throw new BuildException(exc.getMessage(), t);
        } catch (FileNotFoundException exc) {
            throw new BuildException(exc);
        } catch (UnsupportedEncodingException exc) {
            throw new BuildException("Encoding of project file is invalid.", exc);
        } catch (IOException exc) {
            throw new BuildException("Error reading project file: " + exc.getMessage(), exc);
        } finally {
            FileUtils.close(inputStream);
        }
    }
    static class AbstractHandler extends HandlerBase {
        protected DocumentHandler parentHandler;
        ProjectHelperImpl helperImpl;
        public AbstractHandler(ProjectHelperImpl helperImpl, DocumentHandler parentHandler) {
            this.parentHandler = parentHandler;
            this.helperImpl = helperImpl;
            helperImpl.parser.setDocumentHandler(this);
        }
        public void startElement(String tag, AttributeList attrs) throws SAXParseException {
            throw new SAXParseException("Unexpected element \"" + tag + "\"", helperImpl.locator);
        }
        public void characters(char[] buf, int start, int count) throws SAXParseException {
            String s = new String(buf, start, count).trim();
            if (s.length() > 0) {
                throw new SAXParseException("Unexpected text \"" + s + "\"", helperImpl.locator);
            }
        }
        public void endElement(String name) throws SAXException {
            helperImpl.parser.setDocumentHandler(parentHandler);
        }
    }
    static class RootHandler extends HandlerBase {
        ProjectHelperImpl helperImpl;
        public RootHandler(ProjectHelperImpl helperImpl) {
            this.helperImpl = helperImpl;
        }
        public InputSource resolveEntity(String publicId, String systemId) {
            helperImpl.project.log("resolving systemId: " + systemId, Project.MSG_VERBOSE);
            if (systemId.startsWith("file:")) {
                String path = FILE_UTILS.fromURI(systemId);
                File file = new File(path);
                if (!file.isAbsolute()) {
                    file = FILE_UTILS.resolveFile(helperImpl.buildFileParent, path);
                    helperImpl.project.log("Warning: '" + systemId + "' in " + helperImpl.buildFile
                            + " should be expressed simply as '" + path.replace('\\', '/')
                            + "' for compliance with other XML tools", Project.MSG_WARN);
                }
                try {
                    InputSource inputSource = new InputSource(new FileInputStream(file));
                    inputSource.setSystemId(FILE_UTILS.toURI(file.getAbsolutePath()));
                    return inputSource;
                } catch (FileNotFoundException fne) {
                    helperImpl.project.log(file.getAbsolutePath() + " could not be found",
                            Project.MSG_WARN);
                }
            }
            return null;
        }
        public void startElement(String tag, AttributeList attrs) throws SAXParseException {
            if (tag.equals("project")) {
                new ProjectHandler(helperImpl, this).init(tag, attrs);
            } else {
                throw new SAXParseException("Config file is not of expected " + "XML type",
                        helperImpl.locator);
            }
        }
        public void setDocumentLocator(Locator locator) {
            helperImpl.locator = locator;
        }
    }
    static class ProjectHandler extends AbstractHandler {
        public ProjectHandler(ProjectHelperImpl helperImpl, DocumentHandler parentHandler) {
            super(helperImpl, parentHandler);
        }
        public void init(String tag, AttributeList attrs) throws SAXParseException {
            String def = null;
            String name = null;
            String id = null;
            String baseDir = null;
            for (int i = 0; i < attrs.getLength(); i++) {
                String key = attrs.getName(i);
                String value = attrs.getValue(i);
                if (key.equals("default")) {
                    def = value;
                } else if (key.equals("name")) {
                    name = value;
                } else if (key.equals("id")) {
                    id = value;
                } else if (key.equals("basedir")) {
                    baseDir = value;
                } else {
                    throw new SAXParseException(
                            "Unexpected attribute \"" + attrs.getName(i)
                            + "\"", helperImpl.locator);
                }
            }
            if (def != null && !def.equals("")) {
                helperImpl.project.setDefault(def);
            } else {
                throw new BuildException("The default attribute is required");
            }
            if (name != null) {
                helperImpl.project.setName(name);
                helperImpl.project.addReference(name, helperImpl.project);
            }
            if (id != null) {
                helperImpl.project.addReference(id, helperImpl.project);
            }
            if (helperImpl.project.getProperty("basedir") != null) {
                helperImpl.project.setBasedir(helperImpl.project.getProperty("basedir"));
            } else {
                if (baseDir == null) {
                    helperImpl.project.setBasedir(helperImpl.buildFileParent.getAbsolutePath());
                } else {
                    if ((new File(baseDir)).isAbsolute()) {
                        helperImpl.project.setBasedir(baseDir);
                    } else {
                        File resolvedBaseDir = FILE_UTILS.resolveFile(helperImpl.buildFileParent,
                                baseDir);
                        helperImpl.project.setBaseDir(resolvedBaseDir);
                    }
                }
            }
            helperImpl.project.addTarget("", helperImpl.implicitTarget);
        }
        public void startElement(String name, AttributeList attrs) throws SAXParseException {
            if (name.equals("target")) {
                handleTarget(name, attrs);
            } else {
                handleElement(helperImpl, this, helperImpl.implicitTarget, name, attrs);
            }
        }
        private void handleTarget(String tag, AttributeList attrs) throws SAXParseException {
            new TargetHandler(helperImpl, this).init(tag, attrs);
        }
    }
    static class TargetHandler extends AbstractHandler {
        private Target target;
        public TargetHandler(ProjectHelperImpl helperImpl, DocumentHandler parentHandler) {
            super(helperImpl, parentHandler);
        }
        public void init(String tag, AttributeList attrs) throws SAXParseException {
            String name = null;
            String depends = "";
            String ifCond = null;
            String unlessCond = null;
            String id = null;
            String description = null;
            for (int i = 0; i < attrs.getLength(); i++) {
                String key = attrs.getName(i);
                String value = attrs.getValue(i);
                if (key.equals("name")) {
                    name = value;
                    if (name.equals("")) {
                        throw new BuildException("name attribute must not" + " be empty",
                                new Location(helperImpl.locator));
                    }
                } else if (key.equals("depends")) {
                    depends = value;
                } else if (key.equals("if")) {
                    ifCond = value;
                } else if (key.equals("unless")) {
                    unlessCond = value;
                } else if (key.equals("id")) {
                    id = value;
                } else if (key.equals("description")) {
                    description = value;
                } else {
                    throw new SAXParseException("Unexpected attribute \"" + key + "\"",
                            helperImpl.locator);
                }
            }
            if (name == null) {
                throw new SAXParseException("target element appears without a name attribute",
                        helperImpl.locator);
            }
            target = new Target();
            target.addDependency("");
            target.setName(name);
            target.setIf(ifCond);
            target.setUnless(unlessCond);
            target.setDescription(description);
            helperImpl.project.addTarget(name, target);
            if (id != null && !id.equals("")) {
                helperImpl.project.addReference(id, target);
            }
            if (depends.length() > 0) {
                target.setDepends(depends);
            }
        }
        public void startElement(String name, AttributeList attrs) throws SAXParseException {
            handleElement(helperImpl, this, target, name, attrs);
        }
    }
    private static void handleElement(ProjectHelperImpl helperImpl, DocumentHandler parent,
            Target target, String elementName, AttributeList attrs) throws SAXParseException {
        if (elementName.equals("description")) {
            new DescriptionHandler(helperImpl, parent);
        } else if (helperImpl.project.getDataTypeDefinitions().get(elementName) != null) {
            new DataTypeHandler(helperImpl, parent, target).init(elementName, attrs);
        } else {
            new TaskHandler(helperImpl, parent, target, null, target).init(elementName, attrs);
        }
    }
    static class DescriptionHandler extends AbstractHandler {
        public DescriptionHandler(ProjectHelperImpl helperImpl,
                                  DocumentHandler parentHandler) {
            super(helperImpl, parentHandler);
        }
        public void characters(char[] buf, int start, int count) {
            String text = new String(buf, start, count);
            String currentDescription = helperImpl.project.getDescription();
            if (currentDescription == null) {
                helperImpl.project.setDescription(text);
            } else {
                helperImpl.project.setDescription(currentDescription + text);
            }
        }
    }
    static class TaskHandler extends AbstractHandler {
        private Target target;
        private TaskContainer container;
        private Task task;
        private RuntimeConfigurable parentWrapper;
        private RuntimeConfigurable wrapper = null;
        public TaskHandler(ProjectHelperImpl helperImpl, DocumentHandler parentHandler,
                           TaskContainer container,
                           RuntimeConfigurable parentWrapper, Target target) {
            super(helperImpl, parentHandler);
            this.container = container;
            this.parentWrapper = parentWrapper;
            this.target = target;
        }
        public void init(String tag, AttributeList attrs) throws SAXParseException {
            try {
                task = helperImpl.project.createTask(tag);
            } catch (BuildException e) {
            }
            if (task == null) {
                task = new UnknownElement(tag);
                task.setProject(helperImpl.project);
                task.setTaskName(tag);
            }
            task.setLocation(new Location(helperImpl.locator));
            helperImpl.configureId(task, attrs);
            task.setOwningTarget(target);
            container.addTask(task);
            task.init();
            wrapper = task.getRuntimeConfigurableWrapper();
            wrapper.setAttributes(attrs);
            if (parentWrapper != null) {
                parentWrapper.addChild(wrapper);
            }
        }
        public void characters(char[] buf, int start, int count) {
            wrapper.addText(buf, start, count);
        }
        public void startElement(String name, AttributeList attrs) throws SAXParseException {
            if (task instanceof TaskContainer) {
                new TaskHandler(helperImpl, this, (TaskContainer) task, wrapper, target).init(name,
                        attrs);
            } else {
                new NestedElementHandler(helperImpl, this, task, wrapper, target).init(name, attrs);
            }
        }
    }
    static class NestedElementHandler extends AbstractHandler {
        private Object parent;
        private Object child;
        private RuntimeConfigurable parentWrapper;
        private RuntimeConfigurable childWrapper = null;
        private Target target;
        public NestedElementHandler(ProjectHelperImpl helperImpl,
                                    DocumentHandler parentHandler,
                                    Object parent,
                                    RuntimeConfigurable parentWrapper,
                                    Target target) {
            super(helperImpl, parentHandler);
            if (parent instanceof TypeAdapter) {
                this.parent = ((TypeAdapter) parent).getProxy();
            } else {
                this.parent = parent;
            }
            this.parentWrapper = parentWrapper;
            this.target = target;
        }
        public void init(String propType, AttributeList attrs) throws SAXParseException {
            Class parentClass = parent.getClass();
            IntrospectionHelper ih = IntrospectionHelper.getHelper(helperImpl.project, parentClass);
            try {
                String elementName = propType.toLowerCase(Locale.ENGLISH);
                if (parent instanceof UnknownElement) {
                    UnknownElement uc = new UnknownElement(elementName);
                    uc.setProject(helperImpl.project);
                    ((UnknownElement) parent).addChild(uc);
                    child = uc;
                } else {
                    child = ih.createElement(helperImpl.project, parent, elementName);
                }
                helperImpl.configureId(child, attrs);
                childWrapper = new RuntimeConfigurable(child, propType);
                childWrapper.setAttributes(attrs);
                parentWrapper.addChild(childWrapper);
            } catch (BuildException exc) {
                throw new SAXParseException(exc.getMessage(), helperImpl.locator, exc);
            }
        }
        public void characters(char[] buf, int start, int count) {
            childWrapper.addText(buf, start, count);
        }
        public void startElement(String name, AttributeList attrs) throws SAXParseException {
            if (child instanceof TaskContainer) {
                new TaskHandler(helperImpl, this, (TaskContainer) child, childWrapper, target)
                        .init(name, attrs);
            } else {
                new NestedElementHandler(helperImpl, this, child, childWrapper, target).init(name,
                        attrs);
            }
        }
    }
    static class DataTypeHandler extends AbstractHandler {
        private Target target;
        private Object element;
        private RuntimeConfigurable wrapper = null;
        public DataTypeHandler(ProjectHelperImpl helperImpl, DocumentHandler parentHandler,
                Target target) {
            super(helperImpl, parentHandler);
            this.target = target;
        }
        public void init(String propType, AttributeList attrs) throws SAXParseException {
            try {
                element = helperImpl.project.createDataType(propType);
                if (element == null) {
                    throw new BuildException("Unknown data type " + propType);
                }
                wrapper = new RuntimeConfigurable(element, propType);
                wrapper.setAttributes(attrs);
                target.addDataType(wrapper);
            } catch (BuildException exc) {
                throw new SAXParseException(exc.getMessage(), helperImpl.locator, exc);
            }
        }
        public void characters(char[] buf, int start, int count) {
            wrapper.addText(buf, start, count);
        }
        public void startElement(String name, AttributeList attrs) throws SAXParseException {
            new NestedElementHandler(helperImpl, this, element, wrapper, target).init(name, attrs);
        }
    }
    private void configureId(Object target, AttributeList attr) {
        String id = attr.getValue("id");
        if (id != null) {
            project.addReference(id, target);
        }
    }
}