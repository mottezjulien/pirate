package fr.plop.contexts.game.session.event.adapter.action;

import fr.plop.contexts.game.session.core.domain.model.GameOver;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCast;
import fr.plop.contexts.i18n.domain.I18n;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Stream;

@Component
public class GameEventActionGame {

    public interface OutputPort {
        Stream<GamePlayer.Id> findPlayerIds(GameSession.Id sessionId);
        void win(GameSession.Id sessionId, GamePlayer.Id playerId, I18n.Id i18nId);
        void lose(GameSession.Id sessionId, GamePlayer.Id otherPlayerId1, I18n.Id i18nId);
        void ended(GameSession.Id sessionId);
    }


    private final OutputPort output;
    private final GameEventBroadCast  broadCast;

    public GameEventActionGame(OutputPort output, GameEventBroadCast broadCast) {
        this.output = output;
        this.broadCast = broadCast;
    }

    public void over(GameSession.Id sessionId, GamePlayer.Id playerId, GameOver gameOver) {
        switch (gameOver.type()) {
            case SUCCESS_ALL_ENDED -> {

                List<GamePlayer.Id> playerIds = output.findPlayerIds(sessionId).toList();
                playerIds.forEach(each -> output.win(sessionId, each, gameOver.labelId()));
                output.ended(sessionId);
                playerIds.forEach(each -> broadCast.fire(new GameEvent.UpdateStatus(sessionId, each)));

            }
            case SUCCESS_ONE_CONTINUE -> {
                output.win(sessionId, playerId, gameOver.labelId());
                broadCast.fire(new GameEvent.UpdateStatus(sessionId, playerId));
            }
        }


    }
}
