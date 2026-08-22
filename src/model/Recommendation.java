public class Recommendation {
    private final Book book;
    private final String reason;
    private final int score;

    public Recommendation(Book book, String reason, int score) {
        this.book = book;
        this.reason = reason;
        this.score = score;
    }

    public Book getBook() { return book; }
    public String getReason() { return reason; }
    public int getScore() { return score; }

    @Override
    public String toString() {
        return book.getIsbn() + " | " + book.getTitle() + " | " + book.getCategory()
            + " | Why: " + reason + " | Available: " + book.isAvailable();
    }
}
