package Graph;

/**
 * 正方形
 */
public class Square extends Rectangle{
    // 锚点、边长实例化
    public Square(Point anchor, double sideLength) {
        super(anchor,sideLength,sideLength);
    }
    // 边长实例化
    public Square(double sideLength) {
        super(new Point(0,0),sideLength,sideLength);
    }

    public double getSideLength() {
        return width;
    }

}
