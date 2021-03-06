package org.apache.cassandra.db.commitlog;
import java.util.concurrent.*;
import org.apache.cassandra.utils.WrappedRunnable;
class PeriodicCommitLogExecutorService implements ICommitLogExecutorService, PeriodicCommitLogExecutorServiceMBean
{
    private final BlockingQueue<Runnable> queue;
    protected volatile long completedTaskCount = 0;
    public PeriodicCommitLogExecutorService()
    {
        this(1024 * Runtime.getRuntime().availableProcessors());
    }
    public PeriodicCommitLogExecutorService(int queueSize)
    {
        queue = new LinkedBlockingQueue<Runnable>(queueSize);
        Runnable runnable = new WrappedRunnable()
        {
            public void runMayThrow() throws Exception
            {
                while (true)
                {
                    queue.take().run();
                    completedTaskCount++;
                }
            }
        };
        new Thread(runnable, "COMMIT-LOG-WRITER").start();
        AbstractCommitLogExecutorService.registerMBean(this);
    }
    public void add(CommitLog.LogRecordAdder adder)
    {
        try
        {
            queue.put(adder);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
    }
    public <T> Future<T> submit(Callable<T> task)
    {
        FutureTask<T> ft = new FutureTask<T>(task);
        try
        {
            queue.put(ft);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }
        return ft;
    }
    public long getPendingTasks()
    {
        return queue.size();
    }
    public int getActiveCount()
    {
        return 1;
    }
    public long getCompletedTasks()
    {
        return completedTaskCount;
    }
}