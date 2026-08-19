import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
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
    private HashMap<String, String> activeLoans;
    private HashMap<String, Member> members;
    private HashMap<String, WaitingQueue> waitingQueues;
    private ArrayList<Notification> notifications;
    private RecommendationSystem recommendationSystem;
    private int nextBookId;
    private int nextStudentId;
    private int nextTeacherId;
    private static final double FINE_PER_WEEK = 1.00;

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
        activeLoans = new HashMap<>();
        members = new HashMap<>();
        waitingQueues = new HashMap<>();
        notifications = new ArrayList<>();
        recommendationSystem = new RecommendationSystem();
        int[] sequences = database.loadSequences();
        nextBookId = sequences[0];
        nextStudentId = sequences[1];
        nextTeacherId = sequences[2];
        for(Member member : database.loadMembers()) members.put(member.getMemberId(), member);
        for(Book book : database.loadBooks()){
            registerBook(book);
            nextBookId = Math.max(nextBookId, nextSequence(book.getIsbn(), "B-"));
        }
        database.saveSequences(nextBookId, nextStudentId, nextTeacherId);
    }
    public boolean addBook(Book book){
        if(book == null || !validText(book.getIsbn()) || !validText(book.getTitle()) || !validText(book.getAuthor())
            || !validText(book.getCategory()) || book.getYear() < 0 || booksByISBN.containsKey(book.getIsbn())){
            return false;
        }
        registerBook(book);
        actionStack.push(new Action("ADD", book));
        database.saveBooks(books);
        return true;
    }

    public String generateBookId(){
        String id = String.format("B-%04d", nextBookId++);
        database.saveSequences(nextBookId, nextStudentId, nextTeacherId);
        return id;
    }

    public String generateMemberId(String type){
        boolean teacher = "Teacher".equalsIgnoreCase(type);
        String id = String.format("%s-%04d", teacher ? "T" : "S", teacher ? nextTeacherId++ : nextStudentId++);
        database.saveSequences(nextBookId, nextStudentId, nextTeacherId);
        return id;
    }

    private void registerBook(Book book){
        books.add(book);
        booksByISBN.put(book.getIsbn(), book);
        booksByYear.insert(book);
        bookGraph.addBook(book.getIsbn());
    }

    private int nextSequence(String id, String prefix){
        if(id == null || !id.startsWith(prefix)) return 1;
        try { return Integer.parseInt(id.substring(prefix.length())) + 1; }
        catch(NumberFormatException ex){ return 1; }
    }
    public void displayBooks(){
        for(Book book: books){
            System.out.println(book);
        }
    }

    public ArrayList<Book> getBooksSnapshot(){
        return new ArrayList<>(books);
    }

    public boolean removeBook(String isbn){
        Book book = booksByISBN.get(isbn);
        if(book == null || !book.isAvailable()) return false;
        books.remove(book);
        booksByISBN.remove(isbn);
        rebuildBooksByYear();
        database.saveBooks(books);
        actionStack.push(new Action("REMOVE", book));
        notifyUser("Removed book: " + book.getTitle());
        return true;
    }

    public boolean updateBook(String isbn, String title, String author, String category, int year){
        Book book = booksByISBN.get(isbn);
        if(book == null || !validText(title) || !validText(author) || !validText(category) || year < 0) return false;
        book.setTitle(title); book.setAuthor(author); book.setCategory(category); book.setYear(year);
        rebuildBooksByYear();
        database.saveBooks(books);
        actionStack.push(new Action("UPDATE", book));
        notifyUser("Updated book: " + title);
        return true;
    }

    public ArrayList<Book> searchBooks(String query, String field){
        ArrayList<Book> result = new ArrayList<>();
        String value = query == null ? "" : query.toLowerCase();
        String selectedField = field == null ? "Title" : field;
        for(Book book : books){
            String candidate = selectedField.equals("ISBN") ? book.getIsbn() : selectedField.equals("Title") ? book.getTitle()
                : selectedField.equals("Author") ? book.getAuthor() : selectedField.equals("Category") ? book.getCategory()
                : book.isAvailable() ? "available" : "borrowed";
            if(candidate.toLowerCase().contains(value)) result.add(book);
        }
        return result;
    }

    public boolean registerMember(Member member){
        if(member == null || !validText(member.getMemberId()) || !validText(member.getName())
            || !validText(member.getType()) || !validEmail(member.getEmail()) || members.containsKey(member.getMemberId())) return false;
        members.put(member.getMemberId(), member);
        database.saveMembers(getMembersSnapshot());
        notifyUser("Registered member: " + member.getName());
        return true;
    }

    public boolean registerMember(String name, String type, String email){
        return registerMember(new Member(generateMemberId(type), name, type, email));
    }

    public boolean removeMember(String memberId){
        Member member = members.get(memberId);
        if(member == null || member.getCurrentBorrowCount() > 0) return false;
        members.remove(memberId);
        database.saveMembers(getMembersSnapshot());
        notifyUser("Removed member: " + member.getName());
        return true;
    }

    public boolean updateMember(String memberId, String name, String type, String email){
        Member member = members.get(memberId);
        if(member == null) return false;
        member.setName(name); member.setType(type); member.setEmail(email);
        database.saveMembers(getMembersSnapshot());
        notifyUser("Updated member: " + name);
        return true;
    }

    public Member searchMember(String memberId){ return members.get(memberId); }

    public String getCurrentHolder(String isbn){
        Book book = booksByISBN.get(isbn);
        if(book == null) return "Book not found.";
        for(BorrowRecord record : borrowHistory.getRecords()){
            if(record.getBook() == book && !record.isReturned()){
                return record.getMemberId() + " | " + record.getBorrowerName();
            }
        }
        return book.isAvailable() ? "Book is currently available." : "Current holder is unavailable in this session.";
    }

    private boolean validText(String value){ return value != null && !value.trim().isEmpty(); }
    private boolean validEmail(String value){ return validText(value) && value.contains("@"); }
    public ArrayList<Member> searchMembersByName(String name){
        ArrayList<Member> result = new ArrayList<>();
        for(Member member : members.values()) if(member.getName().toLowerCase().contains(name.toLowerCase())) result.add(member);
        return result;
    }
    public ArrayList<Member> getMembersSnapshot(){ return new ArrayList<>(members.values()); }
    public int getMemberCount(){ return members.size(); }
    public ArrayList<BorrowRecord> getMemberHistory(String memberId){ return borrowHistory.getRecordsForMember(memberId); }

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
        return borrowBook(isbn, borrowDate, "Guest", "Guest");
    }

    public boolean borrowBook(String isbn, String borrowDate, String borrowerType, String borrowerName){
        Book book = searchByISBN(isbn);
        if(book == null){
            System.out.println("Book not found.");
            return false;
        }
        if(!book.isAvailable()){
            System.out.println("Book is not available.");
            return false;
        }
        Member member = members.get(borrowerType);
        if(member == null) member = findMemberByNameAndType(borrowerName, borrowerType);
        if(member == null){
            System.out.println("Register the borrower before borrowing.");
            return false;
        }
        if(!member.canBorrow()){
            notifyUser(member.getName() + " has reached the borrowing limit of 3 books.");
            return false;
        }
        String borrowerKey = member.getMemberId();
        if(activeLoans.containsKey(borrowerKey)){
            System.out.println("Borrower already has an active loan.");
            return false;
        }
        book.setAvailable(false);
        activeLoans.put(borrowerKey, isbn);
        book.incrementBorrowCount();
        BorrowRecord record = new BorrowRecord(book, member.getMemberId(), member.getName(), borrowDate);
        member.addBorrowingRecord(record);
        borrowHistory.add(record);
        database.saveBooks(books);
        notifyUser("Borrow confirmed: " + member.getName() + " borrowed " + book.getTitle());
        return true;
    }

    public boolean returnBook(String isbn){
        return returnBook(isbn, null, null);
    }

    public boolean returnBook(String isbn, String memberId, String borrowerName){
        Book book = searchByISBN(isbn);
        if(book == null){
            System.out.println("Book not found.");
            return false;
        }
        if(book.isAvailable()){
            return false;
        }
        boolean matchingLoan = memberId == null && borrowerName == null;
        for(BorrowRecord record : borrowHistory.getRecords()){
            if(record.getBook() == book && !record.isReturned()
                && (memberId == null || memberId.trim().isEmpty() || memberId.equals(record.getMemberId()))
                && (borrowerName == null || borrowerName.trim().isEmpty() || borrowerName.equalsIgnoreCase(record.getBorrowerName()))){
                matchingLoan = true;
                break;
            }
        }
        if(!matchingLoan){
            System.out.println("Member ID and name do not match the active loan.");
            return false;
        }
        book.setAvailable(true);
        activeLoans.values().removeIf(value -> value.equals(isbn));
        for(BorrowRecord record : borrowHistory.getRecords()){
            if(record.getBook() == book && !record.isReturned()
                && (memberId == null || memberId.trim().isEmpty() || memberId.equals(record.getMemberId()))
                && (borrowerName == null || borrowerName.trim().isEmpty() || borrowerName.equalsIgnoreCase(record.getBorrowerName()))){
                long overdueDays = daysOverdue(record);
                double fine = calculateFine(overdueDays);
                record.close(LocalDate.now().toString(), (int) overdueDays, fine);
                if(fine > 0) notifyUser("Fine generated for " + book.getTitle() + ": RM" + String.format("%.2f", fine));
                break;
            }
        }
        WaitingQueue queue = waitingQueues.get(isbn);
        if(queue != null){
            String next = queue.dequeue();
            if(next != null) notifyUser("Book available: " + book.getTitle() + ". Next waiting member: " + next);
        }
        database.saveBooks(books);
        notifyUser("Return confirmed: " + book.getTitle());
        return true;
    }

    private Member findMemberByNameAndType(String name, String type){
        for(Member member : members.values()){
            if(member.getName().equalsIgnoreCase(name) && member.getType().equalsIgnoreCase(type)) return member;
        }
        return null;
    }

    private long daysOverdue(BorrowRecord record){
        try{
            LocalDate due = LocalDate.parse(record.getBorrowDate()).plusDays(7);
            return Math.max(0, ChronoUnit.DAYS.between(due, LocalDate.now()));
        }catch(Exception ex){ return 0; }
    }

    public double calculateFine(long overdueDays){
        return overdueDays <= 0 ? 0 : ((overdueDays + 6) / 7) * FINE_PER_WEEK;
    }

    public void displayBorrowHistory(){
        borrowHistory.displayHistory();
    }

    public ArrayList<Book> sortBooks(String criteria){
        ArrayList<Book> sorted = getBooksSnapshot();
        HashMap<String, Comparator<Book>> comparators = new HashMap<>();
        comparators.put("Author", Comparator.comparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER));
        comparators.put("Year", Comparator.comparingInt(Book::getYear));
        comparators.put("Popularity", (first, second) -> Integer.compare(second.getBorrowCount(), first.getBorrowCount()));
        comparators.put("Availability", (first, second) -> Boolean.compare(!first.isAvailable(), !second.isAvailable()));
        Comparator<Book> comparator = comparators.get(criteria);
        if(comparator == null) comparator = Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER);
        for(int index = 1; index < sorted.size(); index++){
            Book current = sorted.get(index);
            int position = index - 1;
            while(position >= 0 && comparator.compare(sorted.get(position), current) > 0){
                sorted.set(position + 1, sorted.get(position));
                position--;
            }
            sorted.set(position + 1, current);
        }
        return sorted;
    }

    public ArrayList<Book> getRecommendations(String memberId){
        Member member = members.get(memberId);
        return member == null ? new ArrayList<>() : recommendationSystem.recommend(member, books);
    }

    public ArrayList<Recommendation> getRecommendationDetails(String memberId){
        Member member = members.get(memberId);
        return member == null ? new ArrayList<>() : recommendationSystem.recommendWithReasons(member, books);
    }

    public ArrayList<Book> getPopularBooks(int limit){
        ArrayList<Book> popular = sortBooks("Popularity");
        return new ArrayList<>(popular.subList(0, Math.min(limit, popular.size())));
    }

    public DashboardStats getDashboardStats(){
        DashboardStats stats = new DashboardStats();
        stats.totalBooks = books.size(); stats.totalMembers = members.size();
        for(Book book : books){ if(book.isAvailable()) stats.availableBooks++; else stats.borrowedBooks++; }
        for(BorrowRecord record : borrowHistory.getRecords()){
            if(!record.isReturned()){
                long overdue = daysOverdue(record);
                if(overdue > 0){ stats.overdueBooks++; stats.outstandingFines += calculateFine(overdue); }
            } else stats.outstandingFines += record.getFine();
        }
        Book popular = getPopularBooks(1).isEmpty() ? null : getPopularBooks(1).get(0);
        stats.mostPopularBook = popular == null ? "None" : popular.getTitle();
        for(WaitingQueue queue : waitingQueues.values()) stats.waitingMembers += queue.size();
        return stats;
    }

    public void addToWaitingList(String isbn, String memberId){
        waitingQueues.computeIfAbsent(isbn, key -> new WaitingQueue()).enqueue(memberId);
        notifyUser("Added " + memberId + " to the waiting queue for " + isbn);
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

    public ArrayList<Notification> getNotifications(){ return new ArrayList<>(notifications); }
    public ArrayList<Notification> getUnreadNotifications(){
        ArrayList<Notification> unread = new ArrayList<>();
        for(Notification notification : notifications) if(!notification.isRead()) unread.add(notification);
        return unread;
    }
    public void markNotificationsRead(){ for(Notification notification : notifications) notification.markRead(); }
    private void notifyUser(String message){ notifications.add(new Notification("LIBRARY", message)); }

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