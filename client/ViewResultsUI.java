import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.net.Socket;

public class ViewResultsUI extends JFrame {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private JTable examTable;
    private DefaultTableModel tableModel;
    private JButton viewResultButton;

    public ViewResultsUI(Socket socket, BufferedReader in, PrintWriter out, String username) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.username = username;

        setTitle("📊 View Exam Results");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(720, 480);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(Color.decode("#f4f6f8"));

        String[] columnNames = { "Exam", "Subject", "Start Time", "End Time" };
        tableModel = new DefaultTableModel(columnNames, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        examTable = new JTable(tableModel);
        examTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        examTable.setRowHeight(28);
        examTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        examTable.getTableHeader().setBackground(Color.decode("#2e86de"));
        examTable.getTableHeader().setForeground(Color.WHITE);
        examTable.setSelectionBackground(Color.decode("#dff9fb"));
        examTable.setSelectionForeground(Color.BLACK);
        examTable.setGridColor(Color.LIGHT_GRAY);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < examTable.getColumnCount(); i++) {
            examTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(examTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        viewResultButton = new JButton("View Result");
        viewResultButton.setEnabled(false);
        viewResultButton.setBackground(Color.decode("#2ecc71"));
        viewResultButton.setForeground(Color.WHITE);
        viewResultButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        viewResultButton.setFocusPainted(false);
        viewResultButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        viewResultButton.setPreferredSize(new Dimension(160, 40));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.decode("#f4f6f8"));
        buttonPanel.setBorder(new EmptyBorder(10, 0, 20, 0));
        buttonPanel.add(viewResultButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        examTable.getSelectionModel().addListSelectionListener(e -> {
            viewResultButton.setEnabled(examTable.getSelectedRow() != -1);
        });

        viewResultButton.addActionListener(e -> handleViewResult());

        loadExams();

        setVisible(true);
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

    private void handleViewResult() {
        int selectedRow = examTable.getSelectedRow();
        if (selectedRow == -1)
            return;

        String examName = (String) tableModel.getValueAt(selectedRow, 0);

        try {
            out.println("GET_RESULT_FOR_EXAM");
            out.println(username);
            out.println(examName);
            String response = in.readLine();
            if ("RESULT_FOUND".equals(response)) {
                int score = Integer.parseInt(in.readLine());
                int total = Integer.parseInt(in.readLine());
                showElegantMessage("Result for " + examName,
                        "✅ You scored <b>" + score + "/" + total + "</b>");
            } else if ("RESULT_NOT_FOUND".equals(response)) {
                showElegantMessage("Result for " + examName,
                        "⚠️ You haven't taken this exam yet.");
            } else {
                showElegantMessage("Error", "❌ " + response);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showElegantMessage(String title, String htmlMessage) {
        JLabel label = new JLabel(
                "<html><div style='font-family:Segoe UI; font-size:14px;'>" + htmlMessage + "</div></html>");
        label.setBorder(new EmptyBorder(15, 25, 15, 25));
        JOptionPane.showMessageDialog(this, label, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
