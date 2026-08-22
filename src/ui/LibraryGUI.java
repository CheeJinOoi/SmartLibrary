import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
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
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public class LibraryGUI {
    private AppTheme theme = AppTheme.light();
    private final Library library;
    private JFrame frame;
    private CardLayout rootLayout;
    private JPanel rootPanel;
    private JTabbedPane tabs;
    private final JLabel userLabel = new JLabel("Not signed in");
    private final JLabel bookCount = new JLabel("0 books");
    private final JLabel status = new JLabel("Ready");

    private final DefaultTableModel bookModel = new DefaultTableModel(
        new String[] {"ISBN", "Title", "Author", "Category", "Year", "Shelf", "Copies", "Rating", "Status"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable bookTable = new JTable(bookModel);

    private final DefaultTableModel memberModel = new DefaultTableModel(
        new String[] {"Member ID", "Name", "Role", "Email", "Loans", "Fine (RM)"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable memberTable = new JTable(memberModel);

    private final ArrayList<JTextArea> activityBoxes = new ArrayList<>();
    private final ArrayList<JButton> themedButtons = new ArrayList<>();
    private final ArrayList<JLabel> themedLabels = new ArrayList<>();
    private final ArrayList<JLabel> statCards = new ArrayList<>();
    private JTextArea homeOutput;
    private JTextArea detailOutput;
    private JPanel loginOuterPanel;
    private JPanel mainRootPanel;
    private JPanel headerPanelRef;
    private JButton themeToggleBtn;

    public LibraryGUI() { this(new Library()); }
    public LibraryGUI(Library library) { this.library = library; }

    public void showWindow() {
        frame = new JFrame("SmartLibrary");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1100, 720));
        frame.setSize(1200, 800);
        frame.setLocationRelativeTo(null);

        rootLayout = new CardLayout();
        rootPanel = new JPanel(rootLayout);
        rootPanel.add(buildLoginPanel(), "login");
        rootPanel.add(buildMainPanel(), "main");
        frame.setContentPane(rootPanel);
        frame.setVisible(true);
        rootLayout.show(rootPanel, "login");
    }

    // --- Login ---

    private JPanel buildLoginPanel() {
        loginOuterPanel = new JPanel(new BorderLayout());
        loginOuterPanel.putClientProperty("themeRole", "login-bg");

        JPanel card = new JPanel(new GridBagLayout());
        card.putClientProperty("themeRole", "surface");
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(theme.border),
            BorderFactory.createEmptyBorder(32, 40, 32, 40)));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("SmartLibrary Sign In");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.putClientProperty("themeRole", "ink");
        themedLabels.add(title);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2;
        card.add(title, c);

        JLabel hint = new JLabel("<html><center>Demo: S-0001 / password123 &nbsp;|&nbsp; L-0001 / admin123</center></html>");
        hint.putClientProperty("themeRole", "muted");
        themedLabels.add(hint);
        c.gridy = 1;
        card.add(hint, c);

        JTextField memberId = field("Member ID (e.g. S-0001)");
        JPasswordField password = new JPasswordField();
        password.setPreferredSize(new Dimension(260, 32));

        c.gridwidth = 1; c.gridy = 2; c.gridx = 0;
        card.add(label("MEMBER ID"), c);
        c.gridx = 1; card.add(memberId, c);
        c.gridy = 3; c.gridx = 0;
        card.add(label("PASSWORD"), c);
        c.gridx = 1; card.add(password, c);

        JButton loginBtn = button("Sign In", true);
        loginBtn.addActionListener(e -> {
            Member member = library.login(memberId.getText().trim(), new String(password.getPassword()));
            if (member == null) {
                showError("Invalid member ID or password.");
                return;
            }
            onLoginSuccess();
            rootLayout.show(rootPanel, "main");
        });
        c.gridy = 4; c.gridx = 0; c.gridwidth = 2;
        card.add(loginBtn, c);

        JButton loginThemeBtn = button("Dark mode", false);
        loginThemeBtn.addActionListener(e -> toggleTheme());
        c.gridy = 5;
        card.add(loginThemeBtn, c);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER));
        center.setOpaque(false);
        center.setBorder(BorderFactory.createEmptyBorder(60, 0, 60, 0));
        center.add(card);
        loginOuterPanel.add(center, BorderLayout.CENTER);
        applyTheme();
        return loginOuterPanel;
    }

    private void onLoginSuccess() {
        Member user = library.getCurrentUser();
        userLabel.setText(user.getName() + " (" + user.getType() + ")");
        configureTabsForRole();
        refreshAll();
        log("Signed in as " + user.getMemberId());
    }

    // --- Main layout ---

    private JPanel buildMainPanel() {
        mainRootPanel = new JPanel(new BorderLayout(0, 0));
        mainRootPanel.putClientProperty("themeRole", "background");
        mainRootPanel.add(buildHeader(), BorderLayout.NORTH);

        tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.addTab("Home", buildHomeTab());
        tabs.addTab("Catalog", buildCatalogTab());
        tabs.addTab("Circulation", buildCirculationTab());
        tabs.addTab("Book Details", buildBookDetailsTab());
        tabs.addTab("My Account", buildMyAccountTab());
        tabs.addTab("Members", buildMembersTab());
        tabs.addTab("DSA Lab", buildDsaTab());
        mainRootPanel.add(tabs, BorderLayout.CENTER);

        status.putClientProperty("themeRole", "muted");
        themedLabels.add(status);
        status.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        mainRootPanel.add(status, BorderLayout.SOUTH);
        applyTheme();
        return mainRootPanel;
    }

    private void configureTabsForRole() {
        boolean librarian = library.isLibrarian();
        tabs.setEnabledAt(5, librarian);
        tabs.setEnabledAt(6, librarian);
        if (!librarian && tabs.getSelectedIndex() >= 5) tabs.setSelectedIndex(0);
    }

    private JPanel buildHeader() {
        headerPanelRef = new JPanel(new BorderLayout());
        headerPanelRef.putClientProperty("themeRole", "header");
        headerPanelRef.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));

        JPanel titlePanel = new JPanel(new BorderLayout(0, 2));
        titlePanel.setOpaque(false);
        JLabel title = new JLabel("SMARTLIBRARY");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        userLabel.putClientProperty("themeRole", "header-subtext");
        themedLabels.add(userLabel);
        userLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        titlePanel.add(title, BorderLayout.NORTH);
        titlePanel.add(userLabel, BorderLayout.SOUTH);
        headerPanelRef.add(titlePanel, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        bookCount.putClientProperty("themeRole", "accent");
        themedLabels.add(bookCount);
        bookCount.setFont(new Font("SansSerif", Font.BOLD, 14));
        actions.add(bookCount);
        themeToggleBtn = button("Dark mode", false);
        themeToggleBtn.putClientProperty("themeRole", "header-button");
        themeToggleBtn.addActionListener(e -> toggleTheme());
        actions.add(themeToggleBtn);
        JButton logout = button("Sign Out", false);
        logout.putClientProperty("themeRole", "header-button");
        logout.addActionListener(e -> {
            library.logout();
            rootLayout.show(rootPanel, "login");
            status.setText("Signed out");
        });
        actions.add(logout);
        headerPanelRef.add(actions, BorderLayout.EAST);
        return headerPanelRef;
    }

    // --- Home tab ---

    private JPanel buildHomeTab() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.putClientProperty("themeRole", "background");
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel stats = new JPanel(new GridLayout(2, 4, 10, 10));
        stats.setOpaque(false);
        statCards.clear();
        String[] labels = {"TOTAL BOOKS", "AVAILABLE", "BORROWED", "MEMBERS", "OVERDUE", "OUTSTANDING FINES", "MOST POPULAR", "WAITING"};
        for (String text : labels) {
            JLabel stat = new JLabel(text, JLabel.CENTER);
            stat.putClientProperty("dashboardTitle", text);
            stat.putClientProperty("themeRole", "stat-card");
            stat.setOpaque(true);
            stat.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(theme.border),
                BorderFactory.createEmptyBorder(12, 8, 12, 8)));
            statCards.add(stat);
            stats.add(stat);
        }
        panel.add(stats, BorderLayout.NORTH);

        homeOutput = outputArea();
        panel.add(new JScrollPane(homeOutput), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton refresh = button("Refresh", true);
        refresh.addActionListener(e -> refreshHome(stats, homeOutput));
        JButton recent = button("Recently Added", false);
        recent.addActionListener(e -> homeOutput.setText(booksText(library.getRecentlyAdded(10), "Recently Added Books")));
        JButton trending = button("Trending This Month", false);
        trending.addActionListener(e -> homeOutput.setText(booksText(library.getPopularThisMonth(5), "Most Borrowed This Month")));
        JButton popular = button("All-Time Popular", false);
        popular.addActionListener(e -> homeOutput.setText(booksText(library.getPopularBooks(5), "All-Time Popular")));
        JButton notify = button("Notifications", false);
        notify.addActionListener(e -> { homeOutput.setText(notificationsText(false)); library.markNotificationsRead(); });
        JButton unread = button("New Alerts", false);
        unread.addActionListener(e -> homeOutput.setText(notificationsText(true)));
        actions.add(refresh); actions.add(recent); actions.add(trending); actions.add(popular);
        actions.add(notify); actions.add(unread);
        panel.add(actions, BorderLayout.SOUTH);

        panel.putClientProperty("statsPanel", stats);
        return panel;
    }

    // --- Catalog tab ---

    private JPanel buildCatalogTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.putClientProperty("themeRole", "background");
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel toolbar = new JPanel(new GridLayout(2, 1, 0, 8));
        toolbar.setOpaque(false);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchRow.setOpaque(false);
        JComboBox<String> searchField = new JComboBox<>(new String[] {"Title", "Author", "Category", "ISBN", "Shelf", "Availability"});
        JTextField searchQuery = field("Type to search...");
        JButton search = button("Search", true);
        search.addActionListener(e -> refreshBooks(library.searchBooks(searchQuery.getText().trim(), (String) searchField.getSelectedItem())));
        JButton binary = button("Binary search title", false);
        binary.addActionListener(e -> {
            Book found = library.binarySearchTitle(searchQuery.getText().trim());
            ArrayList<Book> results = new ArrayList<>();
            if (found != null) results.add(found);
            refreshBooks(results);
        });
        JButton showAll = button("Show all", false);
        showAll.addActionListener(e -> refreshBooks());
        searchRow.add(label("SEARCH")); searchRow.add(searchField); searchRow.add(searchQuery);
        searchRow.add(search); searchRow.add(binary); searchRow.add(showAll);

        JPanel sortRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        sortRow.setOpaque(false);
        JComboBox<String> sortChoice = new JComboBox<>(new String[] {"Title", "Author", "Year", "Popularity", "Recently Added", "Rating", "Availability"});
        JButton sort = button("Sort", false);
        sort.addActionListener(e -> refreshBooks(library.sortBooks((String) sortChoice.getSelectedItem())));
        sortRow.add(label("SORT BY")); sortRow.add(sortChoice); sortRow.add(sort);
        toolbar.add(searchRow);
        toolbar.add(sortRow);
        panel.add(toolbar, BorderLayout.NORTH);

        bookTable.setRowHeight(34);
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.putClientProperty("themeRole", "table");
        bookTable.getTableHeader().putClientProperty("themeRole", "table-header");
        bookTable.setShowGrid(false);
        panel.add(new JScrollPane(bookTable), BorderLayout.CENTER);

        JPanel form = buildBookForm();
        panel.add(form, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildBookForm() {
        JPanel addPanel = new JPanel(new BorderLayout(8, 8));
        addPanel.putClientProperty("themeRole", "surface");
        addPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(theme.border),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        JTextField isbn = field("ISBN"); isbn.setEditable(false);
        JTextField title = field("Title");
        JTextField author = field("Author");
        JTextField category = field("Category");
        JTextField year = field("Year");
        JTextField shelf = field("Shelf e.g. A-03-12");
        JTextField copies = field("Copies");
        copies.setText("1");
        JComboBox<String> condition = new JComboBox<>(new String[] {Book.CONDITION_GOOD, Book.CONDITION_DAMAGED, Book.CONDITION_REPAIR, Book.CONDITION_LOST});

        bookTable.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int row = bookTable.getSelectedRow();
            if (row < 0) return;
            isbn.setText(str(row, 0)); title.setText(str(row, 1)); author.setText(str(row, 2));
            category.setText(str(row, 3)); year.setText(str(row, 4)); shelf.setText(str(row, 5));
        });

        JPanel fields = new JPanel(new GridLayout(2, 4, 8, 8));
        fields.setOpaque(false);
        fields.add(labeledField("ID", isbn));
        fields.add(labeledField("TITLE", title));
        fields.add(labeledField("AUTHOR", author));
        fields.add(labeledField("CATEGORY", category));
        fields.add(labeledField("YEAR", year));
        fields.add(labeledField("SHELF", shelf));
        fields.add(labeledField("COPIES", copies));
        JPanel conditionField = new JPanel(new BorderLayout(0, 3));
        conditionField.setOpaque(false);
        conditionField.add(label("CONDITION"), BorderLayout.NORTH);
        conditionField.add(condition, BorderLayout.CENTER);
        fields.add(conditionField);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        JButton add = button("Add Book", true);
        add.addActionListener(e -> {
            if (!library.isLibrarian()) { showError("Only librarians can add books."); return; }
            isbn.setText(library.generateBookId());
            addBook(isbn, title, author, category, year, shelf, copies, condition);
        });
        JButton update = button("Update Selected", false);
        update.addActionListener(e -> {
            if (!library.isLibrarian()) { showError("Only librarians can update books."); return; }
            updateBook(isbn, title, author, category, year, shelf, copies, condition);
        });
        JButton remove = button("Remove Selected", false);
        remove.addActionListener(e -> {
            if (!library.isLibrarian()) { showError("Only librarians can remove books."); return; }
            removeSelectedBook();
        });
        JButton undo = button("Undo Last Action", false);
        undo.addActionListener(e -> { log(library.undo()); refreshBooks(); });
        actions.add(add); actions.add(update); actions.add(remove); actions.add(undo);

        addPanel.add(label("BOOK MANAGEMENT (LIBRARIAN)"), BorderLayout.NORTH);
        addPanel.add(fields, BorderLayout.CENTER);
        addPanel.add(actions, BorderLayout.SOUTH);
        isbn.setText(library.generateBookId());
        return addPanel;
    }

    // --- Circulation tab ---

    private JPanel buildCirculationTab() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.putClientProperty("themeRole", "background");
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);

        JTextField borrowIsbn = field("Book ISBN");
        JTextField borrowDate = field("Date");
        borrowDate.setText(today());
        JTextField borrowerId = field("Your Member ID");
        if (library.getCurrentUser() != null) borrowerId.setText(library.getCurrentUser().getMemberId());

        JPanel borrowCard = cardPanel("BORROW");
        addCardField(borrowCard, 1, "ISBN", borrowIsbn);
        addCardField(borrowCard, 2, "DATE", borrowDate);
        addCardField(borrowCard, 3, "MEMBER ID", borrowerId);
        JButton borrowBtn = button("Borrow Book", true);
        borrowBtn.addActionListener(e -> borrow(borrowIsbn, borrowDate, borrowerId));
        borrowCard.add(borrowBtn, constraints(0, 4, 2));
        cards.add(borrowCard);

        JTextField returnIsbn = field("Book ISBN");
        JTextField returnMemberId = field("Member ID");
        if (library.getCurrentUser() != null) returnMemberId.setText(library.getCurrentUser().getMemberId());
        JPanel returnCard = cardPanel("RETURN");
        addCardField(returnCard, 1, "ISBN", returnIsbn);
        addCardField(returnCard, 2, "MEMBER ID", returnMemberId);
        JButton returnBtn = button("Return Book", false);
        returnBtn.addActionListener(e -> returnBook(returnIsbn, returnMemberId));
        returnCard.add(returnBtn, constraints(0, 3, 2));
        cards.add(returnCard);

        JTextField queueIsbn = field("Book ISBN");
        JTextField queueMemberId = field("Member ID");
        if (library.getCurrentUser() != null) queueMemberId.setText(library.getCurrentUser().getMemberId());
        JPanel queueCard = cardPanel("RESERVE / QUEUE");
        addCardField(queueCard, 1, "ISBN", queueIsbn);
        addCardField(queueCard, 2, "MEMBER ID", queueMemberId);
        JButton joinQueue = button("Join Waiting List", false);
        joinQueue.addActionListener(e -> {
            String isbn = queueIsbn.getText().trim();
            String memberId = queueMemberId.getText().trim();
            if (isbn.isEmpty() || memberId.isEmpty()) { showError("ISBN and Member ID required."); return; }
            library.addToWaitingList(isbn, memberId);
            log("Joined queue for " + isbn + ". Position info: " + library.getWaitingList(isbn));
        });
        queueCard.add(joinQueue, constraints(0, 3, 2));
        JButton viewQueue = button("View Queue", false);
        viewQueue.addActionListener(e -> log("Queue for " + queueIsbn.getText().trim() + ": " + library.getWaitingList(queueIsbn.getText().trim())));
        queueCard.add(viewQueue, constraints(0, 4, 2));
        cards.add(queueCard);

        panel.add(cards, BorderLayout.NORTH);
        panel.add(buildActivityPanel(), BorderLayout.CENTER);
        return panel;
    }

    // --- Book details tab ---

    private JPanel buildBookDetailsTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.putClientProperty("themeRole", "background");
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        top.setOpaque(false);
        JTextField isbn = field("Enter ISBN");
        JButton locate = button("Locate Book", true);
        detailOutput = outputArea();
        locate.addActionListener(e -> detailOutput.setText(library.getBookLocator(isbn.getText().trim()) + "\n\nHolder: "
            + library.getCurrentHolder(isbn.getText().trim())));
        JButton reviews = button("Show Reviews", false);
        reviews.addActionListener(e -> detailOutput.setText(reviewsText(isbn.getText().trim())));
        top.add(label("ISBN")); top.add(isbn); top.add(locate); top.add(reviews);
        panel.add(top, BorderLayout.NORTH);

        JPanel bottom = new JPanel(new GridLayout(1, 2, 12, 0));
        bottom.setOpaque(false);

        JPanel reviewForm = cardPanel("RATE & REVIEW");
        JComboBox<String> rating = new JComboBox<>(new String[] {"5", "4", "3", "2", "1"});
        JTextField reviewIsbn = field("ISBN");
        JTextArea comment = new JTextArea(3, 20);
        comment.setLineWrap(true);
        addCardField(reviewForm, 1, "ISBN", reviewIsbn);
        addCardField(reviewForm, 2, "RATING", rating);
        reviewForm.add(label("COMMENT"), constraints(0, 3, 1));
        reviewForm.add(new JScrollPane(comment), constraints(1, 3, 1));
        JButton submitReview = button("Submit Review", true);
        submitReview.addActionListener(e -> {
            Member user = library.getCurrentUser();
            if (user == null) return;
            if (!library.addReview(reviewIsbn.getText().trim(), user.getMemberId(),
                Integer.parseInt((String) rating.getSelectedItem()), comment.getText())) {
                showError("Could not submit review.");
                return;
            }
            log("Review submitted.");
            comment.setText("");
        });
        reviewForm.add(submitReview, constraints(0, 4, 2));
        bottom.add(reviewForm);

        JPanel conditionForm = cardPanel("CONDITION & LOST BOOK");
        JTextField condIsbn = field("ISBN");
        JComboBox<String> condition = new JComboBox<>(new String[] {Book.CONDITION_GOOD, Book.CONDITION_DAMAGED, Book.CONDITION_REPAIR, Book.CONDITION_LOST});
        addCardField(conditionForm, 1, "ISBN", condIsbn);
        addCardField(conditionForm, 2, "CONDITION", condition);
        JButton setCond = button("Update Condition", false);
        setCond.addActionListener(e -> {
            if (!library.isLibrarian()) { showError("Librarian only."); return; }
            library.updateBookCondition(condIsbn.getText().trim(), (String) condition.getSelectedItem());
            log("Condition updated.");
            refreshBooks();
        });
        conditionForm.add(setCond, constraints(0, 3, 2));
        JButton markLost = button("Mark as Lost", false);
        markLost.addActionListener(e -> {
            Member user = library.getCurrentUser();
            if (user == null) return;
            if (library.markBookLost(condIsbn.getText().trim(), user.getMemberId())) {
                log("Lost book processed with replacement fine.");
                refreshBooks();
            } else showError("Could not mark as lost.");
        });
        conditionForm.add(markLost, constraints(0, 4, 2));
        bottom.add(conditionForm);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(detailOutput), bottom);
        split.setResizeWeight(0.55);
        split.setOpaque(false);
        panel.add(split, BorderLayout.CENTER);
        return panel;
    }

    // --- My Account tab ---

    private JPanel buildMyAccountTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.putClientProperty("themeRole", "background");
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JTextArea accountOutput = outputArea();
        panel.add(new JScrollPane(accountOutput), BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(2, 1, 8, 8));
        actions.setOpaque(false);

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row1.setOpaque(false);
        JButton profile = button("My Profile", true);
        profile.addActionListener(e -> {
            Member user = library.getCurrentUser();
            if (user == null) return;
            accountOutput.setText(user + "\n\nBorrowing History:\n" + historyText(user.getMemberId()));
        });
        JButton recommend = button("Recommendations", false);
        recommend.addActionListener(e -> {
            Member user = library.getCurrentUser();
            if (user == null) return;
            accountOutput.setText(recommendationsText(user.getMemberId()));
        });
        JButton viewed = button("Recently Viewed", false);
        viewed.addActionListener(e -> accountOutput.setText(booksText(library.getRecentlyViewedBooks(), "Recently Viewed")));
        row1.add(profile); row1.add(recommend); row1.add(viewed);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        row2.setOpaque(false);
        JTextField payAmount = field("Amount RM");
        JButton payFine = button("Pay Fine", false);
        payFine.addActionListener(e -> {
            Member user = library.getCurrentUser();
            if (user == null) return;
            try {
                double amount = Double.parseDouble(payAmount.getText().trim());
                if (!library.payFine(user.getMemberId(), amount)) showError("Invalid payment amount.");
                else { log("Fine payment recorded."); accountOutput.setText(user.toString()); refreshMembers(); }
            } catch (NumberFormatException ex) { showError("Enter a valid amount."); }
        });
        row2.add(label("OUTSTANDING FINE PAYMENT")); row2.add(payAmount); row2.add(payFine);
        actions.add(row1);
        actions.add(row2);
        panel.add(actions, BorderLayout.SOUTH);
        return panel;
    }

    // --- Members tab (librarian) ---

    private JPanel buildMembersTab() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.putClientProperty("themeRole", "background");
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchRow.setOpaque(false);
        JTextField memberQuery = field("Search by name");
        JButton searchMembers = button("Search", true);
        searchMembers.addActionListener(e -> refreshMembers(library.searchMembersByName(memberQuery.getText().trim())));
        JButton showAll = button("Show all", false);
        showAll.addActionListener(e -> refreshMembers());
        searchRow.add(label("MEMBERS")); searchRow.add(memberQuery); searchRow.add(searchMembers); searchRow.add(showAll);
        panel.add(searchRow, BorderLayout.NORTH);

        memberTable.setRowHeight(30);
        memberTable.putClientProperty("themeRole", "table");
        memberTable.getTableHeader().putClientProperty("themeRole", "table-header");
        panel.add(new JScrollPane(memberTable), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.putClientProperty("themeRole", "surface");
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(theme.border),
            BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        JTextField id = field("ID"); id.setEditable(false);
        JTextField name = field("Name");
        JComboBox<String> type = new JComboBox<>(new String[] {Member.TYPE_STUDENT, Member.TYPE_LIBRARIAN});
        JTextField email = field("Email");
        id.setText(library.generateMemberId("Student"));
        type.addActionListener(e -> id.setText(library.generateMemberId((String) type.getSelectedItem())));

        form.add(label("REGISTER MEMBER"), constraints(0, 0, 4));
        form.add(label("ID"), constraints(0, 1, 1)); form.add(id, constraints(1, 1, 1));
        form.add(label("NAME"), constraints(2, 1, 1)); form.add(name, constraints(3, 1, 1));
        form.add(label("ROLE"), constraints(0, 2, 1)); form.add(type, constraints(1, 2, 1));
        form.add(label("EMAIL"), constraints(2, 2, 1)); form.add(email, constraints(3, 2, 1));
        JButton register = button("Register", true);
        register.addActionListener(e -> registerMember(id, name, type, email));
        form.add(register, constraints(0, 3, 2));
        JButton history = button("Selected History", false);
        history.addActionListener(e -> showSelectedMemberHistory());
        form.add(history, constraints(2, 3, 2));
        panel.add(form, BorderLayout.SOUTH);
        return panel;
    }

    // --- DSA Lab tab ---

    private JPanel buildDsaTab() {
        JPanel panel = new JPanel(new BorderLayout(16, 16));
        panel.putClientProperty("themeRole", "background");
        panel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JTextArea output = outputArea();

        JPanel cards = new JPanel(new GridLayout(1, 3, 12, 0));
        cards.setOpaque(false);

        JPanel bst = structureCard("BST BY YEAR", "Binary search tree indexed by publication year.");
        JTextField year = field("Year");
        JButton findYear = button("Search", true);
        findYear.addActionListener(e -> {
            try { output.setText(bookLine(library.searchByYear(Integer.parseInt(year.getText().trim())))); }
            catch (NumberFormatException ex) { showError("Enter a valid year."); }
        });
        bst.add(year); bst.add(findYear);
        JButton inorder = button("In-order traversal", false);
        inorder.addActionListener(e -> output.setText(capture(() -> library.displayBooksInorder())));
        bst.add(inorder);
        cards.add(bst);

        JPanel graph = structureCard("BOOK GRAPH", "BFS/DFS over related books.");
        JTextField isbn1 = field("Start ISBN");
        JTextField isbn2 = field("Link ISBN");
        JButton link = button("Link", true);
        link.addActionListener(e -> { library.addBookRelationship(isbn1.getText().trim(), isbn2.getText().trim()); output.setText("Linked."); });
        graph.add(isbn1); graph.add(isbn2); graph.add(link);
        JButton bfs = button("BFS explore", false);
        bfs.addActionListener(e -> output.setText(capture(() -> library.breadthFirstBookSearch(isbn1.getText().trim()))));
        graph.add(bfs);
        cards.add(graph);

        JPanel heap = structureCard("OVERDUE HEAP", "Max-heap prioritizes most overdue loans.");
        JButton refresh = button("Refresh from loans", true);
        refresh.addActionListener(e -> {
            library.checkDueDateReminders();
            output.setText(bookLine(library.peekMostOverdueBook()));
        });
        heap.add(refresh);
        JButton mostOverdue = button("Peek highest priority", false);
        mostOverdue.addActionListener(e -> output.setText(bookLine(library.peekMostOverdueBook())));
        heap.add(mostOverdue);
        cards.add(heap);

        panel.add(cards, BorderLayout.NORTH);
        panel.add(new JScrollPane(output), BorderLayout.CENTER);
        return panel;
    }

    // --- Actions ---

    private void addBook(JTextField isbn, JTextField title, JTextField author, JTextField category,
            JTextField year, JTextField shelf, JTextField copies, JComboBox<String> condition) {
        try {
            int copyCount = Integer.parseInt(copies.getText().trim());
            Book book = new Book(isbn.getText().trim(), title.getText().trim(), author.getText().trim(),
                category.getText().trim(), Integer.parseInt(year.getText().trim()), copyCount,
                shelf.getText().trim(), (String) condition.getSelectedItem());
            if (!library.addBook(book)) { showError("Could not add book. Check for duplicate ID."); return; }
            log("Added: " + title.getText().trim());
            isbn.setText(library.generateBookId());
            title.setText(""); author.setText(""); category.setText(""); year.setText("");
            shelf.setText(""); copies.setText("1");
            refreshBooks();
        } catch (NumberFormatException ex) { showError("Year and copies must be numbers."); }
    }

    private void updateBook(JTextField isbn, JTextField title, JTextField author, JTextField category,
            JTextField year, JTextField shelf, JTextField copies, JComboBox<String> condition) {
        try {
            if (!library.updateBook(isbn.getText().trim(), title.getText().trim(), author.getText().trim(),
                category.getText().trim(), Integer.parseInt(year.getText().trim()), shelf.getText().trim(),
                (String) condition.getSelectedItem(), Integer.parseInt(copies.getText().trim()))) {
                showError("Update failed.");
                return;
            }
            log("Updated: " + isbn.getText().trim());
            refreshBooks();
        } catch (NumberFormatException ex) { showError("Year and copies must be numbers."); }
    }

    private void borrow(JTextField isbn, JTextField date, JTextField memberId) {
        String bookIsbn = isbn.getText().trim();
        Book book = library.searchByISBN(bookIsbn);
        if (book == null) { showError("Book not found."); return; }
        if (!book.isAvailable()) {
            int choice = JOptionPane.showConfirmDialog(frame,
                "Book unavailable. Join the waiting list?", "SmartLibrary", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                library.addToWaitingList(bookIsbn, memberId.getText().trim());
                log("Added to waiting list for " + book.getTitle());
            }
            return;
        }
        Member user = library.getCurrentUser();
        String name = user == null ? "Guest" : user.getName();
        if (!library.borrowBook(bookIsbn, date.getText().trim(), memberId.getText().trim(), name)) {
            showError("Borrow failed. Check limit, fines, or member ID.");
            return;
        }
        log("Borrowed: " + book.getTitle());
        refreshBooks();
        refreshMembers();
    }

    private void returnBook(JTextField isbn, JTextField memberId) {
        if (!library.returnBook(isbn.getText().trim(), memberId.getText().trim(), null)) {
            showError("Return failed. Check ISBN and member ID.");
            return;
        }
        log("Returned: " + isbn.getText().trim());
        refreshBooks();
        refreshMembers();
    }

    private void registerMember(JTextField id, JTextField name, JComboBox<String> type, JTextField email) {
        if (!library.registerMember(new Member(id.getText().trim(), name.getText().trim(),
            (String) type.getSelectedItem(), email.getText().trim()))) {
            showError("Registration failed.");
            return;
        }
        log("Registered: " + name.getText().trim());
        name.setText(""); email.setText("");
        id.setText(library.generateMemberId((String) type.getSelectedItem()));
        refreshMembers();
    }

    private void removeSelectedBook() {
        int row = bookTable.getSelectedRow();
        if (row < 0) { showError("Select a book first."); return; }
        String isbn = str(row, 0);
        if (JOptionPane.showConfirmDialog(frame, "Remove " + isbn + "?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        if (!library.removeBook(isbn)) showError("Only fully available books can be removed.");
        else { log("Removed " + isbn); refreshBooks(); }
    }

    private void showSelectedMemberHistory() {
        int row = memberTable.getSelectedRow();
        if (row < 0) { showError("Select a member."); return; }
        log(historyText(memberTable.getValueAt(memberTable.convertRowIndexToModel(row), 0).toString()));
    }

    // --- Refresh helpers ---

    private void refreshAll() {
        refreshBooks();
        refreshMembers();
        refreshHomePanel();
    }

    private void refreshHomePanel() {
        if (tabs == null) return;
        JPanel home = (JPanel) tabs.getComponentAt(0);
        JPanel stats = (JPanel) home.getClientProperty("statsPanel");
        if (stats != null && homeOutput != null) refreshHome(stats, homeOutput);
    }

    private void refreshBooks() { refreshBooks(library.getBooksSnapshot()); }

    private void refreshBooks(ArrayList<Book> source) {
        bookModel.setRowCount(0);
        for (Book book : source) {
            bookModel.addRow(new Object[] {
                book.getIsbn(), book.getTitle(), book.getAuthor(), book.getCategory(), book.getYear(),
                book.getShelf(), book.getAvailableCopies() + "/" + book.getTotalCopies(),
                book.getRatingDisplay(), book.isAvailable() ? "Available" : "Borrowed"
            });
        }
        bookCount.setText(source.size() + " books");
        status.setText("Catalog updated");
    }

    private void refreshMembers() { refreshMembers(library.getMembersSnapshot()); }

    private void refreshMembers(ArrayList<Member> source) {
        memberModel.setRowCount(0);
        for (Member member : source) {
            memberModel.addRow(new Object[] {
                member.getMemberId(), member.getName(), member.getType(), member.getEmail(),
                member.getCurrentBorrowCount(), String.format("%.2f", member.getOutstandingFine())
            });
        }
    }

    private void refreshHome(JPanel stats, JTextArea output) {
        library.checkDueDateReminders();
        DashboardStats values = library.getDashboardStats();
        String[] valuesText = {
            Integer.toString(values.totalBooks), Integer.toString(values.availableBooks),
            Integer.toString(values.borrowedBooks), Integer.toString(values.totalMembers),
            Integer.toString(values.overdueBooks), String.format("RM%.2f", values.outstandingFines),
            values.mostPopularBook, Integer.toString(values.waitingMembers)
        };
        for (int i = 0; i < stats.getComponentCount(); i++) {
            JLabel lbl = (JLabel) stats.getComponent(i);
            String title = (String) lbl.getClientProperty("dashboardTitle");
            lbl.setText("<html><center>" + title + "<br><b style='color:#" + toHex(theme.primary)
                + ";font-size:14px'>" + valuesText[i] + "</b></center></html>");
            lbl.setForeground(theme.ink);
        }
        StringBuilder sb = new StringBuilder(values.toString()).append("\n\n");
        sb.append("--- Recently Added ---\n").append(booksText(library.getRecentlyAdded(5), null));
        sb.append("\n--- Trending This Month ---\n").append(booksText(library.getPopularThisMonth(5), null));
        output.setText(sb.toString());
    }

    // --- Text helpers ---

    private String booksText(ArrayList<Book> books, String heading) {
        StringBuilder sb = new StringBuilder();
        if (heading != null) sb.append(heading).append('\n');
        if (books.isEmpty()) return sb.append("No books found.").toString();
        for (int i = 0; i < books.size(); i++) sb.append(i + 1).append(". ").append(books.get(i)).append('\n');
        return sb.toString();
    }

    private String historyText(String memberId) {
        StringBuilder sb = new StringBuilder();
        for (BorrowRecord record : library.getMemberHistory(memberId)) sb.append(record).append('\n');
        return sb.length() == 0 ? "No history yet." : sb.toString();
    }

    private String recommendationsText(String memberId) {
        ArrayList<Recommendation> recs = library.getRecommendationDetails(memberId);
        if (recs.isEmpty()) return "No recommendations yet. Borrow books to build your profile.";
        StringBuilder sb = new StringBuilder("Recommendations for ").append(memberId).append("\n");
        for (Recommendation rec : recs) sb.append(rec).append('\n');
        return sb.toString();
    }

    private String reviewsText(String isbn) {
        ArrayList<BookReview> list = library.getReviews(isbn);
        if (list.isEmpty()) return "No reviews yet for " + isbn;
        StringBuilder sb = new StringBuilder("Reviews for ").append(isbn).append("\n");
        for (BookReview review : list) sb.append(review).append('\n');
        return sb.toString();
    }

    private String notificationsText(boolean unreadOnly) {
        ArrayList<Notification> source = unreadOnly ? library.getUnreadNotifications() : library.getNotifications();
        StringBuilder sb = new StringBuilder(unreadOnly ? "NEW ALERTS\n" : "ALL NOTIFICATIONS\n");
        for (Notification n : source) sb.append(n).append('\n');
        return source.isEmpty() ? sb.append("No notifications.").toString() : sb.toString();
    }

    // --- UI helpers ---

    private JPanel buildActivityPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.add(label("ACTIVITY LOG"), BorderLayout.NORTH);
        JTextArea activity = outputArea();
        activity.setPreferredSize(new Dimension(0, 180));
        activityBoxes.add(activity);
        panel.add(new JScrollPane(activity), BorderLayout.CENTER);
        return panel;
    }

    private JTextArea outputArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.putClientProperty("themeRole", "output");
        area.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        return area;
    }

    private JPanel cardPanel(String heading) {
        JPanel card = new JPanel(new GridBagLayout());
        card.putClientProperty("themeRole", "surface");
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(theme.border),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        card.add(label(heading), constraints(0, 0, 2));
        return card;
    }

    private JPanel structureCard(String title, String description) {
        JPanel card = new JPanel(new GridLayout(0, 1, 0, 8));
        card.putClientProperty("themeRole", "surface");
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(theme.border),
            BorderFactory.createEmptyBorder(14, 14, 14, 14)));
        card.add(label(title));
        JLabel details = new JLabel("<html>" + description + "</html>");
        details.putClientProperty("themeRole", "muted");
        themedLabels.add(details);
        card.add(details);
        return card;
    }

    private void addCardField(JPanel card, int row, String heading, java.awt.Component field) {
        card.add(label(heading), constraints(0, row, 1));
        card.add(field, constraints(1, row, 1));
    }

    private void log(String message) {
        if (message == null || message.trim().isEmpty()) return;
        for (JTextArea box : activityBoxes) {
            box.append(message.trim() + "\n");
            box.setCaretPosition(box.getDocument().getLength());
        }
        status.setText(message.trim());
    }

    private String capture(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
        try { action.run(); } finally { System.setOut(original); }
        return out.toString();
    }

    private String bookLine(Book book) { return book == null ? "No matching book." : book.toString(); }
    private String bookLine(OverdueBook ob) { return ob == null ? "No overdue records." : ob.toString(); }
    private String str(int row, int col) { return bookTable.getValueAt(bookTable.convertRowIndexToModel(row), col).toString(); }
    private String today() { return new SimpleDateFormat("yyyy-MM-dd").format(new Date()); }

    private JTextField field(String placeholder) {
        JTextField f = new JTextField();
        f.setPreferredSize(new Dimension(150, 30));
        f.putClientProperty("JTextField.placeholderText", placeholder);
        return f;
    }

    private JButton button(String text, boolean primary) {
        JButton b = new JButton(text);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.putClientProperty("buttonPrimary", primary);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        themedButtons.add(b);
        styleButton(b);
        return b;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.putClientProperty("themeRole", "muted");
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        themedLabels.add(l);
        return l;
    }

    private JPanel labeledField(String heading, JTextField field) {
        JPanel p = new JPanel(new BorderLayout(0, 3));
        p.setOpaque(false);
        p.add(label(heading), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    private GridBagConstraints constraints(int x, int y, int width) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x; c.gridy = y; c.gridwidth = width;
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = width >= 2 ? 1 : 0;
        return c;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "SmartLibrary", JOptionPane.WARNING_MESSAGE);
    }

    private void toggleTheme() {
        theme = theme.toggle();
        String label = theme.dark ? "Light mode" : "Dark mode";
        if (themeToggleBtn != null) themeToggleBtn.setText(label);
        applyTheme();
        if (frame != null) frame.repaint();
    }

    private void applyTheme() {
        if (loginOuterPanel != null) styleTree(loginOuterPanel);
        if (mainRootPanel != null) styleTree(mainRootPanel);
        for (JButton btn : themedButtons) styleButton(btn);
        for (JLabel lbl : themedLabels) styleLabel(lbl);
        for (JLabel stat : statCards) {
            stat.setBackground(theme.surface);
            stat.setForeground(theme.ink);
        }
        styleTable(bookTable);
        styleTable(memberTable);
        for (JTextArea area : activityBoxes) styleOutput(area);
        if (homeOutput != null) styleOutput(homeOutput);
        if (detailOutput != null) styleOutput(detailOutput);
        if (tabs != null) {
            tabs.setBackground(theme.background);
            tabs.setForeground(theme.ink);
        }
    }

    private void styleTree(Container container) {
        for (Component component : container.getComponents()) {
            applyThemeRole(component);
            if (component instanceof Container) styleTree((Container) component);
        }
    }

    private void applyThemeRole(Component component) {
        if (!(component instanceof JComponent jComponent)) return;
        Object role = jComponent.getClientProperty("themeRole");
        if (!(role instanceof String themeRole)) return;
        switch (themeRole) {
            case "login-bg" -> component.setBackground(theme.header);
            case "header" -> component.setBackground(theme.header);
            case "background" -> component.setBackground(theme.background);
            case "surface" -> component.setBackground(theme.surface);
            case "ink" -> {
                component.setForeground(theme.ink);
                if (component instanceof JPanel panel) panel.setBackground(theme.surface);
            }
            case "muted" -> component.setForeground(theme.muted);
            case "accent" -> component.setForeground(theme.accent);
            case "header-subtext" -> component.setForeground(theme.headerSubtext);
            case "output" -> styleOutput((JTextArea) component);
            case "table" -> styleTable((JTable) component);
            case "table-header" -> {
                if (component instanceof JTableHeader header) {
                    header.setBackground(theme.header);
                    header.setForeground(Color.WHITE);
                }
            }
            case "stat-card" -> {
                component.setBackground(theme.surface);
                component.setForeground(theme.ink);
            }
            default -> { }
        }
    }

    private void styleButton(JButton button) {
        Object role = button.getClientProperty("themeRole");
        if ("header-button".equals(role)) {
            button.setBackground(theme.dark ? new Color(44, 56, 70) : new Color(60, 78, 95));
            button.setForeground(Color.WHITE);
            return;
        }
        boolean primary = Boolean.TRUE.equals(button.getClientProperty("buttonPrimary"));
        button.setForeground(primary ? Color.WHITE : theme.ink);
        button.setBackground(primary ? theme.primary : theme.secondaryButton);
    }

    private void styleLabel(JLabel label) {
        Object role = label.getClientProperty("themeRole");
        if ("ink".equals(role)) label.setForeground(theme.ink);
        else if ("accent".equals(role)) label.setForeground(theme.accent);
        else if ("header-subtext".equals(role)) label.setForeground(theme.headerSubtext);
        else if ("muted".equals(role)) label.setForeground(theme.muted);
    }

    private void styleOutput(JTextArea area) {
        area.setBackground(theme.outputBackground);
        area.setForeground(theme.outputForeground);
    }

    private void styleTable(JTable table) {
        table.setBackground(theme.tableBackground);
        table.setForeground(theme.tableForeground);
        table.getTableHeader().setBackground(theme.header);
        table.getTableHeader().setForeground(Color.WHITE);
    }

    private String toHex(Color color) {
        return String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }
}
