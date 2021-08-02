package Graph;

import java.util.ArrayList;

/**
 * 三角形
 */
public class Triangle extends Polygon {

    // 三个顶点A(x,y)、B(x,y)和C(x,y)坐标实例化
    public Triangle(double x1, double y1, double x2, double y2, double x3, double y3) {
        this.points = new ArrayList<>();
        points.add(new Point(x1, y1));
        points.add(new Point(x2, y2));
        points.add(new Point(x3, y3));
    }

    // 三个顶点A(x,y)、B(x,y)和C(x,y)坐标实例化
    public Triangle(Point point1, Point point2, Point point3) {
        this.points = new ArrayList<>();
        points.add(point1);
        points.add(point2);
        points.add(point3);
    }

}
