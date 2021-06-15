import java.io.Serializable;

public class InterPaintImageDraw extends InterPaint implements Serializable {

    private BufferlessActor img;

    public InterPaintImageDraw(types type, BufferlessActor img) {
        super(type);
        this.img = img;
    }

    public BufferlessActor getImg() {
        return img;
    }
}
