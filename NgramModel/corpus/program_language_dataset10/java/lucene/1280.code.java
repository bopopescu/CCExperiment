package org.apache.lucene.spatial.tier;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryWrapperFilter;
import org.apache.lucene.spatial.geohash.GeoHashDistanceFilter;
import org.apache.lucene.misc.ChainedFilter;
public class DistanceQueryBuilder {
  private static final long serialVersionUID = 1L;
  private final double lat;
  private final double lng;
  private final double miles;
  private final Filter filter;
  final DistanceFilter distanceFilter;
  public DistanceQueryBuilder (double lat, double lng, double miles, 
      String latField, String lngField, String tierFieldPrefix, boolean needPrecise) {
    this.lat = lat;
    this.lng = lng;
    this.miles = miles;
    CartesianPolyFilterBuilder cpf = new CartesianPolyFilterBuilder(tierFieldPrefix);
    Filter cartesianFilter = cpf.getBoundingArea(lat, lng, miles);
    if (needPrecise) {
      filter = distanceFilter = new LatLongDistanceFilter(cartesianFilter, lat, lng, miles, latField, lngField);
    } else {
      filter = cartesianFilter;
      distanceFilter = null;
    }
  }
  public DistanceQueryBuilder (double lat, double lng, double miles, 
      String geoHashFieldPrefix, String tierFieldPrefix, boolean needPrecise){
    this.lat = lat;
    this.lng = lng;
    this.miles = miles;
    CartesianPolyFilterBuilder cpf = new CartesianPolyFilterBuilder(tierFieldPrefix);
    Filter cartesianFilter = cpf.getBoundingArea(lat, lng, miles);
    if (needPrecise) {
      filter = distanceFilter = new GeoHashDistanceFilter(cartesianFilter, lat, lng, miles, geoHashFieldPrefix);
    } else {
      filter = cartesianFilter;
      distanceFilter = null;
    }
  }
  public Filter getFilter() {
    if (distanceFilter != null) {
      distanceFilter.reset();
    }
    return filter;
  }
  public Filter getFilter(Query query) {
    if (distanceFilter != null) {
      distanceFilter.reset();
    }
    QueryWrapperFilter qf = new QueryWrapperFilter(query);
    return new ChainedFilter(new Filter[] {qf, filter},
                             ChainedFilter.AND);
  }
  public DistanceFilter getDistanceFilter() {
    return distanceFilter;
  }
  public Query getQuery(Query query){
    return new ConstantScoreQuery(getFilter(query));
  }
  public double getLat() {
    return lat;
  }
  public double getLng() {
    return lng;
  }
  public double getMiles() {
    return miles;
  }
  @Override
  public String toString() {
    return "DistanceQuery lat: " + lat + " lng: " + lng + " miles: "+ miles;
  }
}
