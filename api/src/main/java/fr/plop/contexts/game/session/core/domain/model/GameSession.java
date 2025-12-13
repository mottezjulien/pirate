package fr.plop.contexts.game.session.core.domain.model;

import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.session.scenario.domain.model.ScenarioSession;
import fr.plop.generic.tools.ListTools;
import fr.plop.generic.tools.StringTools;

import java.util.List;

//TODO USEFULL ?? (Cache && ScenarioSession by player)
public record GameSession(Id id, String label, State state, List<GamePlayer> players, ScenarioSession scenario,
                          BoardConfig board, MapConfig map, TalkConfig talk, ImageConfig image) {

    public static GameSession buildWithoutPlayer(Id id, String label, State state, ScenarioConfig scenarioConfig, BoardConfig boardConfig,
                                                 MapConfig mapConfig, TalkConfig talkConfig, ImageConfig imageConfig) {
        return new GameSession(id, label, state, List.of(), ScenarioSession.build(scenarioConfig),
                boardConfig, mapConfig, talkConfig, imageConfig);
    }

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public enum State {
        INIT, ACTIVE, PAUSE, OVER
    }

    public GameSession insertPlayerId(GamePlayer.Id playerId) {
        return withPlayers(ListTools.concat(players, List.of(new GamePlayer(playerId))));
    }

    private GameSession withPlayers(List<GamePlayer> players) {
        return new GameSession(id, label, state, players, scenario, board, map, talk, image);
    }

}
