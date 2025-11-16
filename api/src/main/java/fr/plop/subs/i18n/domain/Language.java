package fr.plop.subs.i18n.domain;

import java.util.Optional;

public enum Language {
    FR, EN;

    public static Language byDefault() {
        return FR;
    }

    public static Optional<Language> safeValueOf(String name) {
        try {
            return Optional.of(valueOf(name));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
