package org.apache.maven.monitor.event;
@Deprecated
public interface EventDispatcher
{
    void addEventMonitor( EventMonitor monitor );
    void dispatchStart( String event, String target );
    void dispatchEnd( String event, String target );
    void dispatchError( String event, String target, Throwable cause );
}