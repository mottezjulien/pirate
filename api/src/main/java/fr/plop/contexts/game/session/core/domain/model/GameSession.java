package fr.plop.contexts.game.session.core.domain.model;

import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSession;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSessionPlayer;
import fr.plop.generic.tools.StringTools;

import java.util.ArrayList;
import java.util.List;

//TODO USEFULL ?? (Cache && ScenarioSession by player)
public record GameSession(Atom atom, State state, List<GamePlayer> players, ScenarioSession scenario,
                          BoardConfig board, MapConfig map, TalkConfig talk) {

    public static GameSession buildWithoutPlayer(Atom atom, State state, ScenarioConfig scenarioConfig, BoardConfig boardConfig, MapConfig mapConfig, TalkConfig talkConfig) {
        return new GameSession(atom, state, new ArrayList<>(), ScenarioSession.build(scenarioConfig), boardConfig, mapConfig, talkConfig);
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

    public ScenarioSessionPlayer scenarioPlayer(GamePlayer.Id playerId) {
        return scenario.player(playerId);
    }

}
