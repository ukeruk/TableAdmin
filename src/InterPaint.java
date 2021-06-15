import java.io.Serializable;

public class InterPaint implements Serializable {

    enum types {
        CHECK_CONNECTION,
        SEND_VAR,
        DRAW_LINE,
        ERASE,
        DRAW_IMAGE,
        CHOOSE_IMAGE,
        UNCHOOSE_IMAGE,
        RESIZE,
        MOVE_IMAGE,
        DRAW_BACKGROUND,
        DELETE_IMAGE,
        LOGIN,
        DENY_REQUEST,
        APPROVE_REQUEST,
        ASK_VAR
    }

    private types type;

    public InterPaint() {
        type = types.CHECK_CONNECTION;
    }

    public InterPaint(types type)// for ASK_VAR
    {
        this.type = type;
    }

    public types getType() {
        return type;
    }

    @Override
    public String toString() {
        return "InterPaint{" +
                ", type=" + type +
                '}';
    }
}