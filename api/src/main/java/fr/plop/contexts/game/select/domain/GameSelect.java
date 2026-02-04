package fr.plop.contexts.game.select.domain;

import fr.plop.subs.i18n.domain.I18n;

import java.time.LocalDateTime;
import java.util.List;

public record GameSelect(
    I18n name,
    String description,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    Author author,
    List<Level> availableLevels,
    List<GameVersion> versions,
    List<Review> reviews,
    List<Achievement> achievements
) {
    public record Level(int value, String label) {
        public static Level from(int value, String label) {
            return new Level(value, label);
        }
    }
}
