public class BookYearBST {
    private Node root;
    private class Node{
        int year;
        Book book;
        Node left;
        Node right;

        Node(Book book){
            this.book = book;
            this.year = book.getYear();
            this.left = null;
            this.right = null;
        }
    }
    public void insert(Book book){
        root = insertRecursive(root, book);
    }
    private Node insertRecursive(Node current, Book book){
        if(current == null){
            return new Node(book);
        }
        if(book.getYear() < current.year){
            current.left = insertRecursive(current.left, book);
        }else if(book.getYear() > current.year){
            current.right = insertRecursive(current.right, book);
        }
        return current;
    }

    public Book search(int year){
        return searchRecursive(root, year);
    }
    private Book searchRecursive(Node current,int year) {
        if (current == null) {
            return null;
        }

        if (year == current.year) {
            return current.book;
        }

        if (year < current.year) {
            return searchRecursive(current.left,year);
        }
        return searchRecursive(current.right,year);
    }
    public void inorder(){
        inorderRecursive(root);
    }
    private void inorderRecursive(Node current){
        if(current == null){
            return;
        }
        inorderRecursive(current.left);
        System.out.println(current.year + " | " + current.book.getTitle());
        inorderRecursive(current.right);
    }
    public void preorder() {
        preorderRecursive(root);
    }

    private void preorderRecursive(Node current) {

        if (current == null) {
            return;
        }

        System.out.println(current.year + " | " + current.book.getTitle());

        preorderRecursive(current.left);
        preorderRecursive(current.right);
    }

    public void postorder() {
        postorderRecursive(root);
    }

    private void postorderRecursive(Node current) {

        if (current == null) {
            return;
        }

        postorderRecursive(current.left);
        postorderRecursive(current.right);

        System.out.println(current.year + " | " + current.book.getTitle());
    }
}    
