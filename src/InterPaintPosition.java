import java.io.Serializable;

public class InterPaintPosition extends InterPaint implements Serializable {

    protected int x, y;

    public InterPaintPosition(types type, int x, int y)
    {
        super(type);
        this.x = x;
        this.y = y;
    }

    public InterPaintPosition(types type)// for ASK_VAR
    {
        super(type);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
