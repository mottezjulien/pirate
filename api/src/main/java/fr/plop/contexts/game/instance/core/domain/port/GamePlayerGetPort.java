package fr.plop.contexts.game.instance.core.domain.port;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;

import java.util.List;

public interface GamePlayerGetPort {
    List<BoardSpace.Id> findSpaceIdsByPlayerId(GamePlayer.Id playerId);

}
