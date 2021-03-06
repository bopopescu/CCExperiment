package org.apache.cassandra.thrift;
import org.apache.commons.lang.builder.HashCodeBuilder;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.thrift.*;
import org.apache.thrift.async.*;
import org.apache.thrift.meta_data.*;
import org.apache.thrift.transport.*;
import org.apache.thrift.protocol.*;
public class CounterMutation implements TBase<CounterMutation, CounterMutation._Fields>, java.io.Serializable, Cloneable {
  private static final TStruct STRUCT_DESC = new TStruct("CounterMutation");
  private static final TField COUNTER_FIELD_DESC = new TField("counter", TType.STRUCT, (short)1);
  private static final TField DELETION_FIELD_DESC = new TField("deletion", TType.STRUCT, (short)2);
  public Counter counter;
  public CounterDeletion deletion;
  public enum _Fields implements TFieldIdEnum {
    COUNTER((short)1, "counter"),
    DELETION((short)2, "deletion");
    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();
    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: 
          return COUNTER;
        case 2: 
          return DELETION;
        default:
          return null;
      }
    }
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }
    public static _Fields findByName(String name) {
      return byName.get(name);
    }
    private final short _thriftId;
    private final String _fieldName;
    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }
    public short getThriftFieldId() {
      return _thriftId;
    }
    public String getFieldName() {
      return _fieldName;
    }
  }
  public static final Map<_Fields, FieldMetaData> metaDataMap;
  static {
    Map<_Fields, FieldMetaData> tmpMap = new EnumMap<_Fields, FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.COUNTER, new FieldMetaData("counter", TFieldRequirementType.OPTIONAL, 
        new StructMetaData(TType.STRUCT, Counter.class)));
    tmpMap.put(_Fields.DELETION, new FieldMetaData("deletion", TFieldRequirementType.OPTIONAL, 
        new StructMetaData(TType.STRUCT, CounterDeletion.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    FieldMetaData.addStructMetaDataMap(CounterMutation.class, metaDataMap);
  }
  public CounterMutation() {
  }
  public CounterMutation(CounterMutation other) {
    if (other.isSetCounter()) {
      this.counter = new Counter(other.counter);
    }
    if (other.isSetDeletion()) {
      this.deletion = new CounterDeletion(other.deletion);
    }
  }
  public CounterMutation deepCopy() {
    return new CounterMutation(this);
  }
  @Override
  public void clear() {
    this.counter = null;
    this.deletion = null;
  }
  public Counter getCounter() {
    return this.counter;
  }
  public CounterMutation setCounter(Counter counter) {
    this.counter = counter;
    return this;
  }
  public void unsetCounter() {
    this.counter = null;
  }
  public boolean isSetCounter() {
    return this.counter != null;
  }
  public void setCounterIsSet(boolean value) {
    if (!value) {
      this.counter = null;
    }
  }
  public CounterDeletion getDeletion() {
    return this.deletion;
  }
  public CounterMutation setDeletion(CounterDeletion deletion) {
    this.deletion = deletion;
    return this;
  }
  public void unsetDeletion() {
    this.deletion = null;
  }
  public boolean isSetDeletion() {
    return this.deletion != null;
  }
  public void setDeletionIsSet(boolean value) {
    if (!value) {
      this.deletion = null;
    }
  }
  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case COUNTER:
      if (value == null) {
        unsetCounter();
      } else {
        setCounter((Counter)value);
      }
      break;
    case DELETION:
      if (value == null) {
        unsetDeletion();
      } else {
        setDeletion((CounterDeletion)value);
      }
      break;
    }
  }
  public Object getFieldValue(_Fields field) {
    switch (field) {
    case COUNTER:
      return getCounter();
    case DELETION:
      return getDeletion();
    }
    throw new IllegalStateException();
  }
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }
    switch (field) {
    case COUNTER:
      return isSetCounter();
    case DELETION:
      return isSetDeletion();
    }
    throw new IllegalStateException();
  }
  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof CounterMutation)
      return this.equals((CounterMutation)that);
    return false;
  }
  public boolean equals(CounterMutation that) {
    if (that == null)
      return false;
    boolean this_present_counter = true && this.isSetCounter();
    boolean that_present_counter = true && that.isSetCounter();
    if (this_present_counter || that_present_counter) {
      if (!(this_present_counter && that_present_counter))
        return false;
      if (!this.counter.equals(that.counter))
        return false;
    }
    boolean this_present_deletion = true && this.isSetDeletion();
    boolean that_present_deletion = true && that.isSetDeletion();
    if (this_present_deletion || that_present_deletion) {
      if (!(this_present_deletion && that_present_deletion))
        return false;
      if (!this.deletion.equals(that.deletion))
        return false;
    }
    return true;
  }
  @Override
  public int hashCode() {
    HashCodeBuilder builder = new HashCodeBuilder();
    boolean present_counter = true && (isSetCounter());
    builder.append(present_counter);
    if (present_counter)
      builder.append(counter);
    boolean present_deletion = true && (isSetDeletion());
    builder.append(present_deletion);
    if (present_deletion)
      builder.append(deletion);
    return builder.toHashCode();
  }
  public int compareTo(CounterMutation other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }
    int lastComparison = 0;
    CounterMutation typedOther = (CounterMutation)other;
    lastComparison = Boolean.valueOf(isSetCounter()).compareTo(typedOther.isSetCounter());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetCounter()) {
      lastComparison = TBaseHelper.compareTo(this.counter, typedOther.counter);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetDeletion()).compareTo(typedOther.isSetDeletion());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetDeletion()) {
      lastComparison = TBaseHelper.compareTo(this.deletion, typedOther.deletion);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }
  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }
  public void read(TProtocol iprot) throws TException {
    TField field;
    iprot.readStructBegin();
    while (true)
    {
      field = iprot.readFieldBegin();
      if (field.type == TType.STOP) { 
        break;
      }
      switch (field.id) {
        case 1: 
          if (field.type == TType.STRUCT) {
            this.counter = new Counter();
            this.counter.read(iprot);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case 2: 
          if (field.type == TType.STRUCT) {
            this.deletion = new CounterDeletion();
            this.deletion.read(iprot);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        default:
          TProtocolUtil.skip(iprot, field.type);
      }
      iprot.readFieldEnd();
    }
    iprot.readStructEnd();
    validate();
  }
  public void write(TProtocol oprot) throws TException {
    validate();
    oprot.writeStructBegin(STRUCT_DESC);
    if (this.counter != null) {
      if (isSetCounter()) {
        oprot.writeFieldBegin(COUNTER_FIELD_DESC);
        this.counter.write(oprot);
        oprot.writeFieldEnd();
      }
    }
    if (this.deletion != null) {
      if (isSetDeletion()) {
        oprot.writeFieldBegin(DELETION_FIELD_DESC);
        this.deletion.write(oprot);
        oprot.writeFieldEnd();
      }
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("CounterMutation(");
    boolean first = true;
    if (isSetCounter()) {
      sb.append("counter:");
      if (this.counter == null) {
        sb.append("null");
      } else {
        sb.append(this.counter);
      }
      first = false;
    }
    if (isSetDeletion()) {
      if (!first) sb.append(", ");
      sb.append("deletion:");
      if (this.deletion == null) {
        sb.append("null");
      } else {
        sb.append(this.deletion);
      }
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }
  public void validate() throws TException {
  }
}
