public class BorrowRecord {
    private Book book;
    private String borrowDate;

    public BorrowRecord(Book book, String borrowDate){
        this.book = book;
        this.borrowDate = borrowDate;
    }
    public Book getBook(){
        return book;
    }
    public String getBorrowDate(){
        return borrowDate;
    }
    @Override 
    public String toString(){
        return book.getTitle() + " | Borrowed: " + borrowDate;
    }
}
