package org.apache.cassandra.utils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import org.apache.cassandra.io.util.DataOutputBuffer;
import org.junit.Before;
import org.junit.Test;
public class LegacyBloomFilterTest
{
    public LegacyBloomFilter bf;
    public LegacyBloomFilterTest()
    {
        bf = LegacyBloomFilter.getFilter(FilterTestHelper.ELEMENTS, FilterTestHelper.MAX_FAILURE_RATE);
    }
    public static Filter testSerialize(LegacyBloomFilter f) throws IOException
    {
        f.add(ByteBufferUtil.bytes("a"));
        DataOutputBuffer out = new DataOutputBuffer();
        f.serializer().serialize(f, out);
        ByteArrayInputStream in = new ByteArrayInputStream(out.getData(), 0, out.getLength());
        LegacyBloomFilter f2 = f.serializer().deserialize(new DataInputStream(in));
        assert f2.isPresent(ByteBufferUtil.bytes("a"));
        assert !f2.isPresent(ByteBufferUtil.bytes("b"));
        return f2;
    }
    @Before
    public void clear()
    {
        bf.clear();
    }
    @Test(expected=UnsupportedOperationException.class)
    public void testBloomLimits1()
    {
        int maxBuckets = BloomCalculations.probs.length - 1;
        int maxK = BloomCalculations.probs[maxBuckets].length - 1;
        BloomCalculations.computeBloomSpec(maxBuckets, BloomCalculations.probs[maxBuckets][maxK]);
        BloomCalculations.computeBloomSpec(maxBuckets, BloomCalculations.probs[maxBuckets][maxK] / 2);
    }
    @Test
    public void testOne()
    {
        bf.add(ByteBufferUtil.bytes("a"));
        assert bf.isPresent(ByteBufferUtil.bytes("a"));
        assert !bf.isPresent(ByteBufferUtil.bytes("b"));
    }
    @Test
    public void testFalsePositivesInt()
    {
        FilterTestHelper.testFalsePositives(bf, FilterTestHelper.intKeys(), FilterTestHelper.randomKeys2());
    }
    @Test
    public void testFalsePositivesRandom()
    {
        FilterTestHelper.testFalsePositives(bf, FilterTestHelper.randomKeys(), FilterTestHelper.randomKeys2());
    }
    @Test
    public void testWords()
    {
        if (KeyGenerator.WordGenerator.WORDS == 0)
        {
            return;
        }
        LegacyBloomFilter bf2 = LegacyBloomFilter.getFilter(KeyGenerator.WordGenerator.WORDS / 2, FilterTestHelper.MAX_FAILURE_RATE);
        int skipEven = KeyGenerator.WordGenerator.WORDS % 2 == 0 ? 0 : 2;
        FilterTestHelper.testFalsePositives(bf2,
                                      new KeyGenerator.WordGenerator(skipEven, 2),
                                      new KeyGenerator.WordGenerator(1, 2));
    }
    @Test
    public void testSerialize() throws IOException
    {
        LegacyBloomFilterTest.testSerialize(bf);
    }
    public void testManyHashes(Iterator<ByteBuffer> keys)
    {
        int MAX_HASH_COUNT = 128;
        Set<Integer> hashes = new HashSet<Integer>();
        int collisions = 0;
        while (keys.hasNext())
        {
            hashes.clear();
            for (int hashIndex : LegacyBloomFilter.getHashBuckets(keys.next(), MAX_HASH_COUNT, 1024 * 1024))
            {
                hashes.add(hashIndex);
            }
            collisions += (MAX_HASH_COUNT - hashes.size());
        }
        assert collisions <= 100;
    }
    @Test
    public void testManyRandom()
    {
        testManyHashes(FilterTestHelper.randomKeys());
    }
}
