import java.util.ArrayList;
import java.util.HashMap;

public class Library{
    private ArrayList<Book> books;
    private HashMap<String, Book> booksByISBN;

    public Library(){
        books = new ArrayList<>();
        booksByISBN = new HashMap<>();
    }
    public void addBook(Book book){
        books.add(book);
        booksByISBN.put(book.getIsbn(), book);
    }
    public void displayBooks(){
        for(Book book: books){
            System.out.println(book);
        }
    }
    public Book searchByISBN(String isbn){
        return booksByISBN.get(isbn);
    }
}