package fr.plop.contexts.game.domain.model;

public record Game(Atom atom) {

    public record Id(String value) {

    }

    public record Atom(Id id, String label) {

    }

    public enum State {
        INIT, PLAYING, OVER, PAUSED
    }

}
