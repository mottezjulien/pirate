package fr.plop.contexts.game.instance.core.domain.model;

import fr.plop.contexts.game.config.Image.domain.ImageConfig;
import fr.plop.contexts.game.config.board.domain.model.BoardConfig;
import fr.plop.contexts.game.config.inventory.domain.model.InventoryConfig;
import fr.plop.contexts.game.config.map.domain.MapConfig;
import fr.plop.contexts.game.config.scenario.domain.model.ScenarioConfig;
import fr.plop.contexts.game.config.talk.domain.TalkConfig;
import fr.plop.contexts.game.instance.scenario.domain.model.ScenarioInstance;
import fr.plop.contexts.user.User;
import fr.plop.generic.tools.ListTools;
import fr.plop.generic.tools.StringTools;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public record GameInstance(Atom atom, ScenarioInstance scenario,
                           BoardConfig board, MapConfig map, TalkConfig talk, ImageConfig image,
                           InventoryConfig inventory) {



    public record Atom(Id id, State state, List<GamePlayer> players) {
        public Atom withPlayers(List<GamePlayer> players) {
            return new Atom(id, state, players);
        }

        public Optional<GameInstanceContext> byUserId(User.Id userId) {
            return players.stream()
                    .filter(pl -> pl.is(userId))
                    .findFirst()
                    .map(pl -> new GameInstanceContext(id, pl.id()));
        }

        public Optional<GamePlayer> byPlayerId(GamePlayer.Id playerId) {
            return players.stream().filter(player -> player.id().equals(playerId)).findFirst();
        }

    }

    public static GameInstance buildWithoutPlayer(Id id, State state, ScenarioConfig scenarioConfig, BoardConfig boardConfig,
                                                  MapConfig mapConfig, TalkConfig talkConfig, ImageConfig imageConfig, InventoryConfig inventory) {
        return new GameInstance(new Atom(id, state, List.of()), ScenarioInstance.build(scenarioConfig),
                boardConfig, mapConfig, talkConfig, imageConfig, inventory);
    }

    public record Id(String value) {
        public Id() {
            this(StringTools.generate());
        }
    }

    public enum State {
        INIT, ACTIVE, PAUSE, OVER
    }


    public Id id() {
        return atom.id();
    }

    public Stream<GamePlayer> players() {
        return atom.players().stream();
    }

    public GameInstance insertPlayer(GamePlayer player) {
        return withPlayers(ListTools.concat(atom.players, List.of(player)));
    }

    private GameInstance withPlayers(List<GamePlayer> players) {
        return new GameInstance(atom.withPlayers(players), scenario, board, map, talk, image, inventory);
    }

}
