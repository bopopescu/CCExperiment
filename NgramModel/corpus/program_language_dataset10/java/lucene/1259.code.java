package org.apache.lucene.spatial.geohash;
import java.io.IOException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.FilteredDocIdSet;
import org.apache.lucene.spatial.tier.DistanceFilter;
import org.apache.lucene.spatial.tier.DistanceUtils;
public class GeoHashDistanceFilter extends DistanceFilter {
  private static final long serialVersionUID = 1L;
  private double lat;
  private double lng;
  private String geoHashField;
  public GeoHashDistanceFilter(Filter startingFilter, double lat, double lng, double miles, String geoHashField) {
    super(startingFilter, miles);
    this.lat = lat;
    this.lng = lng;
    this.geoHashField = geoHashField;
  }
  @Override
  public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
    final String[] geoHashValues = FieldCache.DEFAULT.getStrings(reader, geoHashField);
    final int docBase = nextDocBase;
    nextDocBase += reader.maxDoc();
    return new FilteredDocIdSet(startingFilter.getDocIdSet(reader)) {
      @Override
      public boolean match(int doc) {
        String geoHash = geoHashValues[doc];
        double[] coords = GeoHashUtils.decode(geoHash);
        double x = coords[0];
        double y = coords[1];
        Double cachedDistance = distanceLookupCache.get(geoHash);
        double d;
        if (cachedDistance != null) {
          d = cachedDistance.doubleValue();
        } else {
          d = DistanceUtils.getInstance().getDistanceMi(lat, lng, x, y);
          distanceLookupCache.put(geoHash, d);
        }
        if (d < distance){
          distances.put(doc+docBase, d);
          return true;
        } else {
          return false;
        }
      }
    };
  }
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof GeoHashDistanceFilter)) return false;
    GeoHashDistanceFilter other = (GeoHashDistanceFilter) o;
    if (!this.startingFilter.equals(other.startingFilter) ||
        this.distance != other.distance ||
        this.lat != other.lat ||
        this.lng != other.lng ||
        !this.geoHashField.equals(other.geoHashField) ) {
      return false;
    }
    return true;
  }
  @Override
  public int hashCode() {
    int h = Double.valueOf(distance).hashCode();
    h ^= startingFilter.hashCode();
    h ^= Double.valueOf(lat).hashCode();
    h ^= Double.valueOf(lng).hashCode();
    h ^= geoHashField.hashCode();
    return h;
  }
}
