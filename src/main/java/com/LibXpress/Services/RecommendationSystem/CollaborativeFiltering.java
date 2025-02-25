package com.LibXpress.Services.RecommendationSystem;

import com.LibXpress.Entitys.Feedback;
import com.LibXpress.Entitys.User;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class CollaborativeFiltering {

    public double calculateCosineSimilarity(Map<Long, Integer> userRatings, Map<Long, Integer> otherUserRatings) {
        Set<Long> allBooks = new HashSet<>(userRatings.keySet());
        allBooks.addAll(otherUserRatings.keySet());

        double dotProduct = 0;
        double normUser = 0;
        double normOtherUser = 0;

        for (Long bookId : allBooks) {
            int ratingUser = userRatings.getOrDefault(bookId, 0);
            int ratingOtherUser = otherUserRatings.getOrDefault(bookId, 0);

            dotProduct += ratingUser * ratingOtherUser;
            normUser += Math.pow(ratingUser, 2);
            normOtherUser += Math.pow(ratingOtherUser, 2);
        }

        return dotProduct / (Math.sqrt(normUser) * Math.sqrt(normOtherUser));
    }

    // Get similar users to a target user
    public List<User> findSimilarUsers(User targetUser, List<User> allUsers) {
        Map<Long, Integer> targetUserRatings = getUserRatings(targetUser);
        Map<User, Double> userSimilarities = new HashMap<>();

        for (User user : allUsers) {
            if (!user.equals(targetUser)) {
                Map<Long, Integer> otherUserRatings = getUserRatings(user);
                double similarity = calculateCosineSimilarity(targetUserRatings, otherUserRatings);
                userSimilarities.put(user, similarity);
            }
        }

        return userSimilarities.entrySet().stream()
                .sorted((entry1, entry2) -> Double.compare(entry2.getValue(), entry1.getValue()))  // Sort by similarity
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // Helper method to get user ratings (for simplicity)
    private Map<Long, Integer> getUserRatings(User user) {
        Map<Long, Integer> ratings = new HashMap<>();
        for (Feedback feedback : user.getFeedbacks()) {
            ratings.put(feedback.getBook().getId(), feedback.getRating());
        }
        return ratings;
    }
}
