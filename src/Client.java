import java.io.*;
import java.net.Socket;

class Client {

    private static ObjectOutputStream toServer;
    private static ObjectInputStream fromServer;
    private static BufferedInputStream checkFromServer;

    private Socket socket;

    public Client() {

        try {
            // Create a socket to connect to the server
            socket = new Socket("localhost", 8000);
            // Create an output stream to send data to the server
            toServer = new ObjectOutputStream(socket.getOutputStream());

            // Create an input stream to receive data
            // from the server
            fromServer = new ObjectInputStream(socket.getInputStream());
            checkFromServer = new BufferedInputStream(socket.getInputStream());

        } catch (IOException ex) {
        }
    }



    public void writeToServer(InterPaint s) {
        try {
            toServer.writeObject(s);
            toServer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public InterPaint readFromServer() {
        try {
            synchronized (checkFromServer) {
                if (checkFromServer.available() > 0) {
                    InterPaint intr = (InterPaint) fromServer.readObject();
                    if (intr != null)
                        return intr;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
