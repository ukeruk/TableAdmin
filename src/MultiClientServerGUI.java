import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

//TODO add dice system, need better UI, need database possibly for users to connect and permissions.
public class MultiClientServerGUI extends JFrame implements ActionListener {

    private JTextArea jta = new JTextArea();
    private ServerSocket serverSocket;
    Socket socket;
    ArrayList<HandleAClient> clients;
    ArrayList<HandleAClient> logInClient;

    JFileChooser jfc;

    static ArrayList<Paint.Actor> layers;

    static boolean isLoadSave = false;

    final static int LIMIT = 156;
    ArrayList<Object> buffer = new ArrayList<>();

    ConnectSQL sql;

    class ChosenImage {
        String imageID;
        Socket choosingSocket;

        public ChosenImage(String imageID, Socket choosingSocket) {
            this.imageID = imageID;
            this.choosingSocket = choosingSocket;
        }

        public String getImageID() {
            return imageID;
        }
    }

    ArrayList<ChosenImage> chosenImages = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        new MultiClientServerGUI();

    }

    public MultiClientServerGUI() throws IOException {
        clients = new ArrayList<>();
        logInClient = new ArrayList<>();

        layers = new ArrayList<>();

        sql = new ConnectSQL();

        JPanel buttons = new JPanel();
        JPanel text = new JPanel();

        text.setLayout(new BorderLayout());

        setLayout(new BorderLayout());

        JButton newSave = new JButton("Create New Save");
        buttons.add(newSave, BorderLayout.CENTER);
        newSave.addActionListener(this);
        JButton oldSave = new JButton("Create Save As");
        buttons.add(oldSave, BorderLayout.CENTER);
        oldSave.addActionListener(this);
        JButton loadSave = new JButton("Load Save");
        buttons.add(loadSave, BorderLayout.CENTER);
        loadSave.addActionListener(this);

        text.add(new JScrollPane(jta), BorderLayout.CENTER);
        add(buttons, BorderLayout.NORTH);
        add(text, BorderLayout.CENTER);
        setTitle("TableAdminServer");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        jta.append("MultiThreadServer started at " + new Date() + '\n');

        jfc = new JFileChooser();
        String myDocumentPath = System.getProperty("user.home") + "/Documents";
        File dir = new File(myDocumentPath + "/TableAdminSaves");
        if (dir.exists())
            dir.mkdir();
        jfc.setCurrentDirectory(dir);

        try {
            serverSocket = new ServerSocket(8000);
            serverSocket.setSoTimeout(100);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        while (true) {
            // Listen for a new connection request
            if (isLoadSave) {
                loadSave();
                isLoadSave = false;
            }

            try {
                //TODO Check for outputs from all clients
                //writeToClient
                for (int i = clients.size() - 1; i > -1; i--) {
                    if (!isClientConnected(clients, i))
                        clients.remove(i);
                }
                for (Object obj : buffer) {
                    for (HandleAClient reciever : clients) {
                        reciever.writeToClient(obj);
                    }
                }
                for (int i = logInClient.size() - 1; i > -1; i--) {
                    InterPaint intr;
                    if (isClientConnected(logInClient, i)) {
                        if (logInClient.get(i).checkFromClient.available() > 0) {
                            intr = (InterPaintLogin) logInClient.get(i).inputFromClient.readObject();
                            if (intr.getType() == InterPaint.types.LOGIN) {
                                if (sql.connectUser(logInClient.get(i).getSocket(), ((InterPaintLogin) intr).getName(), ((InterPaintLogin) intr).getPass())) {
                                    logInClient.get(i).writeToClient(new InterPaint(InterPaint.types.LOGIN));
                                    intr = getInter();
                                    if (intr != null) {
                                        logInClient.get(i).writeToClient(intr);
                                        saveToFile(intr);
                                    }
                                    clients.add(logInClient.remove(i));
                                }
                            }
                        }
                    }
                }
                for (HandleAClient client : clients) {
                    int count = 0;
                    while (client.checkFromClient.available() > 0 && count < LIMIT) {
                        Object pack = client.inputFromClient.readObject();
                        if (pack instanceof InterPaintImageChange) {
                            InterPaintImageChange intr = (InterPaintImageChange) pack;
                            if (intr.getType() == InterPaint.types.CHOOSE_IMAGE) {
                                boolean approve = true;
                                for (ChosenImage chsImg : chosenImages) {
                                    if (chsImg.getImageID().equals(intr.getImageID())) {
                                        client.writeToClient(new InterPaint(InterPaint.types.DENY_REQUEST));
                                        approve = false;
                                        break;
                                    }
                                }
                                if (approve) {
                                    chosenImages.add(new ChosenImage(intr.getImageID(), client.getSocket()));
                                    client.writeToClient(new InterPaint(InterPaint.types.APPROVE_REQUEST));
                                    for (HandleAClient reciever : clients) {
                                        if (client != reciever)
                                            reciever.writeToClient(pack);
                                    }
                                    break;
                                }
                            } else if (intr.getType() == InterPaint.types.UNCHOOSE_IMAGE) {
                                for (int i = 0; i < chosenImages.size(); i++) {
                                    if (chosenImages.get(i).getImageID().equals(intr.getImageID())) {
                                        chosenImages.remove(i);
                                        for (HandleAClient reciever : clients) {
                                            if (client != reciever)
                                                reciever.writeToClient(pack);
                                        }
                                        break;
                                    }
                                }//TODO make chosen images also chosen for new users, check what happened to the ID
                            } else {
                                for (HandleAClient reciever : clients) {
                                    if (client != reciever)
                                        reciever.writeToClient(pack);
                                }
                            }
                        } else {
                            for (HandleAClient reciever : clients) {
                                if (client != reciever)
                                    reciever.writeToClient(pack);
                            }
                        }
                    }
                    count++;
                }
                socket = null;
                socket = serverSocket.accept();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            //TODO maybe add own painter to use?
            if (socket != null) {
                // Display the client number
                jta.append("Connecting to new client at " + new Date() + '\n');

                // Find the client's host name, and IP address
                InetAddress inetAddress = socket.getInetAddress();
                jta.append("Client's host name is " + inetAddress.getHostName() + "\n");
                jta.append("Client's IP Address is " + inetAddress.getHostAddress() + "\n");
                // Create a new task for the connection
                logInClient.add(new HandleAClient(socket));

                socket = null;
            }
        }
    }

    public InterPaintTable getInter() {
        InterPaintTable intr = null;
        try {
            while (clients.size() > 0 && !isClientConnected(clients, 0))
                clients.remove(0);
            if (clients.size() == 0)
                return null;
            clients.get(0).writeToClient(new InterPaint(InterPaint.types.ASK_VAR));
            Object pack = clients.get(0).inputFromClient.readObject();
            InterPaint temp = (InterPaint) pack;
            while (temp.getType() != InterPaint.types.SEND_VAR && clients.size() > 0) {
                buffer.add(pack);
                if (!isClientConnected(clients, 0))
                    clients.remove(0);
                pack = clients.get(0).inputFromClient.readObject();
                temp = (InterPaint) pack;
            }
            intr = (InterPaintTable) pack;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (intr == null || intr.getType() != InterPaint.types.SEND_VAR)
            return null;
        return intr;
    }

    public boolean saveToFile(InterPaint saveFile) {
        String myDocumentPath = System.getProperty("user.home") + "/Documents";
        File dir = new File(myDocumentPath + "/TableAdminSaves");
        if (!dir.exists())
            dir.mkdir();
        int count = 1;
        File save = new File(dir.getPath() + "/save_" + count);
        while (save.exists()) {
            count++;
            save = new File(dir.getPath() + "/save_" + count);
        }
        try {
            FileOutputStream fop = new FileOutputStream(save);
            ObjectOutputStream ous = new ObjectOutputStream(fop);

            ous.writeObject(saveFile);

            ous.close();
            fop.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean customSaveToFile(InterPaint saveFile) {
        String myDocumentPath = System.getProperty("user.home") + "/Documents";
        File dir = new File(myDocumentPath + "/TableAdminSaves");
        if (!dir.exists())
            dir.mkdir();
        jfc.setCurrentDirectory(dir);

        int retval = jfc.showSaveDialog(jfc);

        if (retval == JFileChooser.APPROVE_OPTION) {
            File save = jfc.getSelectedFile();
            try {
                FileOutputStream fop = new FileOutputStream(save);
                ObjectOutputStream ous = new ObjectOutputStream(fop);

                ous.writeObject(saveFile);

                ous.close();
                fop.close();
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public InterPaint loadFromFile(File file) {
        InterPaint intr = null;
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);

            intr = (InterPaint) ois.readObject();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return intr;
    }

    public void loadSave() {
        jfc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.getName().endsWith("") || f.isDirectory();
            }

            @Override
            public String getDescription() {
                return null;
            }
        });
        int returnVal = jfc.showOpenDialog(jfc);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = jfc.getSelectedFile();
            Object obj = loadFromFile(file);
            if (obj != null) {
                InterPaint intr = (InterPaint) obj;
                for (HandleAClient client : clients) {
                    client.writeToClient(intr);
                }
                chosenImages = new ArrayList<>();
            }
        }
    }

    public boolean isClientConnected(ArrayList<HandleAClient> alc, int client) {
        try {
            alc.get(client).outputToClient.writeObject(new InterPaint());
        } catch (IOException e) {
            jta.append("Client disconnected.");
            sql.disconnect(alc.get(client).getSocket());
            return false;
        }
        return true;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("Create New Save")) {
            if (!clients.isEmpty()) {
                InterPaint intr = getInter();
                if (intr != null) {
                    InterPaintTable intrTable = (InterPaintTable) intr;
                    ArrayList<BufferlessActor> chos = intrTable.getChosen();
                    ArrayList<BufferlessActor> lay = intrTable.getLayers();
                    for(BufferlessActor bfa : chos)
                    {
                        lay.add(bfa);
                    }
                    intrTable.setLayers(lay);
                    intrTable.setChosen(new ArrayList<>());
                    if (saveToFile(intrTable))
                        jta.append("Successfully saved to file.");
                    else
                        jta.append("Error: Couldn't save to file.");
                } else {
                    jta.append("Error: Couldn't save to file. Are any users connected?");
                }
            } else {
                jta.append("Error: Couldn't save to file. Are any users connected?");
            }
        } else if (e.getActionCommand().equals("Create Save As")) {
            if (!clients.isEmpty()) {
                InterPaint intr = getInter();
                if (intr != null) {
                    InterPaintTable intrTable = (InterPaintTable) intr;
                    ArrayList<BufferlessActor> chos = intrTable.getChosen();
                    ArrayList<BufferlessActor> lay = intrTable.getLayers();
                    for(BufferlessActor bfa : chos)
                    {
                        lay.add(bfa);
                    }
                    intrTable.setLayers(lay);
                    intrTable.setChosen(new ArrayList<>());
                    if (customSaveToFile(intrTable))
                        jta.append("Successfully saved to file.");
                    else
                        jta.append("Error: Couldn't save to file.");
                } else {
                    jta.append("Error: Couldn't save to file. Are any users connected?");
                }
            } else {
                jta.append("Error: Couldn't save to file. Are any users connected?");
            }
        } else if (e.getActionCommand().equals("Load Save")) {
            isLoadSave = true;
        }
    }
}
