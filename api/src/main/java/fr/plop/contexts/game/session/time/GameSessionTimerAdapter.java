package fr.plop.contexts.game.session.time;

import fr.plop.contexts.game.session.core.domain.model.GameSession;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

@Component
public class GameSessionTimerAdapter implements GameSessionTimer {

    private Map<GameSession.Id, TimerBySession> bySession = new HashMap<>();

    @Override
    public void start(GameSession.Id id) {
        TimerBySession timer = bySession.get(id);
        if (timer != null) {
            timer.restart();
        } else {
            timer = new TimerBySession();
            timer.start();
            bySession.put(id, timer);
        }
    }

    @Override
    public TimeClick currentClick(GameSession.Id id) {
        TimerBySession timer = bySession.get(id);
        if (timer != null) {
            return timer.currentClick();
        }
        throw new RuntimeException("Timer not implemented by session " + id);
    }

    static class TimerBySession {

        private final TaskScheduler taskScheduler = new ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor());

        private TimeClick currentClick = new TimeClick(0);

        public void start() {
            currentClick = new TimeClick(0);
            taskScheduler.scheduleAtFixedRate(() -> currentClick = currentClick.inc(), TimeClick.ONE_CLICK);
        }

        public void restart() {
            currentClick = new TimeClick(0);
        }

        public void stop() {
            //TODO : taskScheduler.
        }

        public TimeClick currentClick() {
            return currentClick;
        }

    }

}
