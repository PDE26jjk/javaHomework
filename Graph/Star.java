package Graph;

import java.util.ArrayList;

/**
 * 星型
 */
public class Star extends Polygon {
    protected Point anchor;

    // 中心和边数、半径、旋转角度实例化
    public Star(Point anchor, double radius, double beginAngle) {
        this.anchor = anchor;
        double offset = Math.PI * 2 / 10;
        this.points = new ArrayList<>();
        double inner = 0.3818;

        for (int i = 0; i < 10; i++) {
            double angle = beginAngle + offset * i;
            Point p = null;
            if (i % 2 == 0) {
                p = new Point(anchor.x + Math.cos(angle) * radius, anchor.y + Math.sin(angle) * radius);
            } else {
                p = new Point(anchor.x + Math.cos(angle) * radius * inner, anchor.y + Math.sin(angle) * radius * inner);
            }
            this.points.add(p);
        }
    }

    // 中心和边数、半径实例化
    public Star(Point anchor, double radius) {
        this(anchor, radius, -Math.PI / 10);
    }
}
