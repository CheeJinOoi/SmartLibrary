import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Notification {
    private final String message;
    private final String type;
    private final LocalDateTime createdAt;
    private boolean read;

    public Notification(String message) {
        this("INFO", message);
    }

    public Notification(String type, String message) {
        this.type = type;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
    public String getMessage() { return message; }
    public String getType() { return type; }
    public boolean isRead() { return read; }
    public void markRead() { read = true; }
    @Override
    public String toString() {
        return createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + " | " + type + " | " + message
            + (read ? " [read]" : " [new]");
    }
}
