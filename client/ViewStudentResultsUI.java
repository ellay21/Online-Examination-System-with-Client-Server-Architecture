import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ViewStudentResultsUI extends JFrame {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private JTable examTable;
    private DefaultTableModel examTableModel;
    private JButton viewResultsButton;

    public ViewStudentResultsUI(Socket socket, BufferedReader in, PrintWriter out, String username) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.username = username;

        setTitle("📊 View Student Results");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        JPanel container = new JPanel(new BorderLayout(20, 20));
        container.setBorder(new EmptyBorder(30, 50, 30, 50));
        container.setBackground(new Color(245, 245, 245));
        setContentPane(container);

        String[] examColumns = { "Exam Name", "Subject Name", "Start Time", "End Time" };
        examTableModel = new DefaultTableModel(examColumns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        examTable = new JTable(examTableModel);
        examTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(examTable);

        JScrollPane scrollPane = new JScrollPane(examTable);
        scrollPane.getViewport().setBackground(Color.WHITE);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));

        viewResultsButton = createButton("View Results");
        viewResultsButton.setEnabled(false);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setBackground(container.getBackground());
        buttonPanel.add(viewResultsButton);

        container.add(scrollPane, BorderLayout.CENTER);
        container.add(buttonPanel, BorderLayout.SOUTH);

        examTable.getSelectionModel().addListSelectionListener(e -> {
            viewResultsButton.setEnabled(examTable.getSelectedRow() != -1);
        });

        viewResultsButton.addActionListener(e -> handleViewResults());

        loadExams();
        setVisible(true);
    }

    private void loadExams() {
        try {
            out.println("GET_TEACHER_EXAMS");
            out.println(username);
            examTableModel.setRowCount(0);
            String response = in.readLine();
            if ("EXAMS_LIST".equals(response)) {
                int count = Integer.parseInt(in.readLine());
                for (int i = 0; i < count; i++) {
                    String examName = in.readLine();
                    String subjectName = in.readLine();
                    String startTime = in.readLine();
                    String endTime = in.readLine();
                    examTableModel.addRow(new Object[] { examName, subjectName, startTime, endTime });
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

    private void handleViewResults() {
        int selectedRow = examTable.getSelectedRow();
        if (selectedRow == -1)
            return;
        String examName = (String) examTableModel.getValueAt(selectedRow, 0);

        try {
            out.println("GET_STUDENT_RESULTS_FOR_EXAM");
            out.println(examName);
            String response = in.readLine();
            if ("STUDENT_RESULTS_LIST".equals(response)) {
                int count = Integer.parseInt(in.readLine());
                int totalQuestions = Integer.parseInt(in.readLine());
                String[] columns = { "Student ID", "Student Name", "Score (out of " + totalQuestions + ")" };
                DefaultTableModel resultsModel = new DefaultTableModel(columns, 0) {
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };
                for (int i = 0; i < count; i++) {
                    String studentId = in.readLine();
                    String studentName = in.readLine();
                    String score = in.readLine();
                    resultsModel.addRow(new Object[] { studentId, studentName, score });
                }

                JTable resultsTable = new JTable(resultsModel);
                styleTable(resultsTable);
                JScrollPane resultsScroll = new JScrollPane(resultsTable);
                resultsScroll.setPreferredSize(new Dimension(700, 300));
                resultsScroll.getViewport().setBackground(Color.WHITE);
                JOptionPane.showMessageDialog(this, resultsScroll, "Results - " + examName,
                        JOptionPane.PLAIN_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load student results: " + response, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleTable(JTable table) {
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setForeground(Color.BLACK);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        table.setRowHeight(28);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(173, 216, 230)); // Light blue
        table.setSelectionForeground(Color.BLACK);
        table.setShowVerticalLines(false);
        table.getTableHeader().setBackground(new Color(240, 240, 240));
        table.getTableHeader().setForeground(Color.DARK_GRAY);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 16));
        table.setIntercellSpacing(new Dimension(0, 0));
        ((DefaultTableCellRenderer) table.getDefaultRenderer(Object.class))
                .setHorizontalAlignment(SwingConstants.CENTER);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(100, 149, 237)); // Cornflower Blue
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        return button;
    }
}
