public class DashboardStats {
    public int totalBooks;
    public int availableBooks;
    public int borrowedBooks;
    public int totalMembers;
    public int overdueBooks;
    public double outstandingFines;
    public String mostPopularBook;
    public int waitingMembers;

    @Override
    public String toString() {
        return "Books: " + totalBooks + " | Available: " + availableBooks + " | Borrowed: " + borrowedBooks
            + " | Members: " + totalMembers + " | Overdue: " + overdueBooks + " | Fines: RM"
            + String.format("%.2f", outstandingFines) + " | Popular: " + mostPopularBook
            + " | Waiting: " + waitingMembers;
    }
}
