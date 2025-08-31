package fr.plop.contexts.game.session.event.adapter.action;

import fr.plop.contexts.game.config.consequence.Consequence;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.persistence.GamePlayerEntity;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.session.push.PushEvent;
import fr.plop.contexts.game.session.push.PushPort;
import org.springframework.stereotype.Component;

@Component
public class GameEventActionMessage {

    private final PushPort pushPort;
    private final GamePlayerRepository playerRepository;

    public GameEventActionMessage(PushPort pushPort, GamePlayerRepository playerRepository) {
        this.pushPort = pushPort;
        this.playerRepository = playerRepository;
    }

    public void alert(GameSession.Id sessionId, GamePlayer.Id playerId, Consequence.DisplayTalkAlert alert) {
        GamePlayerEntity player = playerRepository.findByIdFetchUser(playerId.value()).orElseThrow();
        PushEvent event = new PushEvent.Message(sessionId, playerId, alert.value().value(player.getUser().getLanguage()));
        pushPort.push(event);
    }
}
