package fr.plop.contexts.game.session.push;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;

import java.util.List;

public sealed interface PushEvent permits PushEvent.GameStatus, PushEvent.GameMove {

    GamePlayer.Id playerId();

    record GameStatus(GamePlayer.Id playerId) implements PushEvent {

    }

    record GameMove(GamePlayer.Id playerId, List<BoardSpace.Id> spaceIds) implements PushEvent {

    }

}
