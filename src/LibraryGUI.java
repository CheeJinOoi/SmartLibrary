import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
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
import javax.swing.table.DefaultTableModel;

public class LibraryGUI {
    private static final Color NAVY = new Color(18, 31, 48);
    private static final Color TEAL = new Color(28, 143, 137);
    private static final Color GOLD = new Color(239, 170, 71);
    private static final Color INK = new Color(37, 48, 61);
    private static final Color MUTED = new Color(105, 119, 132);
    private static final Color PAPER = new Color(247, 249, 247);

    private final Library library;
    private final DefaultTableModel tableModel = new DefaultTableModel(
        new String[] {"ISBN", "Title", "Author", "Category", "Year", "Status"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable bookTable = new JTable(tableModel);
    private final DefaultTableModel memberModel = new DefaultTableModel(
        new String[] {"Member ID", "Name", "Type", "Email", "Active loans"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable memberTable = new JTable(memberModel);
    private final ArrayList<JTextArea> activityBoxes = new ArrayList<>();
    private final JLabel bookCount = new JLabel("0 books");
    private final JLabel status = new JLabel("Ready");

    public LibraryGUI() {
        this(new Library());
    }

    public LibraryGUI(Library library) {
        this.library = library;
    }

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
        tabs.addTab("Members", buildMembers());
        tabs.addTab("Circulation", buildCirculation());
        tabs.addTab("Structures", buildStructures());
        tabs.addTab("Dashboard", buildDashboard());
        root.add(tabs, BorderLayout.CENTER);

        status.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
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

        JPanel bookSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bookSearch.setOpaque(false);
        JComboBox<String> searchField = new JComboBox<>(new String[] {"Title", "Author", "Category", "ISBN", "Availability"});
        JTextField searchQuery = field("Search by title, author, category...");
        JButton search = button("Search", true);
        search.addActionListener(e -> refreshBooks(library.searchBooks(searchQuery.getText().trim(), (String) searchField.getSelectedItem())));
        bookSearch.add(label("SEARCH BOOKS BY"));
        bookSearch.add(searchField);
        bookSearch.add(searchQuery);
        bookSearch.add(search);
        JPanel isbnSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        isbnSearch.setOpaque(false);
        JTextField isbnLookup = field("Enter ISBN");
        JButton searchISBN = button("Search by ISBN", true);
        searchISBN.addActionListener(e -> searchByISBN(isbnLookup));
        isbnSearch.add(label("ISBN LOOKUP"));
        isbnSearch.add(isbnLookup);
        isbnSearch.add(searchISBN);
        JPanel sortTools = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        sortTools.setOpaque(false);
        JComboBox<String> sortChoice = new JComboBox<>(new String[] {"Title", "Author", "Year", "Popularity", "Availability"});
        JButton sort = button("Sort books", false);
        sort.addActionListener(e -> refreshBooks(library.sortBooks((String) sortChoice.getSelectedItem())));
        JButton remove = button("Remove selected", false);
        remove.addActionListener(e -> removeSelectedBook());
        JButton allStatus = button("All book status", false);
        allStatus.addActionListener(e -> refreshBooks());
        sortTools.add(label("SORT BY")); sortTools.add(sortChoice); sortTools.add(sort); sortTools.add(allStatus); sortTools.add(remove);
        JPanel catalogTools = new JPanel(new GridLayout(3, 1, 0, 8));
        catalogTools.setOpaque(false);
        catalogTools.add(bookSearch);
        catalogTools.add(isbnSearch);
        catalogTools.add(sortTools);
        panel.add(catalogTools, BorderLayout.NORTH);

        bookTable.setRowHeight(38);
        bookTable.setFont(new Font("SansSerif", Font.PLAIN, 15));
        bookTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        bookTable.getTableHeader().setBackground(NAVY);
        bookTable.getTableHeader().setForeground(Color.WHITE);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.setShowGrid(false);
        bookTable.setIntercellSpacing(new Dimension(0, 1));
        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);

        JPanel addPanel = new JPanel(new BorderLayout(8, 8));
        addPanel.setBackground(Color.WHITE);
        addPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 227, 225)),
            BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JTextField isbn = field("ISBN");
        JTextField title = field("Title");
        JTextField author = field("Author");
        JTextField category = field("Category");
        JTextField year = field("Year");
        isbn.setText(library.generateBookId());
        isbn.setEditable(false);
        addPanel.add(label("ADD OR UPDATE BOOK"), BorderLayout.NORTH);
        JPanel fields = new JPanel(new GridLayout(1, 5, 8, 0));
        fields.setOpaque(false);
        fields.add(labeledField("ISBN", isbn));
        fields.add(labeledField("TITLE", title));
        fields.add(labeledField("AUTHOR", author));
        fields.add(labeledField("CATEGORY", category));
        fields.add(labeledField("YEAR", year));
        addPanel.add(fields, BorderLayout.CENTER);
        JPanel bookActions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bookActions.setOpaque(false);
        JButton add = button("Add book", true);
        add.addActionListener(e -> addBook(isbn, title, author, category, year));
        bookActions.add(add);
        JButton update = button("Update selected", false);
        update.addActionListener(e -> updateSelectedBook(title, author, category, year));
        bookActions.add(update);
        addPanel.add(bookActions, BorderLayout.SOUTH);
        panel.add(addPanel, BorderLayout.SOUTH);
        refreshBooks();
        return panel;
    }

