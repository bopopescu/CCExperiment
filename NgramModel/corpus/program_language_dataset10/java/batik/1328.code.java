package org.apache.batik.transcoder;
public interface ErrorHandler {
    void error(TranscoderException ex) throws TranscoderException;
    void fatalError(TranscoderException ex) throws TranscoderException;
    void warning(TranscoderException ex) throws TranscoderException;
}
