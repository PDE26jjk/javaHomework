package Graph;

/**
 * 直线
 */
public class Line {
    protected Point pointA;
    protected Point pointB;

    public Point getPointA() {
        return pointA;
    }

    public void setPointA(Point pointA) {
        this.pointA = pointA;
    }

    public Point getPointB() {
        return pointB;
    }

    public void setPointB(Point pointB) {
        this.pointB = pointB;
    }

    public Line(Point pointA, Point pointB) {
        this.pointA = pointA;
        this.pointB = pointB;
    }

    public Line(double x1,double y1,double x2, double y2) {
        this.pointA = new Point(x1,y1);
        this.pointB = new Point(x2,y2);
    }
}
