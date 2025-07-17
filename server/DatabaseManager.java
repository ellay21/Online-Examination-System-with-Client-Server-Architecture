import java.sql.*;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/online_exam_system";
    private static final String DB_USER = "root"; // Change as needed
    private static final String DB_PASS = ""; // Change as needed
    private Connection conn;

    public DatabaseManager() throws SQLException {
        conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
        initializeDatabase();
    }

    public Connection getConnection() {
        return conn;
    }

    private void initializeDatabase() throws SQLException {
        Statement stmt = conn.createStatement();

        // 1. Users table
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS users (" +
                        "user_id INT PRIMARY KEY AUTO_INCREMENT," +
                        "username VARCHAR(50) UNIQUE NOT NULL," +
                        "password VARCHAR(255) NOT NULL," +
                        "role VARCHAR(20) NOT NULL," +
                        "full_name VARCHAR(100)" +
                        ")");

        // 2. Subjects table
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS subjects (" +
                        "subject_id INT PRIMARY KEY AUTO_INCREMENT," +
                        "name VARCHAR(100) UNIQUE NOT NULL" +
                        ")");

        // 3. Exams table
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS exams (" +
                        "exam_id INT PRIMARY KEY AUTO_INCREMENT," +
                        "subject_id INT NOT NULL," +
                        "exam_name TEXT NOT NULL," +
                        "start_time DATETIME NOT NULL," +
                        "end_time DATETIME NOT NULL," +
                        "user_id INT NOT NULL," +
                        "published BOOLEAN NOT NULL DEFAULT FALSE," +
                        "FOREIGN KEY (subject_id) REFERENCES subjects(subject_id)," +
                        "FOREIGN KEY (user_id) REFERENCES users(user_id)" +
                        ")");

        // 4. Questions table
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS questions (" +
                        "question_id INT PRIMARY KEY AUTO_INCREMENT," +
                        "exam_id INT NOT NULL," +
                        "question_text TEXT NOT NULL," +
                        "option_a TEXT NOT NULL," +
                        "option_b TEXT NOT NULL," +
                        "option_c TEXT NOT NULL," +
                        "option_d TEXT NOT NULL," +
                        "correct_option VARCHAR(1) NOT NULL," +
                        "FOREIGN KEY (exam_id) REFERENCES exams(exam_id)" +
                        ")");

        // 5. Results table
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS results (" +
                        "result_id INT PRIMARY KEY AUTO_INCREMENT," +
                        "exam_id INT NOT NULL," +
                        "student_id INT NOT NULL," +
                        "score INT NOT NULL," +
                        "total_questions INT NOT NULL," +
                        "FOREIGN KEY (exam_id) REFERENCES exams(exam_id)," +
                        "FOREIGN KEY (student_id) REFERENCES users(user_id)" +
                        ")");

        // 6. Answers table
        stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS answers (" +
                        "answer_id INT PRIMARY KEY AUTO_INCREMENT," +
                        "user_id INT NOT NULL," +
                        "question_id INT NOT NULL," +
                        "selected_option VARCHAR(1) NOT NULL," +
                        "FOREIGN KEY (user_id) REFERENCES users(user_id)," +
                        "FOREIGN KEY (question_id) REFERENCES questions(question_id)" +
                        ")");

        stmt.close();
    }

    public void close() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }
}