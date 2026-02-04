package fr.plop.contexts.game.instance.time.persistence;

import fr.plop.contexts.game.instance.core.domain.model.GameInstance;
import fr.plop.contexts.game.instance.time.GameInstanceTimeUnit;
import fr.plop.contexts.game.instance.time.GameInstanceTimerGet;
import fr.plop.contexts.game.instance.time.GameInstanceTimerRemove;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;


@Repository
public class GameInstanceTimerMemoryRepository implements GameInstanceTimerGet, GameInstanceTimerRemove {

    private final Map<GameInstance.Id, GameInstanceTimeUnit> bySession = new HashMap<>();

    @Override
    public GameInstanceTimeUnit current(GameInstance.Id sessionId) {
        return bySession.get(sessionId);
    }

    public void insert(GameInstance.Id sessionId) {
        bySession.put(sessionId, new GameInstanceTimeUnit());
    }

    public void remove(GameInstance.Id sessionId) {
        bySession.remove(sessionId);
    }

    public void incAll() {
        bySession.replaceAll((key, timeUnit) -> timeUnit.inc());
    }

    public void forEach(BiConsumer<GameInstance.Id, GameInstanceTimeUnit> consumer) {
        bySession.forEach(consumer);
    }

}
