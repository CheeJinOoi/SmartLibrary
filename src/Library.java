import java.util.ArrayList;
import java.util.HashMap;

public class Library{
    private ArrayList<Book> books;
    private HashMap<String, Book> booksByISBN;
    private Stack actionStack;

    public Library(){
        books = new ArrayList<>();
        booksByISBN = new HashMap<>();
        actionStack = new Stack();
    }
    public void addBook(Book book){
        books.add(book);
        booksByISBN.put(book.getIsbn(), book);
        actionStack.push(new Action("ADD", book));
    }
    public void displayBooks(){
        for(Book book: books){
            System.out.println(book);
        }
    }
    public Book searchByISBN(String isbn){
        return booksByISBN.get(isbn);
    }
    public void undo(){
        Action action = actionStack.pop();
        if(action == null){
            System.out.println("Nothing to undo.");
            return;
        }
        if(action.getType().equals("ADD")){
            Book book = action.getBook();
            books.remove(book);
            booksByISBN.remove(book.getIsbn());
            System.out.println("Undo successful: Removed" + book.getTitle());
        }
    }
    public Book binarySearchhTitle(String title){
        Book[] bookArray = books.toArray(new Book[0]);

        if(bookArray.length == 0){
            return null;
        }
        MergeSort.sort(bookArray, 0, bookArray.length -1);
        return BinarySearch.search(bookArray, title);
    }
}