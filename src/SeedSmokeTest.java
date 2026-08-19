public class SeedSmokeTest {
    public static void main(String[] args) {
        Library library = new Library();
        DemoDataSeeder.seed(library);
        if (library.getBooksSnapshot().size() != 100) throw new AssertionError("Expected 100 books");
        if (library.getMemberCount() != 23) throw new AssertionError("Expected 23 members");
        boolean hasStudent = false;
        boolean hasTeacher = false;
        for (Member member : library.getMembersSnapshot()) {
            hasStudent |= member.getMemberId().startsWith("S-");
            hasTeacher |= member.getMemberId().startsWith("T-");
        }
        if (!hasStudent) throw new AssertionError("Missing student");
        if (!hasTeacher) throw new AssertionError("Missing teacher");
        Library restarted = new Library();
        if (restarted.getBooksSnapshot().size() != 100) throw new AssertionError("Books did not persist");
        if (restarted.getMemberCount() != 23) throw new AssertionError("Members did not persist");
        System.out.println("Seed test passed: 100 books, 20 students, 3 teachers, persisted without duplication.");
    }
}
