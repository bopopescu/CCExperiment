package org.apache.solr.schema;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.search.SortField;
import org.apache.lucene.spatial.geohash.GeoHashUtils;
import org.apache.solr.response.TextResponseWriter;
import org.apache.solr.response.XMLWriter;
import org.apache.solr.search.QParser;
import org.apache.solr.search.function.ValueSource;
import org.apache.solr.search.function.distance.DistanceUtils;
import java.io.IOException;
public class GeoHashField extends FieldType {
  @Override
  public SortField getSortField(SchemaField field, boolean top) {
    return getStringSort(field, top);
  }
  @Override
  public void write(XMLWriter xmlWriter, String name, Fieldable f)
          throws IOException {
    xmlWriter.writeStr(name, toExternal(f));
  }
  @Override
  public void write(TextResponseWriter writer, String name, Fieldable f)
          throws IOException {
    writer.writeStr(name, toExternal(f), false);
  }
  @Override
  public String toExternal(Fieldable f) {
    double[] latLon = GeoHashUtils.decode(f.stringValue());
    return latLon[0] + "," + latLon[1];
  }
  @Override
  public String toInternal(String val) {
    double[] latLon = DistanceUtils.parseLatitudeLongitude(null, val);
    return GeoHashUtils.encode(latLon[0], latLon[1]);
  }
  @Override
  public ValueSource getValueSource(SchemaField field, QParser parser) {
    return new StrFieldSource(field.name);
  }
}
