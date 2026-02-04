package fr.plop.contexts.game.select.domain;

import java.time.LocalDateTime;

public record GameVersion(String versionNumber, LocalDateTime date, String evolutionText) {
}
