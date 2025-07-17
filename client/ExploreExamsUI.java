import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExploreExamsUI extends JFrame {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private JTable examTable;
    private DefaultTableModel tableModel;
    private JButton takeExamButton;

    public ExploreExamsUI(Socket socket, BufferedReader in, PrintWriter out, String username) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.username = username;

        setTitle("Explore Exams - " + username);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Fullscreen

        // Main panel
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(245, 245, 245)); // Light gray

        // Table model and table
        String[] columnNames = { "Exam Name", "Subject Name", "Start Time", "End Time" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        examTable = new JTable(tableModel);
        examTable.setRowHeight(28);
        examTable.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        examTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JTableHeader tableHeader = examTable.getTableHeader();
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableHeader.setBackground(new Color(230, 230, 230));

        JScrollPane scrollPane = new JScrollPane(examTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button
        takeExamButton = new JButton("Take Exam");
        takeExamButton.setEnabled(false);
        styleButton(takeExamButton);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245, 245, 245));
        buttonPanel.add(takeExamButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 30, 0));

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);

        // Event bindings
        examTable.getSelectionModel().addListSelectionListener(e -> {
            takeExamButton.setEnabled(examTable.getSelectedRow() != -1);
        });

        takeExamButton.addActionListener(e -> handleTakeExam());

        loadExams();
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setBackground(new Color(70, 130, 180)); // Steel blue
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 50));
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(60, 110, 160));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180));
            }
        });
    }

    private void loadExams() {
        try {
            out.println("GET_PUBLISHED_EXAMS");
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

    private void handleTakeExam() {
        int selectedRow = examTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an exam to take.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String examName = (String) tableModel.getValueAt(selectedRow, 0);
        String startTimeStr = (String) tableModel.getValueAt(selectedRow, 2);
        String endTimeStr = (String) tableModel.getValueAt(selectedRow, 3);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date startTime = sdf.parse(startTimeStr);
            Date endTime = sdf.parse(endTimeStr);
            Date now = new Date();

            if (now.before(startTime)) {
                JOptionPane.showMessageDialog(this, "Exam is scheduled for the future. You can't take it now.",
                        "Info", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (now.after(endTime)) {
                JOptionPane.showMessageDialog(this, "Exam is over. You can't take it.", "Info",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            if (hasStudentTakenExam(username, examName)) {
                JOptionPane.showMessageDialog(this, "You have already taken this exam.", "Info",
                        JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Calculate remaining time
            long timeLeftMillis = endTime.getTime() - now.getTime();
            new TakeExamUI(socket, in, out, username, examName, timeLeftMillis);

        } catch (ParseException e) {
            JOptionPane.showMessageDialog(this, "Error parsing date: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean hasStudentTakenExam(String username, String examName) {
        try {
            out.println("HAS_STUDENT_TAKEN_EXAM");
            out.println(username);
            out.println(examName);
            String response = in.readLine();
            if ("EXAM_TAKEN_STATUS".equals(response)) {
                return Boolean.parseBoolean(in.readLine());
            } else {
                JOptionPane.showMessageDialog(this, "Failed to check exam status: " + response, "Error",
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