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

    public ArrayList<Book> loadBooks() {
        ArrayList<Book> books = new ArrayList<>();
        if (!Files.exists(DATABASE_FILE)) {
            return books;
        }
        try (BufferedReader reader = Files.newBufferedReader(DATABASE_FILE, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\\t", -1);
                if (values.length != 5) {
                    continue;
                }
                Book book = new Book(decode(values[0]), decode(values[1]), decode(values[2]), Integer.parseInt(values[3]));
                book.setAvailable(Boolean.parseBoolean(values[4]));
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
                writer.write(Integer.toString(book.getYear()));
                writer.write('\t');
                writer.write(Boolean.toString(book.isAvailable()));
                writer.newLine();
            }
        } catch (IOException ex) {
            System.err.println("Could not save library database: " + ex.getMessage());
        }
    }

    private String encode(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        return new String(Base64.getDecoder().decode(value), StandardCharsets.UTF_8);
    }
}
