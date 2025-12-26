package fr.plop.contexts.game.session.push;

import fr.plop.contexts.game.config.Image.domain.ImageItem;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;

public sealed interface PushEvent permits PushEvent.GameStatus, PushEvent.GameMove,
        PushEvent.Message, PushEvent.Talk, PushEvent.Image {

    GameSessionContext context();

    record GameStatus(GameSessionContext context) implements PushEvent {

    }

    record GameMove(GameSessionContext context) implements PushEvent {

    }

    record Message(GameSessionContext context, String message) implements PushEvent {

    }

    record Talk(GameSessionContext context, TalkItem.Id talkId) implements PushEvent {

    }

    record Image(GameSessionContext context, ImageItem.Id imageId) implements PushEvent {

    }

}
