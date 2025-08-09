package fr.plop.contexts.game.session.time;

import fr.plop.contexts.game.session.core.domain.model.GamePlayer;
import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.core.persistence.GamePlayerRepository;
import fr.plop.contexts.game.session.event.domain.GameEvent;
import fr.plop.contexts.game.session.event.domain.GameEventBroadCast;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class GameSessionTimerAdapter implements GameSessionTimer {

    private final Map<GameSession.Id, TimeUnit> bySession = new HashMap<>();

    private final GameEventBroadCast eventBroadCast;
    private final GamePlayerRepository gamePlayerRepository;

    public GameSessionTimerAdapter(GameEventBroadCast eventBroadCast, GamePlayerRepository gamePlayerRepository) {
        this.eventBroadCast = eventBroadCast;
        this.gamePlayerRepository = gamePlayerRepository;
    }

    @Scheduled(cron = "${game.session.timer.cron:0 */1 * * * *}")
    public void run() {
        incAll();
        fireAll();
    }

    @Override
    public void start(GameSession.Id id) {
        bySession.put(id, new TimeUnit());
    }

    @Override
    public TimeUnit current(GameSession.Id sessionId) {
        return bySession.get(sessionId);
    }

    private void incAll() {
        bySession.keySet()
                .forEach(sessionId -> bySession.put(sessionId, bySession.get(sessionId).inc()));
    }

    private void fireAll() {
        bySession.keySet()
                .forEach(sessionId -> gamePlayerRepository.activeIdsBySessionId(sessionId.value()).stream()
                        .map(GamePlayer.Id::new)
                        .forEach(playerId -> eventBroadCast.fire(new GameEvent.TimeClick(sessionId, playerId, bySession.get(sessionId)))));
    }

}
