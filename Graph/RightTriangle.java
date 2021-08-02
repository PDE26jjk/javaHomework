package Graph;

/**
 * 直角三角形
 */
public class RightTriangle extends Triangle {
    // 两点和高初始化，直角在第二点
    public RightTriangle(Point point1, Point point2, double height) {
        super(point1, point2, getPoint3(point1, point2, height));
    }

    // 线段和高初始化，直角在第点B
    public RightTriangle(Line line, double height) {
        super(line.getPointA(), line.getPointB(), getPoint3(line.getPointA(), line.getPointB(), height));
    }

    // 计算第三点
    private static Point getPoint3(Point point1, Point point2, double height) {
        double a = height / Math.sqrt((
                Math.pow((point2.x - point1.x) / (point2.y - point1.y), 2) + 1));
        double b = -(point2.x - point1.x) / (point2.y - point1.y) * a;
        return new Point(point2.x + a, point2.y + b);
    }

}
