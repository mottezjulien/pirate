package fr.plop.contexts.game.config.template.domain.model;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.generic.tools.StringTools;

import java.time.Duration;

public record Template(Atom atom, String label, String version, Duration maxDuration, ScenarioConfig scenario,
                       BoardConfig board,
                       MapConfig map) {

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
