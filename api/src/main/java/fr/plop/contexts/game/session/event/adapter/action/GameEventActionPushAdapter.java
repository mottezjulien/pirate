package fr.plop.contexts.game.session.event.adapter.action;

import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.session.push.PushEvent;
import fr.plop.contexts.game.session.push.PushPort;
import fr.plop.subs.i18n.domain.I18n;
import org.springframework.stereotype.Component;

@Component
public class GameEventActionPushAdapter {

    private final PushPort pushPort;
    private final GamePlayerRepository playerRepository;

    public GameEventActionPushAdapter(PushPort pushPort, GamePlayerRepository playerRepository) {
        this.pushPort = pushPort;
        this.playerRepository = playerRepository;
    }

    public void message(GameSessionContext context, I18n message) {
        GamePlayerEntity player = playerRepository.findByIdFetchUser(context.playerId().value()).orElseThrow();
        PushEvent event = new PushEvent.Message(context, message.value(player.getUser().getLanguage()));
        pushPort.push(event);
    }

    public void talk(GameSessionContext context, TalkItem.Id talkId) {
        PushEvent event = new PushEvent.Talk(context, talkId);
        pushPort.push(event);
    }

}
