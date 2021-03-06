package org.apache.cassandra.gms;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.apache.cassandra.io.ICompactSerializer;
class HeartBeatState
{
    private static ICompactSerializer<HeartBeatState> serializer_;
    static
    {
        serializer_ = new HeartBeatStateSerializer();
    }
    int generation_;
    int version_;
    HeartBeatState(int generation)
    {
        this(generation, 0);
    }
    HeartBeatState(int generation, int version)
    {
        generation_ = generation;
        version_ = version;
    }
    public static ICompactSerializer<HeartBeatState> serializer()
    {
        return serializer_;
    }
    int getGeneration()
    {
        return generation_;
    }
    void updateHeartBeat()
    {
        version_ = VersionGenerator.getNextVersion();
    }
    int getHeartBeatVersion()
    {
        return version_;
    }
}
class HeartBeatStateSerializer implements ICompactSerializer<HeartBeatState>
{
    public void serialize(HeartBeatState hbState, DataOutputStream dos) throws IOException
    {
        dos.writeInt(hbState.generation_);
        dos.writeInt(hbState.version_);
    }
    public HeartBeatState deserialize(DataInputStream dis) throws IOException
    {
        return new HeartBeatState(dis.readInt(), dis.readInt());
    }
}
