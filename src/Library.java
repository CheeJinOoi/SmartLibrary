import java.util.ArrayList;
import java.util.HashMap;

public class Library{
    private ArrayList<Book> books;
    private HashMap<String, Book> booksByISBN;
    private Stack actionStack;
    private BorrowHistory borrowHistory;
    private WaitingQueue waitingQueue;
    private BookYearBST booksByYear;
    private MaxHeap overdueBooks;
    private BookGraph bookGraph;
    private LibraryDatabase database;

    public Library(){
        this(100);
    }

    public Library(int overdueBookCapacity){
        books = new ArrayList<>();
        booksByISBN = new HashMap<>();
        actionStack = new Stack();
        borrowHistory = new BorrowHistory();
        waitingQueue = new WaitingQueue();
        booksByYear = new BookYearBST();
        overdueBooks = new MaxHeap(overdueBookCapacity);
        bookGraph = new BookGraph();
        database = new LibraryDatabase();
        for(Book book : database.loadBooks()){
            registerBook(book);
        }
    }
    public boolean addBook(Book book){
        if(book == null || booksByISBN.containsKey(book.getIsbn())){
            return false;
        }
        registerBook(book);
        actionStack.push(new Action("ADD", book));
        database.saveBooks(books);
        return true;
    }

    private void registerBook(Book book){
        books.add(book);
        booksByISBN.put(book.getIsbn(), book);
        booksByYear.insert(book);
        bookGraph.addBook(book.getIsbn());
    }
    public void displayBooks(){
        for(Book book: books){
            System.out.println(book);
        }
    }

    public ArrayList<Book> getBooksSnapshot(){
        return new ArrayList<>(books);
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
            rebuildBooksByYear();
            database.saveBooks(books);
            System.out.println("Undo successful: Removed" + book.getTitle());
        }
    }
    public Book binarySearchhTitle(String title){
        Book[] bookArray = new Book[books.size()];
        for(int index = 0; index < books.size(); index++){
            bookArray[index] = books.get(index);
        }

        if(bookArray.length == 0){
            return null;
        }
        MergeSort.sort(bookArray, 0, bookArray.length -1);
        return BinarySearch.search(bookArray, title);
    }

    public Book binarySearchTitle(String title){
        return binarySearchhTitle(title);
    }

    public boolean borrowBook(String isbn, String borrowDate){
        Book book = searchByISBN(isbn);
        if(book == null){
            System.out.println("Book not found.");
            return false;
        }
        if(!book.isAvailable()){
            System.out.println("Book is not available.");
            return false;
        }
        book.setAvailable(false);
        borrowHistory.add(new BorrowRecord(book, borrowDate));
        database.saveBooks(books);
        return true;
    }

    public boolean returnBook(String isbn){
        Book book = searchByISBN(isbn);
        if(book == null){
            System.out.println("Book not found.");
            return false;
        }
        if(book.isAvailable()){
            return false;
        }
        book.setAvailable(true);
        database.saveBooks(books);
        return true;
    }

    public void displayBorrowHistory(){
        borrowHistory.displayHistory();
    }

    public void addToWaitingList(String studentName){
        waitingQueue.enqueue(studentName);
    }

    public String serveNextWaitingStudent(){
        return waitingQueue.dequeue();
    }

    public void displayWaitingList(){
        waitingQueue.displayQueue();
    }

    public Book searchByYear(int year){
        return booksByYear.search(year);
    }

    public void displayBooksInorder(){
        booksByYear.inorder();
    }

    public void displayBooksPreorder(){
        booksByYear.preorder();
    }

    public void displayBooksPostorder(){
        booksByYear.postorder();
    }

    public void addOverdueBook(OverdueBook overdueBook){
        overdueBooks.insert(overdueBook);
    }

    public OverdueBook peekMostOverdueBook(){
        return overdueBooks.peek();
    }

    public OverdueBook extractMostOverdueBook(){
        return overdueBooks.extractMax();
    }

    public boolean hasOverdueBooks(){
        return !overdueBooks.isEmpty();
    }

    public void addBookRelationship(String isbn1, String isbn2){
        bookGraph.addRelationship(isbn1, isbn2);
    }

    public void displayBookGraph(){
        bookGraph.display();
    }

    public void breadthFirstBookSearch(String startIsbn){
        bookGraph.bfs(startIsbn);
    }

    public void depthFirstBookSearch(String startIsbn){
        bookGraph.dfs(startIsbn);
    }

    public void depthFirstBookSearchUsingStack(String startIsbn){
        bookGraph.dfsUsingStack(startIsbn);
    }

    private void rebuildBooksByYear(){
        booksByYear = new BookYearBST();
        for(Book book : books){
            booksByYear.insert(book);
        }
    }
}