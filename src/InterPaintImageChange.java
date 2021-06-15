import java.awt.*;
import java.io.Serializable;

public class InterPaintImageChange extends InterPaintPosition implements Serializable {

    private String imageID;
    private Dimension dim;

    public InterPaintImageChange(types type, String mvimg)// for DELETE_IMAGE, CHOOSE_IMAGE, UNCHOOSE_IMAGE
    {
        super(type);
        this.imageID = mvimg;
    }

    public InterPaintImageChange(types type, String mvimg, int x, int y)// for MOVE_IMAGE
    {
        super(type, x, y);
        this.imageID = mvimg;
    }

    public InterPaintImageChange(types type, String mvimg, int x, int y, Dimension dim)// for MOVE_IMAGE
    {
        super(type, x, y);
        this.imageID = mvimg;
        this.dim = dim;
    }

    public Dimension getDim() {
        return dim;
    }

    public String getImageID() {
        return imageID;
    }

}
