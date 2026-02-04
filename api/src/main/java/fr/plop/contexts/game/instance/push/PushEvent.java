package fr.plop.contexts.game.instance.push;

import fr.plop.contexts.game.config.Image.domain.ImageItem;
import fr.plop.contexts.game.config.message.MessageToken;
import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;

public sealed interface PushEvent permits PushEvent.GameStatus, PushEvent.GameMove,
        PushEvent.Message, PushEvent.Talk, PushEvent.Image, PushEvent.Confirm, PushEvent.Inventory {

    GameInstanceContext context();

    record GameStatus(GameInstanceContext context) implements PushEvent {

    }

    record GameMove(GameInstanceContext context) implements PushEvent {

    }

    record Message(GameInstanceContext context, String message) implements PushEvent {

    }

    record Talk(GameInstanceContext context, TalkItem.Id talkId) implements PushEvent {

    }

    record Image(GameInstanceContext context, ImageItem.Id imageId) implements PushEvent {

    }

    record Confirm(GameInstanceContext context, MessageToken token, String message) implements PushEvent {

    }

    record Inventory(GameInstanceContext context) implements PushEvent {

    }

}
