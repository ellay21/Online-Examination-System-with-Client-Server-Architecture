import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.Socket;

public class RegisterUI extends JFrame {
    private JTextField usernameField, fullNameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton registerButton, cancelButton;
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;

    public RegisterUI(Socket socket, BufferedReader in, PrintWriter out) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        setTitle("Create New Account");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(700, 600);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(20, 20));

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(45, 50, 70)); // Deep Charcoal Blue
        JLabel titleLabel = new JLabel("Register for an Account");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 30));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));
        inputPanel.setBackground(new Color(250, 250, 250)); // Off-White

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        userLabel.setForeground(new Color(50, 50, 50));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(userLabel, gbc);

        usernameField = new JTextField(22);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        styleTextField(usernameField);
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(usernameField, gbc);

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        passLabel.setForeground(new Color(50, 50, 50));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(passLabel, gbc);

        passwordField = new JPasswordField(22);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        styleTextField(passwordField);
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(passwordField, gbc);

        JLabel nameLabel = new JLabel("Full Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        nameLabel.setForeground(new Color(50, 50, 50));
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(nameLabel, gbc);

        fullNameField = new JTextField(22);
        fullNameField.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        styleTextField(fullNameField);
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(fullNameField, gbc);

        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        roleLabel.setForeground(new Color(50, 50, 50));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.EAST;
        inputPanel.add(roleLabel, gbc);

        roleComboBox = new JComboBox<>(new String[] { "student", "teacher" });
        roleComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 17));
        ((JLabel) roleComboBox.getRenderer()).setHorizontalAlignment(SwingConstants.CENTER);
        roleComboBox.setBackground(Color.WHITE);
        roleComboBox.setForeground(new Color(50, 50, 50));
        roleComboBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.anchor = GridBagConstraints.WEST;
        inputPanel.add(roleComboBox, gbc);

        add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        buttonPanel.setBackground(new Color(240, 240, 240));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));

        registerButton = new JButton("Register");
        styleButton(registerButton, new Color(50, 130, 180)); // Muted Blue
        buttonPanel.add(registerButton);

        cancelButton = new JButton("Cancel");
        styleButton(cancelButton, new Color(100, 100, 100)); // Muted Grey
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        registerButton.addActionListener(e -> handleRegister());
        cancelButton.addActionListener(e -> dispose());

        setVisible(true);
    }

    private void styleTextField(JTextField field) {
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));
        field.setBackground(Color.WHITE);
        field.setForeground(new Color(50, 50, 50));
    }

    private void styleButton(JButton button, Color backgroundColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 17));
        button.setBackground(backgroundColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.putClientProperty("JButton.buttonType", "roundRect");
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                button.setBackground(backgroundColor.darker());
            }

            public void mouseExited(MouseEvent evt) {
                button.setBackground(backgroundColor);
            }
        });
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        String fullName = fullNameField.getText().trim();
        String role = (String) roleComboBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            out.println("REGISTER");
            out.println(username);
            out.println(password);
            out.println(fullName);
            out.println(role);

            String response = in.readLine();
            if ("SUCCESS".equals(response)) {
                JOptionPane.showMessageDialog(this, "Registration successful! You can now log in.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Registration failed: " + response, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}