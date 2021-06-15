import java.io.Serializable;

public class InterPaintLogin extends InterPaint implements Serializable {

    String pass,name;

    public InterPaintLogin(types type, String name, String pass)
    {
        super(type);
        this.name = name;
        this.pass = pass;
    }

    public String getPass() {
        return pass;
    }

    public String getName() {
        return name;
    }
}
