import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class ClientMain {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;

    public static void main(String[] args) {
        try {
            // Connect to server
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Show login UI
            SwingUtilities.invokeLater(() -> new LoginUI(socket, in, out));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to connect to server: " + e.getMessage(),
                    "Connection Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}