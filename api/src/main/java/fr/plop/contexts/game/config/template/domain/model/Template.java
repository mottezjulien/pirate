package fr.plop.contexts.game.config.template.domain.model;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.generic.tools.StringTools;

import java.time.Duration;
import java.util.List;

public record Template(Atom atom, String label, String version, Duration maxDuration, ScenarioConfig scenario,
                       BoardConfig board,
                       MapConfig map) {

    private static final Duration DEFAULT_DURATION = Duration.ofMinutes(30);

    public Template(Code code, ScenarioConfig scenario) {
        this(code, scenario, new BoardConfig(List.of()), new MapConfig(List.of()));
    }

    public Template(Code code) {
        this(code, "");
    }

    public Template(Code code, String label) {
        this(code, label, new ScenarioConfig(), new BoardConfig(), new MapConfig());
    }

    public Template(Code code, ScenarioConfig scenario, BoardConfig board, MapConfig map) {
        this(code, "", scenario, board, map);
    }

    public Template(Code code, String label, ScenarioConfig scenario, BoardConfig board, MapConfig map) {
        this(new Atom(new Id(), code), label, "", DEFAULT_DURATION, scenario, board, map);
    }



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
