public final class DemoDataSeeder {
    private DemoDataSeeder() { }

    public static void seed(Library library) {
        if (!library.getBooksSnapshot().isEmpty() || library.getMemberCount() > 0) return;

        String[] categories = {"Technology", "Science", "History", "Business", "Arts", "Literature", "Education", "Health"};
        for (int index = 1; index <= 100; index++) {
            String id = library.generateBookId();
            String category = categories[(index - 1) % categories.length];
            library.addBook(new Book(id, "Library Collection " + String.format("%03d", index),
                "Author " + ((index - 1) % 20 + 1), category, 1990 + (index % 35)));
        }

        for (int index = 1; index <= 20; index++) {
            library.registerMember(new Member(library.generateMemberId("Student"),
                "Student " + String.format("%02d", index), "Student", "student" + index + "@smartlibrary.local"));
        }
        for (int index = 1; index <= 3; index++) {
            library.registerMember(new Member(library.generateMemberId("Teacher"),
                "Teacher " + index, "Teacher", "teacher" + index + "@smartlibrary.local"));
        }
    }
}
