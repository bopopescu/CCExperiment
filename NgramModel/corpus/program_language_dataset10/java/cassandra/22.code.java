package org.apache.cassandra.cql.driver;
public interface IConnectionPool
{
    public Connection borrowConnection();
    public void returnConnection(Connection connection);
}
