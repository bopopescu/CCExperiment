package org.apache.cassandra.utils;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import org.safehaus.uuid.EthernetAddress;
import org.safehaus.uuid.UUIDGenerator;
public class UUIDGen
{
    public static synchronized UUID makeType1UUIDFromHost(InetAddress addr)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(addr.getAddress());
            byte[] md5 = digest.digest();
            byte[] fauxMac = new byte[6];
            System.arraycopy(md5, 0, fauxMac, 0, Math.min(md5.length, fauxMac.length));
            return getUUID(ByteBuffer.wrap(UUIDGenerator.getInstance().generateTimeBasedUUID(new EthernetAddress(fauxMac)).toByteArray()));
        }
        catch (NoSuchAlgorithmException ex)
        {
            throw new RuntimeException("Your platform has no support for generating MD5 sums");
        }
    }
    public static UUID getUUID(ByteBuffer raw)
    {
        return new UUID(raw.getLong(raw.position()), raw.getLong(raw.position() + 8));
    }
    public static byte[] decompose(UUID uuid)
    {
        long most = uuid.getMostSignificantBits();
        long least = uuid.getLeastSignificantBits();
        byte[] b = new byte[16];
        for (int i = 0; i < 8; i++)
        {
            b[i] = (byte)(most >>> ((7-i) * 8));
            b[8+i] = (byte)(least >>> ((7-i) * 8));
        }
        return b;
    }
}
