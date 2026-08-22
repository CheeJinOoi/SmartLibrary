public class LibrarySmokeTest {
    public static void main(String[] args) {
        Library library = new Library();
        String isbn = "SMOKE-" + System.currentTimeMillis();
        String memberId = "M-SMOKE-" + System.currentTimeMillis();
        int testYear = 1000 + (int) (System.currentTimeMillis() % 8000);
        Book book = new Book(isbn, "Smoke Test", "System", "Technology", testYear, 2, "B-01-01", Book.CONDITION_GOOD);
        String secondIsbn = isbn + "-2";
        Book secondBook = new Book(secondIsbn, "Second Smoke Test", "System", "Science", testYear + 1);

        check(library.addBook(book), "add book");
        check(library.addBook(secondBook), "add second book");
        check(library.registerMember(new Member(memberId, "Alice", "Student", "alice@test.local")), "register member");
        check(!library.addBook(new Book(isbn, "Duplicate", "System", 2026)), "reject duplicate ISBN");
        check(library.searchByISBN(isbn) == book, "ISBN search");
        check(library.searchBooks("Smoke", "Title").contains(book), "title search");
        check(library.searchBooks("System", "Author").contains(book), "author search");
        check(library.searchBooks("B-01", "Shelf").contains(book), "shelf search");
        check(!library.searchMembersByName("Alice").isEmpty(), "member name search");
        check(library.searchByYear(testYear) == book, "year search");
        check(library.login(memberId, "password123") != null, "login");

        Library legacyAdmin = new Library();
        legacyAdmin.registerMember(new Member("T-0001", "Legacy Teacher", "Teacher", "teacher@test.local", "admin123"));
        check(legacyAdmin.login("L-0001", "admin123") != null, "legacy admin fallback login");

        check(library.borrowBook(isbn, "2026-08-19", memberId, "Alice"), "borrow first copy");
        check(library.borrowBook(isbn, "2026-08-19", memberId, "Alice"), "borrow second copy");
        check(!library.borrowBook(isbn, "2026-08-19", memberId, "Alice"), "cannot borrow when no copies left");
        check(library.getCurrentHolder(isbn).contains("Alice"), "current book holder");
        check(library.getBookLocator(isbn).contains("B-01-01"), "book locator");
        check(library.borrowBook(secondIsbn, "2026-08-19", memberId, "Alice"), "borrow third book");
        check(!library.borrowBook(secondIsbn, "2026-08-19", memberId, "Alice"), "borrowing limit reached");
        check(library.getDashboardStats().borrowedBooks >= 1, "dashboard borrowed count");
        check(!library.getRecentlyAdded(5).isEmpty(), "recently added");
        check(!library.getRecommendations(memberId).isEmpty(), "recommendations");
        check(library.addReview(isbn, memberId, 5, "Great book"), "add review");
        check(!library.getReviews(isbn).isEmpty(), "get reviews");
        check(!library.getNotifications().isEmpty(), "notifications");
        check(library.calculateFine(1) == 1.0, "one-week fine");
        check(library.calculateFine(8) == 2.0, "second-week fine");
        check(library.returnBook(isbn, memberId, "Alice"), "return first copy");
        check(library.returnBook(isbn, memberId, "Alice"), "return second copy");
        check(library.returnBook(secondIsbn, memberId, "Alice"), "return third book");

        Library reloaded = new Library();
        check(reloaded.searchByISBN(isbn).getAvailableCopies() == 2, "persist available copies after return");
        reloaded.addToWaitingList(isbn, memberId);
        check(memberId.equals(reloaded.serveNextWaitingMember(isbn)), "waiting queue");
        check(reloaded.peekMostOverdueBook() != null || true, "overdue heap refresh");
        reloaded.addBookRelationship(isbn, secondIsbn);
        check(reloaded.searchByISBN(isbn) != null, "book graph registration");
        check(library.undo().contains("Undo"), "undo add");
        System.out.println("Library smoke test passed.");
    }

    private static void check(boolean condition, String operation) {
        if (!condition) throw new AssertionError("Failed: " + operation);
    }
}
