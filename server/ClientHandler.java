import java.io.*;
import java.net.Socket;
import java.sql.*;

public class ClientHandler implements Runnable {
    private Socket clientSocket;
    private Connection dbConn;
    private BufferedReader in;
    private PrintWriter out;

    public ClientHandler(Socket clientSocket, Connection dbConn) {
        this.clientSocket = clientSocket;
        this.dbConn = dbConn;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            String command;
            while ((command = in.readLine()) != null) {
                if ("LOGIN".equalsIgnoreCase(command)) {
                    handleLogin();
                } else if ("REGISTER".equalsIgnoreCase(command)) {
                    handleRegister();
                } else if ("GET_TEACHER_EXAMS".equalsIgnoreCase(command)) {
                    handleGetTeacherExams();
                } else if ("GET_SUBJECTS".equalsIgnoreCase(command)) {
                    handleGetSubjects();
                } else if ("ADD_EXAM".equalsIgnoreCase(command)) {
                    handleAddExam();
                } else if ("ADD_SUBJECT".equalsIgnoreCase(command)) {
                    handleAddSubject();
                } else if ("GET_QUESTIONS_FOR_EXAM".equalsIgnoreCase(command)) {
                    handleGetQuestionsForExam();
                } else if ("ADD_QUESTION".equalsIgnoreCase(command)) {
                    handleAddQuestion();
                } else if ("GET_ALL_EXAMS".equalsIgnoreCase(command)) {
                    handleGetAllExams();
                } else if ("SUBMIT_EXAM".equalsIgnoreCase(command)) {
                    handleSubmitExam();
                } else if ("GET_RESULT_FOR_EXAM".equalsIgnoreCase(command)) {
                    handleGetResultForExam();
                } else if ("GET_STUDENT_RESULTS_FOR_EXAM".equalsIgnoreCase(command)) {
                    handleGetStudentResultsForExam();
                } else if ("DELETE_QUESTION".equalsIgnoreCase(command)) {
                    handleDeleteQuestion();
                } else if ("UPDATE_QUESTION".equalsIgnoreCase(command)) {
                    handleUpdateQuestion();
                } else if ("PUBLISH_EXAM".equalsIgnoreCase(command)) {
                    handlePublishExam();
                } else if ("UNPUBLISH_EXAM".equalsIgnoreCase(command)) {
                    handleUnpublishExam();
                } else if ("GET_EXAM_PUBLISHED_STATUS".equalsIgnoreCase(command)) {
                    handleGetExamPublishedStatus();
                } else if ("GET_PUBLISHED_EXAMS".equalsIgnoreCase(command)) {
                    handleGetPublishedExams();
                } else if ("HAS_STUDENT_TAKEN_EXAM".equalsIgnoreCase(command)) {
                    handleHasStudentTakenExam();
                } else if ("UPDATE_EXAM".equalsIgnoreCase(command)) {
                    handleUpdateExam();
                } else if ("GET_EXAM_DETAILS".equalsIgnoreCase(command)) {
                    handleGetExamDetails();
                } else {
                    out.println("UNKNOWN_COMMAND");
                }
            }
        } catch (IOException e) {
            System.out.println("Client disconnected: " + clientSocket.getInetAddress());
        } finally {
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
                clientSocket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    private void handleLogin() {
        try {
            String username = in.readLine();
            String password = in.readLine();

            String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, password);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    String role = rs.getString("role");
                    out.println("SUCCESS");
                    out.println(role);
                } else {
                    out.println("Invalid username or password");
                }
            }
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }

    private void handleRegister() {
        try {
            String username = in.readLine();
            String password = in.readLine();
            String fullName = in.readLine();
            String role = in.readLine();

            // Check if username exists
            String checkSql = "SELECT user_id FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = dbConn.prepareStatement(checkSql)) {
                checkStmt.setString(1, username);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    out.println("Username already exists");
                    return;
                }
            }

            // Insert new user
            String insertSql = "INSERT INTO users (username, password, role, full_name) VALUES (?, ?, ?, ?)";
            try (PreparedStatement insertStmt = dbConn.prepareStatement(insertSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, password);
                insertStmt.setString(3, role);
                insertStmt.setString(4, fullName);
                int rows = insertStmt.executeUpdate();
                if (rows > 0) {
                    out.println("SUCCESS");
                } else {
                    out.println("Registration failed");
                }
            }
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }

    private void handleGetTeacherExams() {
        try {
            String username = in.readLine();
            // Get user_id for the given username
            String userIdSql = "SELECT user_id FROM users WHERE username = ?";
            int userId = -1;
            try (PreparedStatement userStmt = dbConn.prepareStatement(userIdSql)) {
                userStmt.setString(1, username);
                ResultSet rs = userStmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                } else {
                    out.println("EXAMS_LIST");
                    out.println("0");
                    return;
                }
            }

            // Join exams and subjects to get exam_name and subject name
            String examsSql = "SELECT e.exam_name, s.name AS subject_name, e.start_time, e.end_time " +
                    "FROM exams e JOIN subjects s ON e.subject_id = s.subject_id " +
                    "WHERE e.user_id = ?";
            try (PreparedStatement examsStmt = dbConn.prepareStatement(examsSql)) {
                examsStmt.setInt(1, userId);
                ResultSet rs = examsStmt.executeQuery();

                java.util.List<String[]> exams = new java.util.ArrayList<>();
                while (rs.next()) {
                    String examName = rs.getString("exam_name");
                    String subjectName = rs.getString("subject_name");
                    String startTime = rs.getString("start_time");
                    String endTime = rs.getString("end_time");
                    exams.add(new String[] { examName, subjectName, startTime, endTime });
                }

                out.println("EXAMS_LIST");
                out.println(exams.size());
                for (String[] exam : exams) {
                    for (String field : exam) {
                        out.println(field);
                    }
                }
            }
        } catch (Exception e) {
            out.println("EXAMS_LIST");
            out.println("0");
        }
    }

    private void handleGetSubjects() {
        try {
            String sql = "SELECT subject_id, name FROM subjects";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                java.util.List<String[]> subjects = new java.util.ArrayList<>();
                while (rs.next()) {
                    subjects.add(new String[] { String.valueOf(rs.getInt("subject_id")), rs.getString("name") });
                }
                out.println("SUBJECTS_LIST");
                out.println(subjects.size());
                for (String[] subj : subjects) {
                    out.println(subj[0]);
                    out.println(subj[1]);
                }
            }
        } catch (Exception e) {
            out.println("SUBJECTS_LIST");
            out.println("0");
        }
    }

    private void handleAddExam() {
        try {
            String username = in.readLine();
            String examName = in.readLine();
            int subjectId = Integer.parseInt(in.readLine());
            long startMillis = Long.parseLong(in.readLine());
            long endMillis = Long.parseLong(in.readLine());

            // Get user_id
            int userId = -1;
            String userSql = "SELECT user_id FROM users WHERE username = ?";
            try (PreparedStatement userStmt = dbConn.prepareStatement(userSql)) {
                userStmt.setString(1, username);
                ResultSet rs = userStmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                } else {
                    out.println("User not found");
                    return;
                }
            }

            String insertSql = "INSERT INTO exams (exam_name, subject_id, user_id, start_time, end_time) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = dbConn.prepareStatement(insertSql)) {
                stmt.setString(1, examName);
                stmt.setInt(2, subjectId);
                stmt.setInt(3, userId);
                stmt.setTimestamp(4, new java.sql.Timestamp(startMillis));
                stmt.setTimestamp(5, new java.sql.Timestamp(endMillis));
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    out.println("SUCCESS");
                } else {
                    out.println("Failed to add exam");
                }
            }
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }

    private void handleAddSubject() {
        try {
            String subjectName = in.readLine();
            // Check if subject already exists
            String checkSql = "SELECT subject_id FROM subjects WHERE name = ?";
            try (PreparedStatement checkStmt = dbConn.prepareStatement(checkSql)) {
                checkStmt.setString(1, subjectName);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    out.println("Subject already exists");
                    return;
                }
            }
            // Insert new subject
            String insertSql = "INSERT INTO subjects (name) VALUES (?)";
            try (PreparedStatement insertStmt = dbConn.prepareStatement(insertSql)) {
                insertStmt.setString(1, subjectName);
                int rows = insertStmt.executeUpdate();
                if (rows > 0) {
                    out.println("SUCCESS");
                } else {
                    out.println("Failed to add subject");
                }
            }
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }

    private void handleGetQuestionsForExam() {
        try {
            String examName = in.readLine();
            // Find exam_id by exam name
            int examId = -1;
            String examIdSql = "SELECT exam_id FROM exams WHERE exam_name = ?";
            try (PreparedStatement examStmt = dbConn.prepareStatement(examIdSql)) {
                examStmt.setString(1, examName);
                ResultSet rs = examStmt.executeQuery();
                if (rs.next()) {
                    examId = rs.getInt("exam_id");
                } else {
                    out.println("QUESTIONS_LIST");
                    out.println("0");
                    return;
                }
            }

            String sql = "SELECT question_id, question_text, option_a, option_b, option_c, option_d, correct_option FROM questions WHERE exam_id = ?";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setInt(1, examId);
                ResultSet rs = stmt.executeQuery();
                java.util.List<String[]> questions = new java.util.ArrayList<>();
                while (rs.next()) {
                    questions.add(new String[] {
                            String.valueOf(rs.getInt("question_id")),
                            rs.getString("question_text"),
                            rs.getString("option_a"),
                            rs.getString("option_b"),
                            rs.getString("option_c"),
                            rs.getString("option_d"),
                            rs.getString("correct_option")
                    });
                }
                out.println("QUESTIONS_LIST");
                out.println(questions.size());
                for (String[] q : questions) {
                    for (String field : q) {
                        out.println(field);
                    }
                }
            }
        } catch (Exception e) {
            out.println("QUESTIONS_LIST");
            out.println("0");
        }
    }

    private void handleAddQuestion() {
        try {
            String examName = in.readLine();
            String questionText = in.readLine();
            String optionA = in.readLine();
            String optionB = in.readLine();
            String optionC = in.readLine();
            String optionD = in.readLine();
            String correctOption = in.readLine();

            // Get exam_id from exam name
            int examId = -1;
            String examIdSql = "SELECT exam_id FROM exams WHERE exam_name = ?";
            try (PreparedStatement examStmt = dbConn.prepareStatement(examIdSql)) {
                examStmt.setString(1, examName);
                ResultSet rs = examStmt.executeQuery();
                if (rs.next()) {
                    examId = rs.getInt("exam_id");
                } else {
                    out.println("Exam not found");
                    return;
                }
            }

            String insertSql = "INSERT INTO questions (exam_id, question_text, option_a, option_b, option_c, option_d, correct_option) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement stmt = dbConn.prepareStatement(insertSql)) {
                stmt.setInt(1, examId);
                stmt.setString(2, questionText);
                stmt.setString(3, optionA);
                stmt.setString(4, optionB);
                stmt.setString(5, optionC);
                stmt.setString(6, optionD);
                stmt.setString(7, correctOption);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    out.println("SUCCESS");
                } else {
                    out.println("Failed to add question");
                }
            }
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }

    private void handleDeleteQuestion() {
        try {
            String questionId = in.readLine();

            // First, delete all answers for this question
            String deleteAnswersSql = "DELETE FROM answers WHERE question_id = ?";
            try (PreparedStatement stmt = dbConn.prepareStatement(deleteAnswersSql)) {
                stmt.setInt(1, Integer.parseInt(questionId));
                stmt.executeUpdate();
            }

            // Now delete the question
            String deleteQuestionSql = "DELETE FROM questions WHERE question_id = ?";
            try (PreparedStatement stmt = dbConn.prepareStatement(deleteQuestionSql)) {
                stmt.setInt(1, Integer.parseInt(questionId));
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    out.println("SUCCESS");
                } else {
                    out.println("Failed to delete question");
                }
            }
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }

    private void handleUpdateQuestion() {
        try {
            String questionId = in.readLine();
            String questionText = in.readLine();
            String optionA = in.readLine();
            String optionB = in.readLine();
            String optionC = in.readLine();
            String optionD = in.readLine();
            String correctOption = in.readLine();

            // Update the question
            String sql = "UPDATE questions SET question_text=?, option_a=?, option_b=?, option_c=?, option_d=?, correct_option=? WHERE question_id=?";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setString(1, questionText);
                stmt.setString(2, optionA);
                stmt.setString(3, optionB);
                stmt.setString(4, optionC);
                stmt.setString(5, optionD);
                stmt.setString(6, correctOption);
                stmt.setInt(7, Integer.parseInt(questionId));
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    // Regrade all results for this question's exam
                    regradeResultsForQuestion(questionId);
                    out.println("SUCCESS");
                } else {
                    out.println("Failed to update question");
                }
            }
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }

    // Helper method to regrade all results for the exam containing this question
    private void regradeResultsForQuestion(String questionId) {
        try {
            // Get exam_id for this question
            int examId = -1;
            String examSql = "SELECT exam_id FROM questions WHERE question_id = ?";
            try (PreparedStatement stmt = dbConn.prepareStatement(examSql)) {
                stmt.setInt(1, Integer.parseInt(questionId));
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    examId = rs.getInt("exam_id");
                } else {
                    return;
                }
            }

            // Get all students who took this exam
            String studentsSql = "SELECT DISTINCT student_id FROM results WHERE exam_id = ?";
            try (PreparedStatement stmt = dbConn.prepareStatement(studentsSql)) {
                stmt.setInt(1, examId);
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    int studentId = rs.getInt("student_id");
                    // Recalculate score for this student and exam
                    int score = 0;
                    int totalQuestions = 0;

                    // Get all questions for this exam
                    String questionsSql = "SELECT question_id, correct_option FROM questions WHERE exam_id = ?";
                    try (PreparedStatement qStmt = dbConn.prepareStatement(questionsSql)) {
                        qStmt.setInt(1, examId);
                        ResultSet qrs = qStmt.executeQuery();
                        while (qrs.next()) {
                            int qid = qrs.getInt("question_id");
                            String correct = qrs.getString("correct_option");
                            totalQuestions++;
                            // Get student's answer for this question
                            String answerSql = "SELECT selected_option FROM answers WHERE user_id = ? AND question_id = ?";
                            try (PreparedStatement aStmt = dbConn.prepareStatement(answerSql)) {
                                aStmt.setInt(1, studentId);
                                aStmt.setInt(2, qid);
                                ResultSet ars = aStmt.executeQuery();
                                if (ars.next()) {
                                    String selected = ars.getString("selected_option");
                                    if (selected != null && selected.equalsIgnoreCase(correct)) {
                                        score++;
                                    }
                                }
                            }
                        }
                    }

                    // Update the result
                    String updateResultSql = "UPDATE results SET score=?, total_questions=? WHERE exam_id=? AND student_id=?";
                    try (PreparedStatement uStmt = dbConn.prepareStatement(updateResultSql)) {
                        uStmt.setInt(1, score);
                        uStmt.setInt(2, totalQuestions);
                        uStmt.setInt(3, examId);
                        uStmt.setInt(4, studentId);
                        uStmt.executeUpdate();
                    }
                }
            }
        } catch (Exception e) {
            // Log or ignore
        }
    }

    private void handleGetAllExams() {
        try {
            String sql = "SELECT e.exam_name, s.name AS subject_name, e.start_time, e.end_time " +
                    "FROM exams e JOIN subjects s ON e.subject_id = s.subject_id";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                java.util.List<String[]> exams = new java.util.ArrayList<>();
                while (rs.next()) {
                    String examName = rs.getString("exam_name");
                    String subjectName = rs.getString("subject_name");
                    String startTime = rs.getString("start_time");
                    String endTime = rs.getString("end_time");
                    exams.add(new String[] { examName, subjectName, startTime, endTime });
                }
                out.println("EXAMS_LIST");
                out.println(exams.size());
                for (String[] exam : exams) {
                    for (String field : exam) {
                        out.println(field);
                    }
                }
            }
        } catch (Exception e) {
            out.println("EXAMS_LIST");
            out.println("0");
        }
    }

    private void handleSubmitExam() {
        try {
            String username = in.readLine();
            String examName = in.readLine();
            int answerCount = Integer.parseInt(in.readLine());

            // Get user_id
            int userId = -1;
            String userSql = "SELECT user_id FROM users WHERE username = ?";
            try (PreparedStatement userStmt = dbConn.prepareStatement(userSql)) {
                userStmt.setString(1, username);
                ResultSet rs = userStmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                } else {
                    out.println("User not found");
                    return;
                }
            }

            // Get exam_id
            int examId = -1;
            String examSql = "SELECT exam_id FROM exams WHERE exam_name = ?";
            try (PreparedStatement examStmt = dbConn.prepareStatement(examSql)) {
                examStmt.setString(1, examName);
                ResultSet rs = examStmt.executeQuery();
                if (rs.next()) {
                    examId = rs.getInt("exam_id");
                } else {
                    out.println("Exam not found");
                    return;
                }
            }

            // Prepare to calculate score
            int score = 0;
            int totalQuestions = answerCount;

            // For each answer, store and check correctness
            for (int i = 0; i < answerCount; i++) {
                int questionId = Integer.parseInt(in.readLine());
                String selectedOption = in.readLine();

                // Store answer
                String insertAnswerSql = "INSERT INTO answers (user_id, question_id, selected_option) VALUES (?, ?, ?)";
                try (PreparedStatement ansStmt = dbConn.prepareStatement(insertAnswerSql)) {
                    ansStmt.setInt(1, userId);
                    ansStmt.setInt(2, questionId);
                    ansStmt.setString(3, selectedOption);
                    ansStmt.executeUpdate();
                }

                // Check correctness
                String correctSql = "SELECT correct_option FROM questions WHERE question_id = ?";
                try (PreparedStatement correctStmt = dbConn.prepareStatement(correctSql)) {
                    correctStmt.setInt(1, questionId);
                    ResultSet rs = correctStmt.executeQuery();
                    if (rs.next()) {
                        String correctOption = rs.getString("correct_option");
                        if (correctOption != null && correctOption.equalsIgnoreCase(selectedOption)) {
                            score++;
                        }
                    }
                }
            }

            // Store result
            String insertResultSql = "INSERT INTO results (exam_id, student_id, score, total_questions) VALUES (?, ?, ?, ?)";
            try (PreparedStatement resStmt = dbConn.prepareStatement(insertResultSql)) {
                resStmt.setInt(1, examId);
                resStmt.setInt(2, userId);
                resStmt.setInt(3, score);
                resStmt.setInt(4, totalQuestions);
                resStmt.executeUpdate();
            }

            out.println("SUCCESS");
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }

    private void handleGetResultForExam() {
        try {
            String username = in.readLine();
            String examName = in.readLine();

            // Get user_id
            int userId = -1;
            String userSql = "SELECT user_id FROM users WHERE username = ?";
            try (PreparedStatement userStmt = dbConn.prepareStatement(userSql)) {
                userStmt.setString(1, username);
                ResultSet rs = userStmt.executeQuery();
                if (rs.next()) {
                    userId = rs.getInt("user_id");
                } else {
                    out.println("RESULT_NOT_FOUND");
                    return;
                }
            }

            // Get exam_id
            int examId = -1;
            String examSql = "SELECT exam_id FROM exams WHERE exam_name = ?";
            try (PreparedStatement examStmt = dbConn.prepareStatement(examSql)) {
                examStmt.setString(1, examName);
                ResultSet rs = examStmt.executeQuery();
                if (rs.next()) {
                    examId = rs.getInt("exam_id");
                } else {
                    out.println("RESULT_NOT_FOUND");
                    return;
                }
            }

            // Get result
            String resultSql = "SELECT score, total_questions FROM results WHERE exam_id = ? AND student_id = ?";
            try (PreparedStatement resultStmt = dbConn.prepareStatement(resultSql)) {
                resultStmt.setInt(1, examId);
                resultStmt.setInt(2, userId);
                ResultSet rs = resultStmt.executeQuery();
                if (rs.next()) {
                    int score = rs.getInt("score");
                    int total = rs.getInt("total_questions");
                    out.println("RESULT_FOUND");
                    out.println(score);
                    out.println(total);
                } else {
                    out.println("RESULT_NOT_FOUND");
                }
            }
        } catch (Exception e) {
            out.println("RESULT_NOT_FOUND");
        }
    }

    private void handleGetStudentResultsForExam() {
        try {
            String examName = in.readLine();

            // Get exam_id and total_questions
            int examId = -1;
            int totalQuestions = 0;
            String examSql = "SELECT exam_id FROM exams WHERE exam_name = ?";
            try (PreparedStatement examStmt = dbConn.prepareStatement(examSql)) {
                examStmt.setString(1, examName);
                ResultSet rs = examStmt.executeQuery();
                if (rs.next()) {
                    examId = rs.getInt("exam_id");
                } else {
                    out.println("STUDENT_RESULTS_LIST");
                    out.println("0");
                    out.println("0");
                    return;
                }
            }
            String countSql = "SELECT COUNT(*) FROM questions WHERE exam_id = ?";
            try (PreparedStatement countStmt = dbConn.prepareStatement(countSql)) {
                countStmt.setInt(1, examId);
                ResultSet rs = countStmt.executeQuery();
                if (rs.next()) {
                    totalQuestions = rs.getInt(1);
                }
            }

            // Get all student results for this exam
            String resultsSql = "SELECT r.student_id, u.full_name, r.score FROM results r JOIN users u ON r.student_id = u.user_id WHERE r.exam_id = ?";
            try (PreparedStatement resultsStmt = dbConn.prepareStatement(resultsSql)) {
                resultsStmt.setInt(1, examId);
                ResultSet rs = resultsStmt.executeQuery();
                java.util.List<String[]> results = new java.util.ArrayList<>();
                while (rs.next()) {
                    String studentId = String.valueOf(rs.getInt("student_id"));
                    String studentName = rs.getString("full_name");
                    String score = String.valueOf(rs.getInt("score"));
                    results.add(new String[] { studentId, studentName, score });
                }
                out.println("STUDENT_RESULTS_LIST");
                out.println(results.size());
                out.println(totalQuestions);
                for (String[] row : results) {
                    for (String field : row) {
                        out.println(field);
                    }
                }
            }
        } catch (Exception e) {
            out.println("STUDENT_RESULTS_LIST");
            out.println("0");
            out.println("0");
        }
    }

    private void handlePublishExam() {
        try {
            String examName = in.readLine();
            String sql = "UPDATE exams SET published = TRUE WHERE exam_name = ?";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setString(1, examName);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    out.println("SUCCESS");
                } else {
                    out.println("Failed to publish exam");
                }
            }
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }

    private void handleUnpublishExam() {
        try {
            String examName = in.readLine();
            String sql = "UPDATE exams SET published = FALSE WHERE exam_name = ?";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setString(1, examName);
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    out.println("SUCCESS");
                } else {
                    out.println("Failed to unpublish exam");
                }
            }
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }

    private void handleGetExamPublishedStatus() {
        try {
            String examName = in.readLine();
            String sql = "SELECT published FROM exams WHERE exam_name = ?";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setString(1, examName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    boolean published = rs.getBoolean("published");
                    out.println("PUBLISHED_STATUS");
                    out.println(published);
                } else {
                    out.println("PUBLISHED_STATUS");
                    out.println("false"); // Exam not found, default to not published
                }
            }
        } catch (Exception e) {
            out.println("PUBLISHED_STATUS");
            out.println("false"); // Error, default to not published
        }
    }

    private void handleGetPublishedExams() {
        try {
            String sql = "SELECT e.exam_name, s.name AS subject_name, e.start_time, e.end_time " +
                    "FROM exams e JOIN subjects s ON e.subject_id = s.subject_id WHERE e.published = TRUE";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                java.util.List<String[]> exams = new java.util.ArrayList<>();
                while (rs.next()) {
                    String examName = rs.getString("exam_name");
                    String subjectName = rs.getString("subject_name");
                    String startTime = rs.getString("start_time");
                    String endTime = rs.getString("end_time");
                    exams.add(new String[] { examName, subjectName, startTime, endTime });
                }

                out.println("EXAMS_LIST");
                out.println(exams.size());
                for (String[] exam : exams) {
                    for (String field : exam) {
                        out.println(field);
                    }
                }
            }
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }

    private void handleHasStudentTakenExam() {
        try {
            String username = in.readLine();
            String examName = in.readLine();

            String sql = "SELECT COUNT(*) FROM results r " +
                    "JOIN users u ON r.student_id = u.user_id " +
                    "JOIN exams e ON r.exam_id = e.exam_id " +
                    "WHERE u.username = ? AND e.exam_name = ?";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setString(1, username);
                stmt.setString(2, examName);
                ResultSet rs = stmt.executeQuery();
                rs.next();
                int count = rs.getInt(1);

                out.println("EXAM_TAKEN_STATUS");
                out.println(count > 0);
            }
        } catch (Exception e) {
            out.println("EXAM_TAKEN_STATUS");
            out.println("false");
        }
    }

    private void handleGetExamDetails() {
        try {
            String examName = in.readLine();
            String sql = "SELECT subject_id, start_time, end_time FROM exams WHERE exam_name = ?";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setString(1, examName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int subjectId = rs.getInt("subject_id");
                    Timestamp startTime = rs.getTimestamp("start_time");
                    Timestamp endTime = rs.getTimestamp("end_time");

                    out.println("EXAM_DETAILS");
                    out.println(subjectId);
                    out.println(startTime.getTime());
                    out.println(endTime.getTime());
                } else {
                    out.println("EXAM_NOT_FOUND");
                }
            }
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }

    private void handleUpdateExam() {
        try {
            String oldExamName = in.readLine();
            String newExamName = in.readLine();
            int subjectId = Integer.parseInt(in.readLine());
            long startTimeMillis = Long.parseLong(in.readLine());
            long endTimeMillis = Long.parseLong(in.readLine());

            // Convert milliseconds to Timestamp
            Timestamp startTime = new Timestamp(startTimeMillis);
            Timestamp endTime = new Timestamp(endTimeMillis);

            String sql = "UPDATE exams SET exam_name = ?, subject_id = ?, start_time = ?, end_time = ? WHERE exam_name = ?";
            try (PreparedStatement stmt = dbConn.prepareStatement(sql)) {
                stmt.setString(1, newExamName);
                stmt.setInt(2, subjectId);
                stmt.setTimestamp(3, startTime);
                stmt.setTimestamp(4, endTime);
                stmt.setString(5, oldExamName);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    out.println("SUCCESS");
                } else {
                    out.println("EXAM_NOT_FOUND");
                }
            }
        } catch (Exception e) {
            out.println("Error: " + e.getMessage());
        }
    }
}