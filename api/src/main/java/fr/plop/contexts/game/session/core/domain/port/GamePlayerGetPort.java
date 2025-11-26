package fr.plop.contexts.game.session.core.domain.port;

import fr.plop.contexts.game.session.core.domain.model.GamePlayer;

import java.util.Optional;

public interface GamePlayerGetPort {

    Optional<GamePlayer> findById(GamePlayer.Id playerId);

}
