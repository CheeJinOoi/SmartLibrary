public class Main {
    public static void main(String[] args) {
        Library library = new Library();
        DemoDataSeeder.seed(library);
        javax.swing.SwingUtilities.invokeLater(() -> new LibraryGUI(library).showWindow());
    }
}