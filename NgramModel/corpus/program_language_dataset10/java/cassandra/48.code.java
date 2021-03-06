package org.apache.cassandra.thrift;
import java.util.Map;
import java.util.HashMap;
import org.apache.thrift.TEnum;
public enum IndexOperator implements TEnum {
  EQ(0),
  GTE(1),
  GT(2),
  LTE(3),
  LT(4);
  private final int value;
  private IndexOperator(int value) {
    this.value = value;
  }
  public int getValue() {
    return value;
  }
  public static IndexOperator findByValue(int value) { 
    switch (value) {
      case 0:
        return EQ;
      case 1:
        return GTE;
      case 2:
        return GT;
      case 3:
        return LTE;
      case 4:
        return LT;
      default:
        return null;
    }
  }
}
