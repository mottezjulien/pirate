package fr.plop.contexts.game.instance.time;

public record GameInstanceTimeUnit(int value) implements Comparable<GameInstanceTimeUnit> {

    public GameInstanceTimeUnit() {
        this(0);
    }

    public static GameInstanceTimeUnit ofMinutes(int minutes) {
        return new GameInstanceTimeUnit(minutes);
    }

    public int toMinutes() {
        return value;
    }

    public int compareTo(GameInstanceTimeUnit other) {
        return this.value - other.value;
    }

    public boolean isBefore(GameInstanceTimeUnit value) {
        return this.compareTo(value) < 0;
    }

    public boolean isAfter(GameInstanceTimeUnit value) {
        return this.compareTo(value) > 0;
    }

    public GameInstanceTimeUnit add(GameInstanceTimeUnit timeClick) {
        return new GameInstanceTimeUnit(this.value + timeClick.value);
    }

    public GameInstanceTimeUnit inc() {
        return new GameInstanceTimeUnit(this.value + 1);
    }

}
