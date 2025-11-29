package fr.plop.generic.enumerate;

public enum Priority {
    HIGHEST, HIGH, MEDIUM, LOW, LOWEST;

    public int value() {
        return switch (this) {
            case LOWEST -> 1;
            case LOW -> 2;
            case MEDIUM -> 3;
            case HIGH -> 4;
            case HIGHEST -> 5;
        };
    }
}
