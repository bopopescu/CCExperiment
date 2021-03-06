package org.apache.solr.common.util;
import java.lang.System;
import java.lang.Thread;
import java.util.*;
public class RTimer {
  public static final int STARTED = 0;
  public static final int STOPPED = 1;
  public static final int PAUSED = 2;
  protected int state;
  protected double startTime;
  protected double time;
  protected double culmTime;
  protected SimpleOrderedMap<RTimer> children;
  public RTimer() {
    time = 0;
    culmTime = 0;
    children = new SimpleOrderedMap<RTimer>();
    startTime = now();
    state = STARTED;
  }
  protected double now() { return System.currentTimeMillis(); }
  public double stop() {
    assert state == STARTED || state == PAUSED;
    time = culmTime;
    if(state == STARTED) 
      time += now() - startTime;
    state = STOPPED;
    for( Map.Entry<String,RTimer> entry : children ) {
      RTimer child = entry.getValue();
      if(child.state == STARTED || child.state == PAUSED) 
        child.stop();
    }
    return time;
  }
  public void pause() {
    assert state == STARTED;
    culmTime += now() - startTime;
    state = PAUSED;
  }
  public void resume() {
    if(state == STARTED)
      return;
    assert state == PAUSED;
    state = STARTED;
    startTime = now();
  }
  public double getTime() {
    assert state == STOPPED;
    return time;
  }
  public RTimer sub(String desc) {
    RTimer child = children.get( desc );
    if( child == null ) {
      child = new RTimer();
      children.add(desc, child);
    }
    return child;
  }
  @Override
  public String toString() {
    return asNamedList().toString();
  }
  public NamedList asNamedList() {
    NamedList<Object> m = new SimpleOrderedMap<Object>();
    m.add( "time", time );
    if( children.size() > 0 ) {
      for( Map.Entry<String, RTimer> entry : children ) {
        m.add( entry.getKey(), entry.getValue().asNamedList() );
      }
    }
    return m;
  }
  public SimpleOrderedMap<RTimer> getChildren()
  {
    return children;
  }
  public static void main(String []argv) throws InterruptedException {
    RTimer rt = new RTimer(), subt, st;
    Thread.sleep(100);
    subt = rt.sub("sub1");
    Thread.sleep(50);
    st = subt.sub("sub1.1");
    st.resume();
    Thread.sleep(10);
    st.pause();
    Thread.sleep(50);
    st.resume();
    Thread.sleep(10);
    st.pause();
    subt.stop();
    rt.stop();
    System.out.println( rt.toString());
  }
}
