package fr.plop.contexts.game.session.event.domain;

import fr.plop.contexts.game.config.board.domain.model.BoardSpace;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.time.TimeClick;

public interface GameEvent {

    GameSession.Id sessionId();

    GamePlayer.Id playerId();

    TimeClick timeClick();

    record GoIn(GameSession.Id sessionId, GamePlayer.Id playerId, TimeClick timeClick,
                BoardSpace.Id spaceId) implements GameEvent {

    }

    record GoOut(GameSession.Id sessionId, GamePlayer.Id playerId, TimeClick timeClick,
                 BoardSpace.Id spaceId) implements GameEvent {

    }

    record EachTimeClick(GameSession.Id sessionId, GamePlayer.Id playerId, TimeClick timeClick) implements GameEvent {

        public boolean is(TimeClick timeClick) {
            return this.timeClick.equals(timeClick);
        }

    }


}
