import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class LibraryGUI {
    private static final Color NAVY = new Color(18, 31, 48);
    private static final Color TEAL = new Color(28, 143, 137);
    private static final Color GOLD = new Color(239, 170, 71);
    private static final Color INK = new Color(37, 48, 61);
    private static final Color MUTED = new Color(105, 119, 132);
    private static final Color PAPER = new Color(247, 249, 247);

    private final Library library = new Library();
    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[] {"ISBN", "Title", "Author", "Year", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bookTable = new JTable(tableModel);
    private final JTextArea activity = new JTextArea();
    private final JLabel bookCount = new JLabel("0 books");
    private final JLabel status = new JLabel("Ready");

    public void showWindow() {
        JFrame frame = new JFrame("SmartLibrary");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(980, 680));
        frame.setSize(1120, 760);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(buildContent());
        frame.setVisible(true);
    }

    private JPanel buildContent() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(PAPER);
        root.add(buildHeader(), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.addTab("Catalog", buildCatalog());
        tabs.addTab("Circulation", buildCirculation());
        tabs.addTab("Structures", buildStructures());
        root.add(tabs, BorderLayout.CENTER);

        status.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        status.setForeground(MUTED);
        root.add(status, BorderLayout.SOUTH);
        return root;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(NAVY);
        header.setBorder(BorderFactory.createEmptyBorder(20, 26, 20, 26));

        JPanel titlePanel = new JPanel(new BorderLayout(0, 3));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("SMARTLIBRARY");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 26));
        JLabel subtitle = new JLabel("A calm command center for your collection");
        subtitle.setForeground(new Color(176, 197, 208));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 13));
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(subtitle, BorderLayout.SOUTH);
        header.add(titlePanel, BorderLayout.WEST);

        bookCount.setForeground(GOLD);
        bookCount.setFont(new Font("SansSerif", Font.BOLD, 15));
        header.add(bookCount, BorderLayout.EAST);
        return header;
    }

    private JPanel buildCatalog() {
        JPanel panel = new JPanel(new BorderLayout(18, 18));
        panel.setBackground(PAPER);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);
        JTextField filter = new JTextField();
        filter.putClientProperty("JTextField.placeholderText", "Filter catalog by ISBN, title, or author");
        filter.setFont(new Font("SansSerif", Font.PLAIN, 14));
        JButton clear = button("Clear", false);
        toolbar.add(filter, BorderLayout.CENTER);
        toolbar.add(clear, BorderLayout.EAST);
        panel.add(toolbar, BorderLayout.NORTH);

        bookTable.setRowHeight(30);
        bookTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        bookTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        bookTable.getTableHeader().setBackground(NAVY);
        bookTable.getTableHeader().setForeground(Color.WHITE);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.setShowGrid(false);
        bookTable.setIntercellSpacing(new Dimension(0, 1));
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        bookTable.setRowSorter(sorter);
        filter.getDocument().addDocumentListener((SimpleDocumentListener) () -> {
            String text = filter.getText().trim();
            sorter.setRowFilter(text.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
        });
        clear.addActionListener(e -> filter.setText(""));
        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);

        JPanel addPanel = new JPanel(new GridBagLayout());
        addPanel.setBackground(Color.WHITE);
        addPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 227, 225)),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JTextField isbn = field("ISBN");
        JTextField title = field("Title");
        JTextField author = field("Author");
        JTextField year = field("Year");
        addPanel.add(label("ADD A BOOK"), constraints(0, 0, 2));
        addPanel.add(label("ISBN"), constraints(0, 1, 1));
        addPanel.add(isbn, constraints(1, 1, 1));
        addPanel.add(label("Title"), constraints(0, 2, 1));
        addPanel.add(title, constraints(1, 2, 1));
        addPanel.add(label("Author"), constraints(0, 3, 1));
        addPanel.add(author, constraints(1, 3, 1));
        addPanel.add(label("Year"), constraints(0, 4, 1));
        addPanel.add(year, constraints(1, 4, 1));
        JButton add = button("Add book", true);
        add.addActionListener(e -> addBook(isbn, title, author, year));
        addPanel.add(add, constraints(0, 5, 2));
        panel.add(addPanel, BorderLayout.SOUTH);
        refreshBooks();
        return panel;
    }

    private JPanel buildCirculation() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PAPER);
        panel.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        JPanel actions = new JPanel(new GridBagLayout());
        actions.setBackground(Color.WHITE);
        actions.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 227, 225)),
            BorderFactory.createEmptyBorder(18, 20, 18, 20)));

        JTextField borrowIsbn = field("ISBN");
        JTextField borrower = field("Borrow date");
        borrower.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        addRow(actions, 0, "BORROW BOOK", borrowIsbn, button("Borrow", true), e -> borrow(borrowIsbn, borrower));
        JTextField returnIsbn = field("ISBN");
        addRow(actions, 1, "RETURN BOOK", returnIsbn, null, null);
        JButton returnButton = button("Return", false);
        returnButton.addActionListener(e -> returnBook(returnIsbn));
        actions.add(returnButton, constraints(2, 1, 1));
        JTextField student = field("Student name");
        addRow(actions, 2, "WAITING LIST", student, null, null);
        JButton waitButton = button("Add student", false);
        waitButton.addActionListener(e -> {
            String name = student.getText().trim();
            if (name.isEmpty()) { showError("Enter a student name."); return; }
            library.addToWaitingList(name);
            log("Added to waiting list: " + name);
            student.setText("");
        });
        actions.add(waitButton, constraints(2, 2, 1));
        JButton serve = button("Serve next student", false);
        serve.addActionListener(e -> log("Next student: " + valueOrEmpty(library.serveNextWaitingStudent())));
        actions.add(serve, constraints(1, 3, 2));
        JButton history = button("View borrow history", false);
        history.addActionListener(e -> log(capture(() -> library.displayBorrowHistory())));
        actions.add(history, constraints(1, 4, 2));

        GridBagConstraints outer = constraints(0, 0, 1);
        outer.weightx = 1;
        outer.weighty = 0;
        outer.fill = GridBagConstraints.HORIZONTAL;
        panel.add(actions, outer);
        GridBagConstraints logConstraints = constraints(0, 1, 1);
        logConstraints.weightx = 1;
        logConstraints.weighty = 1;
        logConstraints.fill = GridBagConstraints.BOTH;
        panel.add(buildActivityPanel(), logConstraints);
        return panel;
    }

    private JPanel buildStructures() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(PAPER);
        panel.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        controls.setOpaque(false);
        JTextField isbn1 = field("ISBN 1");
        JTextField isbn2 = field("ISBN 2");
        JTextField year = field("Year");
        JTextField overdueIsbn = field("ISBN");
        JTextField student = field("Student");
        JTextField days = field("Days");
        controls.add(isbn1); controls.add(isbn2);
        JButton relate = button("Link books", true);
        relate.addActionListener(e -> { library.addBookRelationship(isbn1.getText().trim(), isbn2.getText().trim()); log("Linked " + isbn1.getText().trim() + " and " + isbn2.getText().trim()); });
        controls.add(relate);
        controls.add(year);
        JButton findYear = button("Find year", false);
        findYear.addActionListener(e -> { try { log(bookLine(library.searchByYear(Integer.parseInt(year.getText().trim())))); } catch (NumberFormatException ex) { showError("Enter a valid year."); } });
        controls.add(findYear);
        controls.add(overdueIsbn); controls.add(student); controls.add(days);
        JButton overdue = button("Add overdue", false);
        overdue.addActionListener(e -> addOverdue(overdueIsbn, student, days));
        controls.add(overdue);
        JButton mostOverdue = button("Most overdue", false);
        mostOverdue.addActionListener(e -> log(bookLine(library.peekMostOverdueBook())));
        controls.add(mostOverdue);
        panel.add(controls, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.setOpaque(false);
        addStructureButton(buttons, "BST inorder", () -> library.displayBooksInorder());
        addStructureButton(buttons, "BST preorder", () -> library.displayBooksPreorder());
        addStructureButton(buttons, "BST postorder", () -> library.displayBooksPostorder());
        addStructureButton(buttons, "Graph", () -> library.displayBookGraph());
        addStructureButton(buttons, "Graph BFS", () -> library.breadthFirstBookSearch(isbn1.getText().trim()));
        addStructureButton(buttons, "Graph DFS", () -> library.depthFirstBookSearch(isbn1.getText().trim()));
        addStructureButton(buttons, "Undo last add", () -> { library.undo(); refreshBooks(); });
        panel.add(buttons, BorderLayout.CENTER);
        panel.add(buildActivityPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private void addStructureButton(JPanel panel, String text, Runnable action) {
        JButton button = button(text, false);
        button.addActionListener(e -> log(capture(action)));
        panel.add(button);
    }

    private JPanel buildActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel title = label("ACTIVITY");
        panel.add(title, BorderLayout.NORTH);
        activity.setEditable(false);
        activity.setFont(new Font("Monospaced", Font.PLAIN, 12));
        activity.setForeground(new Color(211, 228, 226));
        activity.setBackground(NAVY);
        activity.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        panel.add(new JScrollPane(activity), BorderLayout.CENTER);
        return panel;
    }

    private void addBook(JTextField isbn, JTextField title, JTextField author, JTextField year) {
        try {
            if (isbn.getText().trim().isEmpty() || title.getText().trim().isEmpty()) throw new IllegalArgumentException();
            boolean added = library.addBook(new Book(isbn.getText().trim(), title.getText().trim(), author.getText().trim(), Integer.parseInt(year.getText().trim())));
            if (!added) { showError("That ISBN already exists in the database."); return; }
            log("Added book: " + title.getText().trim());
            isbn.setText(""); title.setText(""); author.setText(""); year.setText("");
            refreshBooks();
        } catch (IllegalArgumentException ex) {
            showError("ISBN, title, author, and a valid year are required.");
        }
    }

    private void borrow(JTextField isbn, JTextField date) {
        Book book = library.searchByISBN(isbn.getText().trim());
        if (book == null) { showError("No book matches that ISBN."); return; }
        if (!library.borrowBook(isbn.getText().trim(), date.getText().trim())) {
            showError("That book is already borrowed.");
            return;
        }
        log("Borrowed: " + book.getTitle());
        refreshBooks();
    }

    private void returnBook(JTextField isbn) {
        Book book = library.searchByISBN(isbn.getText().trim());
        if (book == null) { showError("No book matches that ISBN."); return; }
        if (!library.returnBook(isbn.getText().trim())) {
            showError("That book is already available.");
            return;
        }
        log("Returned: " + book.getTitle());
        refreshBooks();
    }

    private void addOverdue(JTextField isbn, JTextField student, JTextField days) {
        try {
            Book book = library.searchByISBN(isbn.getText().trim());
            if (book == null) { showError("No book matches that ISBN."); return; }
            library.addOverdueBook(new OverdueBook(student.getText().trim(), book, Integer.parseInt(days.getText().trim())));
            log("Added overdue record for " + book.getTitle());
        } catch (NumberFormatException ex) { showError("Days overdue must be a number."); }
    }

    private void refreshBooks() {
        tableModel.setRowCount(0);
        for (Book book : library.getBooksSnapshot()) {
            tableModel.addRow(new Object[] { book.getIsbn(), book.getTitle(), book.getAuthor(), book.getYear(), book.isAvailable() ? "Available" : "Borrowed" });
        }
        bookCount.setText(library.getBooksSnapshot().size() + " books");
        status.setText("Catalog synced");
    }

    private void log(String message) {
        if (message == null || message.trim().isEmpty()) return;
        activity.append(message.trim() + "\n");
        activity.setCaretPosition(activity.getDocument().getLength());
        status.setText("Last action completed");
    }

    private String capture(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
        try { action.run(); } finally { System.setOut(original); }
        return output.toString();
    }

    private String bookLine(Book book) { return book == null ? "No matching book." : book.toString(); }
    private String bookLine(OverdueBook overdueBook) { return overdueBook == null ? "No overdue books." : overdueBook.toString(); }
    private String valueOrEmpty(String value) { return value == null ? "Waiting list is empty." : value; }
    private void showError(String message) { JOptionPane.showMessageDialog(null, message, "SmartLibrary", JOptionPane.WARNING_MESSAGE); }

    private JTextField field(String placeholder) {
        JTextField field = new JTextField();
        field.setPreferredSize(new Dimension(142, 30));
        field.putClientProperty("JTextField.placeholderText", placeholder);
        return field;
    }

    private JButton button(String text, boolean primary) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(primary ? Color.WHITE : INK);
        button.setBackground(primary ? TEAL : new Color(232, 238, 235));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        return button;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(MUTED);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        return label;
    }

    private GridBagConstraints constraints(int x, int y, int width) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = x; constraints.gridy = y; constraints.gridwidth = width;
        constraints.insets = new Insets(5, 5, 5, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = width == 2 ? 1 : 0;
        return constraints;
    }

    private void addRow(JPanel panel, int row, String heading, JTextField input, JButton button, java.awt.event.ActionListener action) {
        panel.add(label(heading), constraints(0, row, 1));
        panel.add(input, constraints(1, row, 1));
        if (button != null) {
            button.addActionListener(action);
            panel.add(button, constraints(2, row, 1));
        }
    }

    private interface SimpleDocumentListener extends javax.swing.event.DocumentListener {
        void update();
        @Override
        default void insertUpdate(javax.swing.event.DocumentEvent e) { update(); }
        @Override
        default void removeUpdate(javax.swing.event.DocumentEvent e) { update(); }
        @Override
        default void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
    }
}
