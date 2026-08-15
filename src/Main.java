public class Main {
    public static void main(String[] args) {

        Library library = new Library();

        Book java = new Book(
            "978001",
            "Effective Java",
            "Joshua Bloch",
            2018
        );

        Book python = new Book(
            "978002",
            "Python Crash Course",
            "Eric Mattheus",
            2023
        );

        library.addBook(java);
        library.addBook(python);

        System.out.println("===== BEFORE UNDO =====");

        library.displayBooks();

        System.out.println();

        library.undo();

        System.out.println();

        System.out.println("===== AFTER UNDO =====");

        library.displayBooks();
    }
}