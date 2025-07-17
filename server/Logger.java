import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {

    private static final String LOG_FILE = "resources/exam_log.txt";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static synchronized void log(String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        String logEntry = "[" + timestamp + "] " + message;
        try (FileWriter fw = new FileWriter(LOG_FILE, true)) {
            fw.write(logEntry + System.lineSeparator());
        } catch (IOException e) {
            System.err.println("Logger error: " + e.getMessage());
        }
    }
}