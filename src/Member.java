import java.util.ArrayList;

public class Member {
    private final String memberId;
    private String name;
    private String type;
    private String email;
    private final ArrayList<BorrowRecord> borrowingHistory;

    public Member(String memberId, String name, String type, String email) {
        this.memberId = memberId;
        this.name = name;
        this.type = type;
        this.email = email;
        this.borrowingHistory = new ArrayList<>();
    }

    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getEmail() { return email; }
    public ArrayList<BorrowRecord> getBorrowingHistory() { return new ArrayList<>(borrowingHistory); }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setEmail(String email) { this.email = email; }
    public void addBorrowingRecord(BorrowRecord record) { borrowingHistory.add(record); }
    public int getCurrentBorrowCount() {
        int count = 0;
        for (BorrowRecord record : borrowingHistory) {
            if (record.getReturnDate() == null) count++;
        }
        return count;
    }
    public boolean canBorrow() { return getCurrentBorrowCount() < 3; }

    @Override
    public String toString() {
        return memberId + " | " + name + " | " + type + " | " + email + " | Active loans: " + getCurrentBorrowCount();
    }
}
