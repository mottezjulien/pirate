package fr.plop.contexts.game.session.core.domain.port;

import fr.plop.contexts.game.config.scenario.domain.model.Possibility;
import fr.plop.contexts.game.session.core.domain.model.GameAction;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;

import java.util.List;

public interface GamePlayerActionPort {

    List<GameAction> findByPlayerId(GamePlayer.Id playerId);

    void save(GamePlayer.Id playerId, Possibility.Id possibilityId, GameSessionTimeUnit timeUnit);

}
