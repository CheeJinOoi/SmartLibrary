import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;

public class LibraryDatabase {
    private static final Path DATABASE_FILE = Paths.get("smartlibrary-books.db");
    private static final Path SEQUENCE_FILE = Paths.get("smartlibrary-sequences.db");
    private static final Path MEMBERS_FILE = Paths.get("smartlibrary-members.db");
    private static final Path REVIEWS_FILE = Paths.get("smartlibrary-reviews.db");

    public ArrayList<Book> loadBooks() {
        ArrayList<Book> books = new ArrayList<>();
        if (!Files.exists(DATABASE_FILE)) return books;
        try (BufferedReader reader = Files.newBufferedReader(DATABASE_FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\\t", -1);
                if (values.length < 5) continue;
                Book book = parseBook(values);
                if (book != null) books.add(book);
            }
        } catch (IOException | NumberFormatException ex) {
            System.err.println("Could not load library database: " + ex.getMessage());
        }
        return books;
    }

    private Book parseBook(String[] values) {
        try {
            if (values.length >= 13) {
                Book book = new Book(decode(values[0]), decode(values[1]), decode(values[2]), decode(values[3]),
                    Integer.parseInt(values[4]), Integer.parseInt(values[8]), decode(values[6]), decode(values[7]));
                book.setAvailableCopies(Integer.parseInt(values[9]));
                book.setBorrowCount(Integer.parseInt(values[5]));
                book.setMonthlyBorrowCount(Integer.parseInt(values[10]));
                book.setDateAdded(decode(values[11]));
                int ratingCount = Integer.parseInt(values[12]);
                if (values.length >= 14) {
                    int ratingSum = Integer.parseInt(values[13]);
                    for (int i = 0; i < ratingCount; i++) {
                        book.addRating(ratingCount == 0 ? 0 : Math.max(1, Math.min(5, ratingSum / ratingCount)));
                    }
                } else {
                    for (int i = 0; i < ratingCount; i++) book.addRating(4);
                }
                return book;
            }
            if (values.length == 7) {
                Book book = new Book(decode(values[0]), decode(values[1]), decode(values[2]), decode(values[3]),
                    Integer.parseInt(values[4]));
                boolean available = Boolean.parseBoolean(values[5]);
                book.setBorrowCount(Integer.parseInt(values[6]));
                if (!available) book.setAvailableCopies(0);
                return book;
            }
            if (values.length == 5) {
                Book book = new Book(decode(values[0]), decode(values[1]), decode(values[2]),
                    Integer.parseInt(values[3]));
                if (!Boolean.parseBoolean(values[4])) book.setAvailableCopies(0);
                return book;
            }
        } catch (Exception ex) {
            System.err.println("Skipping invalid book row: " + ex.getMessage());
        }
        return null;
    }

    public void saveBooks(ArrayList<Book> books) {
        try (BufferedWriter writer = Files.newBufferedWriter(DATABASE_FILE, StandardCharsets.UTF_8)) {
            for (Book book : books) {
                writer.write(encode(book.getIsbn())); writer.write('\t');
                writer.write(encode(book.getTitle())); writer.write('\t');
                writer.write(encode(book.getAuthor())); writer.write('\t');
                writer.write(encode(book.getCategory())); writer.write('\t');
                writer.write(Integer.toString(book.getYear())); writer.write('\t');
                writer.write(Integer.toString(book.getBorrowCount())); writer.write('\t');
                writer.write(encode(book.getShelf())); writer.write('\t');
                writer.write(encode(book.getCondition())); writer.write('\t');
                writer.write(Integer.toString(book.getTotalCopies())); writer.write('\t');
                writer.write(Integer.toString(book.getAvailableCopies())); writer.write('\t');
                writer.write(Integer.toString(book.getMonthlyBorrowCount())); writer.write('\t');
                writer.write(encode(book.getDateAdded())); writer.write('\t');
                writer.write(Integer.toString(book.getRatingCount())); writer.write('\t');
                writer.write(Integer.toString(book.getRatingCount() == 0 ? 0
                    : (int) Math.round(book.getAverageRating() * book.getRatingCount())));
                writer.newLine();
            }
        } catch (IOException ex) {
            System.err.println("Could not save library database: " + ex.getMessage());
        }
    }

