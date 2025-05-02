package fr.plop.contexts.game.config.template.domain.model;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.generic.tools.StringTools;

public record Template(Atom atom, String label, String version, ScenarioConfig scenario, BoardConfig board) {

    public record Id(String value) {

        public Id() {
            this(StringTools.generate());
        }
    }

    public record Code(String value) {

    }

    public record Atom(Id id, Code code) {

    }

    public Id id() {
        return atom.id();
    }

    public Code code() {
        return atom.code();
    }

}
