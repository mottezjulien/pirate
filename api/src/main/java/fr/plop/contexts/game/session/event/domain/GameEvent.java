package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;

public interface GameEvent {

    GameSession.Id gameId();

    GamePlayer.Id playerId();

    record GoIn(GameSession.Id gameId, GamePlayer.Id playerId, BoardSpace.Id spaceId) implements GameEvent {

    }

    record GoOut(GameSession.Id gameId, GamePlayer.Id playerId, BoardSpace.Id spaceId) implements GameEvent {

    }




}
