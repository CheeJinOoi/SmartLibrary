public class LibrarySmokeTest {
    public static void main(String[] args) {
        Library library = new Library();
        String isbn = "SMOKE-" + System.currentTimeMillis();
        String memberId = "M-SMOKE-" + System.currentTimeMillis();
        int testYear = 1000 + (int) (System.currentTimeMillis() % 8000);
        Book book = new Book(isbn, "Smoke Test", "System", testYear);
        String secondIsbn = isbn + "-2";
        Book secondBook = new Book(secondIsbn, "Second Smoke Test", "System", testYear + 1);

        check(library.addBook(book), "add book");
        check(library.addBook(secondBook), "add second book");
        check(library.registerMember(new Member(memberId, "Alice", "Student", "alice@test.local")), "register member");
        check(!library.addBook(new Book(isbn, "Duplicate", "System", 2026)), "reject duplicate ISBN");
        check(library.searchByISBN(isbn) == book, "ISBN search");
        check(library.searchBooks("Smoke", "Title").contains(book), "title search");
        check(library.searchBooks("System", "Author").contains(book), "author search");
        check(!library.searchMembersByName("Alice").isEmpty(), "member name search");
        check(library.searchByYear(testYear) == book, "year search");
        check(library.borrowBook(isbn, "2026-08-19", "Student", "Alice"), "borrow book");
        check(library.getCurrentHolder(isbn).contains("Alice"), "current book holder");
        check(!library.borrowBook(secondIsbn, "2026-08-19", "Student", "Alice"), "one active loan per borrower");
        check(!library.borrowBook(isbn, "2026-08-19"), "reject second borrow");
        check(library.getDashboardStats().borrowedBooks >= 1, "dashboard borrowed count");
        java.util.ArrayList<Book> sorted = library.sortBooks("Popularity");
        check(sorted.indexOf(book) < sorted.indexOf(secondBook), "manual popularity sort");
        check(!library.getRecommendations(memberId).isEmpty(), "recommendations");
        check(!library.getNotifications().isEmpty(), "notifications");
        check(library.calculateFine(1) == 1.0, "one-week fine");
        check(library.calculateFine(8) == 2.0, "second-week fine");

        Library reloaded = new Library();
        check(!reloaded.searchByISBN(isbn).isAvailable(), "persist borrowed status");
        check(reloaded.returnBook(isbn), "return book");
        reloaded.addToWaitingList("Test Student");
        check("Test Student".equals(reloaded.serveNextWaitingStudent()), "waiting queue");
        reloaded.addOverdueBook(new OverdueBook("Test Student", book, 4));
        check(reloaded.peekMostOverdueBook() != null, "overdue heap");
        reloaded.addBookRelationship(isbn, isbn);
        check(reloaded.searchByISBN(isbn) != null, "book graph registration");
        check(library.searchByISBN(isbn) != null, "original session retains book");
        library.undo();
        check(library.searchByISBN(secondIsbn) == null, "undo second add");
        library.undo();
        check(library.searchByISBN(isbn) == null, "undo removal");
        System.out.println("Library smoke test passed: catalog, members, search, persistence, circulation, queue, history, heap, graph, dashboard, recommendations, notifications, and undo.");
    }

    private static void check(boolean condition, String operation) {
        if (!condition) {
            throw new AssertionError("Failed: " + operation);
        }
    }
}
