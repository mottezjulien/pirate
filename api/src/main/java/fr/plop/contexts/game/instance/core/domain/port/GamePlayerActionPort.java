package fr.plop.contexts.game.instance.core.domain.port;

import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.instance.core.domain.model.GameAction;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.time.GameInstanceTimeUnit;

import java.util.List;

public interface GamePlayerActionPort {

    List<GameAction> findByPlayerId(GamePlayer.Id playerId);

    void save(GamePlayer.Id playerId, Possibility.Id possibilityId, GameInstanceTimeUnit timeUnit);

}
