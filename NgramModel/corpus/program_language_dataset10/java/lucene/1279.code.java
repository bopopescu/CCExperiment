package org.apache.lucene.spatial.tier;
import java.util.HashMap;
import java.util.Map;
public class DistanceHandler {
  public enum Precision {EXACT, TWOFEET, TWENTYFEET, TWOHUNDREDFEET}
  private Map<Integer,Double> distances;
  private Map<String, Double> distanceLookupCache;
  private Precision precise;
  public DistanceHandler (Map<Integer,Double> distances, Map<String, Double> distanceLookupCache, Precision precise){
    this.distances = distances;
    this.distanceLookupCache = distanceLookupCache;
    this.precise = precise; 
  }
  public static double getPrecision(double x, Precision thisPrecise){
    if(thisPrecise != null){
      double dif = 0;
      switch(thisPrecise) {
        case EXACT: return x;
        case TWOFEET:        dif = x % 0.0001; break;
        case TWENTYFEET:     dif = x % 0.001;  break;
        case TWOHUNDREDFEET: dif = x % 0.01; break;
      }
      return x - dif;
    }
    return x;
  }
  public Precision getPrecision() {
    return precise;
  }
  public double getDistance(int docid, double centerLat, double centerLng, double lat, double lng){
    if(distances == null){
      return DistanceUtils.getInstance().getDistanceMi(centerLat, centerLng, lat, lng);
    }
    Double docd = distances.get( docid );
    if (docd != null){
      return docd.doubleValue();
    }
    if (precise != null) {
      double xLat = getPrecision(lat, precise);
      double xLng = getPrecision(lng, precise);
      String k = Double.valueOf(xLat).toString() +","+ Double.valueOf(xLng).toString();
      Double d = (distanceLookupCache.get(k));
      if (d != null){
        return d.doubleValue();
      }
    }
    return DistanceUtils.getInstance().getDistanceMi(centerLat, centerLng, lat, lng);
  }
  public static void main(String args[]){ 
    DistanceHandler db = new DistanceHandler(new HashMap<Integer,Double>(), new HashMap<String,Double>(), Precision.TWOHUNDREDFEET);
    System.out.println(DistanceHandler.getPrecision(-1234.123456789, db.getPrecision()));
  }
}
