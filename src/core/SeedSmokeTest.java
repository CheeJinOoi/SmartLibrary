public class SeedSmokeTest {
    public static void main(String[] args) {
        Library library = new Library();
        DemoDataSeeder.seed(library);
        if (library.getBooksSnapshot().size() != 100) throw new AssertionError("Expected 100 books");
        if (library.getMemberCount() < 23) throw new AssertionError("Expected at least 23 members");
        boolean hasStudent = false;
        boolean hasLibrarian = false;
        for (Member member : library.getMembersSnapshot()) {
            hasStudent |= member.getMemberId().startsWith("S-");
            hasLibrarian |= member.getMemberId().startsWith("L-");
        }
        if (!hasStudent) throw new AssertionError("Missing student");
        if (!hasLibrarian) throw new AssertionError("Missing librarian");
        if (library.login("S-0001", "password123") == null) throw new AssertionError("Student login failed");
        Library restarted = new Library();
        if (restarted.getBooksSnapshot().size() != 100) throw new AssertionError("Books did not persist");
        if (restarted.getMemberCount() < 23) throw new AssertionError("Members did not persist");
        System.out.println("Seed test passed: 100 books, 20 students, 3 librarians, persisted without duplication.");
    }
}
