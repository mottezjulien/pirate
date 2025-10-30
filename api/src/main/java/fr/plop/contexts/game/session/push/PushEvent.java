package fr.plop.contexts.game.session.push;

import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;

public sealed interface PushEvent permits PushEvent.GameStatus, PushEvent.GameMove,
        PushEvent.Message, PushEvent.Talk {

    GameSession.Id sessionId();

    GamePlayer.Id playerId();

    record GameStatus(GameSession.Id sessionId, GamePlayer.Id playerId) implements PushEvent {

    }

    record GameMove(GameSession.Id sessionId, GamePlayer.Id playerId) implements PushEvent {

    }

    record Message(GameSession.Id sessionId, GamePlayer.Id playerId, String message) implements PushEvent {

    }

    record Talk(GameSession.Id sessionId, GamePlayer.Id playerId, TalkItem.Id talkId) implements PushEvent {

    }

}
