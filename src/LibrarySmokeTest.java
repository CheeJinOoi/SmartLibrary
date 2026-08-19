public class LibrarySmokeTest {
    public static void main(String[] args) {
        Library library = new Library();
        String isbn = "SMOKE-" + System.currentTimeMillis();
        Book book = new Book(isbn, "Smoke Test", "System", 1900);

        check(library.addBook(book), "add book");
        check(!library.addBook(new Book(isbn, "Duplicate", "System", 2026)), "reject duplicate ISBN");
        check(library.searchByISBN(isbn) == book, "ISBN search");
        check(library.searchByYear(1900) == book, "year search");
        check(library.borrowBook(isbn, "2026-08-19"), "borrow book");
        check(!library.borrowBook(isbn, "2026-08-19"), "reject second borrow");

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
        check(library.searchByISBN(isbn) == null, "undo removal");
        System.out.println("Library smoke test passed: catalog, search, persistence, circulation, queue, heap, graph, and undo.");
    }

    private static void check(boolean condition, String operation) {
        if (!condition) {
            throw new AssertionError("Failed: " + operation);
        }
    }
}
