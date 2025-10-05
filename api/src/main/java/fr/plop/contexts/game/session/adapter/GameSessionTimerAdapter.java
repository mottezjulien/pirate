package fr.plop.contexts.game.session.adapter;

import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCast;
import fr.plop.contexts.game.session.event.domain.GameEventContext;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.contexts.game.session.time.GameSessionTimer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameSessionTimerAdapter implements GameSessionTimer {

    private static final Duration TIME_UNIT_DEFAULT_DURATION = Duration.ofMinutes(1);

    private record SessionValue(ScheduledExecutorService scheduledExecutorService, GameSessionTimeUnit timeUnit) {}

    private final Map<GameSession.Id, SessionValue> bySession = new HashMap<>();
    private final GamePlayerRepository gamePlayerRepository;

    private final GameEventBroadCast broadCast;

    private final Duration timeUnitDuration;


    public GameSessionTimerAdapter(GamePlayerRepository gamePlayerRepository, GameEventBroadCast broadCast) {
        this(gamePlayerRepository, broadCast, TIME_UNIT_DEFAULT_DURATION);
    }

    GameSessionTimerAdapter(GamePlayerRepository gamePlayerRepository, GameEventBroadCast broadCast, Duration timeUnitDuration) {
        this.gamePlayerRepository = gamePlayerRepository;
        this.broadCast = broadCast;
        this.timeUnitDuration = timeUnitDuration;
    }

    @Override
    public void start(GameSession.Id sessionId) {
        Runnable task  = () -> {
            SessionValue sessionValue = bySession.get(sessionId);
            runEachTimeUnit(sessionId, sessionValue.scheduledExecutorService(), sessionValue.timeUnit.inc());
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        runEachTimeUnit(sessionId, executor, new GameSessionTimeUnit());
        executor.scheduleAtFixedRate(task, timeUnitDuration.toSeconds(), timeUnitDuration.toSeconds(), TimeUnit.SECONDS);
    }

    private void runEachTimeUnit(GameSession.Id sessionId, ScheduledExecutorService scheduledExecutorService, GameSessionTimeUnit timeUnit) {
        bySession.put(sessionId, new SessionValue(scheduledExecutorService, timeUnit));
        tick(sessionId, timeUnit);
    }

    private void tick(GameSession.Id sessionId, GameSessionTimeUnit time) {
        for (GamePlayer.Id playerId : activePlayers(sessionId)) {
            GameEventContext context = new GameEventContext(sessionId, playerId);
            broadCast.fire(new GameEvent.TimeClick(time), context);
        }
    }

    private List<GamePlayer.Id> activePlayers(GameSession.Id sessionId) {
        return gamePlayerRepository.activeIdsBySessionId(sessionId.value())
                .stream().map(GamePlayer.Id::new).toList();
    }

    @Override
    public GameSessionTimeUnit current(GameSession.Id sessionId) {
        return bySession.get(sessionId).timeUnit();
    }


}
