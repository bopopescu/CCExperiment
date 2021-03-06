package org.apache.lucene.spatial.tier.projections;
public class CartesianTierPlotter {
  public static final String DEFALT_FIELD_PREFIX = "_tier_";
  final int tierLevel;
  int tierLength;
  int tierBoxes;
  int tierVerticalPosDivider;
  final IProjector projector;
  final String fieldPrefix;
  Double idd = Double.valueOf(180);
  public CartesianTierPlotter (int tierLevel, IProjector projector, String fieldPrefix) {
    this.tierLevel  = tierLevel;
    this.projector = projector;
    this.fieldPrefix = fieldPrefix;
    setTierLength();
    setTierBoxes();
    setTierVerticalPosDivider();
  }
  private void setTierLength (){
    this.tierLength = (int) Math.pow(2 , this.tierLevel);
  }
  private void setTierBoxes () {
    this.tierBoxes = (int)Math.pow(this.tierLength, 2);
  }
  private void setTierVerticalPosDivider() {
    tierVerticalPosDivider = Double.valueOf(Math.ceil(
          Math.log10(Integer.valueOf(this.tierLength).doubleValue()))).intValue();
    tierVerticalPosDivider = (int)Math.pow(10, tierVerticalPosDivider );
  }
  public double getTierVerticalPosDivider(){
    return tierVerticalPosDivider;
  }
  public double getTierBoxId (double latitude, double longitude) {
    double[] coords = projector.coords(latitude, longitude);
    double id = getBoxId(coords[0]) + (getBoxId(coords[1]) / tierVerticalPosDivider);
    return id ;
  }
  private double getBoxId (double coord){
    return Math.floor(coord / (idd / this.tierLength));
  }
  @SuppressWarnings("unused")
  private double getBoxId (double coord, int tierLen){
    return Math.floor(coord / (idd / tierLen) );
  }
  public String getTierFieldName (){
    return fieldPrefix + this.tierLevel;
  }
  public String getTierFieldName (int tierId){
    return fieldPrefix + tierId;
  }
  public int bestFit(double miles){
    int circ = 28892;
    double r = miles / 2.0;
    double corner = r - Math.sqrt(Math.pow(r, 2) / 2.0d);
    double times = circ / corner;
    int bestFit =  (int)Math.ceil(log2(times)) + 1;
    if (bestFit > 15) {
      return 15;
    }
    return bestFit;
  }
  public double log2(double value) {
    return Math.log(value) / Math.log(2);
  }
}
