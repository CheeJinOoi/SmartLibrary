import java.util.ArrayList;

public class RecentlyViewed {
    private static final int CAPACITY = 10;
    private final Stack stack = new Stack();

    public void view(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) return;
        stack.push(new Action("VIEW", new Book(isbn.trim(), "view", "view", 0)));
        trim();
    }

    private void trim() {
        ArrayList<String> seen = new ArrayList<>();
        Stack temp = new Stack();
        while (!stack.isEmpty()) {
            Action action = stack.pop();
            String isbn = action.getBook().getIsbn();
            if (!seen.contains(isbn)) {
                seen.add(isbn);
                temp.push(action);
            }
            if (seen.size() >= CAPACITY) break;
        }
        while (!temp.isEmpty()) stack.push(temp.pop());
    }

    public ArrayList<String> getIsbns() {
        ArrayList<String> result = new ArrayList<>();
        Stack temp = new Stack();
        while (!stack.isEmpty()) {
            Action action = stack.pop();
            result.add(0, action.getBook().getIsbn());
            temp.push(action);
        }
        while (!temp.isEmpty()) stack.push(temp.pop());
        return result;
    }
}
