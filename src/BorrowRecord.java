public class BorrowRecord {
    private Book book;
    private String borrowDate;
    private String returnDate;
    private String memberId;
    private String borrowerName;
    private String dueDate;
    private double fine;

    public BorrowRecord(Book book, String borrowDate){
        this(book, "", "", borrowDate);
    }

    public BorrowRecord(Book book, String memberId, String borrowerName, String borrowDate){
        this.book = book;
        this.memberId = memberId;
        this.borrowerName = borrowerName;
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate;
    }
    public Book getBook(){
        return book;
    }
    public String getBorrowDate(){
        return borrowDate;
    }
    public String getReturnDate(){ return returnDate; }
    public String getMemberId(){ return memberId; }
    public String getBorrowerName(){ return borrowerName; }
    public String getDueDate(){ return dueDate; }
    public double getFine(){ return fine; }
    public void close(String returnDate, int overdueDays, double fine){
        this.returnDate = returnDate;
        this.fine = fine;
    }
    public boolean isReturned(){ return returnDate != null; }
    @Override 
    public String toString(){
        return book.getTitle() + " | Borrowed: " + borrowDate + " | Returned: "
            + (returnDate == null ? "No" : returnDate) + " | Fine: RM" + String.format("%.2f", fine);
    }
}
