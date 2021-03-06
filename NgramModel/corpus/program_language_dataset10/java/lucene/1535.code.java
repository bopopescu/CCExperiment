package org.apache.lucene.index;
import java.io.IOException;
abstract class FormatPostingsPositionsConsumer {
  abstract void addPosition(int position, byte[] payload, int payloadOffset, int payloadLength) throws IOException;
  abstract void finish() throws IOException;
}
