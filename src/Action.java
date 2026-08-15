public class Action {
    private String type;
    private Book book;

    public Action(String type, Book book){
        this.type = type;
        this.book = book;
    }
    public String getType(){
        return type;
    }

    public Book getBook(){
        return book;
    }
    @Override
    public String toString(){
        return type + ": " + book.getTitle();
    }
}
