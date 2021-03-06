package org.apache.tools.ant.taskdefs;
public class TestProcess
    implements Runnable
{
    private boolean run = true;
    private boolean done = false;
    public void shutdown()
    {
        if (!done)
        {
            System.out.println("shutting down TestProcess");
            run = false;
            synchronized(this)
            {
                while (!done)
                {
                    try { wait(); } catch (InterruptedException ie) {}
                }
            }
            System.out.println("TestProcess shut down");
        }
    }
    public void run()
    {
        for (int i = 0; i < 5 && run; i++)
        {
            System.out.println(Thread.currentThread().getName());
            try { Thread.sleep(2000); } catch (InterruptedException ie) {}
        }
        synchronized(this)
        {
            done = true;
            notifyAll();
        }
    }
    public Thread getShutdownHook()
    {
        return new TestProcessShutdownHook();
    }
    private class TestProcessShutdownHook
        extends Thread
    {
        public void run()
        {
            shutdown();
        }
    }
    public static void main(String[] args)
    {
        TestProcess tp = new TestProcess();
        new Thread(tp, "TestProcess thread").start();
        Runtime.getRuntime().addShutdownHook(tp.getShutdownHook());
    }
}
