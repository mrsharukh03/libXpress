package com.LibXpress.Services.RecommendationSystem;

import com.LibXpress.Entitys.Books;
import com.LibXpress.Entitys.Feedback;
import com.LibXpress.Entitys.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class HybridRecommendation {

    private final CollaborativeFiltering collaborativeFiltering;
    private final ContentBasedFiltering contentBasedFiltering;

    @Autowired
    public HybridRecommendation(CollaborativeFiltering collaborativeFiltering, ContentBasedFiltering contentBasedFiltering) {
        this.collaborativeFiltering = collaborativeFiltering;
        this.contentBasedFiltering = contentBasedFiltering;
    }

    // Combine Collaborative and Content-Based Recommendations
    public List<Books> recommendBooks(User user, List<User> allUsers, List<Books> allBooks) {
        // Get Collaborative Recommendations
        List<User> similarUsers = collaborativeFiltering.findSimilarUsers(user, allUsers);
        List<Books> collaborativeBooks = new ArrayList<>();
        for (User similarUser : similarUsers) {
            collaborativeBooks.addAll(getBooksNotRatedByUser(similarUser, user));
        }

        // Get Content-Based Recommendations
        List<Books> contentBooks = contentBasedFiltering.recommendBooks(user, allBooks);

        // Combine the results
        Set<Books> recommendedBooks = new HashSet<>(collaborativeBooks);
        recommendedBooks.addAll(contentBooks);

        return new ArrayList<>(recommendedBooks);
    }

    private List<Books> getBooksNotRatedByUser(User targetUser, User user) {
        Set<Long> ratedBooksByUser = new HashSet<>();
        for (Feedback feedback : user.getFeedbacks()) {
            ratedBooksByUser.add(feedback.getBook().getId());
        }

        List<Books> booksToRecommend = new ArrayList<>();
        for (Feedback feedback : targetUser.getFeedbacks()) {
            if (!ratedBooksByUser.contains(feedback.getBook().getId())) {
                booksToRecommend.add(feedback.getBook());
            }
        }

        return booksToRecommend;
    }
}
