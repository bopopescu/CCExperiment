package org.apache.log4j.pattern;
public final class FormattingInfo {
  private static final char[] SPACES =
    new char[] { ' ', ' ', ' ', ' ', ' ', ' ', ' ', ' ' };
  private static final FormattingInfo DEFAULT =
    new FormattingInfo(false, 0, Integer.MAX_VALUE);
  private final int minLength;
  private final int maxLength;
  private final boolean leftAlign;
  public FormattingInfo(
    final boolean leftAlign, final int minLength, final int maxLength) {
    this.leftAlign = leftAlign;
    this.minLength = minLength;
    this.maxLength = maxLength;
  }
  public static FormattingInfo getDefault() {
    return DEFAULT;
  }
  public boolean isLeftAligned() {
    return leftAlign;
  }
  public int getMinLength() {
    return minLength;
  }
  public int getMaxLength() {
    return maxLength;
  }
  public void format(final int fieldStart, final StringBuffer buffer) {
    final int rawLength = buffer.length() - fieldStart;
    if (rawLength > maxLength) {
      buffer.delete(fieldStart, buffer.length() - maxLength);
    } else if (rawLength < minLength) {
      if (leftAlign) {
        final int fieldEnd = buffer.length();
        buffer.setLength(fieldStart + minLength);
        for (int i = fieldEnd; i < buffer.length(); i++) {
          buffer.setCharAt(i, ' ');
        }
      } else {
        int padLength = minLength - rawLength;
        for (; padLength > 8; padLength -= 8) {
          buffer.insert(fieldStart, SPACES);
        }
        buffer.insert(fieldStart, SPACES, 0, padLength);
      }
    }
  }
}
