package fr.plop.contexts.game.session.time;

import java.time.Duration;

public record TimeClick(int index) implements Comparable<TimeClick> {

    static final Duration ONE_CLICK = Duration.ofMinutes(1);

    public static TimeClick ofMinutes(int minutes) {
        return new TimeClick(minutes);
    }

    public int compareTo(TimeClick other) {
        return this.index - other.index;
    }

    public TimeClick add(TimeClick timeClick) {
        return new TimeClick(this.index + timeClick.index);
    }

    public TimeClick inc() {
        return new TimeClick(this.index + 1);
    }

    public int minutes() {
        return index;
    }

}
