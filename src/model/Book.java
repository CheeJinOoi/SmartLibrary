import java.time.LocalDate;

public class Book {
    public static final String CONDITION_GOOD = "Good";
    public static final String CONDITION_DAMAGED = "Damaged";
    public static final String CONDITION_LOST = "Lost";
    public static final String CONDITION_REPAIR = "Under repair";

    private String isbn;
    private String title;
    private String author;
    private String category;
    private int year;
    private int totalCopies;
    private int availableCopies;
    private String shelf;
    private String condition;
    private String dateAdded;
    private int borrowCount;
    private int monthlyBorrowCount;
    private int ratingSum;
    private int ratingCount;

    public Book(String isbn, String title, String author, int year) {
        this(isbn, title, author, "General", year);
    }

    public Book(String isbn, String title, String author, String category, int year) {
        this(isbn, title, author, category, year, 1, "A-01-01", CONDITION_GOOD);
    }

    public Book(String isbn, String title, String author, String category, int year,
            int copies, String shelf, String condition) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.category = category;
        this.year = year;
        this.totalCopies = Math.max(1, copies);
        this.availableCopies = this.totalCopies;
        this.shelf = shelf == null || shelf.trim().isEmpty() ? "A-01-01" : shelf.trim();
        this.condition = condition == null || condition.trim().isEmpty() ? CONDITION_GOOD : condition;
        this.dateAdded = LocalDate.now().toString();
    }

    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public int getYear() { return year; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
    public String getShelf() { return shelf; }
    public String getCondition() { return condition; }
    public String getDateAdded() { return dateAdded; }
    public int getBorrowCount() { return borrowCount; }
    public int getMonthlyBorrowCount() { return monthlyBorrowCount; }
    public int getRatingCount() { return ratingCount; }

    public boolean isAvailable() {
        return availableCopies > 0 && !CONDITION_LOST.equals(condition);
    }

    public double getAverageRating() {
        return ratingCount == 0 ? 0 : (double) ratingSum / ratingCount;
    }

    public String getRatingDisplay() {
        if (ratingCount == 0) return "No ratings yet";
        return String.format("%.1f/5 (%d review%s)", getAverageRating(), ratingCount, ratingCount == 1 ? "" : "s");
    }

    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setCategory(String category) { this.category = category; }
    public void setYear(int year) { this.year = year; }
    public void setShelf(String shelf) { this.shelf = shelf; }
    public void setCondition(String condition) { this.condition = condition; }
    public void setDateAdded(String dateAdded) { this.dateAdded = dateAdded; }
    public void setBorrowCount(int borrowCount) { this.borrowCount = borrowCount; }
    public void setMonthlyBorrowCount(int monthlyBorrowCount) { this.monthlyBorrowCount = monthlyBorrowCount; }
    public void setTotalCopies(int totalCopies) { this.totalCopies = Math.max(1, totalCopies); }
    public void setAvailableCopies(int availableCopies) { this.availableCopies = Math.max(0, availableCopies); }

    /** @deprecated use copy-based availability via {@link #borrowCopy()} / {@link #returnCopy()} */
    @Deprecated
    public void setAvailable(boolean available) {
        if (available && availableCopies == 0) availableCopies = 1;
        if (!available && availableCopies > 0) availableCopies = 0;
    }

    public void incrementBorrowCount() {
        borrowCount++;
        monthlyBorrowCount++;
    }

    public void addRating(int rating) {
        if (rating < 1 || rating > 5) return;
        ratingSum += rating;
        ratingCount++;
    }

    public boolean borrowCopy() {
        if (availableCopies <= 0 || CONDITION_LOST.equals(condition)) return false;
        availableCopies--;
        return true;
    }

    public void returnCopy() {
        if (availableCopies < totalCopies) availableCopies++;
    }

    public void markCopyLost() {
        if (totalCopies > 0) totalCopies--;
        if (availableCopies > totalCopies) availableCopies = totalCopies;
        if (totalCopies == 0) condition = CONDITION_LOST;
    }

    public Book snapshot() {
        Book copy = new Book(isbn, title, author, category, year, totalCopies, shelf, condition);
        copy.availableCopies = availableCopies;
        copy.dateAdded = dateAdded;
        copy.borrowCount = borrowCount;
        copy.monthlyBorrowCount = monthlyBorrowCount;
        copy.ratingSum = ratingSum;
        copy.ratingCount = ratingCount;
        return copy;
    }

    @Override
    public String toString() {
        return isbn + " | " + title + " | " + author + " | " + category + " | " + year
            + " | Shelf " + shelf + " | " + availableCopies + "/" + totalCopies + " copies"
            + " | " + condition + " | " + getRatingDisplay();
    }
}
