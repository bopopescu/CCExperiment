package org.apache.lucene.index;
import java.io.IOException;
final class FormatPostingsTermsWriter extends FormatPostingsTermsConsumer {
  final FormatPostingsFieldsWriter parent;
  final FormatPostingsDocsWriter docsWriter;
  final TermInfosWriter termsOut;
  FieldInfo fieldInfo;
  FormatPostingsTermsWriter(SegmentWriteState state, FormatPostingsFieldsWriter parent) throws IOException {
    super();
    this.parent = parent;
    termsOut = parent.termsOut;
    docsWriter = new FormatPostingsDocsWriter(state, this);
  }
  void setField(FieldInfo fieldInfo) {
    this.fieldInfo = fieldInfo;
    docsWriter.setField(fieldInfo);
  }
  char[] currentTerm;
  int currentTermStart;
  long freqStart;
  long proxStart;
  @Override
  FormatPostingsDocsConsumer addTerm(char[] text, int start) {
    currentTerm = text;
    currentTermStart = start;
    freqStart = docsWriter.out.getFilePointer();
    if (docsWriter.posWriter.out != null)
      proxStart = docsWriter.posWriter.out.getFilePointer();
    parent.skipListWriter.resetSkip();
    return docsWriter;
  }
  @Override
  void finish() {
  }
  void close() throws IOException {
    docsWriter.close();
  }
}
