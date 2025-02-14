import javax.swing.*;

public class ServerDriver {

    public static void main(String[] args) {
        Server application = new Server();
        application.setTitle("Server");
        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        application.setLocationRelativeTo(null);
        application.runServer();
    }
}
