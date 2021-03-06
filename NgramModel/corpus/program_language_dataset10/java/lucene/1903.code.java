package org.apache.lucene.index;
import org.apache.lucene.util.LuceneTestCase;
import java.io.IOException;
import java.util.BitSet;
import java.util.Map;
public class TestPositionBasedTermVectorMapper extends LuceneTestCase {
  protected String[] tokens;
  protected int[][] thePositions;
  protected TermVectorOffsetInfo[][] offsets;
  protected int numPositions;
  public TestPositionBasedTermVectorMapper(String s) {
    super(s);
  }
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    tokens = new String[]{"here", "is", "some", "text", "to", "test", "extra"};
    thePositions = new int[tokens.length][];
    offsets = new TermVectorOffsetInfo[tokens.length][];
    numPositions = 0;
    for (int i = 0; i < tokens.length - 1; i++)
    {
      thePositions[i] = new int[2 * i + 1];
      for (int j = 0; j < thePositions[i].length; j++)
      {
        thePositions[i][j] = numPositions++;
      }
      offsets[i] = new TermVectorOffsetInfo[thePositions[i].length];
      for (int j = 0; j < offsets[i].length; j++) {
        offsets[i][j] = new TermVectorOffsetInfo(j, j + 1);
      }
    }
    thePositions[tokens.length - 1] = new int[1];
    thePositions[tokens.length - 1][0] = 0;
    offsets[tokens.length - 1] = new TermVectorOffsetInfo[1];
    offsets[tokens.length - 1][0] = new TermVectorOffsetInfo(0, 1);
  }
  public void test() throws IOException {
    PositionBasedTermVectorMapper mapper = new PositionBasedTermVectorMapper();
    mapper.setExpectations("test", tokens.length, true, true);
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i];
      mapper.map(token, 1, null, thePositions[i]);
    }
    Map<String,Map<Integer,PositionBasedTermVectorMapper.TVPositionInfo>> map = mapper.getFieldToTerms();
    assertTrue("map is null and it shouldn't be", map != null);
    assertTrue("map Size: " + map.size() + " is not: " + 1, map.size() == 1);
    Map<Integer,PositionBasedTermVectorMapper.TVPositionInfo> positions = map.get("test");
    assertTrue("thePositions is null and it shouldn't be", positions != null);
    assertTrue("thePositions Size: " + positions.size() + " is not: " + numPositions, positions.size() == numPositions);
    BitSet bits = new BitSet(numPositions);
    for (Map.Entry<Integer,PositionBasedTermVectorMapper.TVPositionInfo> entry : positions.entrySet()) {
      PositionBasedTermVectorMapper.TVPositionInfo info = entry.getValue();
      assertTrue("info is null and it shouldn't be", info != null);
      int pos = entry.getKey().intValue();
      bits.set(pos);
      assertTrue(info.getPosition() + " does not equal: " + pos, info.getPosition() == pos);
      assertTrue("info.getOffsets() is null and it shouldn't be", info.getOffsets() != null);
      if (pos == 0)
      {
        assertTrue("info.getTerms() Size: " + info.getTerms().size() + " is not: " + 2, info.getTerms().size() == 2);
        assertTrue("info.getOffsets() Size: " + info.getOffsets().size() + " is not: " + 2, info.getOffsets().size() == 2);
      }
      else
      {
        assertTrue("info.getTerms() Size: " + info.getTerms().size() + " is not: " + 1, info.getTerms().size() == 1);
        assertTrue("info.getOffsets() Size: " + info.getOffsets().size() + " is not: " + 1, info.getOffsets().size() == 1);
      }
    }
    assertTrue("Bits are not all on", bits.cardinality() == numPositions);
  }
}
