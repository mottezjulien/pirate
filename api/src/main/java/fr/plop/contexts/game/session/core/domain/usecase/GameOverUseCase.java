package fr.plop.contexts.game.session.core.domain.usecase;

import fr.plop.contexts.game.session.core.domain.model.GameOver;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.push.PushEvent;
import fr.plop.contexts.game.session.push.PushPort;
import fr.plop.contexts.i18n.domain.I18n;

import java.util.List;
import java.util.stream.Stream;

public class GameOverUseCase {

    public interface OutputPort {
        Stream<GamePlayer.Id> findActivePlayerIds(GameSession.Id sessionId);

        void win(GamePlayer.Id playerId, I18n.Id reasonId);

        void ended(GameSession.Id sessionId);
    }

    private final OutputPort output;
    private final PushPort pushPort;

    public GameOverUseCase(OutputPort output, PushPort pushPort) {
        this.output = output;
        this.pushPort = pushPort;
    }

    public void apply(GameSession.Id sessionId, GamePlayer.Id playerId, GameOver gameOver) {
        switch (gameOver.type()) {
            case SUCCESS_ALL_ENDED -> {
                List<GamePlayer.Id> playerIds = output.findActivePlayerIds(sessionId).toList();
                playerIds.forEach(each -> output.win(each, gameOver.reasonId()));
                output.ended(sessionId);
                playerIds.forEach(each -> {
                    try {
                        pushPort.push(new PushEvent.GameStatus(sessionId, each));
                    } catch (Exception ignored) {
                        //TODO
                    }
                });
            }
            case SUCCESS_ONE_CONTINUE -> {
                List<GamePlayer.Id> playerIds = output.findActivePlayerIds(sessionId).toList();
                output.win(playerId, gameOver.reasonId());
                if (playerIds.size() == 1) {
                    output.ended(sessionId);
                }
                try {
                    pushPort.push(new PushEvent.GameStatus(sessionId, playerId));
                } catch (Exception ignored) {
                    //TODO
                }
            }
        }
    }
}
