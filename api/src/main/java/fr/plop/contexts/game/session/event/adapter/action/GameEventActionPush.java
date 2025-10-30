package fr.plop.contexts.game.session.event.adapter.action;

import fr.plop.contexts.game.config.talk.domain.TalkItem;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.session.push.PushEvent;
import fr.plop.contexts.game.session.push.PushPort;
import fr.plop.subs.i18n.domain.I18n;
import org.springframework.stereotype.Component;

@Component
public class GameEventActionPush {

    private final PushPort pushPort;
    private final GamePlayerRepository playerRepository;

    public GameEventActionPush(PushPort pushPort, GamePlayerRepository playerRepository) {
        this.pushPort = pushPort;
        this.playerRepository = playerRepository;
    }

    public void message(GameSession.Id sessionId, GamePlayer.Id playerId, I18n message) {
        GamePlayerEntity player = playerRepository.findByIdFetchUser(playerId.value()).orElseThrow();
        PushEvent event = new PushEvent.Message(sessionId, playerId, message.value(player.getUser().getLanguage()));
        pushPort.push(event);
    }

    public void talk(GameSession.Id sessionId, GamePlayer.Id playerId, TalkItem.Id talkId) {
        PushEvent event = new PushEvent.Talk(sessionId, playerId, talkId);
        pushPort.push(event);
    }

}
