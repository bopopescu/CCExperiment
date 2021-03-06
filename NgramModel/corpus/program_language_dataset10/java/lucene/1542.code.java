package org.apache.lucene.index;
final class FreqProxTermsWriterPerThread extends TermsHashConsumerPerThread {
  final TermsHashPerThread termsHashPerThread;
  final DocumentsWriter.DocState docState;
  public FreqProxTermsWriterPerThread(TermsHashPerThread perThread) {
    docState = perThread.docState;
    termsHashPerThread = perThread;
  }
  @Override
  public TermsHashConsumerPerField addField(TermsHashPerField termsHashPerField, FieldInfo fieldInfo) {
    return new FreqProxTermsWriterPerField(termsHashPerField, this, fieldInfo);
  }
  @Override
  void startDocument() {
  }
  @Override
  DocumentsWriter.DocWriter finishDocument() {
    return null;
  }
  @Override
  public void abort() {}
}
