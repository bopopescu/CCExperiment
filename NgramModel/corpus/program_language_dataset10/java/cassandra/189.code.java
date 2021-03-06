package org.apache.cassandra.db.commitlog;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.apache.cassandra.concurrent.IExecutorMBean;
public interface ICommitLogExecutorService extends IExecutorMBean
{
    public <T> Future<T> submit(Callable<T> task);
    public void add(CommitLog.LogRecordAdder adder);
}
