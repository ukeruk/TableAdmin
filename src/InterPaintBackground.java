import javax.swing.*;
import java.io.Serializable;

public class InterPaintBackground extends InterPaint implements Serializable {

    protected ImageIcon background;

    public InterPaintBackground(types type, ImageIcon img)//for DRAW_BACKGROUND
    {
        super(type);
        this.background = img;
    }

    public ImageIcon getBackground() {
        return background;
    }

}