    public int[] loadSequences() {
        int[] sequences = new int[] {1, 1, 1};
        if (!Files.exists(SEQUENCE_FILE)) return sequences;
        try {
            String[] values = Files.readString(SEQUENCE_FILE, StandardCharsets.UTF_8).trim().split("\\t");
            for (int index = 0; index < Math.min(values.length, sequences.length); index++) {
                sequences[index] = Math.max(1, Integer.parseInt(values[index]));
            }
        } catch (IOException | NumberFormatException ex) {
            System.err.println("Could not load ID sequences: " + ex.getMessage());
        }
        return sequences;
    }

    public void saveSequences(int bookSequence, int studentSequence, int librarianSequence) {
        try {
            Files.writeString(SEQUENCE_FILE, bookSequence + "\t" + studentSequence + "\t" + librarianSequence,
                StandardCharsets.UTF_8);
        } catch (IOException ex) {
            System.err.println("Could not save ID sequences: " + ex.getMessage());
        }
    }

    public ArrayList<Member> loadMembers() {
        ArrayList<Member> members = new ArrayList<>();
        if (!Files.exists(MEMBERS_FILE)) return members;
        try (BufferedReader reader = Files.newBufferedReader(MEMBERS_FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\\t", -1);
                if (values.length >= 4) {
                    String password = values.length >= 5 ? decode(values[4]) : "password123";
                    Member member = new Member(decode(values[0]), decode(values[1]), decode(values[2]), decode(values[3]), password);
                    if (values.length >= 6) member.setOutstandingFine(Double.parseDouble(values[5]));
                    members.add(member);
                }
            }
        } catch (IOException ex) {
            System.err.println("Could not load members: " + ex.getMessage());
        }
        return members;
    }

    public void saveMembers(ArrayList<Member> members) {
        try (BufferedWriter writer = Files.newBufferedWriter(MEMBERS_FILE, StandardCharsets.UTF_8)) {
            for (Member member : members) {
                writer.write(encode(member.getMemberId()) + "\t" + encode(member.getName()) + "\t"
                    + encode(member.getType()) + "\t" + encode(member.getEmail()) + "\t"
                    + encode(member.getPassword()) + "\t" + member.getOutstandingFine());
                writer.newLine();
            }
        } catch (IOException ex) {
            System.err.println("Could not save members: " + ex.getMessage());
        }
    }

    public ArrayList<BookReview> loadReviews() {
        ArrayList<BookReview> reviews = new ArrayList<>();
        if (!Files.exists(REVIEWS_FILE)) return reviews;
        try (BufferedReader reader = Files.newBufferedReader(REVIEWS_FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\\t", -1);
                if (values.length >= 5) {
                    reviews.add(new BookReview(decode(values[0]), decode(values[1]), decode(values[2]),
                        Integer.parseInt(values[3]), decode(values[4])));
                }
            }
        } catch (IOException ex) {
            System.err.println("Could not load reviews: " + ex.getMessage());
        }
        return reviews;
    }

    public void saveReviews(ArrayList<BookReview> reviews) {
        try (BufferedWriter writer = Files.newBufferedWriter(REVIEWS_FILE, StandardCharsets.UTF_8)) {
            for (BookReview review : reviews) {
                writer.write(encode(review.getIsbn()) + "\t" + encode(review.getMemberId()) + "\t"
                    + encode(review.getMemberName()) + "\t" + review.getRating() + "\t"
                    + encode(review.getComment()));
                writer.newLine();
            }
        } catch (IOException ex) {
            System.err.println("Could not save reviews: " + ex.getMessage());
        }
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
