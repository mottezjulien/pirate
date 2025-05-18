package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;

public interface GameEvent {

    GameSession.Id sessionId();

    GamePlayer.Id playerId();

    //TODO manage event unique, eachByPlayer or EVER

    record UpdateStatus(GameSession.Id sessionId, GamePlayer.Id playerId) implements GameEvent {

    }

    record GoIn(GameSession.Id sessionId, GamePlayer.Id playerId, BoardSpace.Id spaceId) implements GameEvent {

    }

    record GoOut(GameSession.Id sessionId, GamePlayer.Id playerId, BoardSpace.Id spaceId) implements GameEvent {

    }

}
