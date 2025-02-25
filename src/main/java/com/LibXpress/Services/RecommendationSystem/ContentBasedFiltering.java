package com.LibXpress.Services.RecommendationSystem;

import com.LibXpress.Entitys.Books;
import com.LibXpress.Entitys.Feedback;
import com.LibXpress.Entitys.User;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ContentBasedFiltering {

    // Calculate Cosine Similarity between two books based on their attributes
    public double calculateBookSimilarity(Books book1, Books book2) {
        // Compare genres and authors (you can add more attributes as needed)
        double similarity = 0;

        if (book1.getAuthor().equals(book2.getAuthor())) {
            similarity += 0.5; // Adding weight for matching author
        }

        if (book1.getCategory().equals(book2.getCategory())) {
            similarity += 0.5; // Adding weight for matching category
        }

        return similarity;
    }

    // Recommend books based on content similarity
    public List<Books> recommendBooks(User user, List<Books> allBooks) {
        List<Books> likedBooks = user.getFeedbacks().stream()
                .filter(feedback -> feedback.getRating() >= 4)  // Consider books with rating 4 or more
                .map(Feedback::getBook)
                .collect(Collectors.toList());

        Map<Books, Double> bookSimilarities = new HashMap<>();
        for (Books likedBook : likedBooks) {
            for (Books book : allBooks) {
                if (!likedBook.equals(book)) {
                    double similarity = calculateBookSimilarity(likedBook, book);
                    bookSimilarities.put(book, bookSimilarities.getOrDefault(book, 0.0) + similarity);
                }
            }
        }

        return bookSimilarities.entrySet().stream()
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()))  // Sort by similarity
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
