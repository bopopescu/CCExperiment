package org.apache.solr.analysis;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.RamUsageEstimator;
import java.io.IOException;
final class WordDelimiterFilter extends TokenFilter {
  public static final int LOWER = 0x01;
  public static final int UPPER = 0x02;
  public static final int DIGIT = 0x04;
  public static final int SUBWORD_DELIM = 0x08;
  public static final int ALPHA = 0x03;
  public static final int ALPHANUM = 0x07;
  final boolean generateWordParts;
  final boolean generateNumberParts;
  final boolean catenateWords;
  final boolean catenateNumbers;
  final boolean catenateAll;
  final boolean preserveOriginal;
  final CharArraySet protWords;
  private final TermAttribute termAtttribute = (TermAttribute) addAttribute(TermAttribute.class);
  private final OffsetAttribute offsetAttribute = (OffsetAttribute) addAttribute(OffsetAttribute.class);
  private final PositionIncrementAttribute posIncAttribute = (PositionIncrementAttribute) addAttribute(PositionIncrementAttribute.class);
  private final TypeAttribute typeAttribute = (TypeAttribute) addAttribute(TypeAttribute.class);
  private final WordDelimiterIterator iterator;
  private final WordDelimiterConcatenation concat = new WordDelimiterConcatenation();
  private int lastConcatCount = 0;
  private final WordDelimiterConcatenation concatAll = new WordDelimiterConcatenation();
  private int accumPosInc = 0;
  private char savedBuffer[] = new char[1024];
  private int savedStartOffset;
  private int savedEndOffset;
  private String savedType;
  private boolean hasSavedState = false;
  private boolean hasIllegalOffsets = false;
  private boolean hasOutputToken = false;
  private boolean hasOutputFollowingOriginal = false;
  public WordDelimiterFilter(TokenStream in,
                             byte[] charTypeTable,
                             int generateWordParts,
                             int generateNumberParts,
                             int catenateWords,
                             int catenateNumbers,
                             int catenateAll,
                             int splitOnCaseChange,
                             int preserveOriginal,
                             int splitOnNumerics,
                             int stemEnglishPossessive,
                             CharArraySet protWords) {
    super(in);
    this.generateWordParts = generateWordParts != 0;
    this.generateNumberParts = generateNumberParts != 0;
    this.catenateWords = catenateWords != 0;
    this.catenateNumbers = catenateNumbers != 0;
    this.catenateAll = catenateAll != 0;
    this.preserveOriginal = preserveOriginal != 0;
    this.protWords = protWords;
    this.iterator = new WordDelimiterIterator(charTypeTable, splitOnCaseChange != 0, splitOnNumerics != 0, stemEnglishPossessive != 0);
  }
  @Deprecated
  public WordDelimiterFilter(TokenStream in,
                             byte[] charTypeTable,
                             int generateWordParts,
                             int generateNumberParts,
                             int catenateWords,
                             int catenateNumbers,
                             int catenateAll,
                             int splitOnCaseChange,
                             int preserveOriginal,
                             int splitOnNumerics,
                             CharArraySet protWords) {
    this(in, charTypeTable, generateWordParts, generateNumberParts, catenateWords, catenateNumbers, catenateAll, splitOnCaseChange, preserveOriginal, 1, 1, null);
  }
  @Deprecated
  public WordDelimiterFilter(TokenStream in,
                             byte[] charTypeTable,
                             int generateWordParts,
                             int generateNumberParts,
                             int catenateWords,
                             int catenateNumbers,
                             int catenateAll,
                             int splitOnCaseChange,
                             int preserveOriginal) {
    this(in, charTypeTable, generateWordParts, generateNumberParts, catenateWords, catenateNumbers, catenateAll, splitOnCaseChange, preserveOriginal, 1, null);
  }
  public WordDelimiterFilter(TokenStream in,
                             int generateWordParts,
                             int generateNumberParts,
                             int catenateWords,
                             int catenateNumbers,
                             int catenateAll,
                             int splitOnCaseChange,
                             int preserveOriginal,
                             int splitOnNumerics,
                             int stemEnglishPossessive,
                             CharArraySet protWords) {
    this(in, WordDelimiterIterator.DEFAULT_WORD_DELIM_TABLE, generateWordParts, generateNumberParts, catenateWords, catenateNumbers, catenateAll, splitOnCaseChange, preserveOriginal, splitOnNumerics, stemEnglishPossessive, protWords);
  }
  @Deprecated
  public WordDelimiterFilter(TokenStream in,
                             int generateWordParts,
                             int generateNumberParts,
                             int catenateWords,
                             int catenateNumbers,
                             int catenateAll,
                             int splitOnCaseChange,
                             int preserveOriginal,
                             int splitOnNumerics,
                             CharArraySet protWords) {
    this(in, WordDelimiterIterator.DEFAULT_WORD_DELIM_TABLE, generateWordParts, generateNumberParts, catenateWords, catenateNumbers, catenateAll, splitOnCaseChange, preserveOriginal, splitOnNumerics, 1, protWords);
  }
  @Deprecated
  public WordDelimiterFilter(TokenStream in,
                             int generateWordParts,
                             int generateNumberParts,
                             int catenateWords,
                             int catenateNumbers,
                             int catenateAll,
                             int splitOnCaseChange,
                             int preserveOriginal) {
    this(in, WordDelimiterIterator.DEFAULT_WORD_DELIM_TABLE, generateWordParts, generateNumberParts, catenateWords, catenateNumbers, catenateAll, splitOnCaseChange, preserveOriginal);
  }
  @Deprecated
  public WordDelimiterFilter(TokenStream in,
                             byte[] charTypeTable,
                             int generateWordParts,
                             int generateNumberParts,
                             int catenateWords,
                             int catenateNumbers,
                             int catenateAll) {
    this(in, charTypeTable, generateWordParts, generateNumberParts, catenateWords, catenateNumbers, catenateAll, 1, 0, 1, null);
  }
  @Deprecated
  public WordDelimiterFilter(TokenStream in,
                             int generateWordParts,
                             int generateNumberParts,
                             int catenateWords,
                             int catenateNumbers,
                             int catenateAll) {
    this(in, WordDelimiterIterator.DEFAULT_WORD_DELIM_TABLE, generateWordParts, generateNumberParts, catenateWords, catenateNumbers, catenateAll, 1, 0, 1, null);
  }
  public boolean incrementToken() throws IOException {
    while (true) {
      if (!hasSavedState) {
        if (!input.incrementToken()) {
          return false;
        }
        int termLength = termAtttribute.termLength();
        char[] termBuffer = termAtttribute.termBuffer();
        accumPosInc += posIncAttribute.getPositionIncrement();
        iterator.setText(termBuffer, termLength);
        iterator.next();
        if ((iterator.current == 0 && iterator.end == termLength) ||
            (protWords != null && protWords.contains(termBuffer, 0, termLength))) {
          posIncAttribute.setPositionIncrement(accumPosInc);
          accumPosInc = 0;
          return true;
        }
        if (iterator.end == WordDelimiterIterator.DONE && !preserveOriginal) {
          if (posIncAttribute.getPositionIncrement() == 1) {
            accumPosInc--;
          }
          continue;
        }
        saveState();
        hasOutputToken = false;
        hasOutputFollowingOriginal = !preserveOriginal;
        lastConcatCount = 0;
        if (preserveOriginal) {
          posIncAttribute.setPositionIncrement(accumPosInc);
          accumPosInc = 0;
          return true;
        }
      }
      if (iterator.end == WordDelimiterIterator.DONE) {
        if (!concat.isEmpty()) {
          if (flushConcatenation(concat)) {
            return true;
          }
        }
        if (!concatAll.isEmpty()) {
          if (concatAll.subwordCount > lastConcatCount) {
            concatAll.writeAndClear();
            return true;
          }
          concatAll.clear();
        }
        hasSavedState = false;
        continue;
      }
      if (iterator.isSingleWord()) {
        generatePart(true);
        iterator.next();
        return true;
      }
      int wordType = iterator.type();
      if (!concat.isEmpty() && (concat.type & wordType) == 0) {
        if (flushConcatenation(concat)) {
          hasOutputToken = false;
          return true;
        }
        hasOutputToken = false;
      }
      if (shouldConcatenate(wordType)) {
        if (concat.isEmpty()) {
          concat.type = wordType;
        }
        concatenate(concat);
      }
      if (catenateAll) {
        concatenate(concatAll);
      }
      if (shouldGenerateParts(wordType)) {
        generatePart(false);
        iterator.next();
        return true;
      }
      iterator.next();
    }
  }
  @Override
  public void reset() throws IOException {
    super.reset();
    hasSavedState = false;
    concat.clear();
    concatAll.clear();
    accumPosInc = 0;
  }
  private void saveState() {
    savedStartOffset = offsetAttribute.startOffset();
    savedEndOffset = offsetAttribute.endOffset();
    hasIllegalOffsets = (savedEndOffset - savedStartOffset != termAtttribute.termLength());
    savedType = typeAttribute.type();
    if (savedBuffer.length < termAtttribute.termLength()) {
      savedBuffer = new char[ArrayUtil.oversize(termAtttribute.termLength(), RamUsageEstimator.NUM_BYTES_CHAR)];
    }
    System.arraycopy(termAtttribute.termBuffer(), 0, savedBuffer, 0, termAtttribute.termLength());
    iterator.text = savedBuffer;
    hasSavedState = true;
  }
  private boolean flushConcatenation(WordDelimiterConcatenation concatenation) {
    lastConcatCount = concatenation.subwordCount;
    if (concatenation.subwordCount != 1 || !shouldGenerateParts(concatenation.type)) {
      concatenation.writeAndClear();
      return true;
    }
    concatenation.clear();
    return false;
  }
  private boolean shouldConcatenate(int wordType) {
    return (catenateWords && isAlpha(wordType)) || (catenateNumbers && isDigit(wordType));
  }
  private boolean shouldGenerateParts(int wordType) {
    return (generateWordParts && isAlpha(wordType)) || (generateNumberParts && isDigit(wordType));
  }
  private void concatenate(WordDelimiterConcatenation concatenation) {
    if (concatenation.isEmpty()) {
      concatenation.startOffset = savedStartOffset + iterator.current;
    }
    concatenation.append(savedBuffer, iterator.current, iterator.end - iterator.current);
    concatenation.endOffset = savedStartOffset + iterator.end;
  }
  private void generatePart(boolean isSingleWord) {
    clearAttributes();
    termAtttribute.setTermBuffer(savedBuffer, iterator.current, iterator.end - iterator.current);
    int startOffSet = (isSingleWord || !hasIllegalOffsets) ? savedStartOffset + iterator.current : savedStartOffset;
    int endOffSet = (hasIllegalOffsets) ? savedEndOffset : savedStartOffset + iterator.end;
    offsetAttribute.setOffset(startOffSet, endOffSet);
    posIncAttribute.setPositionIncrement(position(false));
    typeAttribute.setType(savedType);
  }
  private int position(boolean inject) {
    int posInc = accumPosInc;
    if (hasOutputToken) {
      accumPosInc = 0;
      return inject ? 0 : Math.max(1, posInc);
    }
    hasOutputToken = true;
    if (!hasOutputFollowingOriginal) {
      hasOutputFollowingOriginal = true;
      return 0;
    }
    accumPosInc = 0;
    return Math.max(1, posInc);
  }
  static boolean isAlpha(int type) {
    return (type & ALPHA) != 0;
  }
  static boolean isDigit(int type) {
    return (type & DIGIT) != 0;
  }
  static boolean isSubwordDelim(int type) {
    return (type & SUBWORD_DELIM) != 0;
  }
  static boolean isUpper(int type) {
    return (type & UPPER) != 0;
  }
  final class WordDelimiterConcatenation {
    final StringBuilder buffer = new StringBuilder();
    int startOffset;
    int endOffset;
    int type;
    int subwordCount;
    void append(char text[], int offset, int length) {
      buffer.append(text, offset, length);
      subwordCount++;
    }
    void write() {
      clearAttributes();
      if (termAtttribute.termLength() < buffer.length()) {
        termAtttribute.resizeTermBuffer(buffer.length());
      }
      char termbuffer[] = termAtttribute.termBuffer();
      buffer.getChars(0, buffer.length(), termbuffer, 0);
      termAtttribute.setTermLength(buffer.length());
      if (hasIllegalOffsets) {
        offsetAttribute.setOffset(savedStartOffset, savedEndOffset);
      }
      else {
        offsetAttribute.setOffset(startOffset, endOffset);
      }
      posIncAttribute.setPositionIncrement(position(true));
      typeAttribute.setType(savedType);
      accumPosInc = 0;
    }
    boolean isEmpty() {
      return buffer.length() == 0;
    }
    void clear() {
      buffer.setLength(0);
      startOffset = endOffset = type = subwordCount = 0;
    }
    void writeAndClear() {
      write();
      clear();
    }
  }
}
