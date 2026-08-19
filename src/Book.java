public class Book {
    private String isbn;
    private String title;
    private String author;
    private String category;
    private int year;
    private boolean available;
    private int borrowCount;

    public Book(String isbn, String title, String author, int year){
        this(isbn, title, author, "General", year);
    }

    public Book(String isbn, String title, String author, String category, int year){
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.year = year;
        this.available = true;
    }
    public String getIsbn(){
        return isbn;
    }
    public String getTitle(){
        return title;
    }
    public String getAuthor(){
        return author;
    }
    public String getCategory(){
        return category;
    }
    public int getYear(){
        return year;
    }
    public boolean isAvailable(){
        return available;
    }
    public void setAvailable(boolean available){
        this.available = available;
    }
    public void setTitle(String title){ this.title = title; }
    public void setAuthor(String author){ this.author = author; }
    public void setCategory(String category){ this.category = category; }
    public void setYear(int year){ this.year = year; }
    public int getBorrowCount(){ return borrowCount; }
    public void incrementBorrowCount(){ borrowCount++; }
    public void setBorrowCount(int borrowCount){ this.borrowCount = borrowCount; }

    @Override
    public String toString(){
        return isbn + " | " + title + " | "+ author + " | " + category + " | "+ year + " | Available "+ available;
    }
}
