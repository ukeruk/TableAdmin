import javax.swing.*;
import java.io.Serializable;
import java.util.ArrayList;

public class InterPaintTable extends InterPaintBackground implements Serializable {

    private ArrayList<BufferlessActor> layers;
    private ArrayList<BufferlessActor> chosen;
    private ImageIcon grid;
    private ImageIcon imgGrid;

    public InterPaintTable(ArrayList<BufferlessActor> layers, ArrayList<BufferlessActor> chosen, ImageIcon background, ImageIcon grid, ImageIcon imgGrid) {
        super(types.SEND_VAR, background);
        this.layers = layers;
        this.chosen = chosen;
        this.grid = grid;
        this.imgGrid = imgGrid;
    }

    public ArrayList<BufferlessActor> getChosen() {
        return chosen;
    }

    public ArrayList<BufferlessActor> getLayers() {
        return layers;
    }

    public ImageIcon getGrid() {
        return grid;
    }

    public ImageIcon getImgGrid() {
        return imgGrid;
    }

    public void setLayers(ArrayList<BufferlessActor> layers) {
        this.layers = layers;
    }

    public void setChosen(ArrayList<BufferlessActor> chosen) {
        this.chosen = chosen;
    }
}
