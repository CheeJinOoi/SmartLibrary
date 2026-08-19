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

    public ArrayList<Book> loadBooks() {
        ArrayList<Book> books = new ArrayList<>();
        if (!Files.exists(DATABASE_FILE)) {
            return books;
        }
        try (BufferedReader reader = Files.newBufferedReader(DATABASE_FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\\t", -1);
                if (values.length != 5 && values.length != 7) {
                    continue;
                }
                String category = values.length == 7 ? decode(values[3]) : "General";
                int yearIndex = values.length == 7 ? 4 : 3;
                int availableIndex = values.length == 7 ? 5 : 4;
                Book book = new Book(decode(values[0]), decode(values[1]), decode(values[2]), category, Integer.parseInt(values[yearIndex]));
                book.setAvailable(Boolean.parseBoolean(values[availableIndex]));
                if(values.length == 7) book.setBorrowCount(Integer.parseInt(values[6]));
                books.add(book);
            }
        } catch (IOException | NumberFormatException ex) {
            System.err.println("Could not load library database: " + ex.getMessage());
        }
        return books;
    }

    public void saveBooks(ArrayList<Book> books) {
        try (BufferedWriter writer = Files.newBufferedWriter(DATABASE_FILE, StandardCharsets.UTF_8)) {
            for (Book book : books) {
                writer.write(encode(book.getIsbn()));
                writer.write('\t');
                writer.write(encode(book.getTitle()));
                writer.write('\t');
                writer.write(encode(book.getAuthor()));
                writer.write('\t');
                writer.write(encode(book.getCategory()));
                writer.write('\t');
                writer.write(Integer.toString(book.getYear()));
                writer.write('\t');
                writer.write(Boolean.toString(book.isAvailable()));
                writer.write('\t');
                writer.write(Integer.toString(book.getBorrowCount()));
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

    public void saveSequences(int bookSequence, int studentSequence, int teacherSequence) {
        try {
            Files.writeString(SEQUENCE_FILE, bookSequence + "\t" + studentSequence + "\t" + teacherSequence,
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
                if (values.length == 4) members.add(new Member(decode(values[0]), decode(values[1]), decode(values[2]), decode(values[3])));
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
                    + encode(member.getType()) + "\t" + encode(member.getEmail()));
                writer.newLine();
            }
        } catch (IOException ex) {
            System.err.println("Could not save members: " + ex.getMessage());
        }
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
