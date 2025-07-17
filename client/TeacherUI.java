import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;

public class TeacherUI extends JFrame {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public TeacherUI(Socket socket, BufferedReader in, PrintWriter out, String username) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.username = username;

        setTitle("🎓 Teacher Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        headerPanel.setBackground(new Color(44, 62, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        JLabel welcomeLabel = new JLabel("Welcome, Prof. " + username);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        welcomeLabel.setForeground(Color.WHITE);
        headerPanel.add(welcomeLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Main Content
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(new Color(245, 247, 250));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(60, 80, 60, 80));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(30, 30, 30, 30);
        gbc.gridx = 0;
        gbc.gridy = 0;

        JPanel card1 = createDashboardCard("Manage Exams", new Color(52, 152, 219), e -> openManageExams());
        contentPanel.add(card1, gbc);

        gbc.gridx++;
        JPanel card2 = createDashboardCard("View Student Results", new Color(46, 204, 113), e -> openViewResults());
        contentPanel.add(card2, gbc);

        gbc.gridx++;
        JPanel card3 = createDashboardCard("Logout", new Color(231, 76, 60), e -> logout());
        contentPanel.add(card3, gbc);

        add(contentPanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private JPanel createDashboardCard(String text, Color color, java.awt.event.ActionListener action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        button.setPreferredSize(new Dimension(250, 100));

        button.addActionListener(action);

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setBackground(color.darker());
            }

            public void mouseExited(MouseEvent e) {
                button.setBackground(color);
            }
        });

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        panel.setPreferredSize(new Dimension(280, 140));
        panel.add(button);
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        panel.setOpaque(true);

        // Optional: slight shadow look
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 1, 3, 3, new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        return panel;
    }

    private void openManageExams() {
        new ManageExamsUI(socket, in, out, username);
    }

    private void openViewResults() {
        new ViewStudentResultsUI(socket, in, out, username);
    }

    private void logout() {
        dispose();
        new LoginUI(socket, in, out);
    }
}
