import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ManageQuestionsUI extends JFrame {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String examName;
    private boolean isPublished;
    private JTable questionTable;
    private DefaultTableModel tableModel;
    private JButton addQuestionButton;
    private JButton updateQuestionButton;
    private JButton deleteQuestionButton;
    private List<String> questionIds = new ArrayList<>();

    public ManageQuestionsUI(Socket socket, BufferedReader in, PrintWriter out, String examName, boolean isPublished) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.examName = examName;
        this.isPublished = isPublished;

        setTitle("Manage Questions - " + examName);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());

        String[] columnNames = { "Question", "Option A", "Option B", "Option C", "Option D", "Correct Answer" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        questionTable = new JTable(tableModel);
        questionTable.setRowHeight(30);
        questionTable.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        questionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        questionTable.getTableHeader().setBackground(new Color(230, 230, 230));
        questionTable.setGridColor(new Color(220, 220, 220));
        questionTable.setSelectionBackground(new Color(204, 228, 255));
        questionTable.setSelectionForeground(Color.BLACK);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < questionTable.getColumnCount(); i++) {
            questionTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(questionTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 30, 10, 30));
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        buttonPanel.setBackground(new Color(245, 245, 245));

        addQuestionButton = createStyledButton("Add Question", new Color(0, 123, 255));
        deleteQuestionButton = createStyledButton("Delete", new Color(220, 53, 69));
        updateQuestionButton = createStyledButton("Update", new Color(40, 167, 69));

        updateQuestionButton.setEnabled(false);
        deleteQuestionButton.setEnabled(false);

        buttonPanel.add(addQuestionButton);
        buttonPanel.add(updateQuestionButton);
        buttonPanel.add(deleteQuestionButton);

        add(buttonPanel, BorderLayout.SOUTH);

        addQuestionButton.addActionListener(e -> {
            if (isPublished) {
                JOptionPane.showMessageDialog(this, "Please unpublish the exam first.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            addQuestion();
        });

        deleteQuestionButton.addActionListener(e -> {
            if (isPublished) {
                JOptionPane.showMessageDialog(this, "Please unpublish the exam first.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            deleteQuestion();
        });

        updateQuestionButton.addActionListener(e -> {
            if (isPublished) {
                JOptionPane.showMessageDialog(this, "Please unpublish the exam first.", "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            updateQuestion();
        });

        questionTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = questionTable.getSelectedRow();
            updateQuestionButton.setEnabled(selectedRow != -1);
            deleteQuestionButton.setEnabled(selectedRow != -1);
        });

        loadQuestions();

        getContentPane().setBackground(Color.WHITE);
        setVisible(true);
    }

    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(160, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder());
        return button;
    }

    private void loadQuestions() {
        try {
            out.println("GET_QUESTIONS_FOR_EXAM");
            out.println(examName);
            tableModel.setRowCount(0);
            questionIds.clear();
            String response = in.readLine();
            if ("QUESTIONS_LIST".equals(response)) {
                int count = Integer.parseInt(in.readLine());
                for (int i = 0; i < count; i++) {
                    String questionId = in.readLine();
                    String question = in.readLine();
                    String optionA = in.readLine();
                    String optionB = in.readLine();
                    String optionC = in.readLine();
                    String optionD = in.readLine();
                    String correct = in.readLine();
                    tableModel.addRow(new Object[] { question, optionA, optionB, optionC, optionD, correct });
                    questionIds.add(questionId);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load questions: " + response, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addQuestion() {
        new AddQuestionUI(socket, in, out, examName, this::loadQuestions);
    }

    private void deleteQuestion() {
        int selectedRow = questionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a question to delete.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String questionId = questionIds.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this question?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;

        try {
            out.println("DELETE_QUESTION");
            out.println(questionId);
            String response = in.readLine();
            if ("SUCCESS".equals(response)) {
                JOptionPane.showMessageDialog(this, "Question deleted successfully.");
                loadQuestions();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete question: " + response, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateQuestion() {
        int selectedRow = questionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a question to update.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        String questionId = questionIds.get(selectedRow);
        String question = (String) tableModel.getValueAt(selectedRow, 0);
        String optionA = (String) tableModel.getValueAt(selectedRow, 1);
        String optionB = (String) tableModel.getValueAt(selectedRow, 2);
        String optionC = (String) tableModel.getValueAt(selectedRow, 3);
        String optionD = (String) tableModel.getValueAt(selectedRow, 4);
        String correct = (String) tableModel.getValueAt(selectedRow, 5);

        new UpdateQuestionUI(socket, in, out, questionId, question, optionA, optionB, optionC, optionD, correct,
                this::loadQuestions);
    }
}
