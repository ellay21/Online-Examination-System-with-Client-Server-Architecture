import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.LinkedHashMap;

public class AddExamUI extends JFrame {
    private JTextField examNameField;
    private JComboBox<String> subjectComboBox;
    private JButton addExamButton, cancelButton, addSubjectButton;
    private JSpinner startTimeSpinner, endTimeSpinner;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private LinkedHashMap<String, Integer> subjectMap = new LinkedHashMap<>();

    public AddExamUI(Socket socket, BufferedReader in, PrintWriter out, String username, Runnable onExamAdded) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.username = username;

        setTitle("➕ Add New Exam");
        setSize(500, 420);
        setLocationRelativeTo(null);
        setUndecorated(true);
        getRootPane().setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180), 1, true));
        setLayout(new BorderLayout());

        JPanel contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        contentPanel.setLayout(new GridBagLayout());

        JLabel title = new JLabel("Add New Exam");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        title.setForeground(new Color(50, 50, 50));
        add(title, BorderLayout.NORTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(14, 14, 14, 14);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // Row 1 - Exam Name
        gbc.gridx = 0;
        gbc.gridy = row;
        JLabel nameLabel = new JLabel("Exam Name:");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        contentPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        examNameField = new JTextField(20);
        styleTextField(examNameField);
        contentPanel.add(examNameField, gbc);

        // Row 2 - Subject
        gbc.gridx = 0;
        gbc.gridy = ++row;
        JLabel subjectLabel = new JLabel("Subject:");
        subjectLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        contentPanel.add(subjectLabel, gbc);

        gbc.gridx = 1;
        JPanel subjectPanel = new JPanel(new BorderLayout());
        subjectPanel.setOpaque(false);
        subjectComboBox = new JComboBox<>();
        subjectComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subjectComboBox.setPreferredSize(new Dimension(200, 28));
        subjectPanel.add(subjectComboBox, BorderLayout.CENTER);

        addSubjectButton = new JButton("+");
        styleButton(addSubjectButton, new Color(200, 200, 200));
        addSubjectButton.setPreferredSize(new Dimension(45, 28));
        subjectPanel.add(addSubjectButton, BorderLayout.EAST);

        contentPanel.add(subjectPanel, gbc);

        // Row 3 - Start Time
        gbc.gridx = 0;
        gbc.gridy = ++row;
        JLabel startLabel = new JLabel("Start Time:");
        startLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        contentPanel.add(startLabel, gbc);

        gbc.gridx = 1;
        startTimeSpinner = new JSpinner(new SpinnerDateModel());
        styleSpinner(startTimeSpinner);
        contentPanel.add(startTimeSpinner, gbc);

        // Row 4 - End Time
        gbc.gridx = 0;
        gbc.gridy = ++row;
        JLabel endLabel = new JLabel("End Time:");
        endLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        contentPanel.add(endLabel, gbc);

        gbc.gridx = 1;
        endTimeSpinner = new JSpinner(new SpinnerDateModel());
        styleSpinner(endTimeSpinner);
        contentPanel.add(endTimeSpinner, gbc);

        // Row 5 - Buttons
        gbc.gridx = 0;
        gbc.gridy = ++row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        addExamButton = new JButton("Add Exam");
        cancelButton = new JButton("Cancel");
        styleButton(addExamButton, new Color(76, 175, 80));
        styleButton(cancelButton, new Color(244, 67, 54));
        buttonPanel.add(addExamButton);
        buttonPanel.add(cancelButton);

        contentPanel.add(buttonPanel, gbc);

        add(contentPanel, BorderLayout.CENTER);

        loadSubjects();

        addExamButton.addActionListener(e -> handleAddExam(onExamAdded));
        cancelButton.addActionListener(e -> dispose());
        addSubjectButton.addActionListener(e -> new ManageSubjectUI(socket, in, out, this::loadSubjects));

        setVisible(true);
    }

    private void loadSubjects() {
        try {
            out.println("GET_SUBJECTS");
            String response = in.readLine();
            if ("SUBJECTS_LIST".equals(response)) {
                int count = Integer.parseInt(in.readLine());
                subjectComboBox.removeAllItems();
                subjectMap.clear();
                for (int i = 0; i < count; i++) {
                    String subjectId = in.readLine();
                    String subjectName = in.readLine();
                    subjectComboBox.addItem(subjectName);
                    subjectMap.put(subjectName, Integer.parseInt(subjectId));
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load subjects: " + response, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleAddExam(Runnable onExamAdded) {
        String examName = examNameField.getText().trim();
        String subjectName = (String) subjectComboBox.getSelectedItem();
        Integer subjectId = subjectMap.get(subjectName);
        Date startDate = (Date) startTimeSpinner.getValue();
        Date endDate = (Date) endTimeSpinner.getValue();

        if (examName.isEmpty() || subjectId == null) {
            JOptionPane.showMessageDialog(this, "Please enter all fields.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (endDate.before(startDate)) {
            JOptionPane.showMessageDialog(this, "End time must be after start time.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            out.println("ADD_EXAM");
            out.println(username);
            out.println(examName);
            out.println(subjectId);
            out.println(startDate.getTime());
            out.println(endDate.getTime());
            String response = in.readLine();
            if ("SUCCESS".equals(response)) {
                JOptionPane.showMessageDialog(this, "✅ Exam added successfully!");
                if (onExamAdded != null)
                    onExamAdded.run();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add exam: " + response, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(200, 200, 200), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        field.setBackground(Color.WHITE);
        field.setPreferredSize(new Dimension(200, 28));
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(bgColor);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(bgColor.darker(), 1, true));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(130, 36));
    }

    private void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        spinner.setPreferredSize(new Dimension(200, 28));
    }
}
