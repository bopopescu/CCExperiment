package org.apache.cassandra.utils;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import org.apache.commons.collections.iterators.CollatingIterator;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.config.ConfigurationException;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.marshal.AbstractType;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.Range;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.locator.PropertyFileSnitch;
import org.apache.thrift.TBase;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.TSerializer;
public class FBUtilities
{
    private static Logger logger_ = LoggerFactory.getLogger(FBUtilities.class);
    public static final BigInteger TWO = new BigInteger("2");
    private static volatile InetAddress localInetAddress_;
    public static final int MAX_UNSIGNED_SHORT = 0xFFFF;
    public static double parseDoubleOrPercent(String value)
    {
        if (value.endsWith("%"))
        {
            return Double.parseDouble(value.substring(0, value.length() - 1)) / 100;
        }
        else
        {
            return Double.parseDouble(value);
        }
    }
    public static InetAddress getLocalAddress()
    {
        if (localInetAddress_ == null)
            try
            {
                localInetAddress_ = DatabaseDescriptor.getListenAddress() == null
                                    ? InetAddress.getLocalHost()
                                    : DatabaseDescriptor.getListenAddress();
            }
            catch (UnknownHostException e)
            {
                throw new RuntimeException(e);
            }
        return localInetAddress_;
    }
    public static long absoluteFromFraction(double fractOrAbs, long total)
    {
        if (fractOrAbs < 0)
            throw new UnsupportedOperationException("unexpected negative value " + fractOrAbs);
        if (0 < fractOrAbs && fractOrAbs <= 1)
        {
            return Math.max(1, (long)(fractOrAbs * total));
        }
        assert fractOrAbs >= 1 || fractOrAbs == 0;
        return (long)fractOrAbs;
    }
    public static Pair<BigInteger,Boolean> midpoint(BigInteger left, BigInteger right, int sigbits)
    {
        BigInteger midpoint;
        boolean remainder;
        if (left.compareTo(right) < 0)
        {
            BigInteger sum = left.add(right);
            remainder = sum.testBit(0);
            midpoint = sum.shiftRight(1);
        }
        else
        {
            BigInteger max = TWO.pow(sigbits);
            BigInteger distance = max.add(right).subtract(left);
            remainder = distance.testBit(0);
            midpoint = distance.shiftRight(1).add(left).mod(max);
        }
        return new Pair<BigInteger, Boolean>(midpoint, remainder);
    }
    public static void copyIntoBytes(byte[] bytes, int offset, int i)
    {
        bytes[offset]   = (byte)( ( i >>> 24 ) & 0xFF );
        bytes[offset+1] = (byte)( ( i >>> 16 ) & 0xFF );
        bytes[offset+2] = (byte)( ( i >>> 8  ) & 0xFF );
        bytes[offset+3] = (byte)(   i          & 0xFF );
    }
    public static byte[] toByteArray(int i)
    {
        byte[] bytes = new byte[4];
        copyIntoBytes(bytes, 0, i);
        return bytes;
    }
    public static int byteArrayToInt(byte[] bytes, int offset)
    {
        if (bytes.length - offset < 4)
        {
            throw new IllegalArgumentException("An integer must be 4 bytes in size.");
        }
        int n = 0;
        for ( int i = 0; i < 4; ++i )
        {
            n <<= 8;
            n |= bytes[offset + i] & 0xFF;
        }
        return n;
    }
    public static void copyIntoBytes(byte[] bytes, int offset, long l)
    {
        bytes[offset]   = (byte)( ( l >>> 56 ) & 0xFF );
        bytes[offset+1] = (byte)( ( l >>> 48 ) & 0xFF );
        bytes[offset+2] = (byte)( ( l >>> 40 ) & 0xFF );
        bytes[offset+3] = (byte)( ( l >>> 32 ) & 0xFF );
        bytes[offset+4] = (byte)( ( l >>> 24 ) & 0xFF );
        bytes[offset+5] = (byte)( ( l >>> 16 ) & 0xFF );
        bytes[offset+6] = (byte)( ( l >>> 8  ) & 0xFF );
        bytes[offset+7] = (byte)(   l          & 0xFF );
    }
    public static byte[] toByteArray(long l)
    {
        byte[] bytes = new byte[8];
        copyIntoBytes(bytes, 0, l);
        return bytes;
    }
    public static long byteArrayToLong(byte[] bytes)
    {
        return byteArrayToLong(bytes, 0);
    }
    public static long byteArrayToLong(byte[] bytes, int offset)
    {
        if (bytes.length - offset < 8)
        {
            throw new IllegalArgumentException("A long must be 8 bytes in size.");
        }
        long n = 0;
        for ( int i = 0; i < 8; ++i )
        {
            n <<= 8;
            n |= bytes[offset + i] & 0xFF;
        }
        return n;
    }
    public static int compareUnsigned(byte[] bytes1, byte[] bytes2, int offset1, int offset2, int len1, int len2)
    {
        if (bytes1 == null)
        {
            return bytes2 == null ? 0 : -1;
        }
        if (bytes2 == null) return 1;
        int minLength = Math.min(len1 - offset1, len2 - offset2);
        for (int x = 0, i = offset1, j = offset2; x < minLength; x++, i++, j++)
        {
            if (bytes1[i] == bytes2[j])
                continue;
            return (bytes1[i] & 0xFF) < (bytes2[j] & 0xFF) ? -1 : 1;
        }
        if ((len1 - offset1) == (len2 - offset2)) return 0;
        else return ((len1 - offset1) < (len2 - offset2)) ? -1 : 1;
    }
    public static int compareByteSubArrays(byte[] bytes1, int offset1, byte[] bytes2, int offset2, int length)
    {
        if ( null == bytes1 )
        {
            if ( null == bytes2) return 0;
            else return -1;
        }
        if (null == bytes2 ) return 1;
        assert bytes1.length >= (offset1 + length) : "The first byte array isn't long enough for the specified offset and length.";
        assert bytes2.length >= (offset2 + length) : "The second byte array isn't long enough for the specified offset and length.";
        for ( int i = 0; i < length; i++ )
        {
            byte byte1 = bytes1[offset1+i];
            byte byte2 = bytes2[offset2+i];
            if ( byte1 == byte2 )
                continue;
            return (byte1 & 0xFF) < (byte2 & 0xFF) ? -1 : 1;
        }
        return 0;
    }
    public static byte[] xor(byte[] left, byte[] right)
    {
        if (left == null || right == null)
            return null;
        if (left.length > right.length)
        {
            byte[] swap = left;
            left = right;
            right = swap;
        }
        byte[] out = Arrays.copyOf(right, right.length);
        for (int i = 0; i < left.length; i++)
        {
            out[i] = (byte)((left[i] & 0xFF) ^ (right[i] & 0xFF));
        }
        return out;
    }
    public static BigInteger md5hash(ByteBuffer data)
    {
        byte[] result = hash("MD5", data);
        BigInteger hash = new BigInteger(result);
        return hash.abs();        
    }
    public static byte[] hash(String type, ByteBuffer... data)
    {
    	byte[] result;
    	try
        {
            MessageDigest messageDigest = MessageDigest.getInstance(type);
            for(ByteBuffer block : data)
            {
                messageDigest.update(ByteBufferUtil.clone(block));
            }
            result = messageDigest.digest();
    	}
    	catch (Exception e)
        {
            throw new RuntimeException(e);
    	}
    	return result;
	}
    public static byte[] hexToBytes(String str)
    {
        if (str.length() % 2 == 1)
            str = "0" + str;
        byte[] bytes = new byte[str.length()/2];
        for (int i = 0; i < bytes.length; i++)
        {
            bytes[i] = (byte)Integer.parseInt(str.substring(i*2, i*2+2), 16);
        }
        return bytes;
    }
    public static String bytesToHex(byte... bytes)
    {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
        {
            int bint = b & 0xff;
            if (bint <= 0xF)
                sb.append("0");
            sb.append(Integer.toHexString(bint));
        }
        return sb.toString();
    }
    public static void renameWithConfirm(String tmpFilename, String filename) throws IOException
    {
        if (!new File(tmpFilename).renameTo(new File(filename)))
        {
            throw new IOException("rename failed of " + filename);
        }
    }
    public static CollatingIterator getCollatingIterator()
    {
        return new CollatingIterator(new Comparator()
        {
            public int compare(Object o1, Object o2)
            {
                return ((Comparable) o1).compareTo(o2);
            }
        });
    }
    public static void atomicSetMax(AtomicInteger atomic, int i)
    {
        while (true)
        {
            int j = atomic.get();
            if (j >= i || atomic.compareAndSet(j, i))
                break;
        }
    }
    public static void atomicSetMax(AtomicLong atomic, long i)
    {
        while (true)
        {
            long j = atomic.get();
            if (j >= i || atomic.compareAndSet(j, i))
                break;
        }
    }
    public static void serialize(TSerializer serializer, TBase struct, DataOutput out)
    throws IOException
    {
        assert serializer != null;
        assert struct != null;
        assert out != null;
        byte[] bytes;
        try
        {
            bytes = serializer.serialize(struct);
        }
        catch (TException e)
        {
            throw new RuntimeException(e);
        }
        out.writeInt(bytes.length);
        out.write(bytes);
    }
    public static void deserialize(TDeserializer deserializer, TBase struct, DataInput in)
    throws IOException
    {
        assert deserializer != null;
        assert struct != null;
        assert in != null;
        byte[] bytes = new byte[in.readInt()];
        in.readFully(bytes);
        try
        {
            deserializer.deserialize(struct, bytes);
        }
        catch (TException ex)
        {
            throw new IOException(ex);
        }
    }
    public static void sortSampledKeys(List<DecoratedKey> keys, Range range)
    {
        if (range.left.compareTo(range.right) >= 0)
        {
            final Token right = range.right;
            Comparator<DecoratedKey> comparator = new Comparator<DecoratedKey>()
            {
                public int compare(DecoratedKey o1, DecoratedKey o2)
                {
                    if ((right.compareTo(o1.token) < 0 && right.compareTo(o2.token) < 0)
                        || (right.compareTo(o1.token) > 0 && right.compareTo(o2.token) > 0))
                    {
                        return o1.compareTo(o2);
                    }
                    return -o1.compareTo(o2);
                }
            };
            Collections.sort(keys, comparator);
        }
        else
        {
            Collections.sort(keys);
        }
    }
    public static int encodedUTF8Length(String st)
    {
        int strlen = st.length();
        int utflen = 0;
        for (int i = 0; i < strlen; i++)
        {
            int c = st.charAt(i);
            if ((c >= 0x0001) && (c <= 0x007F))
                utflen++;
            else if (c > 0x07FF)
                utflen += 3;
            else
                utflen += 2;
        }
        return utflen;
    }
    public static String decodeToUTF8(ByteBuffer bytes) throws CharacterCodingException
    {
        return Charsets.UTF_8.newDecoder().decode(bytes.duplicate()).toString();
    }
    public static String resourceToFile(String filename) throws ConfigurationException
    {
        ClassLoader loader = PropertyFileSnitch.class.getClassLoader();
        URL scpurl = loader.getResource(filename);
        if (scpurl == null)
            throw new ConfigurationException("unable to locate " + filename);
        return scpurl.getFile();
    }
    public static String getReleaseVersionString()
    {
        try
        {
            InputStream in = FBUtilities.class.getClassLoader().getResourceAsStream("org/apache/cassandra/config/version.properties");
            if (in == null)
            {
                return "Unknown";
            }
            Properties props = new Properties();
            props.load(in);
            return props.getProperty("CassandraVersion");
        }
        catch (Exception e)
        {
            logger_.warn("Unable to load version.properties", e);
            return "debug version";
        }
    }
    public static long timestampMicros()
    {
        return System.currentTimeMillis() * 1000;
    }
    public static void waitOnFutures(Iterable<Future<?>> futures)
    {
        for (Future f : futures)
        {
            try
            {
                f.get();
            }
            catch (ExecutionException ee)
            {
                throw new RuntimeException(ee);
            }
            catch (InterruptedException ie)
            {
                throw new AssertionError(ie);
            }
        }
    }
    public static IPartitioner newPartitioner(String partitionerClassName) throws ConfigurationException
    {
        if (!partitionerClassName.contains("."))
            partitionerClassName = "org.apache.cassandra.dht." + partitionerClassName;
        return FBUtilities.construct(partitionerClassName, "partitioner");
    }
    public static AbstractType getComparator(String compareWith) throws ConfigurationException
    {
        String className = compareWith.contains(".") ? compareWith : "org.apache.cassandra.db.marshal." + compareWith;
        Class<? extends AbstractType> typeClass = FBUtilities.<AbstractType>classForName(className, "abstract-type");
        try
        {
            Field field = typeClass.getDeclaredField("instance");
            return (AbstractType) field.get(null);
        }
        catch (NoSuchFieldException e)
        {
            ConfigurationException ex = new ConfigurationException("Invalid comparator " + compareWith + " : must define a public static instance field.");
            ex.initCause(e);
            throw ex;
        }
        catch (IllegalAccessException e)
        {
            ConfigurationException ex = new ConfigurationException("Invalid comparator " + compareWith + " : must define a public static instance field.");
            ex.initCause(e);
            throw ex;
        }
    }
    public static <T> Class<T> classForName(String classname, String readable) throws ConfigurationException
    {
        try
        {
            return (Class<T>)Class.forName(classname);
        }
        catch (ClassNotFoundException e)
        {
            throw new ConfigurationException(String.format("Unable to find %s class '%s'", readable, classname));
        }
    }
    public static <T> T construct(String classname, String readable) throws ConfigurationException
    {
        Class<T> cls = FBUtilities.classForName(classname, readable);
        try
        {
            return cls.getConstructor().newInstance();
        }
        catch (NoSuchMethodException e)
        {
            throw new ConfigurationException(String.format("No default constructor for %s class '%s'.", readable, classname));
        }
        catch (IllegalAccessException e)
        {
            throw new ConfigurationException(String.format("Default constructor for %s class '%s' is inaccessible.", readable, classname));
        }
        catch (InstantiationException e)
        {
            throw new ConfigurationException(String.format("Cannot use abstract class '%s' as %s.", classname, readable));
        }
        catch (InvocationTargetException e)
        {
            if (e.getCause() instanceof ConfigurationException)
                throw (ConfigurationException)e.getCause();
            throw new ConfigurationException(String.format("Error instantiating %s class '%s'.", readable, classname), e);
        }
    }
    public static <T extends Comparable> SortedSet<T> singleton(T column)
    {
        return new TreeSet<T>(Arrays.asList(column));
    }
    public static String toString(Map<?,?> map)
    {
        Joiner.MapJoiner joiner = Joiner.on(",").withKeyValueSeparator(":");
        return joiner.join(map);
    }
    public static Field getProtectedField(Class klass, String fieldName)
    {
        Field field;
        try
        {
            field = klass.getDeclaredField(fieldName);
            field.setAccessible(true);
        }
        catch (Exception e)
        {
            throw new AssertionError(e);
        }
        return field;
    }
}
