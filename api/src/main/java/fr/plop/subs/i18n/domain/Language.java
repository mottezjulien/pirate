package fr.plop.subs.i18n.domain;

public enum Language {
    FR, EN;

    public static Language byDefault() {
        return FR;
    }
}
