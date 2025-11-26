package fr.plop.contexts.game.session.time;

public record GameSessionTimeUnit(int value) implements Comparable<GameSessionTimeUnit> {

    public GameSessionTimeUnit() {
        this(0);
    }

    public static GameSessionTimeUnit ofMinutes(int minutes) {
        return new GameSessionTimeUnit(minutes);
    }

    public int toMinutes() {
        return value;
    }

    public int compareTo(GameSessionTimeUnit other) {
        return this.value - other.value;
    }

    public boolean isBefore(GameSessionTimeUnit value) {
        return this.compareTo(value) < 0;
    }

    public boolean isAfter(GameSessionTimeUnit value) {
        return this.compareTo(value) > 0;
    }

    public GameSessionTimeUnit add(GameSessionTimeUnit timeClick) {
        return new GameSessionTimeUnit(this.value + timeClick.value);
    }

    public GameSessionTimeUnit inc() {
        return new GameSessionTimeUnit(this.value + 1);
    }

}
