package fr.plop.contexts.game.select.domain;

public record Review(String playerName, int rating, String comment) {
    public Review {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }
}
