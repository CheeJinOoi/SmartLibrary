public class Action {
    private String type;
    private Book book;
    private Book previousState;

    public Action(String type, Book book) {
        this(type, book, null);
    }

    public Action(String type, Book book, Book previousState) {
        this.type = type;
        this.book = book;
        this.previousState = previousState;
    }

    public String getType() { return type; }
    public Book getBook() { return book; }
    public Book getPreviousState() { return previousState; }

    @Override
    public String toString() {
        return type + ": " + book.getTitle();
    }
}
