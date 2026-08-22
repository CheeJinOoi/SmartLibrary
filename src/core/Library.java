import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class Library {
    private static final double FINE_PER_WEEK = 1.00;
    private static final double REPLACEMENT_COST = 50.00;
    private static final int MAX_LOANS = 3;

    private ArrayList<Book> books;
    private HashMap<String, Book> booksByISBN;
    private Stack actionStack;
    private BorrowHistory borrowHistory;
    private BookYearBST booksByYear;
    private MaxHeap overdueBooks;
    private BookGraph bookGraph;
    private LibraryDatabase database;
    private HashMap<String, Member> members;
    private HashMap<String, WaitingQueue> waitingQueues;
    private ArrayList<Notification> notifications;
    private ArrayList<BookReview> reviews;
    private RecommendationSystem recommendationSystem;
    private RecentlyViewed recentlyViewed;
    private Member currentUser;
    private int nextBookId;
    private int nextStudentId;
    private int nextLibrarianId;

    public Library() {
        this(100);
    }

    public Library(int overdueBookCapacity) {
        books = new ArrayList<>();
        booksByISBN = new HashMap<>();
        actionStack = new Stack();
        borrowHistory = new BorrowHistory();
        booksByYear = new BookYearBST();
        overdueBooks = new MaxHeap(overdueBookCapacity);
        bookGraph = new BookGraph();
        database = new LibraryDatabase();
        members = new HashMap<>();
        waitingQueues = new HashMap<>();
        notifications = new ArrayList<>();
        reviews = new ArrayList<>();
        recommendationSystem = new RecommendationSystem();
        recentlyViewed = new RecentlyViewed();
        int[] sequences = database.loadSequences();
        nextBookId = sequences[0];
        nextStudentId = sequences[1];
        nextLibrarianId = sequences[2];
        for (Member member : database.loadMembers()) {
            if (member != null) members.put(canonicalMemberId(member.getMemberId()), normalizeLegacyMember(member));
        }
        repairLegacyMemberData();
        for (Book book : database.loadBooks()) {
            registerBook(book);
            nextBookId = Math.max(nextBookId, nextSequence(book.getIsbn(), "B-"));
        }
        reviews.addAll(database.loadReviews());
        ensureDefaultAdminAccount();
        database.saveSequences(nextBookId, nextStudentId, nextLibrarianId);
        refreshOverdueHeap();
        checkDueDateReminders();
    }

    // --- Authentication ---

    private Member normalizeLegacyMember(Member member) {
        if (member == null) return null;
        String canonicalId = canonicalMemberId(member.getMemberId());
        Member normalized = new Member(canonicalId, member.getName(), member.getType(), member.getEmail(), member.getPassword());
        normalized.setOutstandingFine(member.getOutstandingFine());
        for (BorrowRecord record : member.getBorrowingHistory()) normalized.addBorrowingRecord(record);
        return normalized;
    }

    private void repairLegacyMemberData() {
        HashMap<String, Member> repaired = new HashMap<>();
        for (Member member : members.values()) {
            if (member == null) continue;
            Member normalized = normalizeLegacyMember(member);
            if (normalized != null) repaired.put(normalized.getMemberId(), normalized);
        }
        members = repaired;
        ensureDefaultAdminAccount();
    }

    private Member resolveMember(String memberId) {
        if (memberId == null) return null;
        String trimmed = memberId.trim();
        if (trimmed.isEmpty()) return null;
        Member exact = members.get(trimmed);
        if (exact != null) return exact;
        String canonical = canonicalMemberId(trimmed);
        if (!trimmed.equals(canonical)) {
            Member mapped = members.get(canonical);
            if (mapped != null) return mapped;
        }
        for (Member member : members.values()) {
            if (member != null && canonicalMemberId(member.getMemberId()).equals(canonical)) return member;
        }
        return null;
    }

    private String canonicalMemberId(String memberId) {
        if (memberId == null) return null;
        String trimmed = memberId.trim();
        if (trimmed.startsWith("T-")) return "L-" + trimmed.substring(2);
        return trimmed;
    }

    public void ensureDefaultAdminAccount() {
        Member existing = resolveMember("L-0001");
        if (existing != null && existing.checkPassword("admin123")) {
            return;
        }
        Member admin = new Member("L-0001", "Head Librarian", Member.TYPE_LIBRARIAN,
            "librarian@smartlibrary.local", "admin123");
        members.put(admin.getMemberId(), admin);
        database.saveMembers(getMembersSnapshot());
    }

    public void ensureDefaultStudentAccount() {
        Member existing = resolveMember("S-0001");
        if (existing != null && existing.checkPassword("password123")) {
            return;
        }
        Member student = new Member("S-0001", "Student 01", Member.TYPE_STUDENT,
            "student1@smartlibrary.local", "password123");
        if (existing != null) student.setOutstandingFine(existing.getOutstandingFine());
        members.put(student.getMemberId(), student);
        database.saveMembers(getMembersSnapshot());
    }

    public Member login(String memberId, String password) {
        Member member = resolveMember(memberId);
        if (member != null && member.checkPassword(password)) {
            currentUser = member;
            notifyUser("Login successful: " + member.getName());
            return member;
        }
        return null;
    }

    public void logout() {
        if (currentUser != null) notifyUser("Logout: " + currentUser.getName());
        currentUser = null;
    }

    public Member getCurrentUser() { return currentUser; }
    public boolean isLoggedIn() { return currentUser != null; }
    public boolean isLibrarian() { return currentUser != null && currentUser.isLibrarian(); }

    // --- Book management ---

    public boolean addBook(Book book) {
        if (book == null || !validText(book.getIsbn()) || !validText(book.getTitle())
            || !validText(book.getAuthor()) || !validText(book.getCategory())
            || book.getYear() < 0 || booksByISBN.containsKey(book.getIsbn())) {
            return false;
        }
        registerBook(book);
        actionStack.push(new Action("ADD", book.snapshot()));
        persistBooks();
        notifyUser("Added book: " + book.getTitle());
        return true;
    }

    public String generateBookId() {
        String id = String.format("B-%04d", nextBookId++);
        database.saveSequences(nextBookId, nextStudentId, nextLibrarianId);
        return id;
    }

    public String generateMemberId(String type) {
        boolean librarian = Member.TYPE_LIBRARIAN.equalsIgnoreCase(Member.normalizeType(type));
        String id = String.format("%s-%04d", librarian ? "L" : "S", librarian ? nextLibrarianId++ : nextStudentId++);
        database.saveSequences(nextBookId, nextStudentId, nextLibrarianId);
        return id;
    }

    private void registerBook(Book book) {
        books.add(book);
        booksByISBN.put(book.getIsbn(), book);
        booksByYear.insert(book);
        bookGraph.addBook(book.getIsbn());
    }

    private int nextSequence(String id, String prefix) {
        if (id == null || !id.startsWith(prefix)) return 1;
        try { return Integer.parseInt(id.substring(prefix.length())) + 1; }
        catch (NumberFormatException ex) { return 1; }
    }

    public ArrayList<Book> getBooksSnapshot() { return new ArrayList<>(books); }

    public boolean removeBook(String isbn) {
        Book book = booksByISBN.get(isbn);
        if (book == null || !book.isAvailable() || book.getAvailableCopies() < book.getTotalCopies()) return false;
        books.remove(book);
        booksByISBN.remove(isbn);
        rebuildBooksByYear();
        actionStack.push(new Action("REMOVE", book.snapshot()));
        persistBooks();
        notifyUser("Removed book: " + book.getTitle());
        return true;
    }

    public boolean updateBook(String isbn, String title, String author, String category, int year,
            String shelf, String condition, int totalCopies) {
        Book book = booksByISBN.get(isbn);
        if (book == null || !validText(title) || !validText(author) || !validText(category) || year < 0) return false;
        Book previous = book.snapshot();
        book.setTitle(title);
        book.setAuthor(author);
        book.setCategory(category);
        book.setYear(year);
        book.setShelf(shelf);
        book.setCondition(condition);
        int borrowed = book.getTotalCopies() - book.getAvailableCopies();
        book.setTotalCopies(Math.max(borrowed, totalCopies));
        book.setAvailableCopies(book.getTotalCopies() - borrowed);
        rebuildBooksByYear();
        actionStack.push(new Action("UPDATE", book.snapshot(), previous));
        persistBooks();
        notifyUser("Updated book: " + title);
        return true;
    }

    public boolean updateBook(String isbn, String title, String author, String category, int year) {
        Book book = booksByISBN.get(isbn);
        if (book == null) return false;
        return updateBook(isbn, title, author, category, year, book.getShelf(), book.getCondition(), book.getTotalCopies());
    }

    public boolean updateBookCondition(String isbn, String condition) {
        Book book = booksByISBN.get(isbn);
        if (book == null || condition == null) return false;
        book.setCondition(condition);
        persistBooks();
        notifyUser("Condition updated for " + book.getTitle() + ": " + condition);
        return true;
    }

    // --- Search ---

    public ArrayList<Book> searchBooks(String query, String field) {
        ArrayList<Book> result = new ArrayList<>();
        String value = query == null ? "" : query.toLowerCase();
        String selectedField = field == null ? "Title" : field;
        for (Book book : books) {
            String candidate;
            switch (selectedField) {
                case "ISBN": candidate = book.getIsbn(); break;
                case "Author": candidate = book.getAuthor(); break;
                case "Category": candidate = book.getCategory(); break;
                case "Shelf": candidate = book.getShelf(); break;
                case "Availability": candidate = book.isAvailable() ? "available" : "borrowed"; break;
                default: candidate = book.getTitle(); break;
            }
            if (candidate.toLowerCase().contains(value)) result.add(book);
        }
        return result;
    }

    public Book searchByISBN(String isbn) {
        Book book = booksByISBN.get(isbn);
        if (book != null) recentlyViewed.view(isbn);
        return book;
    }

    public Book binarySearchTitle(String title) {
        Book[] bookArray = books.toArray(new Book[0]);
        if (bookArray.length == 0) return null;
        MergeSort.sort(bookArray, 0, bookArray.length - 1);
        return BinarySearch.search(bookArray, title);
    }

    public Book searchByYear(int year) { return booksByYear.search(year); }

    // --- Members ---

    public boolean registerMember(Member member) {
        if (member == null || !validText(member.getMemberId()) || !validText(member.getName())
            || !validText(member.getType()) || !validEmail(member.getEmail())) return false;
        String canonicalId = canonicalMemberId(member.getMemberId());
        Member normalized = normalizeLegacyMember(member);
        if (normalized == null) return false;
        if (members.containsKey(canonicalId) && !canonicalId.equals(member.getMemberId())) return false;
        if (members.containsKey(normalized.getMemberId()) && !members.get(normalized.getMemberId()).equals(member)) return false;
        members.put(normalized.getMemberId(), normalized);
        database.saveMembers(getMembersSnapshot());
        notifyUser("Registered member: " + normalized.getName());
        return true;
    }

    public boolean registerMember(String name, String type, String email) {
        return registerMember(new Member(generateMemberId(type), name, type, email));
    }

    public boolean removeMember(String memberId) {
        Member member = members.get(memberId);
        if (member == null || member.getCurrentBorrowCount() > 0) return false;
        members.remove(memberId);
        database.saveMembers(getMembersSnapshot());
        notifyUser("Removed member: " + member.getName());
        return true;
    }

    public boolean updateMember(String memberId, String name, String type, String email) {
        Member member = members.get(memberId);
        if (member == null) return false;
        member.setName(name);
        member.setType(type);
        member.setEmail(email);
        database.saveMembers(getMembersSnapshot());
        notifyUser("Updated member: " + name);
        return true;
    }

    public Member searchMember(String memberId) { return members.get(memberId); }

    public ArrayList<Member> searchMembersByName(String name) {
        ArrayList<Member> result = new ArrayList<>();
        for (Member member : members.values()) {
            if (member.getName().toLowerCase().contains(name.toLowerCase())) result.add(member);
        }
        return result;
    }

    public ArrayList<Member> getMembersSnapshot() { return new ArrayList<>(members.values()); }
    public int getMemberCount() { return members.size(); }
    public ArrayList<BorrowRecord> getMemberHistory(String memberId) { return borrowHistory.getRecordsForMember(memberId); }

    // --- Circulation ---

    public boolean borrowBook(String isbn, String borrowDate) {
        return borrowBook(isbn, borrowDate, "Guest", "Guest");
    }

    public boolean borrowBook(String isbn, String borrowDate, String memberId, String borrowerName) {
        Book book = searchByISBN(isbn);
        if (book == null) {
            System.out.println("Book not found.");
            return false;
        }
        if (!book.isAvailable()) {
            System.out.println("Book is not available.");
            return false;
        }
        Member member = members.get(memberId);
        if (member == null) member = findMemberByName(borrowerName);
        if (member == null) {
            System.out.println("Register the borrower before borrowing.");
            return false;
        }
        if (member.getOutstandingFine() > 0) {
            notifyUser(member.getName() + " must pay outstanding fines before borrowing.");
            return false;
        }
        if (!member.canBorrow() || member.getCurrentBorrowCount() >= MAX_LOANS) {
            notifyUser(member.getName() + " has reached the borrowing limit of " + MAX_LOANS + " books.");
            return false;
        }
        if (!book.borrowCopy()) return false;
        book.incrementBorrowCount();
        BorrowRecord record = new BorrowRecord(book, member.getMemberId(), member.getName(), borrowDate);
        member.addBorrowingRecord(record);
        borrowHistory.add(record);
        persistBooks();
        notifyUser("Borrow confirmed: " + member.getName() + " borrowed " + book.getTitle()
            + " (due " + record.getDueDate() + ")");
        checkDueDateReminders();
        return true;
    }

    public boolean returnBook(String isbn) {
        return returnBook(isbn, null, null);
    }

    public boolean returnBook(String isbn, String memberId, String borrowerName) {
        Book book = booksByISBN.get(isbn);
        if (book == null) {
            System.out.println("Book not found.");
            return false;
        }
        BorrowRecord active = findActiveLoan(isbn, memberId, borrowerName);
        if (active == null) {
            System.out.println("No matching active loan found.");
            return false;
        }
        book.returnCopy();
        long overdueDays = active.daysOverdue();
        double fine = calculateFine(overdueDays);
        active.close(LocalDate.now().toString(), (int) overdueDays, fine);
        Member member = members.get(active.getMemberId());
        if (member != null && fine > 0) {
            member.addFine(fine);
            notifyUser("Fine of RM" + String.format("%.2f", fine) + " added for " + member.getName());
        }
        WaitingQueue queue = waitingQueues.get(isbn);
        if (queue != null && !queue.isEmpty()) {
            String next = queue.dequeue();
            notifyUser("Book available: " + book.getTitle() + ". Next in queue: " + next);
        }
        persistBooks();
        database.saveMembers(getMembersSnapshot());
        refreshOverdueHeap();
        notifyUser("Return confirmed: " + book.getTitle());
        return true;
    }

    public boolean markBookLost(String isbn, String memberId) {
        Book book = booksByISBN.get(isbn);
        BorrowRecord active = findActiveLoan(isbn, memberId, null);
        if (book == null || active == null) return false;
        double totalFine = calculateFine(active.daysOverdue()) + REPLACEMENT_COST;
        active.markLost(totalFine);
        book.markCopyLost();
        Member member = members.get(active.getMemberId());
        if (member != null) member.addFine(totalFine);
        persistBooks();
        database.saveMembers(getMembersSnapshot());
        notifyUser("Lost book recorded: " + book.getTitle() + ". Total charge: RM" + String.format("%.2f", totalFine));
        return true;
    }

    private BorrowRecord findActiveLoan(String isbn, String memberId, String borrowerName) {
        for (BorrowRecord record : borrowHistory.getRecords()) {
            if (!record.isReturned() && record.getBook().getIsbn().equals(isbn)
                && (memberId == null || memberId.trim().isEmpty() || memberId.equals(record.getMemberId()))
                && (borrowerName == null || borrowerName.trim().isEmpty()
                    || borrowerName.equalsIgnoreCase(record.getBorrowerName()))) {
                return record;
            }
        }
        return null;
    }

    private Member findMemberByName(String name) {
        for (Member member : members.values()) {
            if (member.getName().equalsIgnoreCase(name)) return member;
        }
        return null;
    }

    public String getCurrentHolder(String isbn) {
        Book book = booksByISBN.get(isbn);
        if (book == null) return "Book not found.";
        BorrowRecord active = findActiveLoan(isbn, null, null);
        if (active != null) return active.getMemberId() + " | " + active.getBorrowerName() + " | Due: " + active.getDueDate();
        return book.isAvailable() ? "Available on shelf " + book.getShelf() : "All copies are borrowed.";
    }

    public String getBookLocator(String isbn) {
        Book book = booksByISBN.get(isbn);
        if (book == null) return "Book not found.";
        recentlyViewed.view(isbn);
        String status = book.isAvailable() ? "Available" : "Borrowed";
        return "ISBN: " + isbn + "\nTitle: " + book.getTitle()
            + "\nShelf: " + book.getShelf()
            + "\nStatus: " + status
            + "\nCopies: " + book.getAvailableCopies() + "/" + book.getTotalCopies()
            + "\nCondition: " + book.getCondition()
            + "\nRating: " + book.getRatingDisplay();
    }

    public double calculateFine(long overdueDays) {
        return overdueDays <= 0 ? 0 : ((overdueDays + 6) / 7) * FINE_PER_WEEK;
    }

    // --- Fines ---

    public boolean payFine(String memberId, double amount) {
        Member member = members.get(memberId);
        if (member == null || !member.payFine(amount)) return false;
        database.saveMembers(getMembersSnapshot());
        notifyUser(member.getName() + " paid RM" + String.format("%.2f", amount)
            + ". Remaining: RM" + String.format("%.2f", member.getOutstandingFine()));
        return true;
    }

    public double getOutstandingFine(String memberId) {
        Member member = members.get(memberId);
        return member == null ? 0 : member.getOutstandingFine();
    }

    // --- Waiting queue ---

    public void addToWaitingList(String isbn, String memberId) {
        waitingQueues.computeIfAbsent(isbn, key -> new WaitingQueue()).enqueue(memberId);
        notifyUser("Added " + memberId + " to waiting queue for " + isbn);
    }

    public String serveNextWaitingMember(String isbn) {
        WaitingQueue queue = waitingQueues.get(isbn);
        return queue == null ? null : queue.dequeue();
    }

    public int getWaitingCount(String isbn) {
        WaitingQueue queue = waitingQueues.get(isbn);
        return queue == null ? 0 : queue.size();
    }

    public ArrayList<String> getWaitingList(String isbn) {
        WaitingQueue queue = waitingQueues.get(isbn);
        return queue == null ? new ArrayList<>() : queue.toList();
    }

    // --- Reviews ---

    public boolean addReview(String isbn, String memberId, int rating, String comment) {
        Book book = booksByISBN.get(isbn);
        Member member = members.get(memberId);
        if (book == null || member == null || rating < 1 || rating > 5) return false;
        BookReview review = new BookReview(isbn, memberId, member.getName(), rating, comment);
        reviews.add(review);
        book.addRating(rating);
        database.saveReviews(reviews);
        persistBooks();
        notifyUser(member.getName() + " rated " + book.getTitle() + " " + rating + "/5");
        return true;
    }

    public ArrayList<BookReview> getReviews(String isbn) {
        ArrayList<BookReview> result = new ArrayList<>();
        for (BookReview review : reviews) {
            if (review.getIsbn().equals(isbn)) result.add(review);
        }
        return result;
    }

    // --- Recommendations & analytics ---

    public ArrayList<Book> getRecommendations(String memberId) {
        Member member = members.get(memberId);
        return member == null ? new ArrayList<>() : recommendationSystem.recommend(member, books);
    }

    public ArrayList<Recommendation> getRecommendationDetails(String memberId) {
        Member member = members.get(memberId);
        return member == null ? new ArrayList<>() : recommendationSystem.recommendWithReasons(member, books);
    }

    public ArrayList<Book> getPopularBooks(int limit) {
        ArrayList<Book> popular = sortBooks("Popularity");
        return new ArrayList<>(popular.subList(0, Math.min(limit, popular.size())));
    }

    public ArrayList<Book> getPopularThisMonth(int limit) {
        resetMonthlyCountsIfNeeded();
        ArrayList<Book> sorted = new ArrayList<>(books);
        sorted.sort((a, b) -> Integer.compare(b.getMonthlyBorrowCount(), a.getMonthlyBorrowCount()));
        return new ArrayList<>(sorted.subList(0, Math.min(limit, sorted.size())));
    }

    public ArrayList<Book> getRecentlyAdded(int limit) {
        ArrayList<Book> sorted = new ArrayList<>(books);
        sorted.sort((a, b) -> b.getDateAdded().compareTo(a.getDateAdded()));
        return new ArrayList<>(sorted.subList(0, Math.min(limit, sorted.size())));
    }

    public ArrayList<Book> getRecentlyViewedBooks() {
        ArrayList<Book> result = new ArrayList<>();
        for (String isbn : recentlyViewed.getIsbns()) {
            Book book = booksByISBN.get(isbn);
            if (book != null) result.add(book);
        }
        return result;
    }

    private String trackedMonth = YearMonth.now().toString();

    private void resetMonthlyCountsIfNeeded() {
        String current = YearMonth.now().toString();
        if (!current.equals(trackedMonth)) {
            for (Book book : books) book.setMonthlyBorrowCount(0);
            trackedMonth = current;
            persistBooks();
        }
    }

    public DashboardStats getDashboardStats() {
        DashboardStats stats = new DashboardStats();
        stats.totalBooks = books.size();
        stats.totalMembers = members.size();
        for (Book book : books) {
            if (book.isAvailable()) stats.availableBooks++;
            else stats.borrowedBooks++;
        }
        for (BorrowRecord record : borrowHistory.getRecords()) {
            if (!record.isReturned()) {
                long overdue = record.daysOverdue();
                if (overdue > 0) {
                    stats.overdueBooks++;
                    stats.outstandingFines += calculateFine(overdue);
                }
            } else {
                stats.outstandingFines += record.getFine();
            }
        }
        for (Member member : members.values()) stats.outstandingFines += member.getOutstandingFine();
        Book popular = getPopularBooks(1).isEmpty() ? null : getPopularBooks(1).get(0);
        stats.mostPopularBook = popular == null ? "None" : popular.getTitle();
        for (WaitingQueue queue : waitingQueues.values()) stats.waitingMembers += queue.size();
        stats.recentlyAdded = getRecentlyAdded(3).size();
        return stats;
    }

    // --- Sorting ---

    public ArrayList<Book> sortBooks(String criteria) {
        ArrayList<Book> sorted = getBooksSnapshot();
        HashMap<String, Comparator<Book>> comparators = new HashMap<>();
        comparators.put("Author", Comparator.comparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER));
        comparators.put("Year", Comparator.comparingInt(Book::getYear));
        comparators.put("Popularity", (first, second) -> Integer.compare(second.getBorrowCount(), first.getBorrowCount()));
        comparators.put("Availability", (first, second) -> Boolean.compare(!first.isAvailable(), !second.isAvailable()));
        comparators.put("Recently Added", (first, second) -> second.getDateAdded().compareTo(first.getDateAdded()));
        comparators.put("Rating", (first, second) -> Double.compare(second.getAverageRating(), first.getAverageRating()));
        Comparator<Book> comparator = comparators.getOrDefault(criteria,
            Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));
        for (int index = 1; index < sorted.size(); index++) {
            Book current = sorted.get(index);
            int position = index - 1;
            while (position >= 0 && comparator.compare(sorted.get(position), current) > 0) {
                sorted.set(position + 1, sorted.get(position));
                position--;
            }
            sorted.set(position + 1, current);
        }
        return sorted;
    }

    // --- Undo ---

    public String undo() {
        Action action = actionStack.pop();
        if (action == null) return "Nothing to undo.";
        Book book = action.getBook();
        switch (action.getType()) {
            case "ADD":
                books.removeIf(b -> b.getIsbn().equals(book.getIsbn()));
                booksByISBN.remove(book.getIsbn());
                rebuildBooksByYear();
                persistBooks();
                return "Undo: removed " + book.getTitle();
            case "REMOVE":
                registerBook(book.snapshot());
                persistBooks();
                return "Undo: restored " + book.getTitle();
            case "UPDATE":
                Book current = booksByISBN.get(book.getIsbn());
                Book previous = action.getPreviousState();
                if (current != null && previous != null) {
                    current.setTitle(previous.getTitle());
                    current.setAuthor(previous.getAuthor());
                    current.setCategory(previous.getCategory());
                    current.setYear(previous.getYear());
                    current.setShelf(previous.getShelf());
                    current.setCondition(previous.getCondition());
                    current.setTotalCopies(previous.getTotalCopies());
                    current.setAvailableCopies(previous.getAvailableCopies());
                    rebuildBooksByYear();
                    persistBooks();
                    return "Undo: reverted changes to " + book.getTitle();
                }
                break;
            default:
                return "Unknown undo action.";
        }
        return "Undo failed.";
    }

    public boolean canUndo() { return !actionStack.isEmpty(); }

    // --- Notifications ---

    public void checkDueDateReminders() {
        for (BorrowRecord record : borrowHistory.getRecords()) {
            if (record.isReturned()) continue;
            long daysLeft = record.daysUntilDue();
            long overdue = record.daysOverdue();
            if (overdue > 0) {
                notifyUser("OVERDUE: " + record.getBorrowerName() + " - " + record.getBook().getTitle()
                    + " (" + overdue + " days, fine RM" + String.format("%.2f", calculateFine(overdue)) + ")");
            } else if (daysLeft <= 2) {
                notifyUser("DUE SOON: " + record.getBorrowerName() + " - " + record.getBook().getTitle()
                    + " (due " + record.getDueDate() + ")");
            }
        }
        refreshOverdueHeap();
    }

    public ArrayList<Notification> getNotifications() { return new ArrayList<>(notifications); }

    public ArrayList<Notification> getUnreadNotifications() {
        ArrayList<Notification> unread = new ArrayList<>();
        for (Notification notification : notifications) if (!notification.isRead()) unread.add(notification);
        return unread;
    }

    public void markNotificationsRead() {
        for (Notification notification : notifications) notification.markRead();
    }

    private void notifyUser(String message) {
        notifications.add(new Notification("LIBRARY", message));
    }

    // --- DSA demos ---

    public void displayBooks() { for (Book book : books) System.out.println(book); }
    public void displayBorrowHistory() { borrowHistory.displayHistory(); }
    public void displayBooksInorder() { booksByYear.inorder(); }
    public void displayBooksPreorder() { booksByYear.preorder(); }
    public void displayBooksPostorder() { booksByYear.postorder(); }
    public void addOverdueBook(OverdueBook overdueBook) { overdueBooks.insert(overdueBook); }
    public OverdueBook peekMostOverdueBook() { return overdueBooks.peek(); }
    public OverdueBook extractMostOverdueBook() { return overdueBooks.extractMax(); }
    public boolean hasOverdueBooks() { return !overdueBooks.isEmpty(); }
    public void addBookRelationship(String isbn1, String isbn2) { bookGraph.addRelationship(isbn1, isbn2); }
    public void displayBookGraph() { bookGraph.display(); }
    public void breadthFirstBookSearch(String startIsbn) { bookGraph.bfs(startIsbn); }
    public void depthFirstBookSearch(String startIsbn) { bookGraph.dfs(startIsbn); }
    public void depthFirstBookSearchUsingStack(String startIsbn) { bookGraph.dfsUsingStack(startIsbn); }

    private void refreshOverdueHeap() {
        overdueBooks = new MaxHeap(Math.max(100, books.size()));
        for (BorrowRecord record : borrowHistory.getRecords()) {
            if (!record.isReturned() && record.daysOverdue() > 0) {
                overdueBooks.insert(new OverdueBook(record.getBorrowerName(), record.getBook(), (int) record.daysOverdue()));
            }
        }
    }

    private void rebuildBooksByYear() {
        booksByYear = new BookYearBST();
        for (Book book : books) booksByYear.insert(book);
    }

    private void persistBooks() { database.saveBooks(books); }

    private boolean validText(String value) { return value != null && !value.trim().isEmpty(); }
    private boolean validEmail(String value) { return validText(value) && value.contains("@"); }
}
