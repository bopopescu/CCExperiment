package org.apache.lucene.document;
import java.io.Reader;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.NumericTokenStream;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.search.NumericRangeQuery; 
import org.apache.lucene.search.NumericRangeFilter; 
import org.apache.lucene.search.SortField; 
import org.apache.lucene.search.FieldCache; 
public final class NumericField extends AbstractField {
  private final NumericTokenStream numericTS;
  public NumericField(String name) {
    this(name, NumericUtils.PRECISION_STEP_DEFAULT, Field.Store.NO, true);
  }
  public NumericField(String name, Field.Store store, boolean index) {
    this(name, NumericUtils.PRECISION_STEP_DEFAULT, store, index);
  }
  public NumericField(String name, int precisionStep) {
    this(name, precisionStep, Field.Store.NO, true);
  }
  public NumericField(String name, int precisionStep, Field.Store store, boolean index) {
    super(name, store, index ? Field.Index.ANALYZED_NO_NORMS : Field.Index.NO, Field.TermVector.NO);
    setOmitTermFreqAndPositions(true);
    numericTS = new NumericTokenStream(precisionStep);
  }
  public TokenStream tokenStreamValue()   {
    return isIndexed() ? numericTS : null;
  }
  @Override
  public byte[] getBinaryValue(byte[] result){
    return null;
  }
  public Reader readerValue() {
    return null;
  }
  public String stringValue()   {
    return (fieldsData == null) ? null : fieldsData.toString();
  }
  public Number getNumericValue() {
    return (Number) fieldsData;
  }
  public NumericField setLongValue(final long value) {
    numericTS.setLongValue(value);
    fieldsData = Long.valueOf(value);
    return this;
  }
  public NumericField setIntValue(final int value) {
    numericTS.setIntValue(value);
    fieldsData = Integer.valueOf(value);
    return this;
  }
  public NumericField setDoubleValue(final double value) {
    numericTS.setDoubleValue(value);
    fieldsData = Double.valueOf(value);
    return this;
  }
  public NumericField setFloatValue(final float value) {
    numericTS.setFloatValue(value);
    fieldsData = Float.valueOf(value);
    return this;
  }
}
