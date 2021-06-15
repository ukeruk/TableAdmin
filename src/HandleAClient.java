import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

class HandleAClient {
    Socket socket; // A connected socket
    ObjectInputStream inputFromClient;
    ObjectOutputStream outputToClient;
    BufferedInputStream checkFromClient;

    public HandleAClient(Socket socket) throws IOException {
        this.socket = socket;
        outputToClient = new ObjectOutputStream(socket.getOutputStream());
        inputFromClient = new ObjectInputStream(socket.getInputStream());
        checkFromClient = new BufferedInputStream(socket.getInputStream());
    }

    public void writeToClient(Object s) {
        try {
            outputToClient.writeObject(s);
            outputToClient.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public ObjectInputStream getInputFromClient() {
        return inputFromClient;
    }

    public ObjectOutputStream getOutputToClient() {
        return outputToClient;
    }
}
