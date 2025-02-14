import javax.swing.*;

public class ClientDriver {

    public static void main(String[] args) {
        String serverIP = "172.17.30.206"; // IP
        String localhost = "127.0.0.1"; // localhost
        Client application = new Client(serverIP);

        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        application.setSize(800, 600);
        application.setVisible(true);
        application.setResizable(false);
        application.setLocationRelativeTo(null);
        application.runClient();
    }
}
