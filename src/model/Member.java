import java.util.ArrayList;

public class Member {
    public static final String TYPE_STUDENT = "Student";
    public static final String TYPE_LIBRARIAN = "Librarian";

    private final String memberId;
    private String name;
    private String type;
    private String email;
    private String password;
    private double outstandingFine;
    private final ArrayList<BorrowRecord> borrowingHistory;

    public Member(String memberId, String name, String type, String email) {
        this(memberId, name, type, email, "password123");
    }

    public Member(String memberId, String name, String type, String email, String password) {
        this.memberId = memberId;
        this.name = name;
        this.type = normalizeType(type);
        this.email = email;
        this.password = password == null || password.isEmpty() ? "password123" : password;
        this.borrowingHistory = new ArrayList<>();
    }

    public static String normalizeType(String type) {
        if (type == null) return TYPE_STUDENT;
        if ("Teacher".equalsIgnoreCase(type) || TYPE_LIBRARIAN.equalsIgnoreCase(type)) return TYPE_LIBRARIAN;
        return TYPE_STUDENT;
    }

    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public double getOutstandingFine() { return outstandingFine; }
    public ArrayList<BorrowRecord> getBorrowingHistory() { return new ArrayList<>(borrowingHistory); }

    public boolean isLibrarian() { return TYPE_LIBRARIAN.equals(type); }

    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = normalizeType(type); }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setOutstandingFine(double outstandingFine) { this.outstandingFine = Math.max(0, outstandingFine); }

    public void addBorrowingRecord(BorrowRecord record) { borrowingHistory.add(record); }

    public int getCurrentBorrowCount() {
        int count = 0;
        for (BorrowRecord record : borrowingHistory) {
            if (!record.isReturned()) count++;
        }
        return count;
    }

    public boolean canBorrow() { return getCurrentBorrowCount() < 3 && outstandingFine <= 0; }

    public void addFine(double amount) {
        if (amount > 0) outstandingFine += amount;
    }

    public boolean payFine(double amount) {
        if (amount <= 0 || amount > outstandingFine) return false;
        outstandingFine -= amount;
        return true;
    }

    public boolean checkPassword(String candidate) {
        return password != null && password.equals(candidate);
    }

    @Override
    public String toString() {
        return memberId + " | " + name + " | " + type + " | " + email
            + " | Active loans: " + getCurrentBorrowCount()
            + " | Fine: RM" + String.format("%.2f", outstandingFine);
    }
}
