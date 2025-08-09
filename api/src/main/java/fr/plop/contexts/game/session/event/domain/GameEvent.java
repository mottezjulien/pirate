package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.time.TimeUnit;

public interface GameEvent {

    GameSession.Id sessionId();

    GamePlayer.Id playerId();

    TimeUnit timeUnit();

    record GoIn(GameSession.Id sessionId, GamePlayer.Id playerId, TimeUnit timeUnit,
                BoardSpace.Id spaceId) implements GameEvent {

    }

    record GoOut(GameSession.Id sessionId, GamePlayer.Id playerId, TimeUnit timeUnit,
                 BoardSpace.Id spaceId) implements GameEvent {

    }

    record TimeClick(GameSession.Id sessionId, GamePlayer.Id playerId, TimeUnit timeUnit) implements GameEvent {
        public boolean is(TimeUnit timeUnit) {
            return this.timeUnit.equals(timeUnit);
        }
    }

}
