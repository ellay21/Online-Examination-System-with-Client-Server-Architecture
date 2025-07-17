import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class ManageSubjectUI extends JFrame {
    private JTextField subjectNameField;
    private JButton addButton, cancelButton;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Runnable onSubjectAdded;

    public ManageSubjectUI(Socket socket, BufferedReader in, PrintWriter out, Runnable onSubjectAdded) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.onSubjectAdded = onSubjectAdded;

        setTitle("Add Subject");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(300, 150);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("Subject Name:"), gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        subjectNameField = new JTextField(15);
        add(subjectNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        cancelButton = new JButton("Cancel");
        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, gbc);

        addButton.addActionListener(e -> handleAddSubject());
        cancelButton.addActionListener(e -> dispose());

        setVisible(true);
    }

    private void handleAddSubject() {
        String subjectName = subjectNameField.getText().trim();
        if (subjectName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a subject name.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            out.println("ADD_SUBJECT");
            out.println(subjectName);
            String response = in.readLine();
            if ("SUCCESS".equals(response)) {
                JOptionPane.showMessageDialog(this, "Subject added successfully!");
                if (onSubjectAdded != null)
                    onSubjectAdded.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add subject: " + response, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
