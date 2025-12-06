package fr.plop.contexts.game.session.core.domain.port;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;

import java.util.List;
import java.util.Optional;

public interface GamePlayerGetPort {
    List<BoardSpace.Id> findSpaceIdsByPlayerId(GamePlayer.Id playerId);

}
