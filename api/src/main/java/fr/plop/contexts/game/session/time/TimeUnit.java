package fr.plop.contexts.game.session.time;

public record TimeUnit(int value) implements Comparable<TimeUnit> {

    public TimeUnit() {
        this(0);
    }

    public static TimeUnit ofMinutes(int minutes) {
        return new TimeUnit(minutes);
    }

    public int toMinutes() {
        return value;
    }

    public int compareTo(TimeUnit other) {
        return this.value - other.value;
    }

    public TimeUnit add(TimeUnit timeClick) {
        return new TimeUnit(this.value + timeClick.value);
    }

    public TimeUnit inc() {
        return new TimeUnit(this.value + 1);
    }
}
