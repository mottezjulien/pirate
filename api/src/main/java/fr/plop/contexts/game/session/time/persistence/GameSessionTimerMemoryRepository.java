package fr.plop.contexts.game.session.time.persistence;

import fr.plop.contexts.game.session.core.domain.model.GameSession;
import fr.plop.contexts.game.session.time.GameSessionTimeUnit;
import fr.plop.contexts.game.session.time.GameSessionTimerGet;
import fr.plop.contexts.game.session.time.GameSessionTimerRemove;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;


@Repository
public class GameSessionTimerMemoryRepository implements GameSessionTimerGet, GameSessionTimerRemove {

    private final Map<GameSession.Id, GameSessionTimeUnit> bySession = new HashMap<>();

    @Override
    public GameSessionTimeUnit current(GameSession.Id sessionId) {
        return bySession.get(sessionId);
    }

    public void insert(GameSession.Id sessionId) {
        bySession.put(sessionId, new GameSessionTimeUnit());
    }

    public void remove(GameSession.Id sessionId) {
        bySession.remove(sessionId);
    }

    public void incAll() {
        bySession.replaceAll((key, timeUnit) -> timeUnit.inc());
    }

    public void forEach(BiConsumer<GameSession.Id, GameSessionTimeUnit> consumer) {
        bySession.forEach(consumer);
    }

}
