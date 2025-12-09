package fr.plop.contexts.game.session.time.adapter;

import fr.plop.contexts.game.session.core.domain.model.GameSessionContext;
import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCast;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.contexts.game.session.time.GameSessionTimer;
import fr.plop.contexts.game.session.time.persistence.GameSessionTimerMemoryRepository;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

public class GameSessionTimerAdapter implements GameSessionTimer {

    private static final String ONE_MINUTE_IN_MILLISECONDS = "60000";
    private final GameSessionTimerMemoryRepository repository;
    private final GamePlayerRepository gamePlayerRepository;
    private final GameEventBroadCast broadCast;

    public GameSessionTimerAdapter(GameSessionTimerMemoryRepository repository, GamePlayerRepository gamePlayerRepository, GameEventBroadCast broadCast) {
        this.repository = repository;
        this.gamePlayerRepository = gamePlayerRepository;
        this.broadCast = broadCast;
    }

    @Scheduled(fixedDelayString = "${game.session.timer.duration:" + ONE_MINUTE_IN_MILLISECONDS + "}")
    public void run() {
        execute();
        repository.incAll();
    }

    private void execute() {
        repository.forEach(this::tick);
    }

    private void tick(GameSession.Id sessionId, GameSessionTimeUnit timeUnit) {
        final GameEvent.TimeClick event = new GameEvent.TimeClick(timeUnit);
        for (GamePlayer.Id playerId : findActivePlayerIdsBySessionId(sessionId)) {
            broadCast.fire( new GameSessionContext(sessionId, playerId), event);
        }
    }

    private List<GamePlayer.Id> findActivePlayerIdsBySessionId(GameSession.Id sessionId) {
        return gamePlayerRepository.activeIdsBySessionId(sessionId.value())
                .stream().map(GamePlayer.Id::new).toList();
    }

    @Override
    public void start(GameSession.Id sessionId) {
        repository.insert(sessionId);
    }

}
