import java.awt.*;
import java.io.Serializable;

public class InterPaintDrawer extends InterPaintPosition implements Serializable {


    private int width, height; //width, height
    Color color;
    private int stroke;

    public InterPaintDrawer(types type, int x, int y, int w, int h, int stroke, Color color) { // to draw line
        super(type, x, y);
        this.width = w;
        this.height = h;
        this.stroke = stroke;
        this.color = color;
    }

    public InterPaintDrawer(types type, int x, int y, int w, int h) { // for Eraser
        super(type, x, y);
        this.width = w;
        this.height = h;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getStroke() {
        return stroke;
    }

    public Color getColor() {
        return color;
    }
}
