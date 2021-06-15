import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;

public class ConnectSQL {
    Connection con;
    Statement stmt;
    String str;
    ResultSet rs;
    ArrayList<User> connected;

    class User {
        Socket client;
        String username;

        public User(Socket st, String user) {
            client = st;
            username = user;
        }

        public Socket getClient() {
            return client;
        }

        public String getUsername() {
            return username;
        }
    }

    public ConnectSQL() {
        try {
            con = DriverManager.getConnection("jdbc:ucanaccess://E:/users.accdb");
            stmt = con.createStatement();                            // Create statement
            connected = new ArrayList<>();

        } catch (SQLException throwable) {
            throwable.printStackTrace();
        }
    }

    public void disconnect(Socket st) {
        User temp = null;
        for (User user : connected) {
            if (user.getClient().equals(st))
                temp = user;
        }
        if (temp != null)
            connected.remove(temp);
    }

    public boolean connectUser(Socket st, String username, String pass) {
        if (userIsConnected(username))
            return false;
        try {
            str = "SELECT ID FROM USERS WHERE Username='" + username + "' AND Password='" + pass + "'";
            rs = stmt.executeQuery(str);
            if (rs.next()) {
                connected.add(new User(st, username));
                return true;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public boolean userIsConnected(String username) {
        for (User user : connected) {
            if (user.username.equals(username))
                return true;
        }
        return false;
    }
}
