package org.apache.cassandra.db;
public interface CompactionManagerMBean
{    
    public String getColumnFamilyInProgress();
    public Long getBytesTotalInProgress();
    public Long getBytesCompacted();
    public String getCompactionType();
    public int getPendingTasks();
    public long getCompletedTasks();
    public void forceUserDefinedCompaction(String ksname, String dataFiles);
}
