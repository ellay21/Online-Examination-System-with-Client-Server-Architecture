import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

public class ManageExamsUI extends JFrame {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private JTable examTable;
    private DefaultTableModel tableModel;

    private JButton addExamButton;
    private JButton manageQuestionsButton;
    private JButton publishButton;
    private JButton unpublishButton;
    private JButton updateExamButton;
    private JButton deleteExamButton;

    public ManageExamsUI(Socket socket, BufferedReader in, PrintWriter out, String username) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.username = username;

        setTitle("📘 Manage Exams");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);

        // Global styling
        UIManager.put("Table.showGrid", false);
        UIManager.put("Table.intercellSpacing", new Dimension(0, 0));
        setUIFont(new Font("Segoe UI", Font.PLAIN, 14));

        String[] columnNames = { "Exam Name", "Subject Name", "Start Time", "End Time" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        examTable = new JTable(tableModel);
        examTable.setRowHeight(28);
        examTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        examTable.setFillsViewportHeight(true);
        examTable.setShowHorizontalLines(false);
        examTable.setShowVerticalLines(false);
        examTable.setBorder(new EmptyBorder(10, 10, 10, 10));
        ((DefaultTableCellRenderer) examTable.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);

        JScrollPane scrollPane = new JScrollPane(examTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addExamButton = createButton("Add Exam");
        manageQuestionsButton = createButton("Manage Questions");
        publishButton = createButton("Publish");
        unpublishButton = createButton("Unpublish");
        updateExamButton = createButton("Update Exam");
        deleteExamButton = createButton("Delete Exam");

        manageQuestionsButton.setEnabled(false);
        publishButton.setEnabled(false);
        unpublishButton.setEnabled(false);
        updateExamButton.setEnabled(false);
        deleteExamButton.setEnabled(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(addExamButton);
        buttonPanel.add(manageQuestionsButton);
        buttonPanel.add(updateExamButton);
        buttonPanel.add(deleteExamButton);
        buttonPanel.add(publishButton);
        buttonPanel.add(unpublishButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        examTable.getSelectionModel().addListSelectionListener(e -> {
            boolean isRowSelected = examTable.getSelectedRow() != -1;
            manageQuestionsButton.setEnabled(isRowSelected);
            publishButton.setEnabled(isRowSelected);
            unpublishButton.setEnabled(isRowSelected);
            updateExamButton.setEnabled(isRowSelected);
            deleteExamButton.setEnabled(isRowSelected);
        });

        addExamButton.addActionListener(e -> addExam());
        manageQuestionsButton.addActionListener(e -> manageQuestions());
        publishButton.addActionListener(e -> publishExam());
        unpublishButton.addActionListener(e -> unpublishExam());
        updateExamButton.addActionListener(e -> updateExam());
        deleteExamButton.addActionListener(e -> deleteExam());

        loadExams();
        setVisible(true);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return button;
    }

    private void setUIFont(Font f) {
        UIManager.put("Label.font", f);
        UIManager.put("Button.font", f);
        UIManager.put("Table.font", f);
        UIManager.put("TableHeader.font", f.deriveFont(Font.BOLD));
        UIManager.put("TextField.font", f);
        UIManager.put("TextArea.font", f);
        UIManager.put("ComboBox.font", f);
        UIManager.put("CheckBox.font", f);
        UIManager.put("RadioButton.font", f);
    }

    private void loadExams() {
        try {
            out.println("GET_TEACHER_EXAMS");
            out.println(username);
            tableModel.setRowCount(0);
            String response = in.readLine();
            if ("EXAMS_LIST".equals(response)) {
                int count = Integer.parseInt(in.readLine());
                for (int i = 0; i < count; i++) {
                    String examName = in.readLine();
                    String subjectName = in.readLine();
                    String startTime = in.readLine();
                    String endTime = in.readLine();
                    tableModel.addRow(new Object[] { examName, subjectName, startTime, endTime });
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load exams: " + response, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addExam() {
        new AddExamUI(socket, in, out, username, this::loadExams);
    }

    private void updateExam() {
        int selectedRow = examTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an exam to update.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String examName = (String) tableModel.getValueAt(selectedRow, 0);
        new UpdateExamUI(socket, in, out, username, examName, this::loadExams);
    }

    private void deleteExam() {
    }

    private void manageQuestions() {
        int selectedRow = examTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an exam to manage questions.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String examName = (String) tableModel.getValueAt(selectedRow, 0);
        boolean isPublished = isExamPublished(examName);
        new ManageQuestionsUI(socket, in, out, examName, isPublished);
    }

    private void publishExam() {
        int selectedRow = examTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an exam to publish.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String examName = (String) tableModel.getValueAt(selectedRow, 0);

        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to publish " + examName + "?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                out.println("PUBLISH_EXAM");
                out.println(examName);
                String response = in.readLine();
                if ("SUCCESS".equals(response)) {
                    JOptionPane.showMessageDialog(this, "Exam published successfully.", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadExams();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to publish exam: " + response, "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void unpublishExam() {
        int selectedRow = examTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an exam to unpublish.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String examName = (String) tableModel.getValueAt(selectedRow, 0);
        int choice = JOptionPane.showConfirmDialog(this, "Are you sure you want to unpublish " + examName + "?",
                "Confirmation", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            try {
                out.println("UNPUBLISH_EXAM");
                out.println(examName);
                String response = in.readLine();
                if ("SUCCESS".equals(response)) {
                    JOptionPane.showMessageDialog(this, "Exam unpublished successfully.", "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadExams();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to unpublish exam: " + response, "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean isExamPublished(String examName) {
        try {
            out.println("GET_EXAM_PUBLISHED_STATUS");
            out.println(examName);
            String response = in.readLine();
            if ("PUBLISHED_STATUS".equals(response)) {
                return Boolean.parseBoolean(in.readLine());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to get published status: " + response, "Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
}
