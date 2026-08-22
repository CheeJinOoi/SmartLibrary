import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BookReview {
    private final String isbn;
    private final String memberId;
    private final String memberName;
    private final int rating;
    private final String comment;
    private final String createdAt;

    public BookReview(String isbn, String memberId, String memberName, int rating, String comment) {
        this.isbn = isbn;
        this.memberId = memberId;
        this.memberName = memberName;
        this.rating = Math.max(1, Math.min(5, rating));
        this.comment = comment == null ? "" : comment.trim();
        this.createdAt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public String getIsbn() { return isbn; }
    public String getMemberId() { return memberId; }
    public String getMemberName() { return memberName; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getCreatedAt() { return createdAt; }

    public String stars() {
        return "★".repeat(rating) + "☆".repeat(5 - rating);
    }

    @Override
    public String toString() {
        return createdAt + " | " + memberName + " | " + stars() + " " + rating + "/5"
            + (comment.isEmpty() ? "" : " | " + comment);
    }
}
