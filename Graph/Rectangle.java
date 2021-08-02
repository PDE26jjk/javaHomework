package Graph;

import java.util.ArrayList;

public class Rectangle extends Polygon {


    // 宽
    protected double width;
    // 高
    protected double height;
    // 左上角坐标
    protected Point anchor;

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public Point getAnchor() {
        return anchor;
    }

    // 边长度和高度实例化
    public Rectangle(Point anchor, double width, double height) {
        this.width = width;
        this.height = height;
        this.anchor = anchor;
        initPoints();

    }

    private void initPoints() {
        if (this.points == null) {
            this.points = new ArrayList<>();
        }
        this.points.clear();
        this.points.add(anchor);
        this.points.add(new Point(anchor.x + width, anchor.y));
        this.points.add(new Point(anchor.x + width, anchor.y + height));
        this.points.add(new Point(anchor.x, anchor.y + height));
    }

    // 坐标实例化
    public Rectangle(double right, double left, double top, double bottom) {
        this.width = right - left;
        this.height = bottom - top;
        anchor = new Point(left, top);
        initPoints();
    }

    public void setAnchor(Point anchor) {
        this.anchor = anchor;
        initPoints();
    }
}
