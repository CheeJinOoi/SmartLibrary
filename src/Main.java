public class Main {
    public static void main(String[] args){
        Library library = new Library();

        library.addBook(
            new Book(
                "978001",
                "Introduction to Algorithms",
                "Cormen",
                2009
            )
        );
        library.addBook(
            new Book(
                "978002",
                "Clean Code",
                "Robert Martin",
                2008
            )
        );
        library.addBook(
            new Book(
                "978003",
                "Effective Warning",
                "Joshua Bloch",
                2018
            )
        );
        System.out.println("==== ALL BOOKS ====");
        library.displayBooks();
        System.out.println();
        System.out.println("==== SEARCH ====");
        Book result = library.searchByISBN("978009");

        if(result != null){
            System.out.println("Book found:");
            System.out.println(result);
        }else{
            System.out.println("Book not found");
        }
    }
}