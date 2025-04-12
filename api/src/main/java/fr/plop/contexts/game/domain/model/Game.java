package fr.plop.contexts.game.domain.model;

import fr.plop.contexts.scenario.domain.model.Scenario;

public record Game(Atom atom, State state, Scenario scenario) {

    public record Id(String value) {

    }

    public record Atom(Id id, String label) {

    }

    public enum State {
        INIT, PLAYING, OVER, PAUSED
    }

    public Id id() {
        return atom.id();
    }

}
