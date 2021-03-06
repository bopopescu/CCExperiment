package org.apache.cassandra.db.context;
public interface IContext
{
    public static enum ContextRelationship
    {
        EQUAL,
        GREATER_THAN,
        LESS_THAN,
        DISJOINT
    };
    public byte[] create();
    public ContextRelationship diff(byte[] left, byte[] right);
    public byte[] merge(byte[] left, byte[] right);
    public String toString(byte[] context);
}
