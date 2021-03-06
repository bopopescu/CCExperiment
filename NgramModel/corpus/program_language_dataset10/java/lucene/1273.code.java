package org.apache.lucene.spatial.geometry.shape;
public class Rectangle implements Geometry2D {
  private Point2D ptMin, ptMax;
  public Rectangle() {
    ptMin=new Point2D(-1, 1);
    ptMax=new Point2D(1, 1);
  }
  public Rectangle(Point2D ptMin, Point2D ptMax) {
    this.ptMin=new Point2D(ptMin);
    this.ptMax=new Point2D(ptMax);
  }
  public Rectangle(double x1, double y1, double x2, double y2) {
    set(x1, y1, x2, y2);
  }
  @Override
  public String toString() {
    return "[" + ptMin + "," + ptMax + "]";
  }
  private void set(double x1, double y1, double x2, double y2) {
    this.ptMin=new Point2D(Math.min(x1, x2), Math.min(y1, y2));
    this.ptMax=new Point2D(Math.max(x1, x2), Math.max(y1, y2));
  }
  public double area() {
    return (ptMax.getX() - ptMin.getX()) * (ptMax.getY() - ptMin.getY());
  }
  public Point2D centroid() {
    return new Point2D( (ptMin.getX() + ptMax.getX()) / 2,
                  (ptMin.getY() + ptMax.getY()) / 2);
  }
  public boolean contains(Point2D p) {
    return p.getX() >= ptMin.getX() && 
      p.getX() <= ptMax.getX() &&
      p.getY() >= ptMin.getY() &&
      p.getY() <= ptMax.getY();
  }
  public void translate(Vector2D v) {
    ptMin.add(v);
    ptMax.add(v);
  }
  Point2D MinPt() {
    return ptMin;
  }
  Point2D MaxPt() {
    return ptMax;
  }
  public IntersectCase intersect(Rectangle r) {
    throw new UnsupportedOperationException();
  }
  public Point2D getMaxPoint() {
    return ptMax;
  }
  public Point2D getMinPoint() {
    return ptMin;
  }
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((ptMax == null) ? 0 : ptMax.hashCode());
    result = prime * result + ((ptMin == null) ? 0 : ptMin.hashCode());
    return result;
  }
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Rectangle other = (Rectangle) obj;
    if (ptMax == null) {
      if (other.ptMax != null)
        return false;
    } else if (!ptMax.equals(other.ptMax))
      return false;
    if (ptMin == null) {
      if (other.ptMin != null)
        return false;
    } else if (!ptMin.equals(other.ptMin))
      return false;
    return true;
  }
}
