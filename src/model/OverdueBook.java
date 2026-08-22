public class OverdueBook {
    private String studentName;
    private Book book;
    private int daysOverdue;

    public OverdueBook(String studentName, Book book, int daysOverdue){
        this.studentName = studentName;
        this.book = book;
        this.daysOverdue = daysOverdue;
    }
    public String getStudentName(){
        return studentName;
    }
    public Book geBook(){
        return book;
    }
    public int getDaysOverdue(){
        return daysOverdue;
    }
    @Override
    public String toString(){
        return studentName + " | " + book.getTitle() + " | " + daysOverdue + "days overdue";
    }
}
