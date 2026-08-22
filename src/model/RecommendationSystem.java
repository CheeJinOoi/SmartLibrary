import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class RecommendationSystem {
    public ArrayList<Book> recommend(Member member, ArrayList<Book> books) {
        ArrayList<Book> result = new ArrayList<>();
        for (Recommendation recommendation : recommendWithReasons(member, books)) result.add(recommendation.getBook());
        return result;
    }

    public ArrayList<Recommendation> recommendWithReasons(Member member, ArrayList<Book> books) {
        Set<String> authors = new HashSet<>();
        Set<String> categories = new HashSet<>();
        Set<Book> alreadyBorrowed = new HashSet<>();
        for (BorrowRecord record : member.getBorrowingHistory()) {
            authors.add(record.getBook().getAuthor().toLowerCase());
            categories.add(record.getBook().getCategory().toLowerCase());
            alreadyBorrowed.add(record.getBook());
        }
        HashMap<Book, Integer> scores = new HashMap<>();
        for (Book book : books) {
            int score = 0;
            if (authors.contains(book.getAuthor().toLowerCase())) score += 3;
            if (categories.contains(book.getCategory().toLowerCase())) score += 2;
            score += book.getBorrowCount();
            if (book.isAvailable()) score++;
            if (score > 0 && !alreadyBorrowed.contains(book)) scores.put(book, score);
        }
        ArrayList<Book> result = new ArrayList<>(scores.keySet());
        for (int i = 1; i < result.size(); i++) {
            Book current = result.get(i);
            int j = i - 1;
            while (j >= 0 && scores.get(result.get(j)) < scores.get(current)) {
                result.set(j + 1, result.get(j));
                j--;
            }
            result.set(j + 1, current);
        }
        ArrayList<Recommendation> recommendations = new ArrayList<>();
        for (Book book : result) recommendations.add(new Recommendation(book, reasonFor(book, authors, categories), scores.get(book)));
        return recommendations;
    }

    private String reasonFor(Book book, Set<String> authors, Set<String> categories) {
        boolean sameAuthor = authors.contains(book.getAuthor().toLowerCase());
        boolean sameCategory = categories.contains(book.getCategory().toLowerCase());
        if (sameAuthor && sameCategory) return "matches an author and category you read";
        if (sameAuthor) return "matches an author you read";
        if (sameCategory) return "matches a category you read";
        if (book.getBorrowCount() > 0) return "popular with other members";
        return "available in the library";
    }
}
