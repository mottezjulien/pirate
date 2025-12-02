package fr.plop.generic.enumerate;

public enum Priority {
    HIGHEST, HIGH, MEDIUM, LOW, LOWEST;

    public static Priority byDefault() {
        return LOWEST;
    }
}
