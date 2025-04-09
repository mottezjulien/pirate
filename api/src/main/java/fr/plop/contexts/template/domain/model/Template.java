package fr.plop.contexts.template.domain.model;

import fr.plop.contexts.board.domain.model.Board;
import fr.plop.contexts.scenario.domain.model.Scenario;

public record Template(Atom atom, String label, String version, Scenario scenario, Board board) {

    public record Id(String value) {

    }

    public record Code(String value) {

    }

    public record Atom(Id id, Code code) {

    }

    public Id id() {
        return atom.id();
    }

}
