
public class Exam {
    private int examId;
    private int subjectId;
    private String examName;
    private String startTime;
    private String endTime;
    private int userId;

    public Exam(int examId, int subjectId, String examName, String startTime, String endTime, int userId) {
        this.examId = examId;
        this.examName = examName;
        this.subjectId = subjectId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.userId = userId;
    }

    public int getExamId() {
        return examId;
    }

    public int getSubjectId() {
        return subjectId;
    }

    public String getExamName() {
        return examName;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public int getUserId() {
        return userId;
    }
}