    private JPanel buildMembers() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(PAPER);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        memberTable.setRowHeight(28);
        memberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        memberTable.getTableHeader().setBackground(NAVY);
        memberTable.getTableHeader().setForeground(Color.WHITE);
        JPanel memberSearch = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        memberSearch.setOpaque(false);
        JTextField memberQuery = field("Search member by name");
        JButton searchMembers = button("Search members", true);
        searchMembers.addActionListener(e -> refreshMembers(library.searchMembersByName(memberQuery.getText().trim())));
        JButton showAllMembers = button("Show all members", false);
        showAllMembers.addActionListener(e -> refreshMembers());
        memberSearch.add(label("MEMBER NAME")); memberSearch.add(memberQuery); memberSearch.add(searchMembers); memberSearch.add(showAllMembers);
        panel.add(memberSearch, BorderLayout.NORTH);
        panel.add(new JScrollPane(memberTable), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 227, 225)),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JTextField id = field("Member ID");
        JTextField name = field("Name");
            JComboBox<String> type = new JComboBox<>(new String[] {"Student", "Teacher"});
        JTextField email = field("Email");
            id.setText(library.generateMemberId("Student"));
            id.setEditable(false);
            type.addActionListener(e -> id.setText(library.generateMemberId((String) type.getSelectedItem())));
        form.add(label("REGISTER MEMBER"), constraints(0, 0, 2));
        form.add(label("ID"), constraints(0, 1, 1)); form.add(id, constraints(1, 1, 1));
        form.add(label("NAME"), constraints(0, 2, 1)); form.add(name, constraints(1, 2, 1));
        form.add(label("TYPE"), constraints(0, 3, 1)); form.add(type, constraints(1, 3, 1));
        form.add(label("EMAIL"), constraints(0, 4, 1)); form.add(email, constraints(1, 4, 1));
        JButton register = button("Register member", true);
        register.addActionListener(e -> registerMember(id, name, type, email));
        form.add(register, constraints(0, 5, 2));
        JButton history = button("Selected member history", false);
        history.addActionListener(e -> showSelectedMemberHistory());
        form.add(history, constraints(0, 6, 2));
        panel.add(form, BorderLayout.SOUTH);
        refreshMembers();
        return panel;
    }

    private JPanel buildDashboard() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(PAPER);
        panel.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        JPanel stats = new JPanel(new GridLayout(2, 4, 10, 10));
        stats.setOpaque(false);
        String[] labels = {"TOTAL BOOKS", "AVAILABLE", "BORROWED", "MEMBERS", "OVERDUE", "OUTSTANDING FINES", "MOST POPULAR", "WAITING"};
        for(String text : labels) {
            JLabel stat = new JLabel(text, JLabel.CENTER);
            stat.putClientProperty("dashboardTitle", text);
            stats.add(stat);
        }
        panel.add(stats, BorderLayout.NORTH);
        JTextArea output = new JTextArea();
        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, 13));
        output.setBackground(NAVY); output.setForeground(Color.WHITE);
        panel.add(new JScrollPane(output), BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.setOpaque(false);
        JButton refresh = button("Refresh dashboard", true);
        refresh.addActionListener(e -> refreshDashboard(stats, output));
        JButton popular = button("Top 5 popular books", false);
        popular.addActionListener(e -> { output.setText(booksText(library.getPopularBooks(5))); });
        JButton notifications = button("Notifications", false);
        notifications.addActionListener(e -> { output.setText(notificationsText(false)); library.markNotificationsRead(); });
        JButton unread = button("New notifications", false);
        unread.addActionListener(e -> output.setText(notificationsText(true)));
        JTextField memberId = field("Member ID for recommendations");
        JButton recommend = button("Recommendations", false);
        recommend.addActionListener(e -> output.setText(recommendationsText(memberId.getText().trim())));
        JTextField holderIsbn = field("ISBN to check current holder");
        JButton holder = button("Who has this book?", false);
        holder.addActionListener(e -> output.setText(library.getCurrentHolder(holderIsbn.getText().trim())));
        actions.add(refresh); actions.add(popular); actions.add(notifications); actions.add(unread);
        actions.add(new JLabel("Recommend for Member ID:")); actions.add(memberId); actions.add(recommend);
        actions.add(new JLabel("Check holder ISBN:")); actions.add(holderIsbn); actions.add(holder);
        panel.add(actions, BorderLayout.SOUTH);
        refreshDashboard(stats, output);
        return panel;
    }

    private JPanel buildCirculation() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(PAPER);
        panel.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);
        JTextField borrowIsbn = field("ISBN");
        JTextField borrowDate = field("Borrow date");
        borrowDate.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
        JTextField borrowerId = field("Member ID");
        JTextField borrowerName = field("Name");
        JPanel borrowCard = cardPanel("BORROW BOOK");
        addCardField(borrowCard, 1, "ISBN", borrowIsbn);
        addCardField(borrowCard, 2, "DATE", borrowDate);
        addCardField(borrowCard, 3, "MEMBER ID", borrowerId);
        addCardField(borrowCard, 4, "NAME", borrowerName);
        JButton borrowButton = button("Borrow", true);
        borrowButton.addActionListener(e -> borrow(borrowIsbn, borrowDate, borrowerId, borrowerName));
        borrowCard.add(borrowButton, constraints(0, 5, 2));
        cards.add(borrowCard);

        JTextField returnIsbn = field("ISBN");
        JTextField returnMemberId = field("Member ID");
        JTextField returnMemberName = field("Name");
        JPanel returnCard = cardPanel("RETURN BOOK");
        addCardField(returnCard, 1, "ISBN", returnIsbn);
        addCardField(returnCard, 2, "MEMBER ID", returnMemberId);
        addCardField(returnCard, 3, "NAME", returnMemberName);
        JButton returnButton = button("Return", false);
        returnButton.addActionListener(e -> returnBook(returnIsbn, returnMemberId, returnMemberName));
        returnCard.add(returnButton, constraints(0, 4, 2));
        cards.add(returnCard);

        JTextField student = field("Student name");
        JPanel waitingCard = cardPanel("WAITING LIST");
        addCardField(waitingCard, 1, "STUDENT", student);
        JButton waitButton = button("Add student", false);
        waitButton.addActionListener(e -> {
            String name = student.getText().trim();
            if (name.isEmpty()) { showError("Enter a student name."); return; }
            library.addToWaitingList(name);
            log("Added to waiting list: " + name);
            student.setText("");
        });
        waitingCard.add(waitButton, constraints(0, 2, 2));
        JButton serve = button("Serve next student", false);
        serve.addActionListener(e -> log("Next student: " + valueOrEmpty(library.serveNextWaitingStudent())));
        waitingCard.add(serve, constraints(0, 3, 2));
        JButton history = button("View borrow history", false);
        history.addActionListener(e -> log(capture(() -> library.displayBorrowHistory())));
        waitingCard.add(history, constraints(0, 4, 2));
        cards.add(waitingCard);

        panel.add(cards, BorderLayout.NORTH);
        panel.add(buildActivityPanel(), BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildStructures() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.setBackground(PAPER);
        panel.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);
        JTextArea output = new JTextArea();
        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, 13));
        output.setBackground(NAVY); output.setForeground(Color.WHITE);

        JPanel bst = structureCard("YEAR INDEX (BST)", "Search and traverse books by publication year.");
        JTextField year = field("Year");
        JButton findYear = button("Find book by year", true);
        findYear.addActionListener(e -> { try { output.setText(bookLine(library.searchByYear(Integer.parseInt(year.getText().trim())))); } catch (NumberFormatException ex) { showError("Enter a valid year."); } });
        bst.add(year); bst.add(findYear);
        JButton inorder = button("Show sorted years", false);
        inorder.addActionListener(e -> output.setText(capture(() -> library.displayBooksInorder())));
        bst.add(inorder);
        cards.add(bst);

        JPanel graph = structureCard("BOOK RELATIONSHIPS (GRAPH)", "Link related books and explore connections.");
        JTextField isbn1 = field("Starting ISBN");
        JTextField isbn2 = field("Related ISBN");
        JButton link = button("Link books", true);
        link.addActionListener(e -> { library.addBookRelationship(isbn1.getText().trim(), isbn2.getText().trim()); output.setText("Linked books: " + isbn1.getText().trim() + " and " + isbn2.getText().trim()); });
        graph.add(isbn1); graph.add(isbn2); graph.add(link);
        JButton bfs = button("Explore connections", false);
        bfs.addActionListener(e -> output.setText(capture(() -> library.breadthFirstBookSearch(isbn1.getText().trim()))));
        graph.add(bfs);
        cards.add(graph);

        JPanel heap = structureCard("OVERDUE PRIORITY (MAX HEAP)", "Track the books with the most overdue days.");
        JTextField overdueIsbn = field("Book ISBN");
        JTextField student = field("Member name");
        JTextField days = field("Days overdue");
        JButton overdue = button("Add overdue record", true);
        overdue.addActionListener(e -> addOverdue(overdueIsbn, student, days));
        heap.add(overdueIsbn); heap.add(student); heap.add(days); heap.add(overdue);
        JButton mostOverdue = button("Show highest priority", false);
        mostOverdue.addActionListener(e -> output.setText(bookLine(library.peekMostOverdueBook())));
        heap.add(mostOverdue);
        cards.add(heap);

        panel.add(cards, BorderLayout.NORTH);
        panel.add(new JScrollPane(output), BorderLayout.CENTER);
        return panel;
    }

    private JPanel structureCard(String title, String description) {
        JPanel card = new JPanel(new GridLayout(0, 1, 0, 8));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 227, 225)), BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        card.add(label(title));
        JLabel details = new JLabel("<html>" + description + "</html>");
        details.setForeground(MUTED);
        card.add(details);
        return card;
    }

    private JPanel buildActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 6));
        panel.setOpaque(false);
        JLabel title = label("ACTIVITY");
        panel.add(title, BorderLayout.NORTH);
        JTextArea activity = new JTextArea();
        activity.setEditable(false);
        activity.setFont(new Font("Monospaced", Font.PLAIN, 12));
        activity.setForeground(new Color(211, 228, 226));
        activity.setBackground(NAVY);
        activity.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        activityBoxes.add(activity);
        panel.add(new JScrollPane(activity), BorderLayout.CENTER);
        return panel;
    }

    private void addBook(JTextField isbn, JTextField title, JTextField author, JTextField category, JTextField year) {
        try {
            if (isbn.getText().trim().isEmpty() || title.getText().trim().isEmpty() || author.getText().trim().isEmpty()
                || category.getText().trim().isEmpty() || Integer.parseInt(year.getText().trim()) < 0) throw new IllegalArgumentException();
            boolean added = library.addBook(new Book(isbn.getText().trim(), title.getText().trim(), author.getText().trim(), category.getText().trim(), Integer.parseInt(year.getText().trim())));
            if (!added) { showError("That ISBN already exists in the database."); return; }
            log("Added book: " + title.getText().trim());
            isbn.setText(library.generateBookId()); title.setText(""); author.setText(""); category.setText(""); year.setText("");
            refreshBooks();
        } catch (IllegalArgumentException ex) {
            showError("ISBN, title, author, and a valid year are required.");
        }
    }

    private void registerMember(JTextField id, JTextField name, JComboBox<String> type, JTextField email) {
        if (id.getText().trim().isEmpty() || name.getText().trim().isEmpty() || !email.getText().contains("@")) { showError("Member name and a valid email are required."); return; }
        if (!library.registerMember(new Member(id.getText().trim(), name.getText().trim(), (String) type.getSelectedItem(), email.getText().trim()))) {
            showError("That member ID already exists."); return;
        }
        log("Registered member: " + id.getText() + " / " + name.getText().trim());
        name.setText(""); email.setText(""); id.setText(library.generateMemberId((String) type.getSelectedItem())); refreshMembers();
    }

    private void refreshMembers() {
        refreshMembers(library.getMembersSnapshot());
    }

    private void refreshMembers(ArrayList<Member> source) {
        memberModel.setRowCount(0);
        for (Member member : source) {
            memberModel.addRow(new Object[] {member.getMemberId(), member.getName(), member.getType(), member.getEmail(), member.getCurrentBorrowCount()});
        }
    }

    private void showSelectedMemberHistory() {
        int row = memberTable.getSelectedRow();
        if (row < 0) { showError("Select a member first."); return; }
        String memberId = memberTable.getValueAt(memberTable.convertRowIndexToModel(row), 0).toString();
        StringBuilder output = new StringBuilder("History for ").append(memberId).append("\n");
        for (BorrowRecord record : library.getMemberHistory(memberId)) output.append(record).append('\n');
        log(output.toString());
    }

    private void refreshDashboard(JPanel stats, JTextArea output) {
        DashboardStats values = library.getDashboardStats();
        String[] valuesText = {Integer.toString(values.totalBooks), Integer.toString(values.availableBooks), Integer.toString(values.borrowedBooks),
            Integer.toString(values.totalMembers), Integer.toString(values.overdueBooks), String.format("RM%.2f", values.outstandingFines),
            values.mostPopularBook, Integer.toString(values.waitingMembers)};
        for (int index = 0; index < stats.getComponentCount(); index++) {
            JLabel label = (JLabel) stats.getComponent(index);
            String title = (String) label.getClientProperty("dashboardTitle");
            label.setText("<html><center>" + title + "<br><b>" + valuesText[index] + "</b></center></html>");
            label.setForeground(INK);
        }
        output.setText(values.toString());
    }

    private String booksText(ArrayList<Book> books) {
        if (books.isEmpty()) return "No matching books.";
        StringBuilder output = new StringBuilder();
        for (Book book : books) output.append(book).append('\n');
        return output.toString();
    }

    private String recommendationsText(String memberId) {
        if (memberId.isEmpty()) return "Enter a Member ID, for example S-0001 or T-0001.";
        Member member = library.searchMember(memberId);
        if (member == null) return "Member not found: " + memberId;
        ArrayList<Recommendation> recommendations = library.getRecommendationDetails(memberId);
        if (recommendations.isEmpty()) return "No recommendations yet. Borrow a book to build a reading profile.";
        StringBuilder output = new StringBuilder("Recommendations for ").append(member.getName()).append("\n");
        for (Recommendation recommendation : recommendations) output.append(recommendation).append('\n');
        return output.toString();
    }

    private String notificationsText(boolean unreadOnly) {
        StringBuilder output = new StringBuilder();
        ArrayList<Notification> source = unreadOnly ? library.getUnreadNotifications() : library.getNotifications();
        output.append(unreadOnly ? "NEW NOTIFICATIONS\n" : "ALL NOTIFICATIONS\n");
        for (Notification notification : source) output.append(notification).append('\n');
        return source.isEmpty() ? output.append("No notifications.").toString() : output.toString();
    }

    private void searchByISBN(JTextField isbnField) {
        String isbn = isbnField.getText().trim();
        if (isbn.isEmpty()) { showError("Enter an ISBN to search."); return; }
        Book book = library.searchByISBN(isbn);
        if (book == null) {
            log("No book found for ISBN: " + isbn);
            showError("No book matches that ISBN.");
            return;
        }
        log(book.toString());
    }

    private void borrow(JTextField isbn, JTextField date, JTextField borrowerId, JTextField borrowerName) {
        Book book = library.searchByISBN(isbn.getText().trim());
        if (book == null) { showError("No book matches that ISBN."); return; }
        if (!book.isAvailable()) {
            String name = borrowerName.getText().trim();
            if (name.isEmpty()) {
                showError("This book is borrowed. Enter a student name to join the waiting list.");
                return;
            }
            library.addToWaitingList(isbn.getText().trim(), name);
            log(name + " joined the waiting list for " + book.getTitle());
            borrowerName.setText("");
            return;
        }
        String name = borrowerName.getText().trim();
        if (name.isEmpty()) {
            showError("Enter the student or teacher name.");
            return;
        }
        String memberId = borrowerId.getText().trim();
        if (!library.borrowBook(isbn.getText().trim(), date.getText().trim(), memberId, name)) {
            showError("This borrower already has an active loan.");
            return;
        }
        log(memberId + " / " + name + " borrowed: " + book.getTitle());
        borrowerId.setText("");
        borrowerName.setText("");
        refreshBooks();
    }

    private JPanel cardPanel(String heading) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 227, 225)),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        card.add(label(heading), constraints(0, 0, 2));
        return card;
    }

    private void addCardField(JPanel card, int row, String heading, java.awt.Component field) {
        card.add(label(heading), constraints(0, row, 1));
        card.add(field, constraints(1, row, 1));
    }

    private void returnBook(JTextField isbn, JTextField memberId, JTextField memberName) {
        Book book = library.searchByISBN(isbn.getText().trim());
        if (book == null) { showError("No book matches that ISBN."); return; }
        if (!library.returnBook(isbn.getText().trim(), memberId.getText().trim(), memberName.getText().trim())) {
            showError("That book is already available.");
            return;
        }
        log("Returned: " + book.getTitle() + " by " + memberId.getText().trim());
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
        refreshBooks(library.getBooksSnapshot());
    }

    private void refreshBooks(ArrayList<Book> source) {
        tableModel.setRowCount(0);
        for (Book book : source) {
            tableModel.addRow(new Object[] { book.getIsbn(), book.getTitle(), book.getAuthor(), book.getCategory(), book.getYear(), book.isAvailable() ? "Available" : "Borrowed" });
        }
        bookCount.setText(library.getBooksSnapshot().size() + " books");
        status.setText("Catalog synced");
    }

    private void removeSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row < 0) { showError("Select a book first."); return; }
        String isbn = bookTable.getValueAt(bookTable.convertRowIndexToModel(row), 0).toString();
        if (!library.removeBook(isbn)) { showError("Only available books can be removed."); return; }
        log("Removed book: " + isbn);
        refreshBooks();
    }

    private void updateSelectedBook(JTextField title, JTextField author, JTextField category, JTextField year) {
        int row = bookTable.getSelectedRow();
        if (row < 0) { showError("Select a book first."); return; }
        try {
            if (title.getText().trim().isEmpty() || author.getText().trim().isEmpty() || category.getText().trim().isEmpty()) {
                showError("Title, author, and category are required for updates."); return;
            }
            String isbn = bookTable.getValueAt(bookTable.convertRowIndexToModel(row), 0).toString();
            if (!library.updateBook(isbn, title.getText().trim(), author.getText().trim(), category.getText().trim(), Integer.parseInt(year.getText().trim()))) {
                showError("Book update failed."); return;
            }
            log("Updated book: " + isbn);
            refreshBooks();
        } catch (NumberFormatException ex) { showError("Year must be a number."); }
    }

    private void log(String message) {
        if (message == null || message.trim().isEmpty()) return;
        for (JTextArea activity : activityBoxes) {
            activity.append(message.trim() + "\n");
            activity.setCaretPosition(activity.getDocument().getLength());
        }
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

    private JPanel labeledField(String heading, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 3));
        panel.setOpaque(false);
        panel.add(label(heading), BorderLayout.NORTH);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

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

}
