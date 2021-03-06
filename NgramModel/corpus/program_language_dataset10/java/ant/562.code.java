package org.apache.tools.ant.taskdefs.optional.testing;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.TaskAdapter;
import org.apache.tools.ant.util.WorkerAnt;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.taskdefs.condition.ConditionBase;
import org.apache.tools.ant.taskdefs.Parallel;
import org.apache.tools.ant.taskdefs.Sequential;
import org.apache.tools.ant.taskdefs.WaitFor;
public class Funtest extends Task {
    private NestedCondition condition;
    private Parallel timedTests;
    private Sequential setup;
    private Sequential application;
    private BlockFor block;
    private Sequential tests;
    private Sequential reporting;
    private Sequential teardown;
    private long timeout;
    private long timeoutUnitMultiplier = WaitFor.ONE_MILLISECOND;
    private long shutdownTime = 10 * WaitFor.ONE_SECOND;
    private long shutdownUnitMultiplier = WaitFor.ONE_MILLISECOND;
    private String failureProperty;
    private String failureMessage = "Tests failed";
    private boolean failOnTeardownErrors = true;
    private BuildException testException;
    private BuildException teardownException;
    private BuildException applicationException;
    private BuildException taskException;
    public static final String WARN_OVERRIDING = "Overriding previous definition of ";
    public static final String APPLICATION_FORCIBLY_SHUT_DOWN = "Application forcibly shut down";
    public static final String SHUTDOWN_INTERRUPTED = "Shutdown interrupted";
    public static final String SKIPPING_TESTS
        = "Condition failed -skipping tests";
    public static final String APPLICATION_EXCEPTION = "Application Exception";
    public static final String TEARDOWN_EXCEPTION = "Teardown Exception";
    private void logOverride(String name, Object definition) {
        if (definition != null) {
            log(WARN_OVERRIDING + '<' + name + '>', Project.MSG_INFO);
        }
    }
     public ConditionBase createCondition() {
        logOverride("condition", condition);
        condition = new NestedCondition();
        return condition;
    }
    public void addApplication(Sequential sequence) {
        logOverride("application", application);
        application = sequence;
    }
    public void addSetup(Sequential sequence) {
        logOverride("setup", setup);
        setup = sequence;
    }
    public void addBlock(BlockFor sequence) {
        logOverride("block", block);
        block = sequence;
    }
    public void addTests(Sequential sequence) {
        logOverride("tests", tests);
        tests = sequence;
    }
    public void addReporting(Sequential sequence) {
        logOverride("reporting", reporting);
        reporting = sequence;
    }
    public void addTeardown(Sequential sequence) {
        logOverride("teardown", teardown);
        teardown = sequence;
    }
    public void setFailOnTeardownErrors(boolean failOnTeardownErrors) {
        this.failOnTeardownErrors = failOnTeardownErrors;
    }
    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }
    public void setFailureProperty(String failureProperty) {
        this.failureProperty = failureProperty;
    }
    public void setShutdownTime(long shutdownTime) {
        this.shutdownTime = shutdownTime;
    }
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
    public void setTimeoutUnit(WaitFor.Unit unit) {
        timeoutUnitMultiplier = unit.getMultiplier();
    }
    public void setShutdownUnit(WaitFor.Unit unit) {
        shutdownUnitMultiplier = unit.getMultiplier();
    }
    public BuildException getApplicationException() {
        return applicationException;
    }
    public BuildException getTeardownException() {
        return teardownException;
    }
    public BuildException getTestException() {
        return testException;
    }
    public BuildException getTaskException() {
        return taskException;
    }
    private void bind(Task task) {
        task.bindToOwner(this);
        task.init();
    }
    private Parallel newParallel(long parallelTimeout) {
        Parallel par = new Parallel();
        bind(par);
        par.setFailOnAny(true);
        par.setTimeout(parallelTimeout);
        return par;
    }
    private Parallel newParallel(long parallelTimeout, Task child) {
        Parallel par = newParallel(parallelTimeout);
        par.addTask(child);
        return par;
    }
    private void validateTask(Task task, String role) {
        if (task!=null && task.getProject() == null) {
            throw new BuildException(role + " task is not bound to the project" + task);
        }
    }
    public void execute() throws BuildException {
        validateTask(setup, "setup");
        validateTask(application, "application");
        validateTask(tests, "tests");
        validateTask(reporting, "reporting");
        validateTask(teardown, "teardown");
        if (condition != null && !condition.eval()) {
            log(SKIPPING_TESTS);
            return;
        }
        long timeoutMillis = timeout * timeoutUnitMultiplier;
        Parallel applicationRun = newParallel(timeoutMillis);
        WorkerAnt worker = new WorkerAnt(applicationRun, null);
        if (application != null) {
            applicationRun.addTask(application);
        }
        long testRunTimeout = 0;
        Sequential testRun = new Sequential();
        bind(testRun);
        if (block != null) {
            TaskAdapter ta = new TaskAdapter(block);
            ta.bindToOwner(this);
            validateTask(ta, "block");
            testRun.addTask(ta);
            testRunTimeout = block.calculateMaxWaitMillis();
        }
        if (tests != null) {
            testRun.addTask(tests);
            testRunTimeout += timeoutMillis;
        }
        if (reporting != null) {
            testRun.addTask(reporting);
            testRunTimeout += timeoutMillis;
        }
        timedTests = newParallel(testRunTimeout, testRun);
        try {
            if (setup != null) {
                Parallel setupRun = newParallel(timeoutMillis, setup);
                setupRun.execute();
            }
            worker.start();
            timedTests.execute();
        } catch (BuildException e) {
            testException = e;
        } finally {
            if (teardown != null) {
                try {
                    Parallel teardownRun = newParallel(timeoutMillis, teardown);
                    teardownRun.execute();
                } catch (BuildException e) {
                    teardownException = e;
                }
            }
        }
        try {
            long shutdownTimeMillis = shutdownTime * shutdownUnitMultiplier;
            worker.waitUntilFinished(shutdownTimeMillis);
            if (worker.isAlive()) {
                log(APPLICATION_FORCIBLY_SHUT_DOWN, Project.MSG_WARN);
                worker.interrupt();
                worker.waitUntilFinished(shutdownTimeMillis);
            }
        } catch (InterruptedException e) {
            log(SHUTDOWN_INTERRUPTED, e, Project.MSG_VERBOSE);
        }
        applicationException = worker.getBuildException();
        processExceptions();
    }
    protected void processExceptions() {
        taskException = testException;
        if (applicationException != null) {
            if (taskException == null || taskException instanceof BuildTimeoutException) {
                taskException = applicationException;
            } else {
                ignoringThrowable(APPLICATION_EXCEPTION, applicationException);
            }
        }
        if (teardownException != null) {
            if (taskException == null && failOnTeardownErrors) {
                taskException = teardownException;
            } else {
                ignoringThrowable(TEARDOWN_EXCEPTION, teardownException);
            }
        }
        if (failureProperty != null
             && getProject().getProperty(failureProperty) != null) {
            log(failureMessage);
            if (taskException == null) {
                taskException = new BuildException(failureMessage);
            }
        }
        if (taskException != null) {
            throw taskException;
        }
    }
    protected void ignoringThrowable(String type, Throwable thrown) {
        log(type + ": " + thrown.toString(),
                thrown,
                Project.MSG_WARN);
    }
    private static class NestedCondition extends ConditionBase implements Condition {
        public boolean eval() {
            if (countConditions() != 1) {
                throw new BuildException(
                    "A single nested condition is required.");
            }
            return ((Condition) (getConditions().nextElement())).eval();
        }
    }
}
