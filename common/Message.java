import java.io.Serializable;

public class Message implements Serializable {
    private String type; // e.g., "LOGIN", "REGISTER", "EXAM", "RESULT", etc.
    private Object data; // Can hold any data related to the message

    public Message(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public Object getData() {
        return data;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setData(Object data) {
        this.data = data;
    }
}