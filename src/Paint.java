import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

/**
 * credit to CMOS and jem from stackoveflow for inspiring the layout of the client side.
 **/

public class Paint extends JPanel implements MouseListener, ActionListener, MouseMotionListener {

    private static final long serialVersionUID = 1L;
    public static int eraser, draw, move, resize, choose = 0;
    private int xX1, yY1, xX2, yY2, choice;
    private static Rectangle rectChosenImage;
    private static Actor chosenImage;
    private static String chosenIndex;
    private static boolean request_choose = false;

    private Client client = new Client();

    private static final Color BACKGROUND_COLOR = Color.WHITE;

    final JFileChooser fc = new JFileChooser();

    private int eraserWidth = 40;
    private int eraserHeight = 40;
    static final int BRUSH_MIN = 1;
    static final int BRUSH_MAX = 20;
    private int strokeSize = BRUSH_MIN;
    private JButton deleteChosen;


    class Actor {
        BufferedImage layer;
        Image origin;
        String uniqueID;
        int x, y;
        Dimension dim;

        public Actor(BufferedImage l, Image f, String uniq, int X, int Y, Dimension DIM) {
            layer = l;
            origin = f;
            uniqueID = uniq;
            x = X;
            y = Y;
            dim = DIM;
        }

        public Actor(BufferedImage l, Image f) {
            layer = l;
            origin = f;
            uniqueID = UUID.randomUUID().toString();
            x = -1;
            y = -1;
            dim = new Dimension(layer.getWidth(null), layer.getHeight(null));
        }

        public Actor(BufferlessActor ba) {
            origin = ba.getOrigin().getImage();
            uniqueID = ba.getUniqueID();
            x = ba.getX();
            y = ba.getY();
            dim = ba.getDim();
        }

        public Actor(BufferlessActor ba, BufferedImage l) {
            layer = l;
            origin = ba.getOrigin().getImage();
            uniqueID = ba.getUniqueID();
            x = ba.getX();
            y = ba.getY();
            dim = ba.getDim();
        }

        public String getUniqueID() {
            return uniqueID;
        }

        public BufferedImage getLayer() {
            return layer;
        }

        public void setLayer(BufferedImage layer) {
            this.layer = layer;
        }

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }

        public Image getOrigin() {
            return origin;
        }

        public void setOrigin(Image origin) {
            this.origin = origin;
        }

        public Dimension getDim() {
            return dim;
        }

