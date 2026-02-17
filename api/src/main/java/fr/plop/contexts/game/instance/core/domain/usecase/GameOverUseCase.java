package fr.plop.contexts.game.instance.core.domain.usecase;

import fr.plop.contexts.game.config.cache.GameConfigCache;
import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.core.domain.model.GameInstanceContext;
import fr.plop.contexts.game.instance.core.domain.model.GamePlayer;
import fr.plop.contexts.game.instance.core.domain.model.InstanceGameOver;
import fr.plop.contexts.game.instance.push.PushEvent;
import fr.plop.contexts.game.instance.push.PushPort;
import fr.plop.contexts.game.instance.time.GameInstanceTimerRemove;
import fr.plop.subs.i18n.domain.I18n;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class GameOverUseCase {



    public interface OutputPort {
        Stream<GamePlayer.Id> findActivePlayerIds(GameInstance.Id instanceId);

        void win(GamePlayer.Id playerId, Optional<I18n.Id> optReasonId);

        void lose(GamePlayer.Id each, Optional<I18n.Id> optReasonId);

        void ended(GameInstance.Id instanceId);
    }

    private final OutputPort output;
    private final PushPort pushPort;
    private final GameInstanceTimerRemove timerRemove;
    private final GameConfigCache cache;

    public GameOverUseCase(OutputPort output, PushPort pushPort, GameInstanceTimerRemove timerRemove, GameConfigCache cache) {
        this.output = output;
        this.pushPort = pushPort;
        this.timerRemove = timerRemove;
        this.cache = cache;
    }

    public void apply(GameInstanceContext context, InstanceGameOver gameOver) {
        switch (gameOver.type()) {
            case SUCCESS_ALL_ENDED -> {
                List<GamePlayer.Id> playerIds = output.findActivePlayerIds(context.instanceId()).toList();
                playerIds.forEach(each -> output.win(each, gameOver.optReasonId()));
                endInstance(context.instanceId());
                playerIds.forEach(each -> {
                    try {
                        pushPort.push(new PushEvent.GameStatus(new GameInstanceContext(context.instanceId(), each)));
                    } catch (Exception ignored) {
                        //TODO
                    }
                });
            }
            case FAILURE_ALL_ENDED -> {
                List<GamePlayer.Id> playerIds = output.findActivePlayerIds(context.instanceId()).toList();
                playerIds.forEach(each -> output.lose(each, gameOver.optReasonId()));
                endInstance(context.instanceId());
                playerIds.forEach(each -> {
                    try {
                        pushPort.push(new PushEvent.GameStatus(new GameInstanceContext(context.instanceId(), each)));
                    } catch (Exception ignored) {
                        //TODO
                    }
                });
            }
            case SUCCESS_ONE_CONTINUE -> {
                List<GamePlayer.Id> playerIds = output.findActivePlayerIds(context.instanceId()).toList();
                output.win(context.playerId(), gameOver.optReasonId());
                if (playerIds.size() == 1) {
                    endInstance(context.instanceId());
                }
                try {
                    pushPort.push(new PushEvent.GameStatus(context));
                } catch (Exception ignored) {
                    //TODO
                }
            }
            case FAILURE_ONE_CONTINUE -> {
                List<GamePlayer.Id> playerIds = output.findActivePlayerIds(context.instanceId()).toList();
                output.lose(context.playerId(), gameOver.optReasonId());
                if (playerIds.size() == 1) {
                    endInstance(context.instanceId());
                }
                try {
                    pushPort.push(new PushEvent.GameStatus(context));
                } catch (Exception ignored) {
                    //TODO
                }
            }
        }
    }

    private void endInstance(GameInstance.Id instanceId) {
        output.ended(instanceId);
        timerRemove.remove(instanceId);
        cache.remove(instanceId);
    }
}
