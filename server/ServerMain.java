import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;

public class ServerMain {
    private static final int PORT = 12345;

    public static void main(String[] args) {
        DatabaseManager dbManager = null;
        try {
            dbManager = new DatabaseManager();
            Connection dbConn = dbManager.getConnection();
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Server started on port " + PORT);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket.getInetAddress());
                    Thread clientThread = new Thread(new ClientHandler(clientSocket, dbConn));
                    clientThread.start();
                }
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        } finally {
            if (dbManager != null) {
                try {
                    dbManager.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}