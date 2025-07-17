import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class UpdateQuestionUI extends JFrame {
    private JTextField questionField, optionAField, optionBField, optionCField, optionDField;
    private JComboBox<String> correctOptionComboBox;
    private JButton updateButton, cancelButton;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String questionId;
    private Runnable onQuestionUpdated;

    public UpdateQuestionUI(Socket socket, BufferedReader in, PrintWriter out,
            String questionId, String question, String optionA, String optionB,
            String optionC, String optionD, String correct, Runnable onQuestionUpdated) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.questionId = questionId;
        this.onQuestionUpdated = onQuestionUpdated;

        setTitle("Edit Question");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(520, 480);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout()); // Center the card panel

        JPanel cardPanel = new JPanel(new GridBagLayout());
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(25, 25, 25, 25)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel title = new JLabel("Update Exam Question", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(40, 40, 40));
        cardPanel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy++;

        addField(cardPanel, gbc, "Question:", questionField = new JTextField(question, 30));
        addField(cardPanel, gbc, "Option A:", optionAField = new JTextField(optionA, 30));
        addField(cardPanel, gbc, "Option B:", optionBField = new JTextField(optionB, 30));
        addField(cardPanel, gbc, "Option C:", optionCField = new JTextField(optionC, 30));
        addField(cardPanel, gbc, "Option D:", optionDField = new JTextField(optionD, 30));

        // Correct Option Dropdown
        gbc.gridx = 0;
        gbc.gridy++;
        cardPanel.add(createStyledLabel("Correct Option:"), gbc);

        gbc.gridx = 1;
        correctOptionComboBox = new JComboBox<>(new String[] { "A", "B", "C", "D" });
        correctOptionComboBox.setSelectedItem(correct);
        correctOptionComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        correctOptionComboBox.setPreferredSize(new Dimension(200, 30));
        cardPanel.add(correctOptionComboBox, gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);
        updateButton = createButton("Update", new Color(0, 123, 255));
        cancelButton = createButton("Cancel", new Color(220, 53, 69));
        buttonPanel.add(updateButton);
        buttonPanel.add(cancelButton);
        cardPanel.add(buttonPanel, gbc);

        updateButton.addActionListener(e -> handleUpdateQuestion());
        cancelButton.addActionListener(e -> dispose());

        // Final Frame Styling
        getContentPane().setBackground(new Color(245, 245, 245));
        add(cardPanel);
        setVisible(true);
    }

    private void addField(JPanel panel, GridBagConstraints gbc, String label, JTextField field) {
        gbc.gridx = 0;
        panel.add(createStyledLabel(label), gbc);
        gbc.gridx = 1;
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(250, 30));
        panel.add(field, gbc);
        gbc.gridy++;
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        label.setForeground(new Color(60, 60, 60));
        return label;
    }

    private JButton createButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(110, 36));
        button.setBorder(BorderFactory.createEmptyBorder());
        return button;
    }

    private void handleUpdateQuestion() {
        String question = questionField.getText().trim();
        String optionA = optionAField.getText().trim();
        String optionB = optionBField.getText().trim();
        String optionC = optionCField.getText().trim();
        String optionD = optionDField.getText().trim();
        String correct = (String) correctOptionComboBox.getSelectedItem();

        if (question.isEmpty() || optionA.isEmpty() || optionB.isEmpty() ||
                optionC.isEmpty() || optionD.isEmpty() || correct == null) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            out.println("UPDATE_QUESTION");
            out.println(questionId);
            out.println(question);
            out.println(optionA);
            out.println(optionB);
            out.println(optionC);
            out.println(optionD);
            out.println(correct);

            String response = in.readLine();
            if ("SUCCESS".equals(response)) {
                JOptionPane.showMessageDialog(this, "Question updated successfully!");
                if (onQuestionUpdated != null)
                    onQuestionUpdated.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update question: " + response, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
