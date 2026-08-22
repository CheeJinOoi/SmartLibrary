import java.time.LocalDate;

public class BorrowRecord {
    private Book book;
    private String borrowDate;
    private String returnDate;
    private String memberId;
    private String borrowerName;
    private String dueDate;
    private double fine;
    private boolean lost;

    public BorrowRecord(Book book, String borrowDate) {
        this(book, "", "", borrowDate);
    }

    public BorrowRecord(Book book, String memberId, String borrowerName, String borrowDate) {
        this.book = book;
        this.memberId = memberId;
        this.borrowerName = borrowerName;
        this.borrowDate = borrowDate;
        try {
            this.dueDate = LocalDate.parse(borrowDate).plusDays(7).toString();
        } catch (Exception ex) {
            this.dueDate = borrowDate;
        }
    }

    public Book getBook() { return book; }
    public String getBorrowDate() { return borrowDate; }
    public String getReturnDate() { return returnDate; }
    public String getMemberId() { return memberId; }
    public String getBorrowerName() { return borrowerName; }
    public String getDueDate() { return dueDate; }
    public double getFine() { return fine; }
    public boolean isLost() { return lost; }
    public boolean isReturned() { return returnDate != null; }

    public void close(String returnDate, int overdueDays, double fine) {
        this.returnDate = returnDate;
        this.fine = fine;
    }

    public void markLost(double replacementFine) {
        this.lost = true;
        this.returnDate = LocalDate.now().toString();
        this.fine = replacementFine;
    }

    public long daysUntilDue() {
        try {
            return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(dueDate));
        } catch (Exception ex) {
            return 0;
        }
    }

    public long daysOverdue() {
        try {
            LocalDate due = LocalDate.parse(dueDate);
            return Math.max(0, java.time.temporal.ChronoUnit.DAYS.between(due, LocalDate.now()));
        } catch (Exception ex) {
            return 0;
        }
    }

    @Override
    public String toString() {
        return book.getTitle() + " | Borrowed: " + borrowDate + " | Due: " + dueDate
            + " | Returned: " + (returnDate == null ? "No" : returnDate)
            + (lost ? " | LOST" : "") + " | Fine: RM" + String.format("%.2f", fine);
    }
}
