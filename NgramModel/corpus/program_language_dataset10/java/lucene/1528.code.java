package org.apache.lucene.index;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.AbstractField;
import org.apache.lucene.document.CompressionTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.AlreadyClosedException;
import org.apache.lucene.store.BufferedIndexInput;
import org.apache.lucene.util.CloseableThreadLocal;
import java.io.IOException;
import java.io.Reader;
import java.util.zip.DataFormatException;
final class FieldsReader implements Cloneable {
  private final FieldInfos fieldInfos;
  private final IndexInput cloneableFieldsStream;
  private final IndexInput fieldsStream;
  private final IndexInput cloneableIndexStream;
  private final IndexInput indexStream;
  private int numTotalDocs;
  private int size;
  private boolean closed;
  private final int format;
  private final int formatSize;
  private int docStoreOffset;
  private CloseableThreadLocal<IndexInput> fieldsStreamTL = new CloseableThreadLocal<IndexInput>();
  private boolean isOriginal = false;
  @Override
  public Object clone() {
    ensureOpen();
    return new FieldsReader(fieldInfos, numTotalDocs, size, format, formatSize, docStoreOffset, cloneableFieldsStream, cloneableIndexStream);
  }
  private FieldsReader(FieldInfos fieldInfos, int numTotalDocs, int size, int format, int formatSize,
                       int docStoreOffset, IndexInput cloneableFieldsStream, IndexInput cloneableIndexStream) {
    this.fieldInfos = fieldInfos;
    this.numTotalDocs = numTotalDocs;
    this.size = size;
    this.format = format;
    this.formatSize = formatSize;
    this.docStoreOffset = docStoreOffset;
    this.cloneableFieldsStream = cloneableFieldsStream;
    this.cloneableIndexStream = cloneableIndexStream;
    fieldsStream = (IndexInput) cloneableFieldsStream.clone();
    indexStream = (IndexInput) cloneableIndexStream.clone();
  }
  FieldsReader(Directory d, String segment, FieldInfos fn) throws IOException {
    this(d, segment, fn, BufferedIndexInput.BUFFER_SIZE, -1, 0);
  }
  FieldsReader(Directory d, String segment, FieldInfos fn, int readBufferSize) throws IOException {
    this(d, segment, fn, readBufferSize, -1, 0);
  }
  FieldsReader(Directory d, String segment, FieldInfos fn, int readBufferSize, int docStoreOffset, int size) throws IOException {
    boolean success = false;
    isOriginal = true;
    try {
      fieldInfos = fn;
      cloneableFieldsStream = d.openInput(IndexFileNames.segmentFileName(segment, IndexFileNames.FIELDS_EXTENSION), readBufferSize);
      cloneableIndexStream = d.openInput(IndexFileNames.segmentFileName(segment, IndexFileNames.FIELDS_INDEX_EXTENSION), readBufferSize);
      int firstInt = cloneableIndexStream.readInt();
      if (firstInt == 0)
        format = 0;
      else
        format = firstInt;
      if (format > FieldsWriter.FORMAT_CURRENT)
        throw new CorruptIndexException("Incompatible format version: " + format + " expected " 
                                        + FieldsWriter.FORMAT_CURRENT + " or lower");
      if (format > FieldsWriter.FORMAT)
        formatSize = 4;
      else
        formatSize = 0;
      if (format < FieldsWriter.FORMAT_VERSION_UTF8_LENGTH_IN_BYTES)
        cloneableFieldsStream.setModifiedUTF8StringsMode();
      fieldsStream = (IndexInput) cloneableFieldsStream.clone();
      final long indexSize = cloneableIndexStream.length()-formatSize;
      if (docStoreOffset != -1) {
        this.docStoreOffset = docStoreOffset;
        this.size = size;
        assert ((int) (indexSize / 8)) >= size + this.docStoreOffset: "indexSize=" + indexSize + " size=" + size + " docStoreOffset=" + docStoreOffset;
      } else {
        this.docStoreOffset = 0;
        this.size = (int) (indexSize >> 3);
      }
      indexStream = (IndexInput) cloneableIndexStream.clone();
      numTotalDocs = (int) (indexSize >> 3);
      success = true;
    } finally {
      if (!success) {
        close();
      }
    }
  }
  private void ensureOpen() throws AlreadyClosedException {
    if (closed) {
      throw new AlreadyClosedException("this FieldsReader is closed");
    }
  }
  final void close() throws IOException {
    if (!closed) {
      if (fieldsStream != null) {
        fieldsStream.close();
      }
      if (isOriginal) {
        if (cloneableFieldsStream != null) {
          cloneableFieldsStream.close();
        }
        if (cloneableIndexStream != null) {
          cloneableIndexStream.close();
        }
      }
      if (indexStream != null) {
        indexStream.close();
      }
      fieldsStreamTL.close();
      closed = true;
    }
  }
  final int size() {
    return size;
  }
  private final void seekIndex(int docID) throws IOException {
    indexStream.seek(formatSize + (docID + docStoreOffset) * 8L);
  }
  boolean canReadRawDocs() {
    return format >= FieldsWriter.FORMAT_LUCENE_3_0_NO_COMPRESSED_FIELDS;
  }
  final Document doc(int n, FieldSelector fieldSelector) throws CorruptIndexException, IOException {
    seekIndex(n);
    long position = indexStream.readLong();
    fieldsStream.seek(position);
    Document doc = new Document();
    int numFields = fieldsStream.readVInt();
    for (int i = 0; i < numFields; i++) {
      int fieldNumber = fieldsStream.readVInt();
      FieldInfo fi = fieldInfos.fieldInfo(fieldNumber);
      FieldSelectorResult acceptField = fieldSelector == null ? FieldSelectorResult.LOAD : fieldSelector.accept(fi.name);
      byte bits = fieldsStream.readByte();
      assert bits <= FieldsWriter.FIELD_IS_COMPRESSED + FieldsWriter.FIELD_IS_TOKENIZED + FieldsWriter.FIELD_IS_BINARY;
      boolean compressed = (bits & FieldsWriter.FIELD_IS_COMPRESSED) != 0;
      assert (compressed ? (format < FieldsWriter.FORMAT_LUCENE_3_0_NO_COMPRESSED_FIELDS) : true)
        : "compressed fields are only allowed in indexes of version <= 2.9";
      boolean tokenize = (bits & FieldsWriter.FIELD_IS_TOKENIZED) != 0;
      boolean binary = (bits & FieldsWriter.FIELD_IS_BINARY) != 0;
      if (acceptField.equals(FieldSelectorResult.LOAD)) {
        addField(doc, fi, binary, compressed, tokenize);
      }
      else if (acceptField.equals(FieldSelectorResult.LOAD_AND_BREAK)){
        addField(doc, fi, binary, compressed, tokenize);
        break;
      }
      else if (acceptField.equals(FieldSelectorResult.LAZY_LOAD)) {
        addFieldLazy(doc, fi, binary, compressed, tokenize);
      }
      else if (acceptField.equals(FieldSelectorResult.SIZE)){
        skipField(binary, compressed, addFieldSize(doc, fi, binary, compressed));
      }
      else if (acceptField.equals(FieldSelectorResult.SIZE_AND_BREAK)){
        addFieldSize(doc, fi, binary, compressed);
        break;
      }
      else {
        skipField(binary, compressed);
      }
    }
    return doc;
  }
  final IndexInput rawDocs(int[] lengths, int startDocID, int numDocs) throws IOException {
    seekIndex(startDocID);
    long startOffset = indexStream.readLong();
    long lastOffset = startOffset;
    int count = 0;
    while (count < numDocs) {
      final long offset;
      final int docID = docStoreOffset + startDocID + count + 1;
      assert docID <= numTotalDocs;
      if (docID < numTotalDocs) 
        offset = indexStream.readLong();
      else
        offset = fieldsStream.length();
      lengths[count++] = (int) (offset-lastOffset);
      lastOffset = offset;
    }
    fieldsStream.seek(startOffset);
    return fieldsStream;
  }
  private void skipField(boolean binary, boolean compressed) throws IOException {
    skipField(binary, compressed, fieldsStream.readVInt());
  }
  private void skipField(boolean binary, boolean compressed, int toRead) throws IOException {
   if (format >= FieldsWriter.FORMAT_VERSION_UTF8_LENGTH_IN_BYTES || binary || compressed) {
     fieldsStream.seek(fieldsStream.getFilePointer() + toRead);
   } else {
     fieldsStream.skipChars(toRead);
   }
  }
  private void addFieldLazy(Document doc, FieldInfo fi, boolean binary, boolean compressed, boolean tokenize) throws IOException {
    if (binary) {
      int toRead = fieldsStream.readVInt();
      long pointer = fieldsStream.getFilePointer();
      doc.add(new LazyField(fi.name, Field.Store.YES, toRead, pointer, binary, compressed));
      fieldsStream.seek(pointer + toRead);
    } else {
      Field.Store store = Field.Store.YES;
      Field.Index index = Field.Index.toIndex(fi.isIndexed, tokenize);
      Field.TermVector termVector = Field.TermVector.toTermVector(fi.storeTermVector, fi.storeOffsetWithTermVector, fi.storePositionWithTermVector);
      AbstractField f;
      if (compressed) {
        int toRead = fieldsStream.readVInt();
        long pointer = fieldsStream.getFilePointer();
        f = new LazyField(fi.name, store, toRead, pointer, binary, compressed);
        fieldsStream.seek(pointer + toRead);
        f.setOmitNorms(fi.omitNorms);
        f.setOmitTermFreqAndPositions(fi.omitTermFreqAndPositions);
      } else {
        int length = fieldsStream.readVInt();
        long pointer = fieldsStream.getFilePointer();
        if (format >= FieldsWriter.FORMAT_VERSION_UTF8_LENGTH_IN_BYTES) {
          fieldsStream.seek(pointer+length);
        } else {
          fieldsStream.skipChars(length);
        }
        f = new LazyField(fi.name, store, index, termVector, length, pointer, binary, compressed);
        f.setOmitNorms(fi.omitNorms);
        f.setOmitTermFreqAndPositions(fi.omitTermFreqAndPositions);
      }
      doc.add(f);
    }
  }
  private void addField(Document doc, FieldInfo fi, boolean binary, boolean compressed, boolean tokenize) throws CorruptIndexException, IOException {
    if (binary) {
      int toRead = fieldsStream.readVInt();
      final byte[] b = new byte[toRead];
      fieldsStream.readBytes(b, 0, b.length);
      if (compressed) {
        doc.add(new Field(fi.name, uncompress(b)));
      } else {
        doc.add(new Field(fi.name, b));
      }
    } else {
      Field.Store store = Field.Store.YES;
      Field.Index index = Field.Index.toIndex(fi.isIndexed, tokenize);
      Field.TermVector termVector = Field.TermVector.toTermVector(fi.storeTermVector, fi.storeOffsetWithTermVector, fi.storePositionWithTermVector);
      AbstractField f;
      if (compressed) {
        int toRead = fieldsStream.readVInt();
        final byte[] b = new byte[toRead];
        fieldsStream.readBytes(b, 0, b.length);
        f = new Field(fi.name,      
                false,
                new String(uncompress(b), "UTF-8"), 
                store,
                index,
                termVector);
        f.setOmitTermFreqAndPositions(fi.omitTermFreqAndPositions);
        f.setOmitNorms(fi.omitNorms);
      } else {
        f = new Field(fi.name,     
         false,
                fieldsStream.readString(), 
                store,
                index,
                termVector);
        f.setOmitTermFreqAndPositions(fi.omitTermFreqAndPositions);
        f.setOmitNorms(fi.omitNorms);
      }
      doc.add(f);
    }
  }
  private int addFieldSize(Document doc, FieldInfo fi, boolean binary, boolean compressed) throws IOException {
    int size = fieldsStream.readVInt(), bytesize = binary || compressed ? size : 2*size;
    byte[] sizebytes = new byte[4];
    sizebytes[0] = (byte) (bytesize>>>24);
    sizebytes[1] = (byte) (bytesize>>>16);
    sizebytes[2] = (byte) (bytesize>>> 8);
    sizebytes[3] = (byte)  bytesize      ;
    doc.add(new Field(fi.name, sizebytes));
    return size;
  }
  private class LazyField extends AbstractField implements Fieldable {
    private int toRead;
    private long pointer;
    @Deprecated
    private boolean isCompressed;
    public LazyField(String name, Field.Store store, int toRead, long pointer, boolean isBinary, boolean isCompressed) {
      super(name, store, Field.Index.NO, Field.TermVector.NO);
      this.toRead = toRead;
      this.pointer = pointer;
      this.isBinary = isBinary;
      if (isBinary)
        binaryLength = toRead;
      lazy = true;
      this.isCompressed = isCompressed;
    }
    public LazyField(String name, Field.Store store, Field.Index index, Field.TermVector termVector, int toRead, long pointer, boolean isBinary, boolean isCompressed) {
      super(name, store, index, termVector);
      this.toRead = toRead;
      this.pointer = pointer;
      this.isBinary = isBinary;
      if (isBinary)
        binaryLength = toRead;
      lazy = true;
      this.isCompressed = isCompressed;
    }
    private IndexInput getFieldStream() {
      IndexInput localFieldsStream = fieldsStreamTL.get();
      if (localFieldsStream == null) {
        localFieldsStream = (IndexInput) cloneableFieldsStream.clone();
        fieldsStreamTL.set(localFieldsStream);
      }
      return localFieldsStream;
    }
    public Reader readerValue() {
      ensureOpen();
      return null;
    }
    public TokenStream tokenStreamValue() {
      ensureOpen();
      return null;
    }
    public String stringValue() {
      ensureOpen();
      if (isBinary)
        return null;
      else {
        if (fieldsData == null) {
          IndexInput localFieldsStream = getFieldStream();
          try {
            localFieldsStream.seek(pointer);
            if (isCompressed) {
              final byte[] b = new byte[toRead];
              localFieldsStream.readBytes(b, 0, b.length);
              fieldsData = new String(uncompress(b), "UTF-8");
            } else {
              if (format >= FieldsWriter.FORMAT_VERSION_UTF8_LENGTH_IN_BYTES) {
                byte[] bytes = new byte[toRead];
                localFieldsStream.readBytes(bytes, 0, toRead);
                fieldsData = new String(bytes, "UTF-8");
              } else {
                char[] chars = new char[toRead];
                localFieldsStream.readChars(chars, 0, toRead);
                fieldsData = new String(chars);
              }
            }
          } catch (IOException e) {
            throw new FieldReaderException(e);
          }
        }
        return (String) fieldsData;
      }
    }
    public long getPointer() {
      ensureOpen();
      return pointer;
    }
    public void setPointer(long pointer) {
      ensureOpen();
      this.pointer = pointer;
    }
    public int getToRead() {
      ensureOpen();
      return toRead;
    }
    public void setToRead(int toRead) {
      ensureOpen();
      this.toRead = toRead;
    }
    @Override
    public byte[] getBinaryValue(byte[] result) {
      ensureOpen();
      if (isBinary) {
        if (fieldsData == null) {
          final byte[] b;
          if (result == null || result.length < toRead)
            b = new byte[toRead];
          else
            b = result;
          IndexInput localFieldsStream = getFieldStream();
          try {
            localFieldsStream.seek(pointer);
            localFieldsStream.readBytes(b, 0, toRead);
            if (isCompressed == true) {
              fieldsData = uncompress(b);
            } else {
              fieldsData = b;
            }
          } catch (IOException e) {
            throw new FieldReaderException(e);
          }
          binaryOffset = 0;
          binaryLength = toRead;
        }
        return (byte[]) fieldsData;
      } else
        return null;     
    }
  }
  private byte[] uncompress(byte[] b)
          throws CorruptIndexException {
    try {
      return CompressionTools.decompress(b);
    } catch (DataFormatException e) {
      CorruptIndexException newException = new CorruptIndexException("field data are in wrong format: " + e.toString());
      newException.initCause(e);
      throw newException;
    }
  }
}
