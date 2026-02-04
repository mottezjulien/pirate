package fr.plop.contexts.game.select.domain;

import fr.plop.contexts.game.config.template.domain.model.Template;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public record PlayerGameHistory(
    String playerId,
    String versionPlayed,
    LocalDateTime playedAt,
    GameSelect.Level difficulty,
    GameState state,
    Duration duration,
    List<Achievement> unlockedAchievements
) {
}
