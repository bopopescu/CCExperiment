package org.apache.cassandra.concurrent;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.management.MBeanServer;
import javax.management.ObjectName;
public class JMXEnabledThreadPoolExecutor extends DebuggableThreadPoolExecutor implements JMXEnabledThreadPoolExecutorMBean
{
    private final String mbeanName;
    public JMXEnabledThreadPoolExecutor(String threadPoolName)
    {
        this(1, 1, Integer.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(threadPoolName), "internal");
    }
    public JMXEnabledThreadPoolExecutor(String threadPoolName, String jmxPath)
    {
        this(1, 1, Integer.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(threadPoolName), jmxPath);
    }
    public JMXEnabledThreadPoolExecutor(String threadPoolName, int priority)
    {
        this(1, 1, Integer.MAX_VALUE, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(threadPoolName, priority), "internal");
    }
    public JMXEnabledThreadPoolExecutor(int corePoolSize,
                                        int maximumPoolSize,
                                        long keepAliveTime,
                                        TimeUnit unit,
                                        BlockingQueue<Runnable> workQueue,
                                        NamedThreadFactory threadFactory,
                                        String jmxPath)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
        super.prestartAllCoreThreads();
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        mbeanName = "org.apache.cassandra." + jmxPath + ":type=" + threadFactory.id;
        try
        {
            mbs.registerMBean(this, new ObjectName(mbeanName));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    public JMXEnabledThreadPoolExecutor(Stage stage)
    {
        this(stage.getJmxName(), stage.getJmxType());
    }
    private void unregisterMBean()
    {
        try
        {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(mbeanName));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    @Override
    public synchronized void shutdown()
    {
        if (!isShutdown())
        {
            unregisterMBean();
        }
        super.shutdown();
    }
    @Override
    public synchronized List<Runnable> shutdownNow()
    {
        if (!isShutdown())
        {
            unregisterMBean();
        }
        return super.shutdownNow();
    }
    public long getCompletedTasks()
    {
        return getCompletedTaskCount();
    }
    public long getPendingTasks()
    {
        return getTaskCount() - getCompletedTaskCount();
    }
}
