package Graph;

import java.util.ArrayList;

/**
 * 正多边形
 */
public class RegulaPolygon extends Polygon {
    protected Point anchor;

    // 锚点和边数、半径、旋转角度实例化
    public RegulaPolygon(Point anchor, int n, double radius, double beginAngle) {
        this.anchor = anchor;
        double offset = Math.PI * 2 / n;
        this.points = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            double angle = beginAngle + offset * i;
            Point p = new Point(anchor.x + Math.cos(angle)*radius,anchor.y + Math.sin(angle)*radius);
            this.points.add(p);
        }
    }

    // 锚点和边数、半径实例化
    public RegulaPolygon(Point anchor, int n, double radius) {
        this(anchor, n, radius, 0);
    }
}
