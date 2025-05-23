package fr.plop.contexts.i18n.domain;

public enum Language {
    FR, EN;

    public static Language valueOfSafe(String language) {
        try {
            return Language.valueOf(language.toUpperCase());
        } catch (IllegalArgumentException e) {
            return byDefault();
        }
    }

    public static Language byDefault() {
        return FR;
    }
}
