public final class DemoDataSeeder {
    private DemoDataSeeder() { }

    public static void seed(Library library) {
        library.ensureDefaultStudentAccount();
        if (!library.getBooksSnapshot().isEmpty() || library.getMemberCount() > 1) {
            library.ensureDefaultAdminAccount();
            return;
        }

        String[] categories = {"Technology", "Science", "History", "Business", "Arts", "Literature", "Education", "Health"};
        for (int index = 1; index <= 100; index++) {
            String id = library.generateBookId();
            String category = categories[(index - 1) % categories.length];
            String shelf = String.format("%c-%02d-%02d", 'A' + (index % 5), (index % 20) + 1, (index % 15) + 1);
            int copies = index % 7 == 0 ? 3 : 1;
            library.addBook(new Book(id, "Library Collection " + String.format("%03d", index),
                "Author " + ((index - 1) % 20 + 1), category, 1990 + (index % 35), copies, shelf, Book.CONDITION_GOOD));
        }

        for (int index = 1; index <= 20; index++) {
            library.registerMember(new Member(library.generateMemberId("Student"),
                "Student " + String.format("%02d", index), "Student",
                "student" + index + "@smartlibrary.local", "password123"));
        }
        library.registerMember(new Member(library.generateMemberId("Librarian"),
            "Head Librarian", "Librarian", "librarian@smartlibrary.local", "admin123"));
        for (int index = 2; index <= 3; index++) {
            library.registerMember(new Member(library.generateMemberId("Librarian"),
                "Librarian " + index, "Librarian", "librarian" + index + "@smartlibrary.local", "admin123"));
        }
    }
}
