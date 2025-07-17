import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class TakeExamUI extends JFrame {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;
    private String examName;
    private long timeLeftMillis;

    private List<Question> questions = new ArrayList<>();
    private Map<Integer, String> answers = new HashMap<>();
    private int currentIndex = 0;

    private JLabel questionLabel;
    private JRadioButton[] optionButtons;
    private ButtonGroup optionGroup;
    private JButton backButton, nextButton, submitButton;
    private JLabel timerLabel;
    private Timer timer;

    public TakeExamUI(Socket socket, BufferedReader in, PrintWriter out, String username, String examName,
            long timeLeftMillis) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.username = username;
        this.examName = examName;
        this.timeLeftMillis = timeLeftMillis;

        setTitle("📘 Take Exam: " + examName);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Prevent closing without confirmation
        setSize(700, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(Color.decode("#f4f6f8"));

        // Header
        JLabel header = new JLabel("Exam: " + examName, SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(Color.decode("#2c3e50"));
        header.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(header, BorderLayout.NORTH);

        // Timer Label
        timerLabel = new JLabel();
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(timerLabel, BorderLayout.NORTH);

        // Center card panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        centerPanel.setBackground(Color.WHITE);

        // Card border
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(new CompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1, true),
                new EmptyBorder(20, 20, 20, 20)));
        cardPanel.setBackground(Color.WHITE);

        questionLabel = new JLabel("Question");
        questionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        questionLabel.setBorder(new EmptyBorder(0, 0, 20, 0));
        cardPanel.add(questionLabel);

        optionButtons = new JRadioButton[4];
        optionGroup = new ButtonGroup();
        for (int i = 0; i < 4; i++) {
            optionButtons[i] = new JRadioButton();
            optionButtons[i].setFont(new Font("Segoe UI", Font.PLAIN, 16));
            optionButtons[i].setBackground(Color.WHITE);
            optionButtons[i].setFocusPainted(false);
            optionGroup.add(optionButtons[i]);
            cardPanel.add(Box.createVerticalStrut(8));
            cardPanel.add(optionButtons[i]);
        }

        centerPanel.add(cardPanel);
        add(centerPanel, BorderLayout.CENTER);

        // Bottom button panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottomPanel.setBackground(Color.decode("#f4f6f8"));

        backButton = createStyledButton("← Back");
        nextButton = createStyledButton("Next →");
        submitButton = createStyledButton("Submit");

        bottomPanel.add(backButton);
        bottomPanel.add(nextButton);
        bottomPanel.add(submitButton);

        add(bottomPanel, BorderLayout.SOUTH);

        backButton.addActionListener(e -> goBack());
        nextButton.addActionListener(e -> goNext());
        submitButton.addActionListener(e -> submitExam(true)); // True for manual submission

        loadQuestionsFromServer();
        startTimer(timeLeftMillis);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                showCloseConfirmation();
            }
        });

        setVisible(true);
    }

    private void startTimer(long durationMillis) {
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeLeftMillis -= 1000;
                if (timeLeftMillis <= 0) {
                    timer.stop();
                    submitExam(false); // False for automatic submission
                    JOptionPane.showMessageDialog(TakeExamUI.this, "Time's up! Exam automatically submitted.");
                }
                updateTimerLabel();
            }
        });
        timer.start();
        updateTimerLabel();
    }

    private void updateTimerLabel() {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(timeLeftMillis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(timeLeftMillis) % 60;
        timerLabel.setText(String.format("Time Remaining: %02d:%02d", minutes, seconds));
    }

    private void showCloseConfirmation() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to close the exam? This will automatically submit your answers.",
                "Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            submitExam(false); // False for automatic submission
            dispose();
        }
        // If choice is NO, the window stays open, and no action is needed.
    }

    private JButton createStyledButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(52, 152, 219));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));
        return btn;
    }

    private void loadQuestionsFromServer() {
        try {
            out.println("GET_QUESTIONS_FOR_EXAM");
            out.println(examName);
            String response = in.readLine();
            if ("QUESTIONS_LIST".equals(response)) {
                int count = Integer.parseInt(in.readLine());
                for (int i = 0; i < count; i++) {
                    int questionId = Integer.parseInt(in.readLine());
                    String questionText = in.readLine();
                    String optionA = in.readLine();
                    String optionB = in.readLine();
                    String optionC = in.readLine();
                    String optionD = in.readLine();
                    in.readLine(); // skip correct answer
                    questions.add(new Question(questionId, questionText, optionA, optionB, optionC, optionD));
                }
                if (!questions.isEmpty()) {
                    showQuestion(0);
                } else {
                    JOptionPane.showMessageDialog(this, "No questions found for this exam.", "Info",
                            JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Failed to load questions: " + response, "Error",
                        JOptionPane.ERROR_MESSAGE);
                dispose();
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void showQuestion(int index) {
        currentIndex = index;
        Question q = questions.get(index);
        questionLabel.setText("<html><b>Q" + (index + 1) + ":</b> " + q.text + "</html>");
        optionButtons[0].setText(q.optionA);
        optionButtons[1].setText(q.optionB);
        optionButtons[2].setText(q.optionC);
        optionButtons[3].setText(q.optionD);

        optionGroup.clearSelection();
        String selected = answers.get(q.id);
        if (selected != null) {
            switch (selected) {
                case "A" -> optionButtons[0].setSelected(true);
                case "B" -> optionButtons[1].setSelected(true);
                case "C" -> optionButtons[2].setSelected(true);
                case "D" -> optionButtons[3].setSelected(true);
            }
        }

        backButton.setEnabled(index > 0);
        nextButton.setEnabled(index < questions.size() - 1);
        submitButton.setEnabled(index == questions.size() - 1);
    }

    private void goBack() {
        saveCurrentAnswer(true); // Save current answer with default if needed
        if (currentIndex > 0) {
            showQuestion(currentIndex - 1);
        }
    }

    private void goNext() {
        saveCurrentAnswer(true); // Save current answer with default if needed
        if (currentIndex < questions.size() - 1) {
            showQuestion(currentIndex + 1);
        }
    }

    private boolean saveCurrentAnswer(boolean allowDefault) {
        int selected = -1;
        for (int i = 0; i < 4; i++) {
            if (optionButtons[i].isSelected()) {
                selected = i;
                break;
            }
        }
        String answer = ""; // Default value if no option is selected
        if (selected != -1) {
            answer = switch (selected) {
                case 0 -> "A";
                case 1 -> "B";
                case 2 -> "C";
                case 3 -> "D";
                default -> "";
            };
        } else if (!allowDefault) {
            JOptionPane.showMessageDialog(this, "Please select an option before proceeding.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }
        answers.put(questions.get(currentIndex).id, answer);
        return true;
    }

    private void submitExam(boolean isManualSubmission) {
        if (isManualSubmission) {
            // Check if all questions are answered and not default
            for (Question q : questions) {
                if (!answers.containsKey(q.id) || answers.get(q.id).isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Please answer all questions before submitting using the Submit button.", "Warning",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }
        try {
            out.println("SUBMIT_EXAM");
            out.println(username);
            out.println(examName);
            out.println(answers.size());
            for (Question q : questions) {
                String selected = answers.get(q.id);
                out.println(q.id);
                out.println(selected != null ? selected : "");
            }
            String response = in.readLine();
            if ("SUCCESS".equals(response)) {
                JOptionPane.showMessageDialog(this, "Exam submitted successfully!");
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to submit exam: " + response, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Connection error: " + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private static class Question {
        int id;
        String text, optionA, optionB, optionC, optionD;

        Question(int id, String text, String a, String b, String c, String d) {
            this.id = id;
            this.text = text;
            this.optionA = a;
            this.optionB = b;
            this.optionC = c;
            this.optionD = d;
        }
    }
}