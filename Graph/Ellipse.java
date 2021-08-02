package Graph;

import javafx.scene.Group;

/**
 * 椭圆
 */
public class Ellipse extends BaseGraph implements Drawable {


    protected Point anchor;
    protected double width;
    protected double height;

    public Ellipse(Point anchor, double width, double height) {
        this.anchor = anchor;
        this.width = width;
        this.height = height;
    }

    @Override
    public void Draw(Group root) {
        javafx.scene.shape.Ellipse ellipse = new javafx.scene.shape.Ellipse();
        ellipse.setCenterX(anchor.x);
        ellipse.setCenterY(anchor.y);
        ellipse.setRadiusX(width/2);
        ellipse.setRadiusY(height/2);
        root.getChildren().add(ellipse);
    }
    public Point getAnchor() {
        return anchor;
    }

    public void setAnchor(Point anchor) {
        this.anchor = anchor;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

}
