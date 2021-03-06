package org.apache.cassandra.locator;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import com.google.common.collect.Iterators;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.apache.cassandra.CleanupHelper;
import static org.apache.cassandra.Util.token;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.locator.TokenMetadata;
import org.apache.cassandra.service.StorageService;
public class TokenMetadataTest
{
    public final static String ONE = "1";
    public final static String SIX = "6";
    public static ArrayList<Token> RING;
    @BeforeClass
    public static void beforeClass() throws Throwable
    {
        TokenMetadata tmd = StorageService.instance.getTokenMetadata();
        tmd.updateNormalToken(token(ONE), InetAddress.getByName("127.0.0.1"));
        tmd.updateNormalToken(token(SIX), InetAddress.getByName("127.0.0.6"));
        RING = tmd.sortedTokens();
    }
    private void testRingIterator(String start, boolean includeMin, String... expected)
    {
        ArrayList<Token> actual = new ArrayList<Token>();
        Iterators.addAll(actual, TokenMetadata.ringIterator(RING, token(start), includeMin));
        assertEquals(actual.toString(), expected.length, actual.size());
        for (int i = 0; i < expected.length; i++)
            assertEquals("Mismatch at index " + i + ": " + actual, token(expected[i]), actual.get(i));
    }
    @Test
    public void testRingIterator()
    {
        testRingIterator("2", false, "6", "1");
        testRingIterator("7", false, "1", "6");
        testRingIterator("0", false, "1", "6");
        testRingIterator("", false, "1", "6");
    }
    @Test
    public void testRingIteratorIncludeMin()
    {
        testRingIterator("2", true, "6", "", "1");
        testRingIterator("7", true, "", "1", "6");
        testRingIterator("0", true, "1", "6", "");
        testRingIterator("", true, "1", "6", "");
    }
}
