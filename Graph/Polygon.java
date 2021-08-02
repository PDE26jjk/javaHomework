package Graph;

import javafx.scene.Group;

import java.util.Arrays;
import java.util.List;

/**
 * 多边形
 */
public class Polygon extends BaseGraph implements Drawable{
    protected List<Point> points;
    protected Polygon(){}
    public Polygon(List<Point> points) {
        this.points = points;
    }

    public Polygon(Point... points) {
        this.points = Arrays.asList(points);
    }

    public List<Point> getPoints() {
        return points;
    }

    @Override
    public void Draw(Group root) {
        javafx.scene.shape.Polygon polygon = new javafx.scene.shape.Polygon();
        for (Point point : getPoints()) {
            polygon.getPoints().addAll(point.x,point.y);
        }
        root.getChildren().add(polygon);
    }
}
