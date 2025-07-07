package fr.plop.contexts.game.session.core.domain.model;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioGoal;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSession;
import fr.plop.generic.tools.StringTools;

import java.util.List;
import java.util.stream.Stream;

public record GameSession(Atom atom, State state, List<GamePlayer> players, ScenarioSession scenario,
                          BoardConfig board) {

    public static GameSession build(Atom atom, ScenarioConfig scenarioConfig, BoardConfig boardConfig) {
        return new GameSession(atom, GameSession.State.ACTIVE, List.of(), ScenarioSession.build(scenarioConfig), boardConfig);
    }

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public record Atom(Id id, String label) {

    }

    public enum State {
        INIT, ACTIVE, PAUSE, OVER
    }

    public Id id() {
        return atom.id();
    }

    public void init(GamePlayer.Id playerId) {
        scenario.init(playerId);
    }

    public Stream<ScenarioGoal> goals(GamePlayer.Id playerId) {
        return scenario.goals(playerId);
    }


}
