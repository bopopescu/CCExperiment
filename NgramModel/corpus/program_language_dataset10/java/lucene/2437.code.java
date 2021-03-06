package org.apache.solr.schema;
public class CopyField {
  private final SchemaField source;
  private final SchemaField destination;
  private final int maxChars;
  public static final int UNLIMITED = 0;
  public CopyField(final SchemaField source, final SchemaField destination) {
    this(source, destination, UNLIMITED);
  }
  public CopyField(final SchemaField source, final SchemaField destination,
      final int maxChars) {
    if (source == null || destination == null) {
      throw new IllegalArgumentException(
          "Source or Destination SchemaField can't be NULL.");
    }
    if (maxChars < 0) {
      throw new IllegalArgumentException(
          "Attribute maxChars can't have a negative value.");
    }
    this.source = source;
    this.destination = destination;
    this.maxChars = maxChars;
  }
  public String getLimitedValue( final String val ){
    return maxChars == UNLIMITED || val.length() < maxChars ?
        val : val.substring( 0, maxChars );
  }
  public SchemaField getSource() {
    return source;
  }
  public SchemaField getDestination() {
    return destination;
  }
  public int getMaxChars() {
    return maxChars;
  }
}
