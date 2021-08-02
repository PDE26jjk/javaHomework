package Graph;

/**
 * 等边三角形
 */
public class EquilateralTriangle extends Triangle{
    // 两点坐标初始化，逆时针
    public EquilateralTriangle(double x1, double y1, double x2, double y2) {
        this(new Point(x1,y1),new Point(x2,y2));
    }
    // 两点初始化，逆时针
    public EquilateralTriangle(Point point1,Point point2) {
        super(point1, point2, getPoint3(point1, point2));
    }
    // 计算第三点
    private static Point getPoint3(Point point1, Point point2) {
        double height = Math.pow(3,0.5)/2* Math.sqrt(Math.pow((point2.x - point1.x), 2)+Math.pow((point2.y - point1.y), 2));
        point2 = new Point((point1.x + point2.x)/2 , (point1.y + point2.y)/2 );

        double a = height / Math.sqrt((
                Math.pow((point2.x - point1.x) / (point2.y - point1.y), 2) + 1));
        double b = -(point2.x - point1.x) / (point2.y - point1.y) * a;
        return new Point(point2.x + a, point2.y + b);
    }
}