        public void setDim(Dimension dim) {
            this.dim = dim;
        }
    }

    ArrayList<Actor> layers;
    private ArrayList<Actor> chosenImages;
    transient BufferedImage background;

    transient BufferedImage grid;
    Graphics2D gc;
    transient BufferedImage imgGrid;
    Graphics2D imgDrawLayer;

    public static void main(String[] args) {
        new Paint();
    }

    JButton bgChooser;
    JButton addImageBtn;
    JButton moveImageBtn;
    JButton drawingBtn;
    JButton colorBtn;
    JButton eraseBtn;
    JButton lineBtn;
    JSlider thicknessSlider;
    JLabel thicknessLabel;

    JLabel userLabel;
    JTextField userField;
    JLabel passLabel;
    JPasswordField passField;

    JButton login;

    Paint() {

        JFrame frame = new JFrame("Paint Program");
        frame.setSize(1920, 1080);
        layers = new ArrayList<>();
        chosenImages = new ArrayList<>();

        frame.setBackground(BACKGROUND_COLOR);
        frame.getContentPane().add(this);

        SwingWorker checkForServer = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                while (true) {
                    Thread.sleep(100);
                    checkInputFromServer();
                }
            }
        };

        checkForServer.execute();


        thicknessLabel = new JLabel("" + BRUSH_MIN);

        class SliderListener implements ChangeListener {
            public void stateChanged(ChangeEvent e) {
                JSlider source = (JSlider) e.getSource();
                strokeSize = source.getValue();
                thicknessLabel.setText("" + strokeSize);
            }
        }

        userLabel = new JLabel("username:");
        userField = new JTextField(50);
        passLabel = new JLabel("password:");
        passField = new JPasswordField(50);

        login = new JButton("Login");
        login.addActionListener(this);
        thicknessSlider = new JSlider(JSlider.HORIZONTAL, BRUSH_MIN, BRUSH_MAX, BRUSH_MIN);
        thicknessSlider.addChangeListener(new SliderListener());

        bgChooser = new JButton("Choose Background");
        bgChooser.addActionListener(this);
        addImageBtn = new JButton("Add Image");
        addImageBtn.addActionListener(this);
        moveImageBtn = new JButton("Move Image");
        moveImageBtn.addActionListener(this);
        drawingBtn = new JButton("Draw");
        drawingBtn.addActionListener(this);
        colorBtn = new JButton("Color");
        colorBtn.addActionListener(this);
        eraseBtn = new JButton("Erase?");
        eraseBtn.addActionListener(this);
        lineBtn = new JButton("Line");
        lineBtn.addActionListener(this);

        deleteChosen = new JButton("Delete Image");
        deleteChosen.addActionListener(this);

        this.add(userLabel);
        this.add(userField);
        this.add(passLabel);
        this.add(passField);
        this.add(login);


        addMouseListener(this);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        int w = this.getWidth();
        int h = this.getHeight();
        if (grid == null) {
            grid = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            gc = grid.createGraphics();
            gc.setColor(Color.BLUE);
        }
        if (imgGrid == null) {
            imgGrid = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            imgDrawLayer = imgGrid.createGraphics();
            imgDrawLayer.setColor(Color.BLUE);
        }
        if (background == null)
            background = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        g2.drawImage(background, null, 0, 0);
        g2.drawImage(grid, null, 0, 0);
        for (Actor layer : layers) {
            g2.drawImage(layer.getLayer(), null, layer.getX(), layer.getY());
        }
        for (Actor layer : chosenImages) {
            g2.drawImage(layer.getLayer(), null, layer.getX(), layer.getY());
        }
        if (chosenImage != null)
            g2.drawImage(chosenImage.getLayer(), null, chosenImage.getX(), chosenImage.getY());
        g2.drawImage(imgGrid, null, 0, 0);

        checkInputFromServer();
    }

    public void checkInputFromServer() {
        Object pack = client.readFromServer();
        while (pack != null) {
            if (pack instanceof InterPaintDrawer) {//----------------------------------------------------------InterPaintDrawer
                InterPaintDrawer intr = (InterPaintDrawer) pack;
                switch (intr.getType()) {
                    case DRAW_LINE: {
                        gc.setStroke(new BasicStroke(intr.getStroke()));
                        Color current = gc.getColor();
                        gc.setColor(intr.getColor());
                        gc.drawLine(intr.getX(), intr.getY(), intr.getWidth(), intr.getHeight());
                        gc.setColor(current);
                        break;
                    }
                    case ERASE: {
                        gc.setComposite(AlphaComposite.Clear);
                        gc.drawRect(intr.getX(), intr.getY(), intr.getWidth(), intr.getHeight());
                        gc.fillRect(intr.getX(), intr.getY(), intr.getWidth(), intr.getHeight());
                        gc.setComposite(AlphaComposite.SrcOver);
                        repaint();
                        break;
                    }
                }
            } else if (pack instanceof InterPaintLogin) {//----------------------------------------------------------InterPaintLogin
                InterPaintLogin intr = (InterPaintLogin) pack;
                switch (intr.getType()) {

                }
            } else if (pack instanceof InterPaintTable) {//----------------------------------------------------------InterPaintTable
                InterPaintTable intr = (InterPaintTable) pack;
                switch (intr.getType()) {
                    case SEND_VAR: {
                        layers = new ArrayList<>();
                        chosenImages = new ArrayList<>();
                        chosenImage = null;
                        chosenIndex = "";
                        for (BufferlessActor ba : intr.getLayers()) {
                            BufferedImage imgLay = imageToActor(ba);
                            layers.add(new Actor(ba, imgLay));
                        }
                        for (BufferlessActor ba : intr.getChosen()) {
                            BufferedImage imgLay = imageToActor(ba);
                            chosenImages.add(new Actor(ba, imgLay));
                        }
                        Image img;
                        if (intr.getBackground() != null) {
                            img = intr.getBackground().getImage();
                            background = imageToBuffered(img);
                        }
                        if (intr.getGrid() != null) {
                            img = intr.getGrid().getImage();
                            grid = imageToBuffered(img);
                            gc = grid.createGraphics();
                            gc.setColor(Color.BLUE);
                        }
                        if (intr.getImgGrid() != null) {
                            img = intr.getImgGrid().getImage();
                            imgGrid = imageToBuffered(img);
                            imgDrawLayer = imgGrid.createGraphics();
                            gc.setColor(Color.BLUE);
                        }

                        break;
                    }
                }
            } else if (pack instanceof InterPaintImageDraw) {//----------------------------------------------------------InterPaintImageDraw
                InterPaintImageDraw intr = (InterPaintImageDraw) pack;
                switch (intr.getType()) {
                    case DRAW_IMAGE: {
                        BufferedImage imgLay = imageToActor(intr.getImg());
                        layers.add(new Actor(intr.getImg(), imgLay));
                        break;
                    }
                }
            } else if (pack instanceof InterPaintImageChange) {
                InterPaintImageChange intr = (InterPaintImageChange) pack;
                switch (intr.getType()) {
                    case MOVE_IMAGE: {
                        for (Actor image : chosenImages) {
                            if (image.getUniqueID().equals(intr.getImageID())) {
                                image.setX(intr.getX());
                                image.setY(intr.getY());
                                break;
                            }
                        }
                        break;
                    }
                    case DELETE_IMAGE: {
                        for (int i = 0; i < chosenImages.size(); i++) {
                            if (chosenImages.get(i).getUniqueID().equals(intr.getImageID())) {
                                chosenImages.remove(i);
                                break;
                            }
                        }
                        break;
                    }
                    case CHOOSE_IMAGE: {
                        for (int i = 0; i < layers.size(); i++) {
                            if (layers.get(i).getUniqueID().equals(intr.getImageID())) {
                                chosenImages.add(layers.remove(i));
                                break;
                            }
                        }
                        break;
                    }
                    case UNCHOOSE_IMAGE: {
                        for (int i = 0; i < chosenImages.size(); i++) {
                            if (chosenImages.get(i).getUniqueID().equals(intr.getImageID())) {
                                layers.add(chosenImages.remove(i));
                                break;
                            }
                        }
                        break;
                    }
                    case RESIZE: {
                        Image img;
                        for (int i = 0; i < chosenImages.size(); i++) {
                            if (chosenImages.get(i).getUniqueID().equals(intr.getImageID())) {
                                img = chosenImages.get(i).getOrigin();
                                img = img.getScaledInstance((int) intr.getDim().getWidth(), (int) intr.getDim().getHeight(), Image.SCALE_REPLICATE);
                                BufferedImage bImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                                Graphics2D bGr = bImage.createGraphics();
                                bGr.drawImage(img, 0, 0, null);
                                bGr.dispose();
                                chosenImages.get(i).setX(intr.getX());
                                chosenImages.get(i).setY(intr.getY());
                                chosenImages.get(i).setDim(intr.getDim());
                                chosenImages.get(i).setLayer(bImage);
                                break;
                            }
                        }

                        break;
                    }
                }
            } else if (pack instanceof InterPaintBackground) {
                InterPaintBackground intr = (InterPaintBackground) pack;
                switch (intr.getType()) {
                    case DRAW_BACKGROUND: {
                        Image img = intr.getBackground().getImage();
                        background = imageToBuffered(img);
                    }
                }
            } else if (pack instanceof InterPaint) {//----------------------------------------------------------InterPaint
                InterPaint intr = (InterPaint) pack;
                switch (intr.getType()) {
                    case LOGIN: {
                        this.remove(userLabel);
                        this.remove(userField);
                        this.remove(passLabel);
                        this.remove(passField);
                        this.remove(login);
                        this.add(bgChooser);
                        this.add(addImageBtn);
                        this.add(moveImageBtn);
                        this.add(deleteChosen);
                        this.add(drawingBtn);
                        this.add(colorBtn);
                        this.add(eraseBtn);
                        this.add(lineBtn);
                        this.add(thicknessSlider);
                        this.add(thicknessLabel);
                        deleteChosen.setVisible(false);
                        repaint();
                        break;
                    }
                    case ASK_VAR: {
                        ArrayList<BufferlessActor> tempLayers = new ArrayList<>();
                        ArrayList<BufferlessActor> tempChosen = new ArrayList<>();
                        for (Actor ac : layers) {
                            tempLayers.add(new BufferlessActor(ac));
                        }
                        for (Actor ac : chosenImages) {
                            tempChosen.add(new BufferlessActor(ac));
                        }
                        if (chosenImage != null) {
                            clearChosenImage();
                            tempChosen.add(new BufferlessActor(chosenImage));
                        }
                        client.writeToServer(new InterPaintTable(tempLayers, tempChosen, new ImageIcon(background), new ImageIcon(grid), new ImageIcon(imgGrid)));
                        if (chosenImage != null)
                            drawChosenImage();
                        break;
                    }
                    case APPROVE_REQUEST: {
                        if (request_choose == true)
                            getChosenImage();
                        break;
                    }
                    case DENY_REQUEST: {
                        if (request_choose == true)
                            request_choose = false;
                    }
                }
            }
            pack = client.readFromServer();
        }
        repaint();
    }

    public BufferedImage imageToActor(BufferlessActor img) {
        Actor temp = new Actor(img);
        if (img != null) {
            Image newImg = temp.getOrigin().getScaledInstance((int) temp.getDim().getWidth(), (int) temp.getDim().getHeight(), Image.SCALE_REPLICATE);

            BufferedImage bImage = new BufferedImage((int) newImg.getWidth(null), (int) newImg.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D bGr = bImage.createGraphics();
            bGr.drawImage(newImg, 0, 0, null);
            bGr.dispose();
            return bImage;
        }
        return null;
    }

    public BufferedImage imageToBuffered(Image img) {

        // Create the buffered image.
        BufferedImage bfImg = new BufferedImage(
                img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // Copy image to buffered image.
        Graphics g = bfImg.createGraphics();

        // Clear background and paint the image.
        g.drawImage(img, 0, 0, null);
        g.dispose();

        return bfImg;
    }

    public void drawLine() {
        gc.setStroke(new BasicStroke(strokeSize));
        gc.drawLine(xX1, yY1, xX2, yY2);

        client.writeToServer(new InterPaintDrawer(InterPaint.types.DRAW_LINE, xX1, yY1, xX2, yY2, strokeSize, gc.getColor()));
        repaint();
    }

    public void actionPerformed(ActionEvent e) {
        choice = -1;
        super.removeMouseMotionListener(this);

        eraser = 0;
        draw = 0;
        move = 0;
        resize = 0;
        choose = 0;

        if (e.getActionCommand().equals("Login")) {
            client.writeToServer(new InterPaintLogin(InterPaint.types.LOGIN, userField.getText(), String.valueOf(passField.getPassword())));
            return;
        } else if (e.getActionCommand().equals("Delete Image")) {
            clearChosenImage();
            deleteChosen.setVisible(false);
            client.writeToServer(new InterPaintImageChange(InterPaint.types.DELETE_IMAGE, chosenImage.getUniqueID()));
            chosenImage = null;
            rectChosenImage = null;
            chosenIndex = "";
            repaint();
            return;
        } else if (e.getActionCommand().equals("Color")) {
            Color bgColor = JColorChooser.showDialog(this, "Choose Background Color", getBackground());
            if (bgColor != null)
                gc.setColor(bgColor);
        } else if (e.getActionCommand().equals("Choose Background")) {
            fc.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.getName().endsWith(".jpg") || f.getName().endsWith(".jpeg") || f.getName().endsWith(".png") || f.isDirectory();
                }

                @Override
                public String getDescription() {
                    return null;
                }
            });
            int returnVal = fc.showOpenDialog(fc);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                try {
                    background = ImageIO.read(file);
                    client.writeToServer(new InterPaintBackground(InterPaint.types.DRAW_BACKGROUND, new ImageIcon(background)));
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                repaint();
            }

        } else if (e.getActionCommand().equals("Add Image")) {
            fc.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.getName().endsWith(".jpg") || f.getName().endsWith(".jpeg") || f.getName().endsWith(".png") || f.isDirectory();
                }

                @Override
                public String getDescription() {
                    return null;
                }
            });

            int returnVal = fc.showOpenDialog(fc);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                try {
                    Image img = ImageIO.read(file);
                    Image newImg;
                    Dimension dim = getScaledDimension(new Dimension(img.getWidth(null), img.getHeight(null)), new Dimension(256, 256));

                    newImg = img.getScaledInstance(dim.width, dim.height, Image.SCALE_REPLICATE);


                    BufferedImage bImage = new BufferedImage(newImg.getWidth(null), newImg.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D bGr = bImage.createGraphics();
                    bGr.drawImage(newImg, 0, 0, null);
                    bGr.dispose();
                    Actor ac = new Actor(bImage, img);

                    int w = this.getWidth();
                    int h = this.getHeight();


                    ac.setX(w / 2 - ac.getLayer().getWidth() / 2);
                    ac.setY(h / 2 - ac.getLayer().getHeight() / 2);

                    client.writeToServer(new InterPaintImageDraw(InterPaint.types.DRAW_IMAGE, new BufferlessActor(ac)));
                    layers.add(ac);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                repaint();
            }

        } else if (e.getActionCommand().equals("Move Image")) {
            super.addMouseMotionListener(this);
            choose = 1;
        } else if (e.getActionCommand().equals("Draw")) {
            draw = 1;
            super.addMouseMotionListener(this);
        } else if (e.getActionCommand().equals("Line")) {
            choice = 1;
        } else if (e.getActionCommand().equals("Erase?")) {
            eraser = 1;
            super.addMouseMotionListener(this);
        }
        repaint();
    }

    public void getChosenImage() {
        int index = -1;
        for (int i = 0; i < layers.size(); i++) {
            if (layers.get(i).getUniqueID().equals(chosenIndex))
                index = i;
        }

        int rngMinX = layers.get(index).getX();
        int rngMaxX = layers.get(index).getLayer().getWidth() + rngMinX;
        int rngMinY = layers.get(index).getY();
        int rngMaxY = layers.get(index).getLayer().getHeight() + rngMinY;

        rectChosenImage = new Rectangle(rngMinX, rngMinY, rngMaxX - rngMinX, rngMaxY - rngMinY);
        drawChosenImage();
        chosenImage = layers.remove(index);
        deleteChosen.setVisible(true);
        request_choose = false;
        repaint();
        return;
    }

    public void drawChosenImage() {
        Color c = imgDrawLayer.getColor();
        imgDrawLayer.setStroke(new BasicStroke(2));
        imgDrawLayer.setColor(Color.BLACK);
        imgDrawLayer.drawRect(rectChosenImage.x - 2, rectChosenImage.y - 2, rectChosenImage.width + 4, rectChosenImage.height + 4);
        imgDrawLayer.setColor(Color.WHITE);
        imgDrawLayer.drawRect(rectChosenImage.x, rectChosenImage.y, rectChosenImage.width, rectChosenImage.height);
        imgDrawLayer.setColor(Color.RED);
        imgDrawLayer.drawRect(rectChosenImage.x - 2, rectChosenImage.y - 2, 16, 16);
        imgDrawLayer.setColor(c);
        imgDrawLayer.setStroke(new BasicStroke(strokeSize));
    }

    public void clearChosenImage() {
        imgDrawLayer.setComposite(AlphaComposite.Clear);
        imgDrawLayer.setStroke(new BasicStroke(2));
        imgDrawLayer.drawRect(rectChosenImage.x - 2, rectChosenImage.y - 2, rectChosenImage.width + 4, rectChosenImage.height + 4);
        imgDrawLayer.fillRect(rectChosenImage.x - 2, rectChosenImage.y - 2, rectChosenImage.width + 4, rectChosenImage.height + 4);
        imgDrawLayer.setComposite(AlphaComposite.SrcOver);
        imgDrawLayer.setStroke(new BasicStroke(strokeSize));
    }

    public boolean isMoveRange() {
        int rngMinX = chosenImage.getX();
        int rngMaxX = chosenImage.getLayer().getWidth() + rngMinX;
        int rngMinY = chosenImage.getY();
        int rngMaxY = chosenImage.getLayer().getHeight() + rngMinY;
        if ((xX2 >= rngMinX && xX2 <= rngMaxX) && (yY2 >= rngMinY && yY2 <= rngMaxY))
            return true;
        return false;
    }

    public boolean isResizeRange() {
        int rngMinX = chosenImage.getX();
        int rngMinY = chosenImage.getY();
        if ((xX2 >= rngMinX && xX2 <= rngMinX + 16) && (yY2 >= rngMinY && yY2 <= rngMinY + 16))
            return true;
        return false;
    }

    public void mouseExited(MouseEvent evt) {
        // No uses yet.
    }

    public void mouseEntered(MouseEvent evt) {
        // No uses yet.
    }

    public void mouseClicked(MouseEvent evt) {
        if (choose == 1 && request_choose == false) {
            if (chosenImage != null) {
                clearChosenImage();
                layers.add(chosenImage);
                deleteChosen.setVisible(false);
                client.writeToServer(new InterPaintImageChange(InterPaint.types.UNCHOOSE_IMAGE, chosenImage.getUniqueID()));
                chosenImage = null;
                rectChosenImage = null;
                chosenIndex = "";
            }
            xX1 = evt.getX();
            yY1 = evt.getY();
            for (int i = layers.size() - 1; i >= 0; i--) {
                int rngMinX = layers.get(i).getX();
                int rngMaxX = layers.get(i).getLayer().getWidth() + rngMinX;
                int rngMinY = layers.get(i).getY();
                int rngMaxY = layers.get(i).getLayer().getHeight() + rngMinY;

                if ((xX1 >= rngMinX && xX1 <= rngMaxX) && (yY1 >= rngMinY && yY1 <= rngMaxY)) {

                    chosenIndex = layers.get(i).getUniqueID();
                    request_choose = true;
                    client.writeToServer(new InterPaintImageChange(InterPaint.types.CHOOSE_IMAGE, layers.get(i).getUniqueID()));
                    break;
                }
            }
            deleteChosen.setVisible(false);
        }
        checkInputFromServer();
    }

    public void mousePressed(MouseEvent evt) {
        xX1 = evt.getX();
        yY1 = evt.getY();
    }

    public void mouseReleased(MouseEvent evt) {
        if (choice == 1) {
            drawLine();
        }
        move = 0;
        resize = 0;
        checkInputFromServer();
    }

    public void mouseDragged(MouseEvent me) {
        xX2 = me.getX();
        yY2 = me.getY();
        if (eraser == 1) {
            gc.setComposite(AlphaComposite.Clear);
            gc.drawRect(me.getX() - eraserWidth / 2, me.getY() - eraserHeight / 2, eraserWidth, eraserHeight);
            gc.fillRect(me.getX() - eraserWidth / 2, me.getY() - eraserHeight / 2, eraserWidth, eraserHeight);
            client.writeToServer(new InterPaintDrawer(InterPaint.types.ERASE, me.getX() - eraserWidth / 2, me.getY() - eraserHeight / 2, eraserWidth, eraserHeight));
            gc.setComposite(AlphaComposite.SrcOver);
            repaint();
        } else if (draw == 1) {
            gc.setStroke(new BasicStroke(strokeSize));
            gc.drawLine(xX1, yY1, xX2, yY2);
            repaint();
            client.writeToServer(new InterPaintDrawer(InterPaint.types.DRAW_LINE, xX1, yY1, xX2, yY2, strokeSize, gc.getColor()));
            xX1 = xX2;
            yY1 = yY2;
        } else if (chosenImage != null) {
            if (move == 0 && resize == 0) {
                if (isMoveRange()) {
                    if (isResizeRange())
                        resize = 1;
                    else
                        move = 1;
                } else
                    return;
            } else if (move == 1) {
                int tempX = xX2 - xX1;
                int tempY = yY2 - yY1;
                int pX = chosenImage.getX() + tempX;
                int pY = chosenImage.getY() + tempY;
                chosenImage.setX(pX);
                chosenImage.setY(pY);
                client.writeToServer(new InterPaintImageChange(InterPaint.types.MOVE_IMAGE, chosenImage.getUniqueID(), pX, pY));
                xX1 = xX2;
                yY1 = yY2;
                clearChosenImage();
                rectChosenImage.setLocation(pX, pY);
                drawChosenImage();
                repaint();
            } else if (resize == 1) {
                int tempX = xX2 - xX1;
                int tempY = yY2 - yY1;
                int sizeChange = (tempX + tempY) / 2;
                int imX = chosenImage.getX();
                int imY = chosenImage.getY();
                int pX = imX + tempX;
                int pY = imY + tempY;
                Image img;
                img = chosenImage.getOrigin();
                Image oldImg = chosenImage.getLayer();
                Dimension dim = getScaledDimension(new Dimension(img.getWidth(null), img.getHeight(null)), new Dimension(oldImg.getWidth(null) - sizeChange, oldImg.getHeight(null) - sizeChange));
                img = img.getScaledInstance(dim.width, dim.height, Image.SCALE_REPLICATE);

                BufferedImage bImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                Graphics2D bGr = bImage.createGraphics();
                bGr.drawImage(img, 0, 0, null);
                bGr.dispose();
                Actor ac = new Actor(bImage, chosenImage.origin, chosenImage.getUniqueID(), pX, pY, dim);

                client.writeToServer(new InterPaintImageChange(InterPaint.types.RESIZE, chosenImage.getUniqueID(), pX, pY, dim));

                chosenImage = ac;

                xX1 = xX2;
                yY1 = yY2;

                clearChosenImage();
                rectChosenImage.setBounds(pX, pY, bImage.getWidth(), bImage.getHeight());
                drawChosenImage();
                repaint();
            }
        }
        checkInputFromServer();
    }

    public void mouseMoved(MouseEvent arg0) {
        xX1 = arg0.getX();
        yY1 = arg0.getY();
    }

    public static Dimension getScaledDimension(Dimension imgSize, Dimension boundary) {

        int originalWidth = imgSize.width;
        int originalHeight = imgSize.height;
        int boundWidth = boundary.width;
        int boundHeight = boundary.height;
        int newWidth = originalWidth;
        int newHeight = originalHeight;

        // first check if we need to scale width
        if (originalWidth > boundWidth) {
            //scale width to fit
            newWidth = boundWidth;
            //scale height to maintain aspect ratio
            newHeight = (newWidth * originalHeight) / originalWidth;
        }

        // then check if we need to scale even with the new height
        if (newHeight > boundHeight) {
            //scale height to fit instead
            newHeight = boundHeight;
            //scale width to maintain aspect ratio
            newWidth = (newHeight * originalWidth) / originalHeight;
        }

        return new Dimension(newWidth, newHeight);
    }
}

class BufferlessActor implements Serializable {
    ImageIcon origin;
    String uniqueID;
    int x, y;
    Dimension dim;

    public BufferlessActor(Image f, String uniq, int X, int Y, Dimension DIM) {
        origin = new ImageIcon(f);
        uniqueID = uniq;
        x = X;
        y = Y;
        dim = DIM;
    }

    public BufferlessActor(Paint.Actor ac) {
        origin = new ImageIcon(ac.getOrigin());
        uniqueID = ac.getUniqueID();
        x = ac.getX();
        y = ac.getY();
        dim = ac.getDim();
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public ImageIcon getOrigin() {
        return origin;
    }

    public void setOrigin(Image origin) {
        this.origin = new ImageIcon(origin);
    }

    public Dimension getDim() {
        return dim;
    }

    public void setDim(Dimension dim) {
        this.dim = dim;
    }
}