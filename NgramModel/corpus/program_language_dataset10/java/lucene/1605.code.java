package org.apache.lucene.index;
import java.io.IOException;
public interface TermPositions
    extends TermDocs
{
    int nextPosition() throws IOException;
    int getPayloadLength();
    byte[] getPayload(byte[] data, int offset) throws IOException;
    public boolean isPayloadAvailable();
}