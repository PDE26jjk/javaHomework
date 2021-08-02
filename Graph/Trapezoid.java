package Graph;

import java.util.ArrayList;

/**
 * 梯形
 */
public class Trapezoid extends Polygon {

    // 两条平行线实例化
    public Trapezoid(Line line1, Line line2) {
        if (Math.abs((line1.pointA.y - line1.pointB.y) / (line1.pointA.x - line1.pointB.x)
                - (line2.pointA.y - line2.pointB.y) / (line2.pointA.x - line2.pointB.x)
        ) > 1e-2) {
            throw new RuntimeException("不平行的直线无法构成梯形!!");
        }
        this.points = new ArrayList<>();
        this.points.add(line1.pointA);
        this.points.add(line1.pointB);
        this.points.add(line2.pointB);
        this.points.add(line2.pointA);
    }

    // 四个点实例化
    public Trapezoid(Point p1, Point p2, Point p3, Point p4) {
        this(new Line(p1, p2), new Line(p3, p4));
    }
}
