package org.apache.lucene.index;
import java.io.IOException;
public class StaleReaderException extends IOException {
  public StaleReaderException(String message) {
    super(message);
  }
}